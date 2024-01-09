package compose.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import compose.terminal.data.Bar
import kotlin.math.roundToInt

private const val VISIBLE_BARS_MIN_VALUE = 20
private const val START_VISIBLE_BARS_COUNT = 100
@Composable
fun Terminal(bars: List<Bar>) {
    var visibleBarsCount by remember {
        mutableIntStateOf(START_VISIBLE_BARS_COUNT)
    }
    var scrolledBy by remember {
        mutableFloatStateOf(0f)
    }
    var barWidth by remember {
        mutableFloatStateOf(0f)
    }
    val transformableState = TransformableState { zoomChange, panChange, _ ->
        val zoomVal = (visibleBarsCount / zoomChange).roundToInt()
        visibleBarsCount = zoomVal.coerceIn(VISIBLE_BARS_MIN_VALUE, bars.size)
        scrolledBy = (scrolledBy + panChange.x)
            .coerceIn(
                0f,
                (bars.size - visibleBarsCount) * barWidth
            )
    }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(transformableState)
    ) {
        val max = bars.maxOf { it.high }
        val min = bars.minOf { it.low }
        val pxPerPoint = size.height / (max - min)
        barWidth = size.width / visibleBarsCount
        translate(left = scrolledBy) {
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * barWidth
                drawLine(
                    color = Color.White,
                    start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open > bar.close) Color.Red else Color.Green,
                    start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = barWidth / 2
                )
            }
        }
    }
}