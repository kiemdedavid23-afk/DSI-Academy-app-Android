package app.dsiacademy.academy.domain.member

/**
 * Destinations d'utilisation des DSI (ordre d'affichage site).
 * La navigation n'est pas décidée ici.
 */
enum class DsiUsageChoice(val id: String) {
    Subscription("subscription"),
    Shop("shop"),
    Marketplace("marketplace"),
}
