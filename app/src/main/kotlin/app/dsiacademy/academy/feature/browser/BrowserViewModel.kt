package app.dsiacademy.academy.feature.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.LinkDecision
import app.dsiacademy.academy.core.common.LinkPolicy
import app.dsiacademy.academy.core.config.SiteLinks
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BrowserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val linkPolicy: LinkPolicy,
) : ViewModel() {

    private val linkId: String? = savedStateHandle["linkId"]
    private val ref: String? = savedStateHandle["ref"]

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _events = Channel<BrowserEvent>(Channel.BUFFERED)
    val events: Flow<BrowserEvent> = _events.receiveAsFlow()

    private var paymentEventEmitted: Boolean = false

    private fun buildInitialState(): BrowserUiState {
        if (!linkPolicy.isConfigured) {
            return BrowserUiState(phase = BrowserPhase.NotConfigured)
        }
        val siteLink = linkId?.let { SiteLinks.find(it) }
        if (siteLink == null) {
            return BrowserUiState(phase = BrowserPhase.UnknownLink)
        }
        val query = buildMap {
            val trimmedRef = ref?.trim().orEmpty()
            if (trimmedRef.isNotEmpty()) {
                put("ref", trimmedRef)
            }
        }
        val startUrl = linkPolicy.startUrl(siteLink.path, query)
        val (host, isSecure) = parseHostAndSecure(startUrl)
        return BrowserUiState(
            phase = BrowserPhase.Ready,
            startUrl = startUrl,
            title = "",
            host = host,
            isSecure = isSecure,
            loading = false,
            progress = 0f,
            hasError = false,
            canGoBack = false,
        )
    }

    fun onPageStarted(url: String) {
        val (host, isSecure) = parseHostAndSecure(url)
        _uiState.update {
            it.copy(
                loading = true,
                hasError = false,
                progress = 0f,
                host = host,
                isSecure = isSecure,
            )
        }
    }

    fun onProgressChanged(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _uiState.update { it.copy(progress = clamped / 100f) }
    }

    fun onTitleReceived(title: String) {
        _uiState.update { state ->
            val displayTitle = if (title.isBlank()) state.host else title
            state.copy(title = displayTitle)
        }
    }

    fun onPageFinished(url: String, canGoBack: Boolean) {
        val (host, isSecure) = parseHostAndSecure(url)
        _uiState.update {
            it.copy(
                loading = false,
                progress = 1f,
                canGoBack = canGoBack,
                host = host,
                isSecure = isSecure,
            )
        }
    }

    fun onMainFrameError() {
        _uiState.update {
            it.copy(
                hasError = true,
                loading = false,
            )
        }
    }

    fun onRetry() {
        _uiState.update {
            it.copy(
                hasError = false,
                loading = true,
                progress = 0f,
            )
        }
    }

    fun onNavigationRequest(url: String): LinkDecision {
        val decision = linkPolicy.decide(url)
        if (decision is LinkDecision.PaymentReturn && !paymentEventEmitted) {
            paymentEventEmitted = true
            viewModelScope.launch {
                _events.send(
                    BrowserEvent.PaymentReturned(
                        status = decision.status,
                        trackingToken = decision.trackingToken,
                    )
                )
            }
        }
        return decision
    }

    fun onCloseRequested() {
        viewModelScope.launch {
            _events.send(BrowserEvent.Close)
        }
    }

    private fun parseHostAndSecure(url: String?): Pair<String, Boolean> {
        if (url.isNullOrBlank()) return "" to false
        return try {
            val uri = URI(url.trim())
            val host = (uri.host ?: "").lowercase().removePrefix("www.")
            val isSecure = (uri.scheme ?: "").equals("https", ignoreCase = true)
            host to isSecure
        } catch (_: Exception) {
            "" to false
        }
    }
}
