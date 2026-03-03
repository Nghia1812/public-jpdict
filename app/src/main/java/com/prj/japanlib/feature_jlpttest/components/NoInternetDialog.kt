package com.prj.japanlib.feature_jlpttest.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prj.japanlib.R
import com.prj.japanlib.common.components.SingleActionDialog

@Composable
fun NoInternetDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    SingleActionDialog(
        showDialog = showDialog,
        title = stringResource(id = R.string.no_internet_title),
        message = stringResource(id = R.string.no_internet_message),
        onDismiss = onDismiss
    )
}