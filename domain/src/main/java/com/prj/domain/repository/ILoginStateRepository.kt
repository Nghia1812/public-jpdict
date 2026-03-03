package com.prj.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ILoginStateRepository {
    val isLoggedIn: StateFlow<Boolean>

    fun saveLoginStatus(isLoggedin: Boolean)
}