package app.dsiacademy.academy.data.community

import app.dsiacademy.academy.core.network.Auth
import retrofit2.http.GET

interface CommunityApi {
    /** GET /api/community/me — AccessProof. Style B. */
    @Auth
    @GET("api/community/me")
    suspend fun getMe(): CommunityEnvelope
}
