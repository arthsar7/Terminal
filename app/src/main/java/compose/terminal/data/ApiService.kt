package compose.terminal.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("aggs/ticker/AAPL/range/{timeFrame}/2022-01-09/2023-01-09?adjusted=true&sort=desc&limit=50000&apiKey=GS9QXH1xhzSTm0nsazTQbyf0WqJeqpR4")
    suspend fun loadBars(
        @Path("timeFrame") timeFrame: String
    ): Result
}