package com.prj.domain.repository

import android.net.Uri

/**
 * Interface for a repository that handles image-related data operations.
 *
 * This interface defines the contract for extracting text from an image. Implementations
 * of this repository are responsible for the underlying logic of Optical Character Recognition (OCR)
 * and data handling.
 */
interface IImageRepository {
    /**
     * Extracts text from an image specified by its URI.
     *
     * @param domainUri The string URI of the image to process.
     */
    suspend fun extractText(domainUri: String): Result<String>
}