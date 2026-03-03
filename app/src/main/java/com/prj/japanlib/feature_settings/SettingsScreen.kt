package com.prj.japanlib.feature_settings

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.recreate
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.review.ReviewManagerFactory
import com.prj.domain.model.NotificationPreferences
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.domain.model.settingsscreen.FontScale
import com.prj.japanlib.BuildConfig
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.appLanguages
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.prj.japanlib.feature_settings.components.RateAppDialog
import com.prj.japanlib.feature_settings.components.SettingsDialog
import com.prj.japanlib.feature_settings.components.*
import com.prj.japanlib.feature_settings.viewmodel.implementation.SettingsScreenViewModel
import com.prj.japanlib.ui.theme.*

/**
 * Dialog types for the settings screen
 */
enum class DialogType {
    HELP, PRIVACY, ABOUT, RATE, NONE
}

/**
 * Launches the in-app review flow
 */
fun launchInAppReview(activity: Activity) {
    val reviewManager = ReviewManagerFactory.create(activity)
    val request = reviewManager.requestReviewFlow()

    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            reviewManager.launchReviewFlow(activity, reviewInfo)
        }
    }
}

/**
 * Main Settings Screen with state management
 */
@Composable
fun SettingsScreen(
    onProfileClick: (Boolean) -> Unit,
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val activity = LocalContext.current as Activity

    // Collect all states
    val notificationsEnabled by viewModel.notificationEnabled.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userInfoUiState by viewModel.userInfoUiState.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val notificationPreferences by viewModel.notificationPreferences.collectAsStateWithLifecycle()

    var currentDialog by remember { mutableStateOf(DialogType.NONE) }

    SettingsContent(
        currentDialog = currentDialog,
        isLoggedIn = isLoggedIn,
        languageList = appLanguages,
        userInfoUiState = userInfoUiState,
        selectedFontSize = fontScale,
        notificationPreferences = notificationPreferences,
        selectedLanguage = selectedLanguage,
        onFontSizeChange = { viewModel.updateFontScale(it.scale) },
        onPreferenceChange = { viewModel.updateNotificationPreferences(it) },
        onTestNotification = { viewModel.sendTestNotification() },
        onNotificationsToggle = { enabled ->
            if (enabled) {
                viewModel.requestNotificationPermission(activity)
            }
            viewModel.updateNotificationEnabled(enabled)
        },
        onProfileClick = onProfileClick,
        onLanguageClick = { language ->
            viewModel.changeLanguage(language.code)
            val intent = activity.intent
            activity.finish()
            activity.startActivity(intent)
        },
        onHelpClick = { currentDialog = DialogType.HELP },
        onPrivacyClick = { currentDialog = DialogType.PRIVACY },
        onAboutClick = { currentDialog = DialogType.ABOUT },
        onRateClick = { currentDialog = DialogType.RATE },
        onDismiss = { currentDialog = DialogType.NONE },
        onLaunchReview = { launchInAppReview(activity) }
    )
}

/**
 * Settings content with dialog management
 */
