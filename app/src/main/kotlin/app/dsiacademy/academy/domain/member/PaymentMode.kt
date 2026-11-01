package app.dsiacademy.academy.domain.member

/**
 * Équivalent de `libelleModePaiement` du site (comparaison sensible à la casse).
 */
enum class PaymentModeKind {
    None,
    Fcfa,
    Dsi,
    Founder,
    Other,
}

data class PaymentMode(
    val kind: PaymentModeKind,
    val raw: String?,
)

fun paymentModeOf(mode: String?): PaymentMode {
    if (mode.isNullOrEmpty()) {
        return PaymentMode(PaymentModeKind.None, mode)
    }
    return when (mode) {
        "fcfa" -> PaymentMode(PaymentModeKind.Fcfa, mode)
        "dsi" -> PaymentMode(PaymentModeKind.Dsi, mode)
        "fondateur" -> PaymentMode(PaymentModeKind.Founder, mode)
        else -> PaymentMode(PaymentModeKind.Other, mode)
    }
}
