package app.dsiacademy.academy.core.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Point d'entrée unique pour lire / écrire / purger la session membre.
 *
 * Contrat identique au SessionStore du site (sessionStore.js) :
 * - Gère UNIQUEMENT une vraie session authentifiée (status: 'connecte').
 * - Ne gère JAMAIS le paymentTrackingToken (brouillon d'inscription séparé).
 * - accessProof est opaque : jamais décodé, jamais journalisé.
 * - Persistant si « se souvenir de moi », sinon mémoire uniquement.
 *
 * Chiffrement : EncryptedSharedPreferences (Keystore) pour le mode persistant.
 * Jamais de jeton / accessProof dans les logs.
 */
interface SessionStore {

    /** Session courante, ou null si aucun token. */
    fun getSession(): MemberSession?

    /** Simple présence de token, sans jugement sur sa validité. */
    fun hasSession(): Boolean

    /**
     * Enregistre une session après connexion réussie.
     * @param souvenir true = persistant (chiffré sur disque), false = mémoire seule
     */
    fun setSession(session: MemberSession, souvenir: Boolean)

    /** Purge complète (token + état + accessProof) des deux espaces. */
    fun clearSession()

    /** Flux de la session courante (null = déconnecté). */
    val sessionFlow: Flow<MemberSession?>

    /** Événements globaux (expiration, etc.). */
    val events: SharedFlow<SessionEvent>

    /** Émet un événement d'expiration (appelé par SessionExpiredInterceptor). */
    fun notifySessionExpired()
}
