package app.dsiacademy.academy.core.session

/**
 * Session membre authentifiée.
 * Miroir exact des champs renvoyés par POST /api/auth/connexion (status: 'connecte')
 * et stockés par le SessionStore du site (sessionStore.js).
 *
 * - [token] : Authorization: Bearer — Worker Paiement uniquement
 * - [accessProof] : X-Access-Proof — Contenu / Boutique / Marketplace / Communauté
 *   Chaîne opaque, jamais décodée ni journalisée. Peut être null.
 * - Les autres champs sont de l'état membre transmis en clair par le Worker.
 */
data class MemberSession(
    val token: String,
    val accountStatus: String? = null,
    val subscriptionStatus: String? = null,
    val subscriptionActive: Boolean = false,
    val dateFin: String? = null,
    val accessProof: String? = null,
)
