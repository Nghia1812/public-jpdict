package com.prj.data.repository

import com.prj.data.datasource.LocalLoginStateDataSource
import com.prj.domain.repository.ILoginStateRepository
import javax.inject.Inject

/**
 * Repository for managing the user's login state.
 *
 * This class serves as a single source of truth for the application's login status.
 *
 * @param localLoginStateDataSource The data source responsible for persisting the login state locally.
 */
class LoginStateRepository @Inject constructor(
    private val localLoginStateDataSource: LocalLoginStateDataSource
):ILoginStateRepository{
    override val isLoggedIn = localLoginStateDataSource.isLoggedIn
    override fun saveLoginStatus(isLoggedin: Boolean) {
        localLoginStateDataSource.saveLoginStatus(isLoggedin)
    }
}