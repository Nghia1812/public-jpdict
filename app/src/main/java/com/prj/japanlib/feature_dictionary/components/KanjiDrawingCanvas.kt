package com.prj.japanlib.feature_dictionary.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.prj.japanlib.BuildConfig
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.Line
import com.prj.japanlib.common.components.CustomGlassButton
import com.prj.japanlib.common.components.dashedBorder
import com.prj.japanlib.ui.theme.onPrimaryLight
import com.prj.japanlib.ui.theme.secondaryLight
import timber.log.Timber

/**
 * A composable that provides a canvas for drawing Kanji characters.
 *
 * This component allows users to draw strokes on a designated area.
 *
 * @param modifier The modifier to be applied to the canvas container.
 * @param strokeWidth The width of the drawing strokes.
 * @param onSaveBitmap A callback function that is invoked when the user saves the drawing. It provides the captured [Bitmap].
 * @param isLoading A boolean to indicate if classification is running, used to show loading state on buttons.
 */
@Composable
fun KanjiDrawingCanvas(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 24f,
    onSaveBitmap: (Bitmap) -> Unit,
    isLoading: Boolean
) {
    val bgColor = Color(0xFF1A2233)
    val borderColor = Color(0xFF708090).copy(alpha = 0.35f)
    val strokeColor = Color.White

    val lines = remember { mutableStateListOf<Line>() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var currentLine by remember { mutableStateOf<Line?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // === DRAWING CANVAS BOX ===
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .fillMaxWidth()
                .height(340.dp)
                .background(bgColor)
                .dashedBorder(8.dp, borderColor, 20.dp)
        ) {
            // The Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size = it }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentLine = Line(offset, offset, strokeColor, strokeWidth)
                            },
                            onDrag = { change, _ ->
                                val newPos = change.position
                                currentLine?.let { line ->
                                    lines.add(
                                        Line(
                                            start = line.end,
                                            end = newPos,
                                            color = strokeColor,
                                            strokeWidth = strokeWidth
                                        )
                                    )
                                    currentLine = line.copy(end = newPos)
                                }
                            },
                            onDragEnd = { currentLine = null }
                        )
                    }
            ) {
                lines.forEach { line ->
                    drawLine(
                        color = line.color,
                        start = line.start,
                        end = line.end,
                        strokeWidth = line.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Placeholder text when empty
            if (lines.isEmpty()) {
                Text(
                    text = stringResource(R.string.draw_kanji),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // === BUTTON ROW ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // CLEAR button
            CustomGlassButton(
                onClick = {
                    lines.clear()
                    bitmap = null
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                text = stringResource(R.string.clear_image),
                enabled = true,
                icon = ImageVector.vectorResource(id = R.drawable.clear_ic),
                isPrimary = false,
                isLoading = isLoading
            )

            Spacer(Modifier.width(12.dp))

            // SUBMIT button
            CustomGlassButton(
                onClick = {
                    if (size.width > 0 && size.height > 0 && lines.isNotEmpty()) {
                        bitmap = captureCanvasToBitmap(
                            lines = lines,
                            strokeWidth = strokeWidth,
                            canvasWidth = size.width,
                            canvasHeight = size.height,
                            outputSize = 64
                        )
                        bitmap?.let(onSaveBitmap)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                text = stringResource(R.string.save),
                enabled = lines.isNotEmpty(),
                icon = ImageVector.vectorResource(id = R.drawable.save_ic),
                isPrimary = true,
                isLoading = isLoading
            )
        }
    }
}

/**
 * Captures the drawn lines on the canvas and converts them into a scaled bitmap.
 *
 * This function takes a list of lines, the original canvas dimensions, and an output size.
 * It creates a new bitmap of the specified output size, scales the lines to fit,
 * and draws them onto the new bitmap.
 *
 * @param lines The list of [Line] objects representing the user's drawing.
 * @param strokeWidth The original stroke width used on the canvas.
 * @param canvasWidth The width of the original composable canvas.
 * @param canvasHeight The height of the original composable canvas.
 * @param outputSize The desired width and height of the output square bitmap. Defaults to 64.
 * @return A [Bitmap] of size `outputSize`x`outputSize` with the drawing rendered on it.
 */
fun captureCanvasToBitmap(
    lines: List<Line>,
    strokeWidth: Float,
    canvasWidth: Int,
    canvasHeight: Int,
    outputSize: Int = 64
): Bitmap {
    val bitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    canvas.drawColor(android.graphics.Color.BLACK)

    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        this.strokeWidth = strokeWidth * (outputSize.toFloat() / canvasWidth)
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    // Scale factors
    val scaleX = outputSize.toFloat() / canvasWidth
    val scaleY = outputSize.toFloat() / canvasHeight

    // Draw all lines
    lines.forEach { line ->
        canvas.drawLine(
            line.start.x * scaleX,
            line.start.y * scaleY,
            line.end.x * scaleX,
            line.end.y * scaleY,
            paint
        )
    }

    return bitmap
}
