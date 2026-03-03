package com.prj.domain.usecase

import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IUserStorageRepository
import javax.inject.Inject

class UpdateUserInStorageUseCase @Inject constructor(
    private val userStoreRepository: IUserStorageRepository,
) {
    suspend operator fun invoke(userId: String, updates: Map<String, Any>): Result<Unit> {
        return userStoreRepository.updateUser(userId, updates)
    }
}