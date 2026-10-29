package app.dsiacademy.academy.domain.member

/**
 * Résultats métier de l'authentification.
 * Alignés sur les réponses réelles de POST /api/auth/connexion
 * et POST /api/inscription (CONTRATS-API + auth.service.js).
 */

/** Connexion réussie → session membre à enregistrer. */
data class LoginSuccess(
    val token: String,
    val accountStatus: String?,
    val subscriptionStatus: String?,
    val subscriptionActive: Boolean,
    val dateFin: String?,
    val accessProof: String?,
)

/** Compte en attente de paiement → rediriger vers le flux inscription/paiement. */
data class PendingPayment(
    val userId: String?,
    val paymentTrackingToken: String?,
    val paymentUrl: String?,
)

/** Demande d'inscription créée (jamais de session). */
data class InscriptionCreated(
    val userId: String?,
    val paymentTrackingToken: String?,
    val paymentUrl: String?,
)

/** Données du formulaire d'inscription (mêmes champs que le site). */
data class InscriptionForm(
    val nom: String,
    val prenom: String = "",
    val whatsapp: String,
    val email: String = "",
    val pays: String,
    val codeParrain: String = "",
    val motDePasse: String,
)
