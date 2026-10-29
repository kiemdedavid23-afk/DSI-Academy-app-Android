package app.dsiacademy.academy.core.network

import app.dsiacademy.academy.core.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intercepteur OkHttp — Worker Paiement uniquement.
 * Si la méthode Retrofit porte [@Auth], ajoute :
 *   Authorization: Bearer <token>
 *
 * Ne s'applique JAMAIS aux autres Workers (CONTRATS-API.md §1.2).
 */
@Singleton
class BearerInterceptor @Inject constructor(
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

        val token = sessionStore.getSession()?.token
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authenticated)
    }
}
