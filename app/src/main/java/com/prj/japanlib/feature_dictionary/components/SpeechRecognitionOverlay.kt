package com.prj.japanlib.feature_dictionary.components

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.japanlib.R

/**
 * A composable that provides a full-screen overlay for speech recognition.
 * Guides the user through a two-step process:
 *
 * It handles the lifecycle of the Android `SpeechRecognizer`, provides feedback to the user,
 * and returns the final recognized text. The overlay can be dismissed by tapping anywhere
 * on the background.
 *
 * @param onDismiss A lambda function invoked when the overlay is dismissed, either by the user
 *                  tapping the background, on a recognition error, or after successful recognition.
 * @param onTextRecognized A lambda function that returns the final recognized text string
 *                         when the speech recognition is successfully completed.
 */
@Composable
fun SpeechRecognitionOverlay(
    onDismiss: () -> Unit,
    onTextRecognized: (String) -> Unit,
    onError: () -> Unit = {}
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf<String?>(null) } // null = not started

    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }
    // Set up the listener for speech recognition events.
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                onError()
                onDismiss()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                recognizedText = text
                isListening = false

                // Send text back and dismiss
                if (text.isNotEmpty()) {
                    onTextRecognized(text)
                    onDismiss()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Update the UI with partial, real-time results
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                recognizedText = matches?.firstOrNull() ?: ""
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    /**
     * Configures and starts the speech recognition process for the given language.
     * @param language The language code (e.g., "ja-JP" or "en-US").
     */
    fun startListening(language: String) {
        selectedLanguage = language
        speechRecognizer.setRecognitionListener(recognitionListener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }
    // Ensure the SpeechRecognizer is properly destroyed when the composable is disposed.
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
        }
    }

    // Fullscreen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Dismiss the overlay on background click.
                speechRecognizer.stopListening()
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            if (selectedLanguage == null) {
                // Language selection screen
                Text(
                    text = stringResource(R.string.choose_language),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Japanese button
                Button(
                    onClick = { startListening("ja-JP") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.japanese),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // English button
                Button(
                    onClick = { startListening("en-US") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.english),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

            } else {
                // Listening screen (after language is selected)

                // Animated microphone icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.mic_ic),
                        contentDescription = "Listening",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                // Language indicator
                Text(
                    text = if (selectedLanguage == "ja-JP") stringResource(R.string.japanese) else stringResource(R.string.english),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )

                // Status text
                Text(
                    text = if (isListening) stringResource(R.string.listening) else stringResource(R.string.processing),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )

                // Recognized text (partial results)
                if (recognizedText.isNotEmpty()) {
                    Text(
                        text = recognizedText,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tap to cancel hint
            Text(
                text = stringResource(R.string.cancel_voice),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}
