package app.dsiacademy.academy.domain.content

/**
 * Résultat normalisé de GET /content/:id/deliver — aligné delivery.service.js
 */
sealed class DeliveryResult {
    data class VideoToken(val playbackUrl: String, val expiresAt: String?) : DeliveryResult()
    data class BookToken(val readUrl: String, val expiresAt: String?) : DeliveryResult()
    data class ExternalUrl(val url: String, val format: String?) : DeliveryResult()
    data class Failure(val code: String, val message: String) : DeliveryResult()
}

object DeliveryMessages {
    private val map = mapOf(
        "ACCESS_PROOF_MISSING" to "Vous devez être connecté pour accéder à ce contenu.",
        "ACCESS_PROOF_EXPIRED" to "Votre session a expiré. Reconnectez-vous.",
        "ACCESS_PROOF_INVALID" to "Votre session est invalide. Reconnectez-vous.",
        "ACCESS_PROOF_MALFORMED" to "Votre session est invalide. Reconnectez-vous.",
        "ACCESS_PROOF_INVALID_SIGNATURE" to "Votre session est invalide. Reconnectez-vous.",
        "ACCESS_SUBSCRIPTION_INACTIVE" to "Votre abonnement doit être actif pour accéder à ce contenu.",
        "SUBSCRIPTION_INACTIVE" to "Votre abonnement doit être actif pour accéder à ce contenu.",
        "CONTENT_NOT_FOUND" to "Ce contenu est introuvable.",
        "CONTENT_NOT_AVAILABLE" to "Ce contenu n'est plus disponible.",
        "NO_COMPATIBLE_ASSET" to "Aucun fichier compatible pour ce contenu.",
        "NO_DELIVERABLE_ASSET" to "Aucun fichier livrable pour ce contenu.",
        "NO_DOWNLOADABLE_ASSET" to "Téléchargement non disponible pour ce contenu.",
    )
    fun message(code: String?): String =
        map[code] ?: "Impossible de récupérer ce contenu pour le moment."
}
