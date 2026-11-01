package app.dsiacademy.academy.feature.member.subscription

import app.dsiacademy.academy.domain.member.MemberAbonnement

enum class RenewPanel {
    Closed,
    ChooseMode,
    ConfirmDsi,
}

enum class FeedbackKind {
    Server,
    Refused,
    Unexpected,
}

sealed class RenewFeedback {
    data class Error(val serverMessage: String?, val kind: FeedbackKind) : RenewFeedback()
    data class Success(val serverMessage: String?) : RenewFeedback()
}

data class SubscriptionUiState(
    val abo: MemberAbonnement? = null,
    val loading: Boolean = true,
    val sessionExpired: Boolean = false,
    val loadFailed: Boolean = false,
    val errorMessage: String? = null,
    val panel: RenewPanel = RenewPanel.Closed,
    val renewing: Boolean = false,
    val feedback: RenewFeedback? = null,
    val paymentLink: String? = null,
)
