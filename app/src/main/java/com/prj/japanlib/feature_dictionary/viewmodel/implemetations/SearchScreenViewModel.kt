package com.prj.japanlib.feature_dictionary.viewmodel.implemetations

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.domain.model.dictionaryscreen.KanjiImage
import com.prj.domain.usecase.ClassifySearchUseCase
import com.prj.domain.usecase.ExtractTextFromImageUseCase
import com.prj.japanlib.uistate.DictionaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * ViewModel for the search screen, handling image-based search functionalities.
 *
 * This ViewModel is responsible for two primary operations:
 * 1.  Extracting Japanese text from a provided image URI using OCR (Optical Character Recognition).
 * 2.  Classifying a handwritten or drawn Kanji from a Bitmap image to find potential matches.
 *
 *
 * @property mClassifySearchUseCase The use case for classifying Kanji images.
 * @property mExtractTextFromImageUseCase The usecase for handling image-related data operations like text extraction.
 */
@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val mClassifySearchUseCase: ClassifySearchUseCase,
    private val mExtractTextFromImageUseCase: ExtractTextFromImageUseCase
) : ViewModel() {
    private val mResultKanjiUiStateFlow = MutableStateFlow<DictionaryUiState<List<String>>>(DictionaryUiState.Empty)
    val resultKanjiUiStateFlow = mResultKanjiUiStateFlow.asStateFlow()

    private val mDetectedTextUiStateFlow = MutableStateFlow<DictionaryUiState<String>>(DictionaryUiState.Empty)
    val detectedTextUiStateFlow = mDetectedTextUiStateFlow.asStateFlow()

    /**
     * Extracts Japanese text from an image specified by its URI.
     *
     * This function initiates an asynchronous text extraction process. It updates the [mDetectedTextUiStateFlow]
     * state to [DictionaryUiState.Loading], then to [DictionaryUiState.Success] with the extracted text,
     * or to [DictionaryUiState.Error] if the operation fails.
     *
     * @param uri The string URI of the image to process.
     */
    fun extractText(uri: String) {
        viewModelScope.launch {
            mDetectedTextUiStateFlow.value = DictionaryUiState.Loading
            try {
                mExtractTextFromImageUseCase(uri).map{ text ->
                    if (text.isEmpty()){
                        mDetectedTextUiStateFlow.value = DictionaryUiState.Error("No text detected")
                    } else {
                        mDetectedTextUiStateFlow.value = DictionaryUiState.Success(text)
                    }
                }
            } catch (e: Exception) {
                mDetectedTextUiStateFlow.value = DictionaryUiState.Error(e.message ?: "Detection failed")
            }
        }
    }

    /**
     * Classifies a Kanji character from a given [Bitmap] image.
     *
     * It first compresses bitmap into a byte array and then passes it to the [mClassifySearchUseCase]. The UI state
     * in [mResultKanjiUiStateFlow] is updated to reflect loading, success, or error states.
     *
     * @param bitmap The [Bitmap] image containing the Kanji to classify.
     */
    fun classifySearch(bitmap: Bitmap) {
        viewModelScope.launch {
            mResultKanjiUiStateFlow.value = DictionaryUiState.Loading
            withContext(Dispatchers.Default){
                try {
                    // Convert bitmap to byte array
                    val bytes = ByteArrayOutputStream().apply {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                    }.toByteArray()
                    // Create a KanjiImage object from the byte array
                    val kanjiImage = KanjiImage(bytes)
                    // Classify the Kanji image
                    mClassifySearchUseCase(kanjiImage).map { it ->
                        mResultKanjiUiStateFlow.value = DictionaryUiState.Success(it)
                    }
                } catch (e: Exception) {
                    mResultKanjiUiStateFlow.value = DictionaryUiState.Error(e.message ?: "Classification failed")
                }
            }
        }
    }

    /**
     * Clears the states for both detected text and classified Kanji results.
     *
     * This resets the UI state to [DictionaryUiState.Empty]
     */
    fun clearDetectedText() {
        mDetectedTextUiStateFlow.value = DictionaryUiState.Empty
        mResultKanjiUiStateFlow.value = DictionaryUiState.Empty
    }

}