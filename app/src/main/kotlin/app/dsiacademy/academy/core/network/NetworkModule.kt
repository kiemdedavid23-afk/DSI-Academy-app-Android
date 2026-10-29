package app.dsiacademy.academy.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Fournit les clients OkHttp / Retrofit par Worker.
 *
 * - Paiement  → BearerInterceptor + SessionExpiredInterceptor
 * - Contenu / Boutique / Marketplace / Communauté → AccessProofInterceptor + SessionExpiredInterceptor
 *
 * Jamais de mélange des mécanismes d'auth (CONTRATS-API.md §1.2).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    private fun baseClientBuilder(
        sessionExpired: SessionExpiredInterceptor,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(sessionExpired)
        .addInterceptor(logging)

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // BODY uniquement en debug ; jamais de token / accessProof dans les logs
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("X-Access-Proof")
            redactHeader("X-Payment-Tracking-Token")
        }

    // —— Worker Paiement (Bearer) ——

    @Provides
    @Singleton
    @Named("payment")
    fun providePaymentOkHttp(
        bearer: BearerInterceptor,
        sessionExpired: SessionExpiredInterceptor,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = baseClientBuilder(sessionExpired, logging)
        .addInterceptor(bearer)
        .build()

    @Provides
    @Singleton
    @Named("payment")
    fun providePaymentRetrofit(
        @Named("payment") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(WorkerUrls.PAYMENT))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // —— Workers Access-Proof ——

    @Provides
    @Singleton
    @Named("accessProof")
    fun provideAccessProofOkHttp(
        accessProof: AccessProofInterceptor,
        sessionExpired: SessionExpiredInterceptor,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient = baseClientBuilder(sessionExpired, logging)
        .addInterceptor(accessProof)
        .build()

    @Provides
    @Singleton
    @Named("content")
    fun provideContentRetrofit(
        @Named("accessProof") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(WorkerUrls.CONTENT))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("shop")
    fun provideShopRetrofit(
        @Named("accessProof") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(WorkerUrls.SHOP))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("marketplace")
    fun provideMarketplaceRetrofit(
        @Named("accessProof") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(WorkerUrls.MARKETPLACE))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("community")
    fun provideCommunityRetrofit(
        @Named("accessProof") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(WorkerUrls.COMMUNITY))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}
