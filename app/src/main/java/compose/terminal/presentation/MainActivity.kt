package compose.terminal.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TerminalViewModel = viewModel()
            val screenState = viewModel.state.collectAsState()
            when (val state = screenState.value) {
                is TerminalScreenState.Content -> {
                    Terminal(bars = state.bars)
                    Log.d("TAG", "onCreate: ${state.bars}")
                }
                is TerminalScreenState.Error -> {
                    Log.d("TAG", "onCreate: ${state.throwable}")
                }
                TerminalScreenState.Loading -> {

                }
            }

        }
    }
}

