package compose.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    var terminalSize by remember {
        mutableStateOf(Size.Zero)
    }
    val barWidth by remember {
        derivedStateOf {
            terminalSize.width / visibleBarsCount
        }
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
        terminalSize = size
        val visibleMax = bars.take(visibleBarsCount).maxOf { it.high }
        val visibleMin = bars.take(visibleBarsCount).minOf { it.low }
        val pxPerPoint = size.height / (visibleMax - visibleMin)
        translate(left = scrolledBy) {
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * barWidth
                drawLine(
                    color = Color.White,
                    start = Offset(offsetX, size.height - ((bar.low - visibleMin) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - visibleMin) * pxPerPoint)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open > bar.close) Color.Red else Color.Green,
                    start = Offset(offsetX, size.height - ((bar.low - visibleMin) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - visibleMin) * pxPerPoint)),
                    strokeWidth = barWidth / 2
                )
            }
        }
    }
}