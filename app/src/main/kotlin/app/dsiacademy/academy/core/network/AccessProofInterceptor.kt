package app.dsiacademy.academy.core.network

import app.dsiacademy.academy.core.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intercepteur OkHttp — Workers Contenu / Boutique / Marketplace / Communauté.
 * Si la méthode Retrofit porte [@Auth], ajoute :
 *   X-Access-Proof: <accessProof>
 *
 * accessProof peut être null (secret non configuré côté Worker) → on n'ajoute
 * pas l'en-tête ; le Worker renverra ACCESS_PROOF_MISSING. Ce n'est pas une
 * erreur de session (CONTRATS-API.md §1.2).
 *
 * Ne s'applique JAMAIS au Worker Paiement.
 */
@Singleton
class AccessProofInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val needsAuth = request.tag(Invocation::class.java)
            ?.method()
            ?.getAnnotation(Auth::class.java) != null

        if (!needsAuth) {
            return chain.proceed(request)
        }

        val accessProof = sessionStore.getSession()?.accessProof
        if (accessProof.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticated = request.newBuilder()
            .header("X-Access-Proof", accessProof)
            .build()
        return chain.proceed(authenticated)
    }
}
