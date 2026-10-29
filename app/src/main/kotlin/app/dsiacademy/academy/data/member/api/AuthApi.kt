package app.dsiacademy.academy.data.member.api

import app.dsiacademy.academy.data.member.dto.ConnexionRequestDto
import app.dsiacademy.academy.data.member.dto.ConnexionResponseDto
import app.dsiacademy.academy.data.member.dto.InscriptionRequestDto
import app.dsiacademy.academy.data.member.dto.InscriptionResponseDto
import app.dsiacademy.academy.data.member.dto.PaiementStatutResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Routes publiques du Worker Paiement (auth.service.js / CONTRATS-API §2).
 * Aucune annotation @Auth — pas de Bearer ni AccessProof.
 */
interface AuthApi {

    @POST("api/auth/connexion")
    suspend fun connecter(@Body body: ConnexionRequestDto): ConnexionResponseDto

    @POST("api/inscription")
    suspend fun creerInscription(@Body body: InscriptionRequestDto): InscriptionResponseDto

    @GET("api/paiement/statut")
    suspend fun verifierStatutPaiement(
        @Header("X-Payment-Tracking-Token") paymentTrackingToken: String,
    ): PaiementStatutResponseDto
}
