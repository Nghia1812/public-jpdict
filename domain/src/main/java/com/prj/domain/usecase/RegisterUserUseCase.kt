package com.prj.domain.usecase

import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IAuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val saveUserDataUseCase: SaveUserDataUseCase,
    private val saveLoginStateUseCase: SaveLoginStateUseCase
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return runCatching {
            val authResult = authRepository.registerUser(email, password).getOrThrow()
            val user = User(authResult.id, authResult.email, authResult.name)
            saveUserDataUseCase(user).getOrThrow()
            saveLoginStateUseCase(true)
        }
    }
}