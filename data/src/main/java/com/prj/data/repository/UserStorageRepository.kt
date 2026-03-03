package com.prj.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.prj.data.mapper.toDomain
import com.prj.data.mapper.toEntity
import com.prj.data.remote.dto.FirebaseUserEntity
import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IUserStorageRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject


/**
 * Repository for managing user data in Cloud Firestore.
 *
 * This class handles all CRUD operations for user profiles
 * by interacting with the "users" collection in Firestore.
 *
 */
class UserStorageRepository @Inject constructor(
): IUserStorageRepository {
    private val mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mUsersCollection = mFirestore.collection("users")

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            val userEntity = user.toEntity()
            mUsersCollection.document(user.id).set(userEntity, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error saving user to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<User?> {
        return try {
            val doc = mUsersCollection.document(userId).get().await()
            val user = doc.toObject(FirebaseUserEntity::class.java)
            Result.success(user?.toDomain())
        } catch (e: Exception) {
            Timber.e("Error getting user from Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            mUsersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error updating user in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            mUsersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error deleting user from Firestore: ${e.message}")
            Result.failure(e)
        }
    }
}