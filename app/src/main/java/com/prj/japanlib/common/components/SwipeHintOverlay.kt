package com.prj.japanlib.common.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.japanlib.R

enum class SwipeDirection {
    LEFT, RIGHT
}


/**
 * A composable that displays a semi-transparent overlay with an animated arrow and text
 * to hint to the user that they can swipe in a particular direction. It also provides a
 * message indicating that they can tap anywhere to dismiss the overlay.
 *
 *
 * @param direction The [SwipeDirection] in which to animate the hint arrow (LEFT or RIGHT).
 * @param onDismiss A lambda function that is invoked when the user taps anywhere on the overlay
 *                  to dismiss it.
 */
@Composable
fun SwipeHintOverlay(
    direction: SwipeDirection,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismiss()
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated arrow
            val infiniteTransition = rememberInfiniteTransition(label = "swipe_animation")
            val offsetX by infiniteTransition.animateFloat(
                initialValue = if (direction == SwipeDirection.LEFT) 20f else -20f,
                targetValue = if (direction == SwipeDirection.LEFT) -20f else 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "arrow_offset"
            )

            Icon(
                imageVector = if (direction == SwipeDirection.LEFT) {
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft
                } else {
                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = offsetX.dp)
            )

            Text(
                text = if (direction == SwipeDirection.LEFT) {
                    stringResource(R.string.swipe_left_hint)
                } else {
                    stringResource(R.string.swipe_right_hint)
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.tap_to_dismiss),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