@Composable
private fun SettingsContent(
    currentDialog: DialogType,
    isLoggedIn: Boolean,
    languageList: List<AppLanguage>,
    userInfoUiState: SettingsScreenViewModel.SettingsUiState<*>,
    selectedFontSize: FontScale,
    notificationPreferences: NotificationPreferences,
    selectedLanguage: AppLanguage,
    onFontSizeChange: (FontScale) -> Unit,
    onPreferenceChange: (NotificationPreferences) -> Unit,
    onTestNotification: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onProfileClick: (Boolean) -> Unit,
    onLanguageClick: (AppLanguage) -> Unit,
    onHelpClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
    onRateClick: () -> Unit,
    onDismiss: () -> Unit,
    onLaunchReview: () -> Unit
) {
    // Handle dialogs
    DialogHandler(
        currentDialog = currentDialog,
        onDismiss = onDismiss,
        onLaunchReview = onLaunchReview
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader()

        ProfileSection(
            userInfoUiState = userInfoUiState,
            isLoggedIn = isLoggedIn,
            onProfileClick = onProfileClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppearanceSection(
            selectedFontSize = selectedFontSize,
            onFontSizeChange = onFontSizeChange
        )
        
        GeneralSection(
            selectedLanguage = selectedLanguage,
            availableLanguages = languageList,
            onLanguageClick = onLanguageClick
        )

        NotificationSection(
            preferences = notificationPreferences,
            onPreferenceChange = onPreferenceChange,
            onToggleMaster = onNotificationsToggle,
            onTestNotification = onTestNotification
        )

        SupportSection(
            onHelpClick = onHelpClick,
            onPrivacyClick = onPrivacyClick,
            onAboutClick = onAboutClick,
            onRateClick = onRateClick
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Dialog handler for all settings dialogs
 */
@Composable
private fun DialogHandler(
    currentDialog: DialogType,
    onDismiss: () -> Unit,
    onLaunchReview: () -> Unit
) {
    when (currentDialog) {
        DialogType.HELP -> {
            SettingsDialog(
                onDismiss = onDismiss,
                title = stringResource(R.string.help_and_support_title),
                content = stringResource(R.string.help_support_content)
            )
        }
        DialogType.ABOUT -> {
            SettingsDialog(
                onDismiss = onDismiss,
                title = stringResource(R.string.about_title),
                content = stringResource(R.string.about_content)
            )
        }
        DialogType.RATE -> {
            RateAppDialog(
                onRateNow = onLaunchReview,
                onDismiss = onDismiss
            )
        }
        DialogType.PRIVACY -> {
            SettingsDialog(
                onDismiss = onDismiss,
                title = stringResource(R.string.privacy_title),
                content = stringResource(R.string.privacy_policy_content)
            )
        }
        DialogType.NONE -> { /* No dialog */ }
    }
}

/**
 * Settings screen header
 */
@Composable
private fun SettingsHeader() {
    Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(16.dp)
    )
}

/**
 * Profile section with user info
 */
@Composable
private fun ProfileSection(
    userInfoUiState: SettingsScreenViewModel.SettingsUiState<*>,
    isLoggedIn: Boolean,
    onProfileClick: (Boolean) -> Unit
) {
    val userName = when (userInfoUiState) {
        is SettingsScreenViewModel.SettingsUiState.Success<*> -> {
            (userInfoUiState.data as? com.prj.domain.model.profilescreen.User)?.name
                ?: stringResource(R.string.guest_user)
        }
        else -> stringResource(R.string.guest_user)
    }

    val profilePrompt = stringResource(
        if (isLoggedIn) R.string.view_your_account
        else R.string.sign_in
    )

    ProfileCard(
        userName = userName,
        profilePrompt = profilePrompt,
        onClick = { onProfileClick(isLoggedIn) }
    )
}

@Composable
fun ProfileCard(
    userName: String,
    profilePrompt: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = settingsCardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = profilePrompt,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Appearance settings section
 */
@Composable
private fun AppearanceSection(
    selectedFontSize: FontScale,
    onFontSizeChange: (FontScale) -> Unit
) {
    SectionHeader(stringResource(R.string.appearance))
    FontSizeSelector(
        selectedFontSize = selectedFontSize,
        onFontSizeChange = onFontSizeChange
    )
}

@Composable
fun FontSizeSelector(
    selectedFontSize: FontScale,
    onFontSizeChange: (FontScale) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            text = stringResource(R.string.font_size),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FontSizeOption(stringResource(R.string.font_size_small), selectedFontSize == FontScale.SMALL) {
                onFontSizeChange(
                    FontScale.SMALL
                )
            }
            FontSizeOption(stringResource(R.string.font_size_medium), selectedFontSize == FontScale.MEDIUM) {
                onFontSizeChange(
                    FontScale.MEDIUM
                )
            }
            FontSizeOption(stringResource(R.string.font_size_large), selectedFontSize == FontScale.LARGE) {
                onFontSizeChange(
                    FontScale.LARGE
                )
            }
        }
    }
}

@Composable
fun RowScope.FontSizeOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) settingsAccentBlue else settingsCardBackground
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 14.sp
        )
    }
}

/**
 * General settings section
 */
@Composable
private fun GeneralSection(
    selectedLanguage: AppLanguage,
    availableLanguages: List<AppLanguage>,
    onLanguageClick: (AppLanguage) -> Unit
) {
    SectionHeader(stringResource(R.string.general))
    LanguageMenuItem(
        selectedLanguage = selectedLanguage,
        availableLanguages = availableLanguages,
        onLanguageSelected = onLanguageClick
    )
}

