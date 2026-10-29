package app.dsiacademy.academy.data.content

import android.content.Context
import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.network.WorkerUrls
import app.dsiacademy.academy.domain.content.DeliveryMessages
import app.dsiacademy.academy.domain.content.DeliveryResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface DeliveryRepository {
    suspend fun deliver(contentId: String, intent: String? = null): DsiResult<DeliveryResult>
}

@Singleton
class DeliveryRepositoryImpl @Inject constructor(
    private val api: DeliveryApi,
    private val json: Json,
    @ApplicationContext private val context: Context,
) : DeliveryRepository {

    override suspend fun deliver(contentId: String, intent: String?): DsiResult<DeliveryResult> {
        return try {
            val response = api.deliver(contentId, intent)
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                val errText = try { body?.string() } catch (_: Exception) { null }
                val errCode = parseErrorCode(errText) ?: "DELIVERY_ERROR"
                return DsiResult.Success(
                    DeliveryResult.Failure(errCode, DeliveryMessages.message(errCode)),
                )
            }

            val contentType = response.headers()["Content-Type"].orEmpty()
            if (contentType.contains("application/json", ignoreCase = true)) {
                parseJsonDelivery(body.string())
            } else {
                // Flux binaire → fichier éphémère session (pas d'URL signée persistée)
                val ext = when {
                    contentType.contains("pdf") -> "pdf"
                    contentType.contains("mpeg") || contentType.contains("mp3") -> "mp3"
                    contentType.contains("mp4") -> "mp4"
                    contentType.contains("text") -> "txt"
                    else -> "bin"
                }
                val file = File(context.cacheDir, "dsi_stream_${contentId.hashCode()}.$ext")
                body.byteStream().use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                val fileUrl = file.toURI().toString()
                val format = ext
                DsiResult.Success(
                    when (ext) {
                        "pdf" -> DeliveryResult.BookToken(readUrl = fileUrl, expiresAt = null)
                        "mp3" -> DeliveryResult.ExternalUrl(url = fileUrl, format = "mp3")
                        "mp4" -> DeliveryResult.VideoToken(playbackUrl = fileUrl, expiresAt = null)
                        "txt" -> DeliveryResult.ExternalUrl(url = fileUrl, format = "txt")
                        else -> DeliveryResult.ExternalUrl(url = fileUrl, format = format)
                    },
                )
            }
        } catch (e: Exception) {
            DsiResult.Error(AppError.Network(e.message))
        }
    }

    private fun parseJsonDelivery(text: String): DsiResult<DeliveryResult> {
        return try {
            val root = json.parseToJsonElement(text).jsonObject
            val type = root["type"]?.jsonPrimitive?.contentOrNull
            val error = root["error"]?.jsonPrimitive?.contentOrNull
            if (error != null) {
                return DsiResult.Success(DeliveryResult.Failure(error, DeliveryMessages.message(error)))
            }
            when (type) {
                "video_playback_token" -> {
                    val path = root["playback_url"]?.jsonPrimitive?.contentOrNull
                    if (path.isNullOrBlank()) {
                        DsiResult.Success(DeliveryResult.Failure("DELIVERY_ERROR", DeliveryMessages.message(null)))
                    } else {
                        DsiResult.Success(
                            DeliveryResult.VideoToken(
                                playbackUrl = absoluteUrl(path),
                                expiresAt = root["expires_at"]?.jsonPrimitive?.contentOrNull,
                            ),
                        )
                    }
                }
                "book_playback_token" -> {
                    val path = root["read_url"]?.jsonPrimitive?.contentOrNull
                    if (path.isNullOrBlank()) {
                        DsiResult.Success(DeliveryResult.Failure("DELIVERY_ERROR", DeliveryMessages.message(null)))
                    } else {
                        DsiResult.Success(
                            DeliveryResult.BookToken(
                                readUrl = absoluteUrl(path),
                                expiresAt = root["expires_at"]?.jsonPrimitive?.contentOrNull,
                            ),
                        )
                    }
                }
                else -> {
                    val delivery = root["delivery"]?.jsonObject
                    val url = delivery?.get("url")?.jsonPrimitive?.contentOrNull
                        ?: root["url"]?.jsonPrimitive?.contentOrNull
                    val format = root["format"]?.jsonPrimitive?.contentOrNull
                    if (url.isNullOrBlank()) {
                        DsiResult.Success(
                            DeliveryResult.Failure(
                                "NO_DELIVERABLE_ASSET",
                                DeliveryMessages.message("NO_DELIVERABLE_ASSET"),
                            ),
                        )
                    } else {
                        DsiResult.Success(DeliveryResult.ExternalUrl(url = absoluteUrl(url), format = format))
                    }
                }
            }
        } catch (_: Exception) {
            DsiResult.Success(DeliveryResult.Failure("INVALID_RESPONSE", DeliveryMessages.message(null)))
        }
    }

    private fun absoluteUrl(path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")) return path
        val base = WorkerUrls.CONTENT.trimEnd('/')
        return if (path.startsWith("/")) "$base$path" else "$base/$path"
    }

    private fun parseErrorCode(text: String?): String? {
        if (text.isNullOrBlank()) return null
        return try {
            json.parseToJsonElement(text).jsonObject["error"]?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }
}
