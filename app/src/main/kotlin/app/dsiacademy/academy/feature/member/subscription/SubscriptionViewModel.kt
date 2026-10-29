package app.dsiacademy.academy.feature.member.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.MemberAbonnement
import app.dsiacademy.academy.domain.member.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val abo: MemberAbonnement? = null,
    val loading: Boolean = true,
    val renewing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

sealed interface SubEvent {
    data class OpenUrl(val url: String) : SubEvent
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repo: MemberRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state.asStateFlow()
    private val _events = Channel<SubEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = repo.getAbonnement()) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Impossible de charger l'abonnement.")
                }
                is DsiResult.Success -> _state.update {
                    it.copy(loading = false, abo = r.data)
                }
            }
        }
    }

    fun renouveler(mode: String) {
        viewModelScope.launch {
            _state.update { it.copy(renewing = true, error = null, message = null) }
            when (val r = repo.renouvelerAbonnement(mode)) {
                is DsiResult.Error -> _state.update {
                    it.copy(renewing = false, error = r.error.message ?: "Renouvellement échoué.")
                }
                is DsiResult.Success -> {
                    when (r.data.status) {
                        "pending_payment" -> {
                            _state.update { it.copy(renewing = false, message = "Paiement en attente…") }
                            r.data.paymentUrl?.let { _events.send(SubEvent.OpenUrl(it)) }
                        }
                        "active" -> {
                            _state.update {
                                it.copy(renewing = false, message = r.data.message ?: "Abonnement activé.")
                            }
                            charger()
                        }
                        else -> _state.update {
                            it.copy(renewing = false, error = r.data.message ?: "Renouvellement refusé.")
                        }
                    }
                }
            }
        }
    }
}
