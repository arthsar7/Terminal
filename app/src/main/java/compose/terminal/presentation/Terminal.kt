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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val textMeasurer = rememberTextMeasurer()
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
                max = visibleMax,
                min = visibleMin,
                pxPerPoint = pxPerPoint,
                lastPrice = it.close,
                textMeasurer = textMeasurer
            )
        }
    }
}

private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasurer: TextMeasurer
) {
    drawDashedLine(
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
    )
    drawPrice(
        textMeasurer = textMeasurer,
        price = max
    )
    drawDashedLine(
        start = Offset(0f, size.height - (lastPrice - min) * pxPerPoint),
        end = Offset(size.width, size.height - (lastPrice - min) * pxPerPoint),
    )
    drawPrice(
        textMeasurer = textMeasurer,
        price = lastPrice,
        topLeftY = size.height - (lastPrice - min) * pxPerPoint
    )
    drawDashedLine(
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
    )
    drawPrice(
        textMeasurer = textMeasurer,
        price = min,
        topLeftY = size.height
    )
}

private fun DrawScope.drawPrice(
    textMeasurer: TextMeasurer,
    price: Float,
    topLeftY: Float = 0f
) {
    val textLayoutInput = textMeasurer.measure(
        text = price.toString(),
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp,
        )
    )
    drawText(
        textLayoutInput,
        topLeft = Offset(
            x = size.width - textLayoutInput.size.width,
            y = textLayoutInput.size.height / 2f + topLeftY
        )
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
