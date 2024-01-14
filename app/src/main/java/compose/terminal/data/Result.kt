package compose.terminal.data

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("results") val bars: List<Bar>
)
