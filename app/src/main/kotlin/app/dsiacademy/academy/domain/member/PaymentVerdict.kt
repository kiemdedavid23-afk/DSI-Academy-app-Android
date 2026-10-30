package app.dsiacademy.academy.domain.member

/**
 * Verdict du statut de paiement d'inscription (GET /api/paiement/statut).
 * Kotlin pur — aligné sur CONTRATS-API.md §2.2.
 */
enum class PaymentVerdict {
    Active,
    Waiting,
    Refused,
}

/**
 * Convertit le champ `status` du serveur.
 * Valeurs connues : active, attente, refuse, introuvable.
 * Toute autre valeur → Waiting (on ne conclut pas).
 */
fun paymentVerdictFromStatus(status: String?): PaymentVerdict {
    return when (status?.trim()?.lowercase()) {
        "active" -> PaymentVerdict.Active
        "attente" -> PaymentVerdict.Waiting
        "refuse", "introuvable" -> PaymentVerdict.Refused
        else -> PaymentVerdict.Waiting
    }
}
