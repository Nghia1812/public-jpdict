package com.prj.japanlib.feature_jlpttest.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.prj.domain.model.testscreen.Question
import com.prj.japanlib.common.components.dashedBorder
import com.prj.japanlib.ui.theme.forgotColor
import com.prj.japanlib.ui.theme.forgotContainerColor
import com.prj.japanlib.ui.theme.rememberedColor
import com.prj.japanlib.ui.theme.rememberedContainerColor
import com.prj.japanlib.R

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ResultQuestionCard(
    question: Question,
    selectedIndex: Int?,
    audioPlayerManager: AudioPlayerManager
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(4.dp, Color.DarkGray),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Question number badge
            val isCorrect = selectedIndex == question.answer

            ResultBadge(question = question.number.toString(), isCorrect = isCorrect)

            Spacer(modifier = Modifier.height(12.dp))

            when (question) {
                is Question.AudioQuestion -> {
                    AudioPlayer(audioUrl = question.audioURL, audioPlayerManager = audioPlayerManager)
                    GlideImage(model = question.text, contentDescription = "Image")
                }

                is Question.PassageQuestion -> {
                    HtmlText(
                        html = question.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HtmlText(
                        html = question.passage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    // Question text
                    HtmlText(
                        html = question.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Answer options
            question.options.forEachIndexed { index, option ->
                val isCorrectId = index == question.answer
                val isCorrect = selectedIndex == question.answer
                val isSelected = selectedIndex == index
                OutlinedButton(
                    onClick = { },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color.Transparent
                        },
                        contentColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = when {
                            isCorrect && isSelected || isCorrectId
                                -> Color.Green
                            isSelected -> Color.Red
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Option letter (A, B, C, D)
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ('A' + index).toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        HtmlText(
                            html = option,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultBadge(
    question: String,
    isCorrect: Boolean
) {
    val bgColor = if (isCorrect) rememberedContainerColor else forgotContainerColor
    val contentColor = if (isCorrect) rememberedColor else forgotColor
    val text = if (isCorrect) 
        stringResource(id = R.string.question_correct, question) 
    else 
        stringResource(id = R.string.question_incorrect, question)
    val icon = if (isCorrect) Icons.Default.Check else Icons.Default.Close

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
