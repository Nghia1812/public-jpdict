package com.prj.japanlib.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color = Color.White,
    borderColor: Color? = null
) {
    val shape = RoundedCornerShape(12.dp)

    if (borderColor != null) {
        // Outlined-style button
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            border = BorderStroke(1.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    } else {
        // Filled button
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
