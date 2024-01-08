package compose.terminal.presentation

import compose.terminal.data.Bar

sealed class TerminalScreenState {
    data object Loading : TerminalScreenState()
    data class Error(val throwable: Throwable) : TerminalScreenState()
    data class Content(
        val bars: List<Bar>
    ) : TerminalScreenState()
}