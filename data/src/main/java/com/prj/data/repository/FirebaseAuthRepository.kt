package com.prj.data.repository

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.prj.data.BuildConfig
import com.prj.data.mapper.toDomain
import com.prj.domain.model.profilescreen.User
import com.prj.domain.repository.IAuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for handling all Firebase Authentication operations.
 *
 * This class provides concrete implementations
 * for user registration, login (email/password and Google), session management, and profile updates.
 * It abstracts all Firebase-specific logic from the rest of the application.
 *
 * @param context The application context.
 */
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) :
    IAuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val currentUser = firebaseAuth.currentUser
            val authResult = currentUser?.linkWithCredential(credential)?.await()

            authResult?.user?.let { firebaseUser ->
                Result.success(firebaseUser.toDomain())
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                Result.success(it.toDomain())
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(
                idToken,
                null
            )
            val authResult =
                firebaseAuth.currentUser!!.linkWithCredential(firebaseCredential).await()
            authResult.user?.let {
                Result.success(it.toDomain())
            } ?: Result.failure(Exception("Google login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateEmail(email: String): Result<List<String>> {
        return try {
            val result = firebaseAuth.fetchSignInMethodsForEmail(email).await()
            val methods = result.signInMethods ?: emptyList()
            Timber.i("Sign in methods: $methods")
            Result.success(methods)
        } catch (e: Exception) {
            Timber.e("Error validating email: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user =
                firebaseAuth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val email = user.email ?: return Result.failure(Exception("No email found"))

            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error updating password: $e")
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(name: String): Result<Unit> {
        return try {
            val user =
                firebaseAuth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val profileUpdates = userProfileChangeRequest { displayName = name }
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e("Error updating display name: $e")
            Result.failure(e)
        }
    }

    override suspend fun getCurrentAuthUser(): User? {
        return firebaseAuth.currentUser?.toDomain()
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            // Validate email format
            if (email.isBlank()) {
                return Result.failure(Exception("Please enter your email address"))
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Please enter a valid email address"))
            }

            // Send password reset email
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.i("Email $email sent.")
                }
            }.await()

            Result.success(Unit)

        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email address"))
        } catch (e: Exception) {
            when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    Result.failure(Exception("Network error. Please check your connection"))
                else ->
                    Result.failure(Exception("Failed to send reset email. Please try again"))
            }
        }
    }

}


