package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.resetPassword(email)
    }
}