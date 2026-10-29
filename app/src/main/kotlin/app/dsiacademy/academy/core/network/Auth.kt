package app.dsiacademy.academy.core.network

/**
 * Annotation placée sur une fonction Retrofit pour indiquer que la route
 * exige une authentification.
 *
 * - Worker Paiement → BearerInterceptor ajoute `Authorization: Bearer <token>`
 * - Autres Workers  → AccessProofInterceptor ajoute `X-Access-Proof: <accessProof>`
 *
 * Voir CONTRATS-API.md §1.2.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auth
