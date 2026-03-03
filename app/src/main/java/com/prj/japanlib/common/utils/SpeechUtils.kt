package com.prj.japanlib.common.utils

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber
import java.util.Locale

object SpeechUtils {
    @Composable
    fun rememberTextToSpeech(): MutableState<TextToSpeech?> {
        val context = LocalContext.current
        val tts = remember { mutableStateOf<TextToSpeech?>(null) }
        DisposableEffect(context) {
            var textToSpeech : TextToSpeech? = null
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(Locale.JAPAN)
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Timber.w("Japanese language not supported or missing data.")
                        Toast
                            .makeText(
                                context,
                                "Japanese language not supported or missing data.",
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }
                }
            }
            tts.value = textToSpeech
            onDispose {
                textToSpeech.stop()
                textToSpeech.shutdown()
            }
        }
        return tts
    }
}