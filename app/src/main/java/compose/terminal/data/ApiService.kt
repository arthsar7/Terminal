package compose.terminal.data

import retrofit2.http.GET

interface ApiService {
    @GET("bars")
    suspend fun loadBars(): Result
}