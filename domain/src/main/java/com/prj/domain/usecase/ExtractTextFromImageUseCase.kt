package com.prj.domain.usecase

import com.prj.domain.repository.IImageRepository
import javax.inject.Inject


/**
 * A use case for extracting text from an image.
 *
 *
 * @param mImageRepository The repository implementation responsible for accessing the image
 *                         and performing the text extraction.
 */
class ExtractTextFromImageUseCase @Inject constructor(
    private val mImageRepository: IImageRepository
) {
    /**
     * Extracts text from the image located at the given URI.
     *
     * @param uri The string representation of the URI for the image to process.
     * @return The extracted text as a [String]. The result may be empty if no text is found.
     */
    suspend operator fun invoke(uri: String) = mImageRepository.extractText(uri)
}
