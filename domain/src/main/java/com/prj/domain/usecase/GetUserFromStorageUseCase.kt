package com.prj.domain.usecase

import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IUserStorageRepository
import javax.inject.Inject

class GetUserFromStorageUseCase @Inject constructor(
    private val userStoreRepository: IUserStorageRepository,
) {
    suspend operator fun invoke(userId : String): Result<User?> {
        return userStoreRepository.getUser(userId)
    }
}