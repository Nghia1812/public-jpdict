package com.prj.japanlib.feature_dictionary.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.domain.model.dictionaryscreen.KanjiDetail
import com.prj.japanlib.R
import kotlin.collections.emptyList

/**
 * KanjiDetailItem Composable
 * Represents a Kanji details with stroke information, meanings, on/kun readings, and meta-information.
 */
@Composable
fun KanjiDetailItem(
    kanji: KanjiDetail,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B2434)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Kanji + Stroke preview
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                KanjiStrokeCanvas(
                    strokePaths = kanji.strokePaths,
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Color(0xFF243354),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                )
            }

            Text(
                text = kanji.meanings.joinToString(", "),
                color = Color.White,
                fontSize = 14.sp
            )

            if (kanji.onyomi.isNotEmpty()) {
                ReadingRow(stringResource(R.string.on_label), kanji.onyomi)
            }

            if (kanji.kunyomi.isNotEmpty()) {
                ReadingRow(stringResource(R.string.kun_label), kanji.kunyomi)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                kanji.strokeCount?.let {
                    MetaChip(stringResource(R.string.stroke_label, it))
                }
                kanji.grade?.let {
                    MetaChip(stringResource(R.string.grade_label, it))
                }
                kanji.jlpt?.let {
                    MetaChip(stringResource(R.string.level_label, it))
                }
            }
        }
    }
}

@Composable
private fun ReadingRow(
    label: String,
    readings: List<String>
) {
    Row {
        Text(
            text = "$label:",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = readings.joinToString("、"),
            color = Color(0xFFCCCCCC)
        )
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF243354)
    ) {
        Text(
            text = text,
            color = Color(0xFFB0C4FF),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

data class ProcessedStroke(
    val originalPathData: String,
    val parsedPath: Path,
    val startPoint: Offset
)

@Composable
fun KanjiStrokeCanvas(
    strokePaths: List<String>,
    modifier: Modifier = Modifier,
    showNumbers: Boolean = true
) {
    // Cache parsed strokes
    val processedStrokes = remember(strokePaths) {
        strokePaths.map { pathData ->
            ProcessedStroke(
                originalPathData = pathData,
                parsedPath = PathParser().parsePathString(pathData).toPath(),
                startPoint = getPathStartPoint(pathData)
            )
        }
    }

    // Cache transformed paths by size
    var cachedSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var transformedPaths by remember { mutableStateOf<List<Path>>(emptyList()) }
    var transformedStartPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var scaledStrokeWidth by remember { mutableStateOf(5f) }
    Canvas(modifier = modifier) {
        // recalculate only when size changes
        if (size != cachedSize) {
            cachedSize = size

            val canvasSize = size.minDimension
            val scale = canvasSize / 109f
            val offsetX = (size.width - 109f * scale) / 2f
            val offsetY = (size.height - 109f * scale) / 2f
            scaledStrokeWidth = 5f * scale
            val transformMatrix = Matrix().apply {
                scale(scale, scale)
                translate(offsetX / scale, offsetY / scale)
            }

            // Transform paths
            transformedPaths = processedStrokes.map { stroke ->
                Path().apply {
                    addPath(stroke.parsedPath)
                    transform(transformMatrix)
                }
            }

            // Transform start points for numbers
            transformedStartPoints = processedStrokes.map { stroke ->
                Offset(
                    x = stroke.startPoint.x * scale + offsetX - 15f,
                    y = stroke.startPoint.y * scale + offsetY
                )
            }
        }

        // Draw cached paths
        transformedPaths.forEachIndexed { index, path ->
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = scaledStrokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            if (showNumbers && index < transformedStartPoints.size) {
                val numberPos = transformedStartPoints[index]

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    val textY = numberPos.y - (paint.descent() + paint.ascent()) / 2
                    drawText("${index + 1}", numberPos.x, textY, paint)
                }
            }
        }
    }
}

// Helper function to extract starting point from SVG path
fun getPathStartPoint(pathData: String): Offset {
    return try {
        // Parse path string to Android Path
        val androidPath = PathParser().parsePathString(pathData).toPath()

        // Convert Compose Path sang Android Path để dùng PathMeasure
        val nativePath = android.graphics.Path()
        androidPath.asAndroidPath().let { nativePath.set(it) }

        // PathMeasure to get start point
        val pathMeasure = android.graphics.PathMeasure(nativePath, false)
        val startPos = FloatArray(2)

        // Get point at position 0
        if (pathMeasure.getPosTan(0f, startPos, null)) {
            Offset(startPos[0], startPos[1])
        } else {
            Offset(0f, 0f)
        }
    } catch (e: Exception) {
        Offset(0f, 0f)
    }
}
