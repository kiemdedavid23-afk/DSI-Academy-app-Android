package app.dsiacademy.academy.feature.member.pendingpayment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.InscriptionDraftStore
import app.dsiacademy.academy.domain.member.AuthRepository
import app.dsiacademy.academy.domain.member.PaymentVerdict
import app.dsiacademy.academy.domain.member.paymentVerdictFromStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PendingPaymentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val draftStore: InscriptionDraftStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingPaymentUiState())
    val uiState: StateFlow<PendingPaymentUiState> = _uiState.asStateFlow()

    private val _events = Channel<PendingPaymentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var trackingToken: String? = null

    init {
        viewModelScope.launch {
            val draft = draftStore.getDraft()
            val token = draft?.paymentTrackingToken
            if (draft == null || token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(phase = PendingPaymentPhase.NoPending)
                }
            } else {
                trackingToken = token
                _uiState.update {
                    it.copy(
                        phase = PendingPaymentPhase.Ready,
                        paymentUrl = draft.paymentUrl,
                    )
                }
            }
        }
    }

    fun onOpenPaymentClicked() {
        val url = _uiState.value.paymentUrl
        if (url.isNullOrBlank() || !url.startsWith("https://")) {
            _uiState.update { it.copy(message = PendingPaymentMessage.Unavailable) }
            return
        }
        viewModelScope.launch {
            _events.send(PendingPaymentEvent.OpenPaymentUrl(url))
        }
    }

    fun verifierPaiement() {
        if (_uiState.value.checking) return
        val token = trackingToken ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(checking = true, message = PendingPaymentMessage.None)
            }
            when (val result = authRepository.verifierStatutPaiement(token)) {
                is DsiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            checking = false,
                            message = PendingPaymentMessage.Unavailable,
                        )
                    }
                }
                is DsiResult.Success -> {
                    when (paymentVerdictFromStatus(result.data)) {
                        PaymentVerdict.Active -> {
                            draftStore.clearDraft()
                            trackingToken = null
                            _uiState.update {
                                it.copy(
                                    checking = false,
                                    message = PendingPaymentMessage.None,
                                )
                            }
                            _events.send(PendingPaymentEvent.Paid)
                        }
                        PaymentVerdict.Waiting -> {
                            _uiState.update {
                                it.copy(
                                    checking = false,
                                    message = PendingPaymentMessage.Waiting,
                                )
                            }
                        }
                        PaymentVerdict.Refused -> {
                            _uiState.update {
                                it.copy(
                                    checking = false,
                                    message = PendingPaymentMessage.Refused,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
