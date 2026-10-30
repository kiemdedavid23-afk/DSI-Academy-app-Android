package app.dsiacademy.academy.feature.member.pendingpayment

enum class PendingPaymentPhase {
    Loading,
    NoPending,
    Ready,
}

enum class PendingPaymentMessage {
    None,
    Waiting,
    Refused,
    Unavailable,
}

data class PendingPaymentUiState(
    val phase: PendingPaymentPhase = PendingPaymentPhase.Loading,
    val paymentUrl: String? = null,
    val checking: Boolean = false,
    val message: PendingPaymentMessage = PendingPaymentMessage.None,
)

sealed interface PendingPaymentEvent {
    data class OpenPaymentUrl(val url: String) : PendingPaymentEvent
    data object Paid : PendingPaymentEvent
}