@Composable
private fun NotificationSection(
    preferences: NotificationPreferences,
    onPreferenceChange: (NotificationPreferences) -> Unit,
    onToggleMaster: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
) {
    SectionHeader(stringResource(R.string.settings_notifications_title))
    
    NotificationToggle(
        enabled = preferences.notificationEnabled,
        onToggle = onToggleMaster
    )

    if (preferences.notificationEnabled) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            // Reminder Time
            var showTimePicker by remember { mutableStateOf(false) }
            
            PreferenceItem(
                title = stringResource(R.string.settings_reminder_time),
                subtitle = preferences.dailyReminderTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onClick = { showTimePicker = true }
            )

            if (showTimePicker) {
                JapanTimePickerDialog(
                    initialTime = preferences.dailyReminderTime,
                    onTimeSelected = { 
                        showTimePicker = false
                        onPreferenceChange(preferences.copy(dailyReminderTime = it))
                    },
                    onDismiss = { showTimePicker = false }
                )
            }

            // Toggles
            NotificationPreferenceToggle(
                title = stringResource(R.string.settings_progress_notifications),
                description = stringResource(R.string.settings_progress_notifications_desc),
                checked = preferences.progressNotificationsEnabled,
                onCheckedChange = { onPreferenceChange(preferences.copy(progressNotificationsEnabled = it)) }
            )

            NotificationPreferenceToggle(
                title = stringResource(R.string.settings_review_reminders),
                description = stringResource(R.string.settings_review_reminders_desc),
                checked = preferences.reviewRemindersEnabled,
                onCheckedChange = { onPreferenceChange(preferences.copy(reviewRemindersEnabled = it)) }
            )

            NotificationPreferenceToggle(
                title = stringResource(R.string.settings_daily_word),
                description = stringResource(R.string.settings_daily_word_desc),
                checked = preferences.randomWordEnabled,
                onCheckedChange = { onPreferenceChange(preferences.copy(randomWordEnabled = it)) }
            )

            // Test Button
            if (BuildConfig.DEBUG) {
                TextButton(
                    onClick = onTestNotification,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_test_notification),
                        color = settingsAccentBlue
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationPreferenceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, color = Color.White)
            Text(text = description, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = settingsAccentBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = settingsCardBackground
            )
        )
    }
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f))
        Text(text = subtitle, fontSize = 16.sp, color = settingsSecondaryText)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = settingsSecondaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JapanTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text(stringResource(R.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_dialog))
            }
        },
        title = { Text(stringResource(R.string.settings_reminder_time)) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

@Composable
fun LanguageMenuItem(
    selectedLanguage: AppLanguage,
    availableLanguages: List<AppLanguage>,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val arrowIcon = if (expanded) {
        Icons.Default.KeyboardArrowDown
    } else {
        Icons.Default.KeyboardArrowRight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.choose_language),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // ===== RIGHT ANCHOR =====
        Box {
            Row(
                modifier = Modifier.clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLanguage.displayName,
                    fontSize = 16.sp,
                    color = settingsSecondaryText
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = arrowIcon,
                    contentDescription = null,
                    tint = settingsSecondaryText
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(x = 0.dp, y = 8.dp)
            ) {
                availableLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = language.displayName,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            expanded = false
                            if (language != selectedLanguage) {
                                onLanguageSelected(language)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.notification),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = settingsAccentBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = settingsCardBackground
            )
        )
    }
}

/**
 * Support section with help items
 */
@Composable
private fun SupportSection(
    onHelpClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit,
    onRateClick: () -> Unit
) {
    SectionHeader(stringResource(R.string.support))

    val supportItems = remember {
        listOf(
            SupportItem(R.string.help_and_support_title, onHelpClick),
            SupportItem(R.string.privacy_title, onPrivacyClick),
            SupportItem(R.string.about_title, onAboutClick),
            SupportItem(R.string.rate_title, onRateClick)
        )
    }

    supportItems.forEach { item ->
        MenuItem(
            text = stringResource(item.textRes),
            onClick = item.onClick
        )
    }
}

@Composable
fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = settingsSecondaryText
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

/**
 * Data class for support menu items
 */
private data class SupportItem(
    val textRes: Int,
    val onClick: () -> Unit
)
