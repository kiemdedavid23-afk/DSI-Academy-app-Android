package app.dsiacademy.academy.domain.member

import app.dsiacademy.academy.core.common.DsiResult

/**
 * Interface domain — aucune dépendance Android / réseau.
 * Implémentée dans data/member.
 */
interface AuthRepository {

    /**
     * POST /api/auth/connexion
     * Succès métier = LoginSuccess | PendingPayment
     * Erreur métier (mauvais identifiants, etc.) = DsiResult.Error avec message serveur
     */
    suspend fun connecter(identifiant: String, motDePasse: String): DsiResult<Any>

    /**
     * POST /api/inscription
     * Succès = InscriptionCreated (jamais de session)
     */
    suspend fun creerInscription(form: InscriptionForm): DsiResult<InscriptionCreated>

    /**
     * GET /api/paiement/statut
     * Header X-Payment-Tracking-Token (pas Bearer)
     */
    suspend fun verifierStatutPaiement(paymentTrackingToken: String): DsiResult<String>
}
