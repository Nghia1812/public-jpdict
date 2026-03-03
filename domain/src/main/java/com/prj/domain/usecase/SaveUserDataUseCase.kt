package com.prj.domain.usecase

import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IUserStorageRepository
import javax.inject.Inject

class SaveUserDataUseCase @Inject constructor(
    private val userStoreRepository: IUserStorageRepository,
) {
    suspend operator fun invoke(user: User) : Result<Unit>{
        return userStoreRepository.saveUser(user)
    }
}

