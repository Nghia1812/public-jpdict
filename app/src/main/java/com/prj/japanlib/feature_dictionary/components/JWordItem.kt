package com.prj.japanlib.feature_dictionary.components

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.SpeechUtils.rememberTextToSpeech
import com.prj.japanlib.ui.theme.displayFontFamily

/**
 * A composable function that displays a single item in a list of Japanese words.
 *
 * This item shows the word's Kanji (if available), its reading (furigana), and its English meaning.
 *
 * @param japaneseWord The [JapaneseWord] data object containing the information to display.
 * @param onClick A callback function that is invoked when the item is clicked. It passes the ID of the word.
 */
@Composable
fun JWordItem(
    japaneseWord: JapaneseWord,
    isStarred: Boolean = false,
    onClick: (Int) -> Unit,
    onBookmarkClick: () -> Unit
) {
    val tts = rememberTextToSpeech()
    var isSpeaking by remember { mutableStateOf(false) }

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .wrapContentHeight()
            .clickable { onClick(japaneseWord.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val kanji = japaneseWord.kanji
            if (kanji?.isEmpty() == false) {
                Text(
                    text = japaneseWord.reading ?: "",
                    color = Color(0xFFAEB5C2),
                    fontSize = 10.sp,
                    fontFamily = displayFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Kanji
                Text(
                    text = kanji,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = displayFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Reading only (when kanji is null)
                Text(
                    text = japaneseWord.reading ?: "",
                    color = Color(0xFFAEB5C2),
                    fontSize = 10.sp,
                    fontFamily = displayFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = japaneseWord.reading ?: "",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = displayFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Meaning — always show
            Text(
                text = japaneseWord.meaning ?: "",
                color = Color(0xFF8C96A5),
                fontSize = 12.sp,
                fontFamily = displayFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = {
                val engine = tts.value ?: return@IconButton
                if (engine.isSpeaking) {
                    engine.stop()
                } else {
                    engine.speak(
                        japaneseWord.reading,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        japaneseWord.id.toString() // utteranceId
                    )
                }
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    if (isSpeaking) R.drawable.speaker_on else R.drawable.speaker_off
                ),
                contentDescription = null
            )
        }

        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = onBookmarkClick
        ) {
            Icon(
                imageVector = if (isStarred) ImageVector.vectorResource(
                    R.drawable.filled_star_ic
                ) else ImageVector.vectorResource(
                    R.drawable.star_ic
                ),
                contentDescription = null,
                tint = if (isStarred) Color(0xFFFFC107) else Color.White
            )
        }
    }
}