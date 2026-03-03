package com.prj.japanlib.feature_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.profilescreen.User
import com.prj.japanlib.R
import com.prj.japanlib.feature_settings.viewmodel.implementation.UserInfoViewModel
import com.prj.japanlib.ui.theme.JapanlibTheme

@Composable
fun UserInfoScreen(
    onSignOutClick: () -> Unit,
    onBackClick: () -> Unit,
    userInfoViewModel: UserInfoViewModel = hiltViewModel()
) {
    val userInfoState by userInfoViewModel.userInfoUiState.collectAsStateWithLifecycle()
    val updateInfoState by userInfoViewModel.updateInfoUiState.collectAsStateWithLifecycle()
    when (userInfoState) {
        is UserInfoViewModel.UserUiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UserInfoViewModel.UserUiState.Error -> Text(stringResource(R.string.user_info_error_loading))
        is UserInfoViewModel.UserUiState.Success -> {
            val user = (userInfoState as UserInfoViewModel.UserUiState.Success<User>).data
            var oldPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }

            var name by remember { mutableStateOf(user.name) }
            UserInfoScreenContent(
                updateState = updateInfoState,
                email = user.email ?: stringResource(R.string.user_info_no_email),
                name = name ?: stringResource(R.string.user_info_guest),
                oldPassword = oldPassword,
                newPassword = newPassword,
                onNameChange = { name = it },
                onOldPasswordChange = { oldPassword = it },
                onNewPasswordChange = { newPassword = it },
                onSaveClick = userInfoViewModel::updateUserInfo,
                onSignOutClick = {
                    onSignOutClick()
                    userInfoViewModel.logout()
                },
                onBackClick = onBackClick
            )
        }
    }

}

@Composable
fun UserInfoScreenContent(
    updateState: UserInfoViewModel.UserUiState<Boolean>,
    email: String,
    name: String,
    oldPassword: String = "",
    newPassword: String = "",
    onNameChange: (String) -> Unit,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onSaveClick: (String, String, String) -> Unit,
    onSignOutClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isLoading = false

    Column(
        modifier = Modifier

    ) {
        // Top-right Sign Out Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.login_back_description)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onSignOutClick) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = stringResource(R.string.user_info_sign_out)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.user_info_sign_out))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (name.isNotEmpty()) {
                    Text(
                        text = name.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.user_info_profile_description),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // User Info Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { }, // No-op since it's read-only
                        label = { Text(stringResource(R.string.user_info_email_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = stringResource(R.string.user_info_email_label)
                            )
                        },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Display Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.user_info_name_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.user_info_name_label)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.clearFocus() }
                        ),
                        singleLine = true
                    )

                    // Current Password Field
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = onOldPasswordChange,
                        label = { Text(stringResource(R.string.user_info_password_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.user_info_password_label)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = if (passwordVisible) stringResource(R.string.user_info_hide_password)
                                    else stringResource(R.string.user_info_show_password)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.user_info_current_password_placeholder)) }
                    )

                    // New Password Field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text(stringResource(R.string.user_info_new_password_label)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.user_info_new_password_label)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = if (passwordVisible) stringResource(R.string.user_info_hide_password)
                                    else stringResource(R.string.user_info_show_password)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onSaveClick(name, oldPassword, newPassword)
                            }
                        ),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.user_info_new_password_placeholder)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //Save button
            Button(
                onClick = {
                    onSaveClick(name, oldPassword, newPassword)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isLoading) stringResource(R.string.user_info_saving) else stringResource(R.string.user_info_save_changes),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (updateState is UserInfoViewModel.UserUiState.Error) {
                // Error Message
                val colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
                UpdateStatusMessage(stringResource(R.string.user_info_update_error), colors, Icons.Default.Warning)
            } else if (updateState is UserInfoViewModel.UserUiState.Success) {
                // Success Message
                val colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.1f)
                )
                UpdateStatusMessage(stringResource(R.string.user_info_update_success), colors, Icons.Default.CheckCircle)
            }
        }
    }
}

@Composable
fun UpdateStatusMessage(text: String, color: CardColors, icon: ImageVector) {
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = color
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    JapanlibTheme {
        UserInfoScreenContent(
            updateState = UserInfoViewModel.UserUiState.Success(true),
            email = "userTest",
            name = "",
            oldPassword = "",
            newPassword = "",
            onNameChange = {},
            onOldPasswordChange = {},
            onNewPasswordChange = {},
            onSaveClick = { name, oldPassword, newPassword -> }
        )
    }
}