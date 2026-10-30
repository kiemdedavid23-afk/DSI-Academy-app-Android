package app.dsiacademy.academy.feature.browser

import app.dsiacademy.academy.core.common.PaymentReturnStatus

enum class BrowserPhase {
    NotConfigured,
    UnknownLink,
    Ready,
}

data class BrowserUiState(
    val phase: BrowserPhase = BrowserPhase.NotConfigured,
    val startUrl: String? = null,
    val title: String = "",
    val host: String = "",
    val isSecure: Boolean = false,
    val loading: Boolean = false,
    val progress: Float = 0f,
    val hasError: Boolean = false,
    val canGoBack: Boolean = false,
)

sealed interface BrowserEvent {
    data class PaymentReturned(
        val status: PaymentReturnStatus,
        val trackingToken: String?,
    ) : BrowserEvent

    data object Close : BrowserEvent
}
