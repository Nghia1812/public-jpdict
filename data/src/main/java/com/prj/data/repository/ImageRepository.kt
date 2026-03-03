package com.prj.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.prj.domain.repository.IImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for handling image-related operations, specifically text extraction.
 *
 */
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context
): IImageRepository {
    private val mRecognizer =
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    /**
     * Extracts Japanese text from an image located at the given URI.
     *
     * This function takes a string URI, converts it to a ML Kit [InputImage],
     * and processes it with the Japanese text recognizer.
     *
     * @param domainUri The string representation of the URI for the image to be processed.
     * @return A [Result] containing the extracted text as a [String] on success,
     *         or an [Exception] on failure.
     */
    override suspend fun extractText(domainUri: String): Result<String> {
        return try {
            // Convert string URI to InputImage
            val uri = Uri.parse(domainUri)
            val image = InputImage.fromFilePath(context, uri)
            // Process image with  text recognizer
            val visionText = mRecognizer.process(image).await()
            Timber.d("Detected Text: ${visionText.text}")
            Result.success(visionText.text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}