package com.prj.japanlib.feature_jlpttest.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.prj.japanlib.R

@Composable
fun LearningPreferencesDialog(
    onDismiss: () -> Unit,
    initialShuffleWords: Boolean,
    initialShowMeaningFirst: Boolean,
    onPreferencesChanged: (shuffleWords: Boolean, showMeaningFirst: Boolean) -> Unit
) {
    var shuffleWords by remember { mutableStateOf(initialShuffleWords) }
    var showMeaningFirst by remember { mutableStateOf(initialShowMeaningFirst) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A2332),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Title
                Text(
                    text = stringResource(R.string.learning_preferences_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.learning_preferences_subtitle),
                    fontSize = 13.sp,
                    color = Color(0xFF8FA0B3)
                )

                Spacer(Modifier.height(24.dp))

                // Shuffle Words Option
                PreferenceItem(
                    title = stringResource(R.string.shuffle_words),
                    description = stringResource(R.string.shuffle_words_description),
                    checked = shuffleWords,
                    onCheckedChange = { shuffleWords = it }
                )

                Spacer(Modifier.height(16.dp))

                // Show Meaning First Option
                PreferenceItem(
                    title = stringResource(R.string.show_meaning_first),
                    description = null,
                    checked = showMeaningFirst,
                    onCheckedChange = { showMeaningFirst = it }
                )

                Spacer(Modifier.height(24.dp))

                // Done Button
                Button(
                    onClick = {
                        onPreferencesChanged(shuffleWords, showMeaningFirst)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F7BFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceItem(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            if (description != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF8FA0B3)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Custom Switch
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4F7BFF),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2F3E52),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
