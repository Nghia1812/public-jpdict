package com.prj.domain.repository

import com.prj.domain.model.profilescreen.User

interface IAuthRepository {
    suspend fun registerUser(email: String, password: String): Result<User>
    suspend fun loginUser(email: String, password: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun validateEmail(email: String): Result<List<String>>
    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun updateDisplayName(name: String): Result<Unit>
    suspend fun getCurrentAuthUser(): User?
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
}