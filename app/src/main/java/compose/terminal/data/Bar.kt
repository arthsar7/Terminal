package compose.terminal.data

import android.icu.util.Calendar
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date
@Parcelize
@Immutable
data class Bar(
    @SerializedName("o") val open: Float,
    @SerializedName("c") val close: Float,
    @SerializedName("l") val low: Float,
    @SerializedName("h") val high: Float,
    @SerializedName("t") val time: Long
): Parcelable {
    val calendar: Calendar
        get() = Calendar.getInstance().apply {
            time = Date(this@Bar.time)
        }
}
