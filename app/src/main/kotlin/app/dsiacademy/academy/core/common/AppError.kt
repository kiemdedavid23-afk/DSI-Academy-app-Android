package app.dsiacademy.academy.core.common

/**
 * Erreurs normalisées. Les messages affichés à l'utilisateur viennent de strings.xml
 * indexés par [code], jamais du texte brut d'une exception (ARCHITECTURE.md §0 règle 6 + CONTRATS-API).
 */
sealed class AppError {
    abstract val code: String
    abstract val message: String?
    open val httpStatus: Int? = null
    open val retryAfterSeconds: Int? = null
    open val field: String? = null

    data class Network(
        override val message: String? = null,
    ) : AppError() {
        override val code: String = "NETWORK_ERROR"
        override val httpStatus: Int = 0
    }

    data class InvalidResponse(
        override val message: String? = null,
    ) : AppError() {
        override val code: String = "INVALID_RESPONSE"
    }

    data class Http(
        override val code: String,
        override val message: String? = null,
        override val httpStatus: Int,
        override val retryAfterSeconds: Int? = null,
        override val field: String? = null,
    ) : AppError()

    data class SessionExpired(
        override val message: String? = null,
    ) : AppError() {
        override val code: String = "SESSION_EXPIRED"
        override val httpStatus: Int = 401
    }

    data class AccessProofMissing(
        override val message: String? = null,
    ) : AppError() {
        override val code: String = "ACCESS_PROOF_MISSING"
    }

    data class Unknown(
        override val message: String? = null,
        override val code: String = "UNKNOWN",
    ) : AppError()
}
