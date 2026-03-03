package com.prj.domain.usecase

import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IAuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val getUserFromStorageUseCase: GetUserFromStorageUseCase
) {
    suspend operator fun invoke(): Result<User?> {
        val authUser = authRepository.getCurrentAuthUser()
        return if (authUser != null) {
            getUserFromStorageUseCase(authUser.id)
        } else {
            Result.success(null)
        }
    }
}