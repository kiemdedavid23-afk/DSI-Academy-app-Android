package app.dsiacademy.academy.feature.member.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repo: MemberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state.asStateFlow()

    init {
        charger()
    }

    fun charger() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    sessionExpired = false,
                    loadFailed = false,
                    errorMessage = null,
                )
            }
            when (val r = repo.getAbonnement()) {
                is DsiResult.Error -> {
                    val expired = r.error is AppError.SessionExpired
                    _state.update {
                        it.copy(
                            loading = false,
                            sessionExpired = expired,
                            loadFailed = !expired,
                            errorMessage = if (expired) null else r.error.message,
                        )
                    }
                }
                is DsiResult.Success -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            abo = r.data,
                            sessionExpired = false,
                            loadFailed = false,
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    fun openPanel() {
        _state.update { it.copy(panel = RenewPanel.ChooseMode, feedback = null, paymentLink = null) }
    }

    fun closePanel() {
        _state.update {
            it.copy(
                panel = RenewPanel.Closed,
                feedback = null,
                paymentLink = null,
            )
        }
    }

    fun chooseDsi() {
        _state.update { it.copy(panel = RenewPanel.ConfirmDsi, feedback = null) }
    }

    fun backToChoice() {
        _state.update { it.copy(panel = RenewPanel.ChooseMode, feedback = null) }
    }

    fun renewFcfa() {
        viewModelScope.launch {
            _state.update {
                it.copy(renewing = true, feedback = null, paymentLink = null)
            }
            when (val r = repo.renouvelerAbonnement("fcfa")) {
                is DsiResult.Error -> _state.update {
                    it.copy(
                        renewing = false,
                        feedback = RenewFeedback.Error(r.error.message, FeedbackKind.Server),
                    )
                }
                is DsiResult.Success -> {
                    val status = r.data.status
                    val url = r.data.paymentUrl
                    if (status == "pending_payment" && !url.isNullOrBlank() && url.startsWith("https://")) {
                        _state.update {
                            it.copy(renewing = false, paymentLink = url)
                        }
                    } else {
                        _state.update {
                            it.copy(
                                renewing = false,
                                feedback = RenewFeedback.Error(null, FeedbackKind.Unexpected),
                            )
                        }
                    }
                }
            }
        }
    }

    fun confirmDsi() {
        viewModelScope.launch {
            _state.update { it.copy(renewing = true, feedback = null) }
            when (val r = repo.renouvelerAbonnement("dsi")) {
                is DsiResult.Error -> _state.update {
                    it.copy(
                        renewing = false,
                        feedback = RenewFeedback.Error(r.error.message, FeedbackKind.Server),
                    )
                }
                is DsiResult.Success -> {
                    when (r.data.status) {
                        "active" -> {
                            _state.update {
                                it.copy(
                                    renewing = false,
                                    panel = RenewPanel.Closed,
                                    feedback = RenewFeedback.Success(r.data.message),
                                    paymentLink = null,
                                )
                            }
                            charger()
                        }
                        "refuse" -> _state.update {
                            it.copy(
                                renewing = false,
                                feedback = RenewFeedback.Error(
                                    r.data.message,
                                    FeedbackKind.Refused,
                                ),
                            )
                        }
                        else -> _state.update {
                            it.copy(
                                renewing = false,
                                feedback = RenewFeedback.Error(null, FeedbackKind.Unexpected),
                            )
                        }
                    }
                }
            }
        }
    }
}
