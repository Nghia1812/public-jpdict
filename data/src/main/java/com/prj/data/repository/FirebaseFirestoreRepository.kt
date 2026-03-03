package com.prj.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.prj.domain.model.dictionaryscreen.WordList
import com.prj.domain.model.dictionaryscreen.CustomWordRef
import com.prj.domain.model.dictionaryscreen.JlptWordRef
import com.prj.domain.model.dictionaryscreen.ThemeWordRef
import com.prj.domain.model.testscreen.TestResult
import com.prj.domain.model.profilescreen.User
import com.prj.domain.model.testscreen.AllSkillTestResult
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.WordListType
import com.prj.domain.repository.IStorageRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirebaseFirestoreRepository @Inject constructor() : IStorageRepository {
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        const val CUSTOM_LIST_COLLECTION = "lists"
        const val JLPT_LIST_COLLECTION = "jlpt"
        const val THEME_LIST_COLLECTION = "theme"
        const val TEST_COLLECTION = "tests"
        const val WORD_COLLECTION = "words"
        const val TEST_RESULT_COLLECTION = "results"
    }

    override suspend fun getUserFromStorage(userId: String): Result<User?> {
        return try {
            val doc = userId.let { mFirestore.collection("users").document(it).get().await() }
            val user = doc?.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTestResult(
        userId: String,
        testId: String,
        skill: String,
        testResult: TestResult
    ): Result<Unit> {
        try {
            val docRef = mFirestore.collection("users")
                .document(userId)
                .collection(TEST_RESULT_COLLECTION)
                .document(testId)

            mFirestore.runTransaction { transaction ->
                // 1. Read the existing document
                val snapshot = transaction.get(docRef)
                val allSkillTestResult = snapshot.toObject(AllSkillTestResult::class.java)
                    ?: AllSkillTestResult()

                val updated = when (skill) {
                    "listening" -> allSkillTestResult.copy(listeningResult = testResult)  // Only changes listening, keeps others
                    "reading" -> allSkillTestResult.copy(readingResult = testResult)      // Only changes reading, keeps others
                    "vocabulary" -> allSkillTestResult.copy(vocabularyResult = testResult)      // Only changes writing, keeps others
                    "grammar" -> allSkillTestResult.copy(grammarResult = testResult)    // Only changes speaking, keeps others
                    else -> throw IllegalArgumentException("Unknown skill: $skill")
                }

                // 3. Save back with only that one skill modified
                val count = listOfNotNull(
                    updated.listeningResult,
                    updated.readingResult,
                    updated.vocabularyResult,
                    updated.grammarResult
                ).size

                transaction.set(
                    docRef,
                    updated.copy(completedSkillsCount = count)
                )
            }.await()
            return Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error saving test result: ${e.message}")
            return Result.failure(e)
        }
    }

    override suspend fun getTestResultForSpecificSkill(
        userId: String,
        testId: String,
        skill: String
    ): Result<TestResult?> {
        try {
            val snapshot = mFirestore.collection("users")
                .document(userId)
                .collection(TEST_RESULT_COLLECTION)
                .document(testId)
                .get()
                .await()

            val aggregate = snapshot.toObject(AllSkillTestResult::class.java)

            // Extract the specific skill result
            val result = when (skill) {
                "listening" -> aggregate?.listeningResult
                "reading" -> aggregate?.readingResult
                "vocabulary" -> aggregate?.vocabularyResult
                "grammar" -> aggregate?.grammarResult
                else -> throw IllegalArgumentException("Unknown skill: $skill")
            }

            return Result.success(result)
        } catch (e: Exception) {
            Timber.e("Error getting test result: ${e.message}")
            return Result.failure(e)
        }
    }

    override suspend fun getAllTestResults(
        userId: String,
        testId: String
    ): Result<AllSkillTestResult?> {
        try {
            val snapshot = mFirestore.collection("users")
                .document(userId)
                .collection(TEST_RESULT_COLLECTION)
                .document(testId)
                .get()
                .await()

            val aggregate = snapshot.toObject(AllSkillTestResult::class.java)

            return Result.success(aggregate)
        } catch (e: Exception) {
            Timber.e("Error getting test results: ${e.message}")
            return Result.failure(e)
        }
    }

    override suspend fun getCustomLists(userId: String): Result<List<WordList>> {
        try {
            val listsSnapshot = mFirestore
                .collection("users")
                .document(userId)
                .collection(CUSTOM_LIST_COLLECTION)
                .get()
                .await()
                .toObjects(WordList::class.java)
            return Result.success(listsSnapshot)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun downloadWordsForAllCustomLists(
        userId: String,
        list: WordList
    ): Result<List<CustomWordRef>> {
        try {
            Timber.i("Downloading words for $list: ${list.listId}")
            val wordsSnapshot = mFirestore
                .collection("users")
                .document(userId)
                .collection(CUSTOM_LIST_COLLECTION)
                .document(list.listId)
                .collection(WORD_COLLECTION)
                .get()
                .await()
                .toObjects(CustomWordRef::class.java)
            Timber.i("Downloaded $wordsSnapshot for $list")
            return Result.success(wordsSnapshot)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun uploadListToOnlineStorage(
        userId: String,
        listId: String,
        name: String
    ): Result<Unit> {
        try {
            mFirestore.collection("users")
                .document(userId)
                .collection(CUSTOM_LIST_COLLECTION)
                .document(listId.toString())
                .set(mapOf("name" to name, "listId" to listId))
                .await()
            return Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error saving list to remote: ${e.message}")
            return Result.failure(e)
        }
    }

    override suspend fun uploadWordToOnlineStorage(
        userId: String,
        listId: String,
        entryId: Int
    ): Result<Unit> {
        try {
            mFirestore.collection("users")
                .document(userId)
                .collection(CUSTOM_LIST_COLLECTION)
                .document(listId)
                .collection(WORD_COLLECTION)
                .document(entryId.toString())
                .set(mapOf(
                    "listId" to listId,
                    "entryId" to entryId,
                    "learningState" to LearningState.NOT_LEARNT_YET.name
                ))
                .await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun deleteWordFromOnlineStorage(
        userId: String,
        listId: String,
        entryId: Int
    ): Result<Unit> {
        try {
            mFirestore.collection("users")
                .document(userId)
                .collection(CUSTOM_LIST_COLLECTION)
                .document(listId.toString())
                .collection(WORD_COLLECTION)
                .document(entryId.toString())
                .delete()
                .await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun updateWordInOnlineStorage(
        userId: String,
        listId: String,
        entryId: Int,
        newState: LearningState,
        listType: WordListType
    ): Result<Unit> {
        try {
            val newStateString = newState.name
            val wordData = mapOf(
                "listId" to listId,
                "entryId" to entryId,
                "learningState" to newStateString
            )

            when (listType) {
                WordListType.CUSTOM ->
                    mFirestore.collection("users")
                        .document(userId)
                        .collection(CUSTOM_LIST_COLLECTION)
                        .document(listId)
                        .collection(WORD_COLLECTION)
                        .document(entryId.toString())
                        .set(wordData)

                WordListType.JLPT ->
                    mFirestore.collection("users")
                        .document(userId)
                        .collection(JLPT_LIST_COLLECTION)
                        .document(listId)
                        .collection(WORD_COLLECTION)
                        .document(entryId.toString())
                        .set(wordData)

                WordListType.THEME ->
                    mFirestore.collection("users")
                        .document(userId)
                        .collection(THEME_LIST_COLLECTION)
                        .document(listId)
                        .collection(WORD_COLLECTION)
                        .document(entryId.toString())
                        .set(wordData)
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun getJlptLists(userId: String): Result<List<WordList>> {
        try {
            val listsSnapshot = mFirestore
                .collection("users")
                .document(userId)
                .collection(JLPT_LIST_COLLECTION)
                .get()
                .await()
                .toObjects(WordList::class.java)
            return Result.success(listsSnapshot)
        } catch (e: Exception) {
            Timber.e(e, "Error getting JLPT lists")
            return Result.failure(e)
        }
    }

    override suspend fun downloadWordsForJlptList(
        userId: String,
        list: WordList
    ): Result<List<JlptWordRef>> {
        try {
            Timber.i("Downloading JLPT words for $list: ${list.listId}")
            val wordsSnapshot = mFirestore
                .collection("users")
                .document(userId)
                .collection(JLPT_LIST_COLLECTION)
                .document(list.listId)
                .collection(WORD_COLLECTION)
                .get()
                .await()
                .toObjects(JlptWordRef::class.java)
            Timber.i("Downloaded ${wordsSnapshot.size} JLPT words for $list")
            return Result.success(wordsSnapshot)
        } catch (e: Exception) {
            Timber.e(e, "Error downloading JLPT words")
            return Result.failure(e)
        }
    }

    override suspend fun getThemeLists(userId: String): Result<List<WordList>> {
        try {
            val listsSnapshot = mFirestore
                .collection("users")
                .document(userId)
                .collection(THEME_LIST_COLLECTION)
                .get()
                .await()
                .toObjects(WordList::class.java)
            return Result.success(listsSnapshot)
        } catch (e: Exception) {
            Timber.e(e, "Error getting Theme lists")
            return Result.failure(e)
        }
    }

    override suspend fun downloadWordsForThemeList(
        userId: String,
        list: WordList
    ): Result<List<ThemeWordRef>> {
        try {
            Timber.i("Downloading Theme words for $list: ${list.listId}")
            val wordsSnapshot = mFirestore
                .collection("users")
                .document(userId)
                .collection(THEME_LIST_COLLECTION)
                .document(list.listId)
                .collection(WORD_COLLECTION)
                .get()
                .await()
                .toObjects(ThemeWordRef::class.java)
            Timber.i("Downloaded ${wordsSnapshot.size} Theme words for $list")
            return Result.success(wordsSnapshot)
        } catch (e: Exception) {
            Timber.e(e, "Error downloading Theme words")
            return Result.failure(e)
        }
    }
}
