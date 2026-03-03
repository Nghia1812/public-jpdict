package com.prj.domain.repository

import com.prj.domain.model.profilescreen.User

/**
 * Handle get/set user info to storage (Firestore, etc)
 */
interface IUserStorageRepository {
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getUser(userId: String): Result<User?>
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
}