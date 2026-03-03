package com.prj.japanlib.feature_jlpttest.components

import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

@Composable
fun HtmlText(
    html: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    val context = LocalContext.current
    val spSize = with(LocalDensity.current) { style.fontSize.toPx() / context.resources.displayMetrics.scaledDensity }

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(context).apply {
                textSize = spSize
                typeface = when (style.fontWeight) {
                    FontWeight.Bold -> android.graphics.Typeface.DEFAULT_BOLD
                    else -> android.graphics.Typeface.DEFAULT
                }
                setTextColor(textColor.toArgb())
            }
        },
        update = { textView ->
            textView.text = HtmlCompat.fromHtml(
                html.replace(Regex("class=\"[^\"]*\""), ""),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    )
}