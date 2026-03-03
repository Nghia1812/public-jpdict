package com.prj.japanlib.feature_translator

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.R
import com.prj.domain.model.translatescreen.TranslationResult
import com.prj.japanlib.common.components.CustomToast
import com.prj.japanlib.common.components.rememberToastState
import com.prj.japanlib.common.utils.SpeechUtils.rememberTextToSpeech
import com.prj.japanlib.feature_translator.viewmodel.TranslationViewModel
import com.prj.japanlib.ui.theme.JapanlibTheme

// ==========================================
// STATEFUL FUNCTION - Manages State
// ==========================================
@Composable
fun TranslatorScreen() {
    var sourceText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var responseText: String? by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf("en") }
    var targetLanguage by remember { mutableStateOf("ja") }
    val translationViewModel: TranslationViewModel = hiltViewModel()
    val translationUiState by translationViewModel.translations.collectAsStateWithLifecycle()
    val tts = rememberTextToSpeech()
    var isSpeaking by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val retryMessage = stringResource(R.string.retrying_message)

    // Listen for TTS playback completion
    LaunchedEffect(tts.value) {
        tts.value?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                isSpeaking = false
            }
        })
    }
    LaunchedEffect(translationUiState) {
        when (translationUiState) {
            is TranslationViewModel.TranslationUiState.Success -> {
                val translationResult =
                    (translationUiState as TranslationViewModel.TranslationUiState.Success).data
                responseText = translationResult.translatedText
            }

            is TranslationViewModel.TranslationUiState.Empty -> {
                responseText = ""
            }

            is TranslationViewModel.TranslationUiState.Error -> {
                responseText =
                    (translationUiState as TranslationViewModel.TranslationUiState.Error<TranslationResult>).message
            }

            is TranslationViewModel.TranslationUiState.Loading -> {
            }

            is TranslationViewModel.TranslationUiState.Retrying -> {
                responseText = retryMessage
            }
        }
    }

    fun playAudio(text: String?){
        val engine = tts.value ?: return
        if (engine.isSpeaking) {
            engine.stop()
        } else {
            engine.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                text // utteranceId
            )
        }
    }

    TranslationScreenContent(
        sourceText = sourceText,
        targetText = responseText,
        sourceLanguage = sourceLanguage,
        targetLanguage = targetLanguage,
        isSpeaking = isSpeaking,
        onSourceTextChange = { sourceText = it },
        onClearSource = {
            sourceText = TextFieldValue("")
            responseText = ""
        },
        onSwapLanguages = {
            val swapped = responseText
            responseText = sourceText.text
            sourceText = TextFieldValue(
                text = swapped ?: "",
                selection = TextRange(swapped?.length ?: 0) // put cursor at end
            )
            sourceLanguage = targetLanguage.also { targetLanguage = sourceLanguage }
        },
        onPlaySourceAudio = {
            playAudio(sourceText.text)
        },
        onPlayTargetAudio = {
            playAudio(responseText)
        },
        onCopySource = {
            clipboardManager.setText(AnnotatedString(sourceText.text))
        },
        onCopyTarget = {
            responseText?.let { clipboardManager.setText(AnnotatedString(it)) }
        },
        onTextTranslate = {
            if (sourceText.text.isNotEmpty()){
                translationViewModel.getTranslatedText(sourceText.text, sourceLanguage, targetLanguage)
            }
        }
    )
}

// ==========================================
// STATELESS FUNCTION - Only UI
// ==========================================
@Composable
fun TranslationScreenContent(
    sourceText: TextFieldValue,
    targetText: String?,
    sourceLanguage: String,
    targetLanguage: String,
    onSourceTextChange: (TextFieldValue) -> Unit,
    onClearSource: () -> Unit,
    onSwapLanguages: () -> Unit,
    onPlaySourceAudio: () -> Unit,
    onPlayTargetAudio: () -> Unit,
    onCopySource: () -> Unit,
    onCopyTarget: () -> Unit,
    onTextTranslate: () -> Unit,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val toastState = rememberToastState()
    val copyToClipBoardText = stringResource(R.string.copy_to_clipboard)
    val limitReach = stringResource(R.string.limit_reach)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1929))
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            }
    ) {
        // TITLE
        Text(
            text = stringResource(R.string.translation_screen_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // SOURCE LANGUAGE CARD
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF132332),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Language header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sourceLanguage,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7D8F)
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF6B7D8F),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onClearSource() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Source text
                TextField(
                    value = sourceText,
                    onValueChange = { onSourceTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text(color = Color.Gray, text = stringResource(R.string.source_text_field)) },
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (sourceText.text.length > 10){
                                toastState.show(limitReach)
                            }
                            else {
                                onTextTranslate()
                            }
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onPlaySourceAudio,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                if (isSpeaking) R.drawable.speaker_on else R.drawable.speaker_off
                            ),
                            contentDescription = "Play audio",
                            tint = Color(0xFF6B7D8F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onCopySource()
                            toastState.show(copyToClipBoardText)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.copy_ic),
                            contentDescription = "Copy",
                            tint = Color(0xFF6B7D8F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SWAP BUTTON
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E3A52))
                    .clickable { onSwapLanguages() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.swap_ic),
                    contentDescription = "Swap languages",
                    tint = Color(0xFF6B9FD8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TARGET LANGUAGE CARD
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A2E42),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Language header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = targetLanguage,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7D8F)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target text
                TextField(
                    value = targetText ?: "",
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    readOnly = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledTextColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onPlayTargetAudio,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                if (isSpeaking) R.drawable.speaker_on else R.drawable.speaker_off
                            ),
                            contentDescription = "Play audio",
                            tint = Color(0xFF6B7D8F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onCopyTarget()
                            toastState.show(copyToClipBoardText)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.copy_ic),
                            contentDescription = "Copy",
                            tint = Color(0xFF6B7D8F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    CustomToast(
        toastState = toastState,
        alignment = Alignment.TopCenter
    )
}

// ==========================================
// PREVIEW
// ==========================================
@Preview
@Composable
fun TranslationScreenPreview() {
    JapanlibTheme() {
        TranslationScreenContent(
            sourceText = TextFieldValue("TODO()"),
            targetText = "TODO()",
            sourceLanguage = "TODO()",
            targetLanguage = "TODO()",
            onSourceTextChange = {},
            onClearSource = {},
            onSwapLanguages = {},
            onPlaySourceAudio = {},
            onPlayTargetAudio = {},
            onCopySource = {},
            onCopyTarget = {},
            onTextTranslate = {},
            modifier = Modifier
        )
    }
}
