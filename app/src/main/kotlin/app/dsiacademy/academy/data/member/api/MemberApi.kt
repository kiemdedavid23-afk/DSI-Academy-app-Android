package app.dsiacademy.academy.data.member.api

import app.dsiacademy.academy.core.network.Auth
import app.dsiacademy.academy.data.member.dto.AbonnementDto
import app.dsiacademy.academy.data.member.dto.DemandeRetraitRequestDto
import app.dsiacademy.academy.data.member.dto.DemandeRetraitResponseDto
import app.dsiacademy.academy.data.member.dto.MoyensRetraitDto
import app.dsiacademy.academy.data.member.dto.PaiementsResponseDto
import app.dsiacademy.academy.data.member.dto.ParrainageDto
import app.dsiacademy.academy.data.member.dto.ProfilDto
import app.dsiacademy.academy.data.member.dto.RenouvelerRequestDto
import app.dsiacademy.academy.data.member.dto.RenouvelerResponseDto
import app.dsiacademy.academy.data.member.dto.RetraitHistoriqueDto
import app.dsiacademy.academy.data.member.dto.UtiliserCodeRequestDto
import app.dsiacademy.academy.data.member.dto.UtiliserCodeResponseDto
import retrofit2.http.Path
import app.dsiacademy.academy.data.member.dto.RetraitsResponseDto
import app.dsiacademy.academy.data.member.dto.SoldeDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MemberApi {

    @Auth
    @GET("api/membre/profil")
    suspend fun getProfil(): ProfilDto

    @Auth
    @GET("api/membre/solde")
    suspend fun getSolde(): SoldeDto

    @Auth
    @GET("api/membre/abonnement")
    suspend fun getAbonnement(): AbonnementDto

    @Auth
    @GET("api/membre/parrainage")
    suspend fun getParrainage(): ParrainageDto

    @Auth
    @GET("api/membre/retraits")
    suspend fun getRetraits(): RetraitsResponseDto

    @GET("api/retrait/moyens-disponibles")
    suspend fun getMoyensRetrait(@Query("pays") pays: String?): MoyensRetraitDto

    @Auth
    @POST("api/retrait/demander")
    suspend fun demanderRetrait(@Body body: DemandeRetraitRequestDto): DemandeRetraitResponseDto

    @Auth
    @GET("api/membre/paiements")
    suspend fun getPaiements(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): PaiementsResponseDto

    @Auth
    @POST("api/abonnement/renouveler")
    suspend fun renouvelerAbonnement(@Body body: RenouvelerRequestDto): RenouvelerResponseDto


    @Auth
    @POST("api/dsi/utiliser-code")
    suspend fun utiliserCode(@Body body: UtiliserCodeRequestDto): UtiliserCodeResponseDto

    @Auth
    @GET("api/membre/retraits/{idRetrait}/historique")
    suspend fun getRetraitHistorique(@Path("idRetrait") idRetrait: String): RetraitHistoriqueDto
}
