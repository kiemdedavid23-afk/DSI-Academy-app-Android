package app.dsiacademy.academy.feature.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.ContinueEntry
import app.dsiacademy.academy.core.session.LocalContentStores
import app.dsiacademy.academy.core.session.MediaProgress
import app.dsiacademy.academy.core.session.MediaProgressStore
import app.dsiacademy.academy.data.content.DeliveryRepository
import app.dsiacademy.academy.domain.content.DeliveryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlayerMode {
    data class Video(val url: String) : PlayerMode
    data class Audio(val url: String) : PlayerMode
    data class Pdf(val url: String) : PlayerMode
    data class TextContent(val url: String) : PlayerMode
    data class Image(val url: String) : PlayerMode
    data class External(val url: String, val format: String?) : PlayerMode
    data class Download(val url: String, val format: String?) : PlayerMode
}

data class ContentPlayerUiState(
    val contentId: String = "",
    val title: String = "",
    val contentType: String = "",
    val loading: Boolean = true,
    val error: String? = null,
    val mode: PlayerMode? = null,
    val resumeMs: Long = 0,
    val resumePage: Int = 0,
)

@HiltViewModel
class ContentPlayerViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val localContent: LocalContentStores,
    private val progressStore: MediaProgressStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contentId: String = savedStateHandle["contentId"] ?: ""
    private val title: String = savedStateHandle["title"] ?: "Contenu"
    private val contentType: String = savedStateHandle["type"] ?: "book"
    private val houseLabel: String = savedStateHandle["house"] ?: "Academy"
    private val isDownload: Boolean = contentType == "download"

    private val _state = MutableStateFlow(
        ContentPlayerUiState(contentId = contentId, title = title, contentType = contentType),
    )
    val state: StateFlow<ContentPlayerUiState> = _state.asStateFlow()

    init { open() }

    fun open() {
        if (contentId.isBlank()) {
            _state.update { it.copy(loading = false, error = "Contenu introuvable.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val progress = progressStore.get(contentId)
            val intent = if (isDownload) "download" else null
            when (val r = deliveryRepository.deliver(contentId, intent)) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Erreur réseau.")
                }
                is DsiResult.Success -> when (val d = r.data) {
                    is DeliveryResult.Failure -> _state.update {
                        it.copy(loading = false, error = d.message)
                    }
                    is DeliveryResult.VideoToken -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                mode = PlayerMode.Video(d.playbackUrl),
                                resumeMs = progress.positionMs,
                            )
                        }
                        saveContinue(progress.percent)
                    }
                    is DeliveryResult.BookToken -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                mode = PlayerMode.Pdf(d.readUrl),
                                resumePage = progress.page,
                            )
                        }
                        saveContinue(progress.percent)
                    }
                    is DeliveryResult.ExternalUrl -> {
                        val mode = if (isDownload) {
                            PlayerMode.Download(d.url, d.format)
                        } else {
                            resolveExternal(d.url, d.format)
                        }
                        _state.update {
                            it.copy(
                                loading = false,
                                mode = mode,
                                resumeMs = progress.positionMs,
                                resumePage = progress.page,
                            )
                        }
                        if (!isDownload) saveContinue(progress.percent)
                    }
                }
            }
        }
    }

    fun saveMediaProgress(positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            val pct = if (durationMs > 0) ((positionMs * 100) / durationMs).toInt().coerceIn(0, 100) else 0
            progressStore.save(contentId, MediaProgress(positionMs = positionMs, percent = pct))
            saveContinue(pct)
        }
    }

    fun savePdfPage(page: Int, pageCount: Int) {
        viewModelScope.launch {
            val pct = if (pageCount > 0) (((page + 1) * 100) / pageCount).coerceIn(0, 100) else 0
            progressStore.save(contentId, MediaProgress(page = page, percent = pct))
            saveContinue(pct)
        }
    }

    private fun resolveExternal(url: String, format: String?): PlayerMode {
        val f = format?.lowercase().orEmpty()
        val u = url.lowercase()
        return when {
            f in listOf("mp3", "aac", "m4a", "ogg", "wav") || contentType == "audio" ->
                PlayerMode.Audio(url)
            f in listOf("mp4", "webm", "mkv") || contentType == "video" ->
                if (u.contains("youtube") || u.contains("youtu.be")) PlayerMode.External(url, format)
                else PlayerMode.Video(url)
            f == "pdf" -> PlayerMode.Pdf(url)
            f in listOf("txt", "text", "md") -> PlayerMode.TextContent(url)
            f in listOf("png", "jpg", "jpeg", "webp", "gif") -> PlayerMode.Image(url)
            else -> PlayerMode.External(url, format)
        }
    }

    private suspend fun saveContinue(percent: Int) {
        localContent.setContinue(
            ContinueEntry(
                title = title,
                house = houseLabel,
                path = "content/$contentId",
                icon = when (contentType) {
                    "video" -> "🎬"
                    "audio" -> "🎧"
                    "training" -> "🎓"
                    else -> "📚"
                },
                progress = percent.takeIf { it > 0 },
            ),
        )
    }
}
