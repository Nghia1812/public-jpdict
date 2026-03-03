package com.prj.japanlib.feature_jlpttest.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.japanlib.common.utils.formatTime
import com.prj.japanlib.ui.theme.sectionProgressBar
import timber.log.Timber
import com.prj.japanlib.R

@Composable
fun AudioPlayer(
    audioUrl: String,
    audioPlayerManager: AudioPlayerManager,
    modifier: Modifier = Modifier
) {
    val currentUrl by audioPlayerManager.currentAudioUrl.collectAsState()
    val isPlaying by audioPlayerManager.isPlaying.collectAsState()
    val duration by audioPlayerManager.duration.collectAsState()
    val position by audioPlayerManager.position.collectAsState()

    val isThisAudioActive = currentUrl == audioUrl
    val displayIsPlaying = isThisAudioActive && isPlaying

    // Progress value Slider (0f -> 1f)
    val sliderValue = if (isThisAudioActive && duration > 0) {
        (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayTime = if (isThisAudioActive) {
        stringResource(id = R.string.audio_time_format, (position/1000).formatTime(), (duration/1000).formatTime())
    } else stringResource(id = R.string.audio_time_format, "00:00", "00:00")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (audioUrl.isNotBlank()) {
                audioPlayerManager.togglePlayPause(audioUrl)
            } else {
                Timber.i("audioUrl is blank")
            }
        }) {
            Icon(
                imageVector = if (displayIsPlaying) ImageVector.vectorResource(R.drawable.ic_pause) else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    if (isThisAudioActive && duration > 0) {
                        val seekPosition = (newValue * duration).toLong()
                        audioPlayerManager.seekTo(seekPosition)
                    }
                },
                onValueChangeFinished = {
                    Timber.i("onValueChangeFinished: seek completed")
                },
                valueRange = 0f..1f,
                steps = 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = sectionProgressBar,
                    inactiveTrackColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayTime,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
