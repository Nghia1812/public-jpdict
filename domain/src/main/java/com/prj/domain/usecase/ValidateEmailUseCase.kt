package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(email: String): Result<List<String>> {
        return runCatching {
            authRepository.validateEmail(email).getOrThrow()
        }
    }
}