package app.dsiacademy.academy.data.member.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs tolérants (ignoreUnknownKeys, valeurs par défaut).
 * Style A — Worker Paiement : champs à la racine, pas d'enveloppe.
 */

@Serializable
data class ConnexionRequestDto(
    val identifiant: String,
    val motDePasse: String,
)

@Serializable
data class ConnexionResponseDto(
    val status: String? = null,
    val authenticated: Boolean? = null,
    val userId: String? = null,
    val accountStatus: String? = null,
    val subscriptionStatus: String? = null,
    val subscriptionActive: Boolean? = null,
    val dateFin: String? = null,
    val token: String? = null,
    val expiresIn: Int? = null,
    val accessProof: String? = null,
    val paymentTrackingToken: String? = null,
    val paymentUrl: String? = null,
    /** Erreur métier éventuelle (HTTP 200 avec { "erreur": "..." }). */
    val erreur: String? = null,
    val message: String? = null,
)

@Serializable
data class InscriptionRequestDto(
    val nom: String,
    val prenom: String = "",
    val whatsapp: String,
    val email: String = "",
    val pays: String,
    val formule: String = "acces_initial",
    val codeParrain: String = "",
    val motDePasse: String,
)

@Serializable
data class InscriptionResponseDto(
    val userId: String? = null,
    val status: String? = null,
    val paymentUrl: String? = null,
    val paymentTrackingToken: String? = null,
    val erreur: String? = null,
    val message: String? = null,
)

@Serializable
data class PaiementStatutResponseDto(
    val status: String? = null,
    val erreur: String? = null,
    val message: String? = null,
)
