package com.prj.japanlib.feature_settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prj.japanlib.R

@Composable
fun RateAppDialog(
    onRateNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.rate_title))
        },
        text = {
            Text(text = stringResource(R.string.rate_content))
        },
        confirmButton = {
            TextButton(onClick = onRateNow) {
                Text(text = stringResource(R.string.rate_app_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.rate_app_negative))
            }
        }
    )
}
