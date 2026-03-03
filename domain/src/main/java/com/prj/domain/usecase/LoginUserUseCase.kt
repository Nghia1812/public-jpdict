package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val saveLoginStateUseCase: SaveLoginStateUseCase
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return runCatching {
            authRepository.loginUser(email, password).getOrThrow()
            saveLoginStateUseCase(true)
        }
    }
}