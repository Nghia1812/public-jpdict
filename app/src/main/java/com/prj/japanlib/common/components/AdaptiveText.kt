package com.prj.japanlib.common.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    style: TextStyle = LocalTextStyle.current,
    maxFontSize: TextUnit = 64.sp,
    minFontSize: TextUnit = 20.sp,
    scaleFactor: Float = 1.0f
) {
    BoxWithConstraints(modifier = modifier) {
        // Cache calculated font size based on text and constraints
        val fontSize = remember(text, maxWidth, maxFontSize, minFontSize, scaleFactor) {
            calculateAdaptiveFontSize(
                text = text,
                availableWidth = maxWidth,
                maxFontSize = maxFontSize,
                minFontSize = minFontSize,
                scaleFactor = scaleFactor
            )
        }

        Text(
            text = text,
            fontSize = fontSize,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            style = style,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Calculate font size based on text length and available width
 * Pure function - no recomposition overhead
 */
private fun calculateAdaptiveFontSize(
    text: String,
    availableWidth: Dp,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    scaleFactor: Float
): TextUnit {
    val textLength = text.length
    val widthValue = availableWidth.value

    // Base font size calculation based on text length
    val baseFontSize = when {
        textLength <= 3 -> maxFontSize.value              // Very short: 1-3 chars
        textLength <= 5 -> maxFontSize.value * 0.85f      // Short: 4-5 chars
        textLength <= 8 -> maxFontSize.value * 0.70f      // Medium-short: 6-8 chars
        textLength <= 12 -> maxFontSize.value * 0.55f     // Medium: 9-12 chars
        textLength <= 18 -> maxFontSize.value * 0.45f     // Medium-long: 13-18 chars
        textLength <= 25 -> maxFontSize.value * 0.35f     // Long: 19-25 chars
        else -> maxFontSize.value * 0.30f                 // Very long: 26+ chars
    }

    // Adjust based on available width (responsive)
    // Estimate: 1 char ≈ 0.6 * fontSize in width
    val estimatedTextWidth = textLength * baseFontSize * 0.6f
    val widthRatio = if (estimatedTextWidth > widthValue) {
        widthValue / estimatedTextWidth
    } else {
        1.0f
    }

    // Apply width ratio and scale factor
    val adjustedSize = baseFontSize * widthRatio * scaleFactor

    // Clamp between min and max
    val clampedSize = adjustedSize.coerceIn(minFontSize.value, maxFontSize.value)

    return clampedSize.sp
}