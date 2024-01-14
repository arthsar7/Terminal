package compose.terminal.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.terminal.data.Bar
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private const val VISIBLE_BARS_MIN_VALUE = 20

@Composable
fun Terminal(
    modifier: Modifier = Modifier,
) {
    val viewModel: TerminalViewModel = viewModel()
    val screenState = viewModel.state.collectAsState()
    when (val state = screenState.value) {
        is TerminalScreenState.Content -> {
            val bars = state.bars
            val terminalState = rememberTerminalState(bars)
            Chart(
                modifier = modifier,
                ownState = terminalState,
                onTerminalStateChanged = { newTerminalState ->
                    terminalState.value = newTerminalState
                },
                timeFrame = state.frame
            )
            bars.firstOrNull()?.let {
                Prices(
                    lastPrice = it.close,
                    terminalState = terminalState
                )
            }
            TimeFrames(selectedFrame = state.frame, onTimeFrameSelected = viewModel::loadBars)
        }

        is TerminalScreenState.Error -> {
        }

        is TerminalScreenState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp),
                    color = Color.White
                )
            }
        }
    }
}

private fun DrawScope.drawTimeDelimiter(
    bar: Bar,
    nextBar: Bar?,
    timeFrame: TimeFrame,
    offsetX: Float,
    textMeasurer: TextMeasurer
) {
    val calendar = bar.calendar

    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val shouldDrawDelimiter = when (timeFrame) {
        TimeFrame.M5 -> {
            minutes == 0
        }

        TimeFrame.M15 -> {
            minutes == 0 && hours % 2 == 0
        }

        TimeFrame.M30, TimeFrame.H1 -> {
            val nextBarDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            day != nextBarDay
        }
    }
    if (!shouldDrawDelimiter) return

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx())
        )
    )

    val nameOfMonth = calendar.getDisplayName(
        Calendar.MONTH,
        Calendar.SHORT,
        Locale.getDefault()
    )
    val text = when (timeFrame) {
        TimeFrame.M5, TimeFrame.M15 -> {
            String.format("%02d:00", hours)
        }

        TimeFrame.M30, TimeFrame.H1 -> {
            String.format("%s %s", day, nameOfMonth)
        }
    }
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(offsetX - textLayoutResult.size.width / 2, size.height)
    )
}

@Composable
private fun TimeFrames(
    modifier: Modifier = Modifier,
    selectedFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit,
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFrame.entries.forEach {
            val frameIsSelected = selectedFrame == it
            AssistChip(
                onClick = {
                    onTimeFrameSelected(it)
                },
                label = {
                    Text(
                        text = it.name,
                        fontSize = 12.sp,
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (frameIsSelected) Color.White else Color.Black,
                    labelColor = if (frameIsSelected) Color.Black else Color.White
                )
            )
        }
    }
}

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    ownState: State<TerminalState>,
    onTerminalStateChanged: (TerminalState) -> Unit,
    timeFrame: TimeFrame
) {
    val terminalState = ownState.value
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val zoomVal = (terminalState.visibleBarsCount / zoomChange).roundToInt()
        val visibleBarsCount = zoomVal.coerceIn(VISIBLE_BARS_MIN_VALUE, terminalState.bars.size)
        val scrolledBy = (terminalState.scrolledBy + panChange.x)
            .coerceIn(
                0f,
                (terminalState.bars.size - terminalState.visibleBarsCount) * terminalState.barWidth
            )
        onTerminalStateChanged(
            terminalState.copy(
                scrolledBy = scrolledBy,
                visibleBarsCount = visibleBarsCount
            )
        )
    }
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
            .padding(32.dp)
            .transformable(transformableState)
            .onSizeChanged {
                onTerminalStateChanged(
                    terminalState.copy(
                        terminalWidth = it.width.toFloat(),
                        terminalHeight = it.height.toFloat()
                    )
                )
            }
    ) {
        translate(left = terminalState.scrolledBy) {
            val barWidth = terminalState.barWidth
            val visibleMin = terminalState.visibleMin
            val pxPerPoint = terminalState.pxPerPoint
            terminalState.bars.forEachIndexed { index, bar ->
                val offsetX = size.width - index * barWidth
                drawTimeDelimiter(
                    bar = bar,
                    nextBar = terminalState.bars.getOrNull(index + 1),
                    timeFrame = timeFrame,
                    offsetX = offsetX,
                    textMeasurer = textMeasurer
                )
                drawLine(
                    color = Color.White,
                    start = Offset(
                        offsetX,
                        size.height - ((bar.low - visibleMin) * pxPerPoint)
                    ),
                    end = Offset(
                        offsetX,
                        size.height - ((bar.high - visibleMin) * pxPerPoint)
                    ),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open > bar.close) Color.Red else Color.Green,
                    start = Offset(
                        offsetX,
                        size.height - ((bar.low - visibleMin) * pxPerPoint)
                    ),
                    end = Offset(
                        offsetX,
                        size.height - ((bar.high - visibleMin) * pxPerPoint)
                    ),
                    strokeWidth = barWidth / 2
                )
            }
        }
    }
}

@Composable
fun Prices(
    modifier: Modifier = Modifier,
    terminalState: State<TerminalState>,
    lastPrice: Float
) {

    val max = terminalState.value.visibleMax
    val min = terminalState.value.visibleMin
    val pxPerPoint = terminalState.value.pxPerPoint
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(32.dp)
    ) {
        drawPrices(
            max = max,
            min = min,
            pxPerPoint = pxPerPoint,
            lastPrice = lastPrice,
            textMeasurer = textMeasurer
        )
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
    intervals: FloatArray = floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(intervals)
    )
}
