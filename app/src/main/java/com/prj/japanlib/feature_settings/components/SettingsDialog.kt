package com.prj.japanlib.feature_settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.prj.japanlib.R

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    title: String,
    content: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = content)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_dialog))
            }
        }
    )
}


@Preview
@Composable
fun SettingsDialogPreview() {
    SettingsDialog(
        onDismiss = {},
        title = stringResource(R.string.privacy_title),
        content = stringResource(R.string.privacy_policy_content)
    )
}