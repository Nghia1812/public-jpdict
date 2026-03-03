package com.prj.japanlib.feature_jlpttest.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.repository.IJlptTestRepository
import com.prj.domain.model.testscreen.JLPTTest
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.TestResult
import com.prj.domain.model.testscreen.TestSectionType
import com.prj.domain.usecase.CalculateTestScoreUseCase
import com.prj.domain.usecase.GetTestResultUseCase
import com.prj.domain.usecase.SaveTestResultUseCase
import com.prj.japanlib.uistate.TestScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel for managing JLPT test state, timer, answers, and results.
 * Coordinates between repository, use cases, and UI state.
 */
class JLPTTestViewModel @Inject constructor(
    private val mTestRepository: IJlptTestRepository,
    private val mGetTestResultUseCase: GetTestResultUseCase,
    private val mSaveTestResultUseCase: SaveTestResultUseCase,
    private val mCalculateTestScoreUseCase: CalculateTestScoreUseCase
) : ViewModel() {

    /**
     * Represents the current state of the test
     */
    enum class TestState {
        ACTIVE,   // Test is running, timer is counting
        PAUSED,   // Test is paused, timer is stopped
        RESET     // Test has been reset
    }

    // Timer management
    private val mTimer = MutableStateFlow(0L)
    val timer: StateFlow<Long> = mTimer.asStateFlow()
    private var mTimerJob: Job? = null

    // Answer selection tracking
    private val mSelectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = mSelectedAnswers.asStateFlow()

    // Test questions state
    private val mTestQuestions = MutableStateFlow<TestScreenUiState<JLPTTest>>(TestScreenUiState.Empty)
    val testQuestions: StateFlow<TestScreenUiState<JLPTTest>> = mTestQuestions.asStateFlow()

    // Score state
    private val mScore = MutableStateFlow<Int?>(null)
    val score: StateFlow<Int?> = mScore.asStateFlow()

    // Save result state
    private val mSaveResultState = MutableStateFlow<TestScreenUiState<Unit>>(TestScreenUiState.Empty)
    val saveResultState: StateFlow<TestScreenUiState<Unit>> = mSaveResultState.asStateFlow()

    // Test metadata
    private var mTestId = ""
    private var mSkill = ""
    private var mSkillType: TestSectionType = TestSectionType.VOCABULARY

    // Time tracking
    private val mTimeTaken = MutableStateFlow(0L)
    val timeTaken: StateFlow<Long> = mTimeTaken.asStateFlow()

    // Test state management
    private val mTestState = MutableStateFlow(TestState.ACTIVE)
    val testState: StateFlow<TestState> = mTestState.asStateFlow()

    // Job for fetching test questions
    private var mFetchJob: Job? = null

    /**
     * Pauses the currently active test.
     * Timer will stop counting when paused.
     */
    fun pauseTest() {
        mTestState.value = TestState.PAUSED
    }

    /**
     * Resumes a paused test.
     * Only resumes if the test is currently in PAUSED state.
     */
    fun resumeTest() {
        if (mTestState.value == TestState.PAUSED) {
            mTestState.value = TestState.ACTIVE
        }
    }

    /**
     * Resets the test state.
     * Used to indicate the test has been reset to initial state.
     */
    fun resetTest() {
        mTestState.value = TestState.RESET
    }

    /**
     * Starts the countdown timer for the test.
     * Timer only decrements when test state is ACTIVE.
     * Auto-submits the exam when timer reaches 0.
     *
     * @param durationSeconds Total duration of the test in seconds
     */
    fun startTimer(durationSeconds: Long) {
        mTimer.value = durationSeconds
        mTimerJob?.cancel()
        mTimerJob = viewModelScope.launch {
            while (mTimer.value > 0) {
                delay(1000)
                if (mTestState.value == TestState.ACTIVE) {
                    mTimer.value--
                    mTimeTaken.value++
                }
            }
            // Auto-submit when time is up
            if (mTimer.value == 0L) {
                submitExam()
            }
        }
    }

    /**
     * Stops the timer and resets it to 0.
     * Cancels the timer coroutine job.
     */
    fun stopTimer() {
        mTimer.value = 0
        mTimerJob?.cancel()
    }

    /**
     * Cleanup when ViewModel is destroyed.
     * Cancels timer and fetch jobs to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        mTimerJob?.cancel()
        mFetchJob?.cancel()
    }

    /**
     * Records the user's answer for a specific question.
     *
     * @param questionNumber The number/ID of the question
     * @param option The selected option (answer choice)
     */
    fun selectAnswer(questionNumber: Int, option: Int) {
        mSelectedAnswers.value = mSelectedAnswers.value.toMutableMap().apply {
            put(questionNumber, option)
        }
    }

    /**
     * Fetches test questions from repository and initializes the test.
     * Prevents duplicate fetching if the same test is already loaded.
     * Restores previous test results if available.
     *
     * @param source Source of the test (e.g., official, practice)
     * @param level JLPT level (N1, N2, N3, N4, N5)
     * @param id Unique identifier for the test
     * @param skill Skill type (vocabulary, grammar, reading, listening)
     */
    fun getTestQuestions(source: Source, level: Level, id: String, skill: String) {
        // Prevent refetching the same test
        if (id == mTestId && skill == this.mSkill) {
            return
        }

        // Cancel any ongoing fetch operation
        mFetchJob?.cancel()

        // Initialize test metadata
        val type = TestSectionType.fromString(skill)
        this.mSkillType = type
        this.mTestId = id
        this.mSkill = skill
        mTestState.value = TestState.ACTIVE
        mTestQuestions.value = TestScreenUiState.Loading

        mFetchJob = viewModelScope.launch {
            try {
                // Try to restore previous test result if exists
                getTestResult(id, skill)

                // Fetch test questions from repository
                val res = mTestRepository.getTestQuestions(source, level, id, skill)
                res.fold(
                    onSuccess = { test ->
                        mSelectedAnswers.value = emptyMap()
                        mTestQuestions.value = TestScreenUiState.Success(test)

                        // Start timer based on level and test type
                        val duration = TestSectionType.getDurationSeconds(level, type)
                        startTimer(duration)
                    },
                    onFailure = { error ->
                        mTestQuestions.value = TestScreenUiState.Error(error.message)
                    }
                )
            } catch (e: Exception) {
                Timber.e("Error getting test questions: ${e.message}")
                mTestQuestions.value = TestScreenUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Retrieves previously saved test results for the current test.
     * Restores score and selected answers if a previous attempt exists.
     *
     * @param testId The test identifier
     * @param skill The skill type
     */
    private suspend fun getTestResult(testId: String, skill: String) {
        val res = mGetTestResultUseCase(testId, skill)
        res.fold(
            onSuccess = { testResult ->
                if (testResult != null) {
                    mScore.value = testResult.score

                    // Convert string keys to integer keys for answer map
                    val mapIntAnswers = testResult.chosenAnswers.mapKeys { entry ->
                        entry.key.toInt()
                    }
                    mSelectedAnswers.value = mapIntAnswers
                    Timber.i("Restored test result - Answers: ${mSelectedAnswers.value}, Score: ${mScore.value}")
                }
            },
            onFailure = { it ->
                Timber.e("Error getting test result: ${it.message}")
                throw it
            }
        )
    }

    /**
     * Submits the exam for grading and saves the results.
     * Calculates score using CalculateTestScoreUseCase and persists via SaveTestResultUseCase.
     * Stops the timer when submission occurs.
     */
    fun submitExam() {
        viewModelScope.launch {
            // Stop the timer immediately
            stopTimer()

            val currentState = testQuestions.value
            if (currentState is TestScreenUiState.Success) {
                val testSections = currentState.data.sections

                try {
                    // Calculate score using use case
                    val scoreResult = mCalculateTestScoreUseCase(
                        testSections = testSections,
                        selectedAnswers = mSelectedAnswers.value,
                        targetSkillType = mSkillType
                    )

                    mScore.value = scoreResult.score
                    Timber.i("Exam submitted - Answers: ${mSelectedAnswers.value}, Score: ${scoreResult.score}/${scoreResult.totalQuestions}")

                    // Prepare test result for saving
                    val chosenAnswers = mSelectedAnswers.value.mapKeys { it.key.toString() }
                    val testResult = TestResult(
                        chosenAnswers = chosenAnswers,
                        score = scoreResult.correctAnswers,
                        totalQuestions = scoreResult.totalQuestions
                    )

                    // Save test result
                    mSaveResultState.value = TestScreenUiState.Loading
                    mSaveTestResultUseCase(mTestId, mSkillType.value, testResult).fold(
                        onSuccess = {
                            mSaveResultState.value = TestScreenUiState.Success(Unit)
                            Timber.i("Test result saved successfully")
                        },
                        onFailure = { error ->
                            mSaveResultState.value = TestScreenUiState.Error(error.message)
                            Timber.e("Failed to save test result: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    Timber.e("Error during exam submission: ${e.message}")
                    mSaveResultState.value = TestScreenUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Resets the exam to its initial state.
     * Clears all selected answers, score, and time taken.
     * Restarts the timer with the appropriate duration.
     *
     * @param level JLPT level for determining timer duration
     */
    fun resetExam(level: Level) {
        viewModelScope.launch {
            stopTimer()
            mSelectedAnswers.value = emptyMap()
            mScore.value = null
            mTimeTaken.value = 0L
            mSaveResultState.value = TestScreenUiState.Empty

            // Restart timer with appropriate duration
            val duration = TestSectionType.getDurationSeconds(level, mSkillType)
            startTimer(duration)

            Timber.i("Exam reset for level $level")
        }
    }
}