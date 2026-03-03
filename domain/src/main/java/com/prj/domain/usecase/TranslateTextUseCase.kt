package com.prj.domain.usecase

import com.prj.domain.exceptions.NetworkException
import com.prj.domain.model.translatescreen.TranslationResult
import com.prj.domain.repository.ITranslationRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val repository: ITranslationRepository
) {
    suspend operator fun invoke(
        text: String,
        source: String,
        target: String,
        onRetry: (attempt: Int) -> Unit
    ): Result<TranslationResult> {
        val maxRetry = 3
        var attempt = 0
        var lastError: Throwable? = null

        while (attempt < maxRetry) {
            attempt++

            val result = repository.translateText(text, source, target)

            if (result.isSuccess) {
                return result
            }

            lastError = result.exceptionOrNull()
            // Not handle retry for unauthorized or forbidden/ no internet exceptions
            if (lastError is NetworkException.UnauthorizedException ||
                lastError is NetworkException.ForbiddenException ||
                lastError is NetworkException.NoInternetException
            ) {
                break
            }

            onRetry(attempt)

            delay((1000 * attempt).toLong()) // backoff
        }
        return Result.failure(lastError ?: Exception("Unknown error"))
    }
}