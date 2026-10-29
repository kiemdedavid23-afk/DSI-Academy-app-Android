package app.dsiacademy.academy.feature.member.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.PaiementItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentsUiState(
    val paiements: List<PaiementItem> = emptyList(),
    val page: Int = 1,
    val totalPages: Int = 1,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val repo: MemberRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(PaymentsUiState())
    val state: StateFlow<PaymentsUiState> = _state.asStateFlow()

    init { load(1) }

    fun load(page: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = repo.getPaiements(page)) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Impossible de charger l'historique.")
                }
                is DsiResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        paiements = r.data.paiements,
                        page = r.data.page,
                        totalPages = r.data.totalPages.coerceAtLeast(1),
                    )
                }
            }
        }
    }
}
