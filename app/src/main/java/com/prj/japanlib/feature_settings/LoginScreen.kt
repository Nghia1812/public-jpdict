package com.prj.japanlib.feature_settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.R
import com.prj.japanlib.feature_settings.viewmodel.implementation.LoginViewModel
import com.prj.japanlib.feature_settings.viewmodel.interfaces.ILoginViewModel
import com.prj.japanlib.ui.theme.errorLight
import com.prj.japanlib.ui.theme.primaryLight
import com.prj.japanlib.uistate.LoginUiState

// Extension to ensure the context is always an activity context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit,
    loginViewModel: ILoginViewModel = hiltViewModel<LoginViewModel>(),
) {
    val loginState by loginViewModel.loginUiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val resetPasswordState by loginViewModel.resetPasswordUiState.collectAsStateWithLifecycle()
    val isResetPasswordMode by loginViewModel.isResetPasswordMode.collectAsStateWithLifecycle()
    var isNewUser by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    when (loginState) {
        is LoginUiState.Exists -> isNewUser = false
        is LoginUiState.NotExists -> isNewUser = true
        else -> {
            // do nothing
        }
    }

    AnimatedContent(
        targetState = isResetPasswordMode,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "login_reset_transition"
    ) { resetMode ->
        if (resetMode) {
            ResetPasswordScreenContent(
                email = email,
                resetPasswordState = resetPasswordState,
                onEmailChange = { email = it },
                onBackClick = {
                    loginViewModel.exitResetPasswordMode()
                    keyboardController?.hide()
                },
                onSendResetEmail = {
                    loginViewModel.sendPasswordResetEmail(email)
                }
            )
        } else {
            LoginScreenContent(
                email = email,
                password = password,
                loginState = loginState,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onLoginClick = {
                    if (!isNewUser) {
                        loginViewModel.loginUser(email, password)
                    } else {
                        loginViewModel.registerUser(email, password)
                    }
                },
                onGoogleLoginClick = loginViewModel::loginUserWithGoogle,
                onLoginSuccess = {
                    onLoginSuccess()
                    loginViewModel.saveLoginStatus(true)
                    loginViewModel.loadWordsFromOnlineStorage()
                },
                onForgotPasswordClick = {
                    loginViewModel.enterResetPasswordMode()
                },
                onFinishEmail = {
                    loginViewModel.validateEmail(email)
                    keyboardController?.hide()
                },
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
fun ResetPasswordScreenContent(
    email: String,
    resetPasswordState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSendResetEmail: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.login_back_description),
                    tint = Color.White
                )
            }

            // Title
            Text(
                text = stringResource(R.string.login_reset_password_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtitle
            Text(
                text = stringResource(R.string.login_reset_password_subtitle),
                fontSize = 14.sp,
                color = Color(0xFF8B92A0),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.login_email_username_placeholder),
                        color = Color(0xFF6B7280)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (resetPasswordState is LoginUiState.Error)
                        errorLight else Color(0xFF374151),
                    unfocusedBorderColor = if (resetPasswordState is LoginUiState.Error)
                        errorLight else Color(0xFF374151),
                    focusedContainerColor = Color(0xFF1F2937),
                    unfocusedContainerColor = Color(0xFF1F2937),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = resetPasswordState is LoginUiState.Error,
                enabled = resetPasswordState !is LoginUiState.Loading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSendResetEmail() }
                )
            )

            // Status Messages
            when (resetPasswordState) {
                is LoginUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 16.dp),
                        color = primaryLight
                    )
                }
                is LoginUiState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_reset_email_sent),
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is LoginUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = errorLight.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = resetPasswordState.message,
                            color = errorLight,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                else -> Spacer(modifier = Modifier.height(16.dp))
            }

            // Send Reset Email Button
            Button(
                onClick = onSendResetEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryLight,
                    disabledContainerColor = primaryLight.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = resetPasswordState !is LoginUiState.Loading
            ) {
                Text(
                    text = stringResource(R.string.login_send_reset_email),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login
            TextButton(
                onClick = onBackClick,
                enabled = resetPasswordState !is LoginUiState.Loading
            ) {
                Text(
                    text = stringResource(R.string.login_back_to_login),
                    fontSize = 15.sp,
                    color = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
fun LoginScreenContent(
    email: String,
    password: String,
    loginState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLoginClick: (Activity) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onFinishEmail: () -> Unit,
    onBackClick: () -> Unit,
    ) {
    val context = LocalContext.current
    var emailFieldWasFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.login_back_description)
                )
            }
            // Welcome Text
            Text(
                text = stringResource(R.string.login_welcome_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.login_email_username_placeholder),
                        color = Color(0xFF6B7280)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            emailFieldWasFocused = true
                        }
                        if (!focusState.hasFocus && emailFieldWasFocused) {
                            onFinishEmail()
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF374151),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedContainerColor = Color(0xFF1F2937),
                    unfocusedContainerColor = Color(0xFF1F2937),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Email
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onFinishEmail() }
                )
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.login_password_placeholder),
                        color = Color(0xFF6B7280)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF374151),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedContainerColor = Color(0xFF1F2937),
                    unfocusedContainerColor = Color(0xFF1F2937),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Forgot Password
            Text(
                text = stringResource(R.string.login_forgot_password),
                color = Color(0xFF3B82F6),
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp, bottom = 24.dp)
                    .clickable(onClick = onForgotPasswordClick)
            )

            // Login State Messages
            when (loginState) {
                is LoginUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = primaryLight
                    )
                }
                is LoginUiState.Success -> {
                    Text(stringResource(R.string.login_success), style = MaterialTheme.typography.bodySmall)
                    LaunchedEffect(Unit) { onLoginSuccess() }
                }
                is LoginUiState.Error -> Text(
                    text = loginState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = errorLight
                )
                is LoginUiState.Exists -> Text(
                    stringResource(R.string.login_enter_password_signin),
                    style = MaterialTheme.typography.bodySmall
                )
                is LoginUiState.NotExists -> Text(
                    stringResource(R.string.login_enter_password_signup),
                    style = MaterialTheme.typography.bodySmall
                )
                LoginUiState.Empty -> Text(
                    stringResource(R.string.login_signup_or_login),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = primaryLight,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_button),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Google Login Button
            OutlinedButton(
                onClick = {
                    val activity = context.findActivity()
                    activity?.let(onGoogleLoginClick)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF374151))
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.login_google_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}