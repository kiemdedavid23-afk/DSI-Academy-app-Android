package app.dsiacademy.academy.data.content

import app.dsiacademy.academy.core.network.Auth
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface DeliveryApi {
    @Auth
    @Streaming
    @GET("content/{id}/deliver")
    suspend fun deliver(
        @Path("id") id: String,
        @Query("intent") intent: String? = null,
    ): Response<ResponseBody>
}
