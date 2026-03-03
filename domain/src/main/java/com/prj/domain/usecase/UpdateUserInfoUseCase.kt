package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import javax.inject.Inject

class UpdateUserInfoUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val updateUserInStorageUseCase: UpdateUserInStorageUseCase
) {
    suspend operator fun invoke(name: String, oldPassword: String, newPassword: String): Result<Unit> {
        return runCatching {
            val currentUser = authRepository.getCurrentAuthUser()
                ?: throw Exception("No user logged in")

            if (oldPassword.isNotBlank() && newPassword.isNotBlank()) {
                authRepository.updatePassword(oldPassword, newPassword).getOrThrow()
            }

            if (name.isNotBlank()) {
                authRepository.updateDisplayName(name).getOrThrow()
                updateUserInStorageUseCase(currentUser.id, mapOf("name" to name)).getOrThrow()
            }
        }
    }
}