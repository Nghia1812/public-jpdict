package com.prj.domain.usecase

import com.prj.domain.repository.IAuthRepository
import com.prj.domain.repository.IWordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val saveLoginStateUseCase: SaveLoginStateUseCase,
    private val wordRepository: IWordRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
        saveLoginStateUseCase(false)
        withContext(Dispatchers.IO){
            wordRepository.clearUserLocalData()
        }
    }
}