package com.prj.japanlib.feature_dictionary.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.japanlib.R

/**
 * A composable that displays a circular progress indicator with a percentage text in the center.
 *
 *
 * @param progress The progress value to display, from 0.0f to 1.0f.
 * @param percentage The integer value of the percentage to display in the center (0-100).
 * @param size The overall diameter of the progress circle.
 */
@Composable
fun ProgressCircle(
    progress: Float,
    percentage: Int,
    size: Dp = 48.dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            color = Color(0xFF007BFF),
            strokeWidth = 4.dp,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = stringResource(R.string.progress_percentage, percentage),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}