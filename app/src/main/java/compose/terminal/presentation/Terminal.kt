package compose.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import compose.terminal.data.Bar
import kotlin.math.roundToInt

private const val VISIBLE_BARS_MIN_VALUE = 20

@Composable
fun Terminal(bars: List<Bar>) {
    var terminalState by rememberTerminalState(bars)
    val transformableState = TransformableState { zoomChange, panChange, _ ->
        val zoomVal = (terminalState.visibleBarsCount / zoomChange).roundToInt()
        val visibleBarsCount = zoomVal.coerceIn(VISIBLE_BARS_MIN_VALUE, bars.size)
        val scrolledBy = (terminalState.scrolledBy + panChange.x)
            .coerceIn(0f, (bars.size - terminalState.visibleBarsCount) * terminalState.barWidth)
        terminalState = terminalState.copy(
            visibleBarsCount = visibleBarsCount,
            scrolledBy = scrolledBy
        )
    }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
            .transformable(transformableState)
            .onSizeChanged {
                terminalState = terminalState.copy(terminalWidth = it.width.toFloat())
            }
    ) {
        val visibleMax = terminalState.visibleBars.maxOf { it.high }
        val visibleMin =
            terminalState.visibleBars.take(terminalState.visibleBarsCount).minOf { it.low }
        val pxPerPoint = size.height / (visibleMax - visibleMin)
        translate(left = terminalState.scrolledBy) {
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * terminalState.barWidth
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
                    strokeWidth = terminalState.barWidth / 2
                )
            }
        }
        bars.firstOrNull()?.let {
            drawPrices(
                min = visibleMin,
                pxPerPoint = pxPerPoint,
                lastPrice = it.close
            )
        }
    }
}

private fun DrawScope.drawPrices(
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float
) {
    drawDashedLine(
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
    )
    drawDashedLine(
        start = Offset(0f, size.height - (lastPrice - min) * pxPerPoint),
        end = Offset(size.width, size.height - (lastPrice - min) * pxPerPoint),
    )
    drawDashedLine(
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
    )
}

private fun DrawScope.drawDashedLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f,
    intervals : FloatArray = floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(intervals)
    )
}
