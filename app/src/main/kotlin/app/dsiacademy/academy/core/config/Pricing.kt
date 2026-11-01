package app.dsiacademy.academy.core.config

/**
 * Montants affichés dans l'interface.
 *
 * Ces valeurs ne servent qu'à l'AFFICHAGE (textes de confirmation). Le Worker
 * Paiement reste seul juge du montant réellement débité et refuse l'opération
 * si le solde est insuffisant : si le tarif change côté Worker, il suffit de
 * modifier cette constante, à un seul endroit.
 */
object Pricing {
    const val RENEWAL_PRICE_DSI: Long = 2500
}
