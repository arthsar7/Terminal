package compose.terminal.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import compose.terminal.data.Bar
import kotlin.math.roundToInt

private const val START_VISIBLE_BARS_COUNT = 100

data class TerminalState(
    val bars: List<Bar>,
    val visibleBarsCount: Int = START_VISIBLE_BARS_COUNT,
    val terminalWidth: Float = 0f,
    val terminalHeight: Float = 0f,
    val scrolledBy: Float = 0f
) {
    val barWidth get() = terminalWidth / visibleBarsCount
    private val visibleBars: List<Bar>
        get() {
            return try {
                val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
                val endIndex = (startIndex + visibleBarsCount).coerceAtMost(bars.size)
                bars.subList(startIndex, endIndex)
            }
            catch (e: Exception) {
                bars
            }
        }
    val visibleMax get() = visibleBars.maxOf { it.high }
    val visibleMin get() = visibleBars.minOf { it.low }
    val pxPerPoint get() =  terminalHeight / (visibleMax - visibleMin)

    companion object {
        val Saver = listSaver(
            save = {
                return@listSaver listOf(
                    it.value.bars,
                    it.value.visibleBarsCount,
                    it.value.terminalWidth,
                    it.value.terminalHeight,
                    it.value.scrolledBy
                )
            },
            restore = {
                return@listSaver mutableStateOf(
                    TerminalState(
                        bars = it[0] as List<Bar>,
                        visibleBarsCount = it[1] as Int,
                        terminalWidth = it[2] as Float,
                        terminalHeight = it[3] as Float,
                        scrolledBy = it[3] as Float
                    )
                )
            }
        )
    }
}

@Composable
fun rememberTerminalState(bars: List<Bar>): MutableState<TerminalState> {
    return rememberSaveable(saver = TerminalState.Saver) {
        mutableStateOf(TerminalState(bars))
    }
}
