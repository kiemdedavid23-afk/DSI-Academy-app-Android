package app.dsiacademy.academy.core.network

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import retrofit2.HttpException
import java.io.IOException

/**
 * Enveloppe les appels réseau : traduit exceptions → AppError.
 * Une seule table code → AppError (ARCHITECTURE.md §5).
 */
suspend fun <T> safeCall(block: suspend () -> T): DsiResult<T> {
    return try {
        DsiResult.Success(block())
    } catch (e: HttpException) {
        val code = e.code()
        val body = try {
            e.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
        val message = body?.let { extractErreur(it) }
        when (code) {
            401 -> DsiResult.Error(AppError.SessionExpired(message))
            else -> DsiResult.Error(
                AppError.Http(
                    code = "HTTP_$code",
                    message = message,
                    httpStatus = code,
                )
            )
        }
    } catch (e: IOException) {
        DsiResult.Error(AppError.Network(e.message))
    } catch (e: Exception) {
        DsiResult.Error(AppError.Unknown(message = e.message))
    }
}

private fun extractErreur(body: String): String? {
    return try {
        val json = org.json.JSONObject(body)
        sequenceOf("erreur", "error", "message", "code")
            .mapNotNull { key ->
                if (json.has(key) && !json.isNull(key)) {
                    json.optString(key).takeIf { it.isNotBlank() }
                } else null
            }
            .firstOrNull()
    } catch (_: Exception) {
        null
    }
}
