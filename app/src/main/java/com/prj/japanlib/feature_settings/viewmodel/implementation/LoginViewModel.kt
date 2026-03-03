package com.prj.japanlib.feature_settings.viewmodel.implementation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.prj.domain.usecase.SaveLoginStateUseCase
import com.prj.domain.usecase.CheckNetworkUseCase
import com.prj.domain.usecase.LoadWordsForListsUseCase
import com.prj.domain.usecase.LoginUserUseCase
import com.prj.domain.usecase.LoginWithGoogleUseCase
import com.prj.domain.usecase.RegisterUserUseCase
import com.prj.domain.usecase.ResetPasswordUseCase
import com.prj.domain.usecase.ValidateEmailUseCase
import com.prj.japanlib.BuildConfig
import com.prj.japanlib.feature_settings.viewmodel.interfaces.ILoginViewModel
import com.prj.japanlib.uistate.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val saveLoginStateUseCase: SaveLoginStateUseCase,
    private val checkNetworkUseCase: CheckNetworkUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val loadWordsForListsUseCase: LoadWordsForListsUseCase
    ) : ViewModel(), ILoginViewModel {
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Empty)
    override val loginUiState = _loginUiState.asStateFlow()

    private val isNetworkConnected = checkNetworkUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    private val _resetPasswordUiState = MutableStateFlow<LoginUiState>(LoginUiState.Empty)
    override val resetPasswordUiState: StateFlow<LoginUiState> = _resetPasswordUiState.asStateFlow()

    private val _isResetPasswordMode = MutableStateFlow(false)
    override val isResetPasswordMode: StateFlow<Boolean> = _isResetPasswordMode.asStateFlow()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun saveLoginStatus(isLoggedin: Boolean) {
        saveLoginStateUseCase(isLoggedin)
    }

    private suspend fun checkNetworkAndSetError(): Boolean {
        val isConnected = isNetworkConnected.first()
        if (!isConnected) {
            _loginUiState.value =
                LoginUiState.Error("No internet connection. Please check your network.")
            return false
        }
        return true
    }

    override fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            if (!checkNetworkAndSetError()) return@launch
            _loginUiState.value = LoginUiState.Loading
            val result = loginUserUseCase(email, password)
            _loginUiState.value = result.fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { LoginUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    override fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            if (!checkNetworkAndSetError()) return@launch
            _loginUiState.value = LoginUiState.Loading
            val result = registerUserUseCase(email, password)
            _loginUiState.value = result.fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { LoginUiState.Error(it.message ?: "Signup failed") }
            )
        }
    }

    override fun loginUserWithGoogle(activity: Activity) {
        viewModelScope.launch {
            if (!checkNetworkAndSetError()) return@launch
            _loginUiState.value = LoginUiState.Loading
            val idToken = getGoogleCredential(activity)
            val result = idToken?.let { loginWithGoogleUseCase(it) }
            if (result != null) {
                _loginUiState.value = result.fold(
                    onSuccess = { LoginUiState.Success },
                    onFailure = {
                        Timber.e("Login failed: ${it.message}")
                        LoginUiState.Error(it.message ?: "Login failed")
                    }
                )
            } else {
                Timber.e("Login failed with null result")
                _loginUiState.value = LoginUiState.Error("Login failed")
            }
        }
    }

    private suspend fun getGoogleCredential(activity: Activity): String? {
        return try {
            val webClientId = BuildConfig.WEB_CLIENT_ID
            val signInWithGoogleOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(
                request = request,
                context = activity,
            )

            if (result.credential is CustomCredential && result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                googleIdCredential.idToken
            } else {
                null
            }
        } catch (e: Exception) {
            // User cancelled the UI, handle silently
            Timber.e("Error getting Google credential: ${e.message}");
            return null
        }
    }

    override fun validateEmail(email: String) {
        viewModelScope.launch {
            if (!checkNetworkAndSetError()) return@launch
            _loginUiState.value = LoginUiState.Loading
            val result = validateEmailUseCase(email)
            result.fold(
                onSuccess = { methods ->
                    if(methods.isEmpty()){
                        _loginUiState.value = LoginUiState.NotExists
                    } else {
                        _loginUiState.value = LoginUiState.Exists
                    }
                },
                onFailure = { e ->
                    _loginUiState.value = LoginUiState.Error(e.message ?: "Login failed")
                }
            )
        }
    }

    override fun enterResetPasswordMode() {
        _isResetPasswordMode.value = true
        _resetPasswordUiState.value = LoginUiState.Empty
        _loginUiState.value = LoginUiState.Empty
    }

    override fun exitResetPasswordMode() {
        _isResetPasswordMode.value = false
        _resetPasswordUiState.value = LoginUiState.Empty
    }

    override fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _resetPasswordUiState.value = LoginUiState.Loading
                resetPasswordUseCase(email)
                _resetPasswordUiState.value = LoginUiState.Success

            } catch (e: Exception) {
                _resetPasswordUiState.value = LoginUiState.Error(
                    e.message ?: "Failed to send reset email"
                )
            }
        }
    }

    override fun resetResetPasswordState() {
        _resetPasswordUiState.value = LoginUiState.Empty
    }

    override fun loadWordsFromOnlineStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = loadWordsForListsUseCase(firebaseAuth.currentUser!!.uid)
        }
    }

}