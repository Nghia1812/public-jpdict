package com.prj.japanlib.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.prj.japanlib.ui.theme.onPrimaryDark
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Toast State Manager
class ToastState {
    var message by mutableStateOf<String?>(null)
        private set

    var isVisible by mutableStateOf(false)
        private set

    fun show(text: String, duration: Long = 3000L) {
        message = text
        isVisible = true

        // Auto-hide after duration
        GlobalScope.launch {
            delay(duration)
            hide()
        }
    }

    fun hide() {
        isVisible = false
    }
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

@Composable
fun CustomToast(
    toastState: ToastState,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        AnimatedVisibility(
            visible = toastState.isVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(8.dp),
                color = onPrimaryDark,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = toastState.message ?: "",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
