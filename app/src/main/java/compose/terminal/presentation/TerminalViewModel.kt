package compose.terminal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.terminal.data.ApiFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel : ViewModel() {
    private val apiService = ApiFactory.apiService

    private val _state = MutableStateFlow<TerminalScreenState>(TerminalScreenState.Loading)
    val state = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.value = TerminalScreenState.Error(throwable)
    }

    init {
        loadBars()
    }
    fun loadBars(frame: TimeFrame = TimeFrame.H1) {
        _state.value = TerminalScreenState.Loading
        viewModelScope.launch(exceptionHandler) {
            _state.value = TerminalScreenState.Content(apiService.loadBars(frame.value).bars, frame)
        }
    }
}