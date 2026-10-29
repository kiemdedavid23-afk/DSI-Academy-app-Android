package app.dsiacademy.academy.core.session

/**
 * Événements globaux de session.
 * SessionExpiredInterceptor émet [Expired] → la navigation revient à l'écran de connexion
 * et vide la pile (ARCHITECTURE.md §11).
 */
sealed interface SessionEvent {
    data object Expired : SessionEvent
}
