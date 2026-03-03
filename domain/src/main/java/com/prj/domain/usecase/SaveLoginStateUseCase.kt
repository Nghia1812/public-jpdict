package com.prj.domain.usecase

import com.prj.domain.repository.ILoginStateRepository
import javax.inject.Inject

class SaveLoginStateUseCase @Inject constructor(
    private val loginStateRepository: ILoginStateRepository
){
     operator fun invoke(isLoggedIn: Boolean){
        loginStateRepository.saveLoginStatus(isLoggedIn)
    }
}