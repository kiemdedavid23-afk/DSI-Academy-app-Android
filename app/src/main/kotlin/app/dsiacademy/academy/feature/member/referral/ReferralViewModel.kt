package app.dsiacademy.academy.feature.member.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferralUiState(
    val code: String = "",
    val filleulsTotal: Int = 0,
    val filleulsActifs: Int = 0,
    val n1: String = "0",
    val n2: String = "0",
    val total: String = "0",
    val historique: List<String> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ReferralUiState())
    val state: StateFlow<ReferralUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val r = memberRepository.getParrainage()) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Impossible de récupérer le parrainage.")
                }
                is DsiResult.Success -> {
                    fun fmt(v: Double) = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
                    val d = r.data
                    _state.update {
                        it.copy(
                            loading = false,
                            code = d.codeParrainagePersonnel,
                            filleulsTotal = d.filleulsDirectsTotal,
                            filleulsActifs = d.filleulsDirectsActifs,
                            n1 = fmt(d.recompensesNiveau1Dsi),
                            n2 = fmt(d.recompensesNiveau2Dsi),
                            total = fmt(d.totalDsiParrainage),
                            historique = d.historique.map { h ->
                                val m = fmt(h.montant)
                                val who = h.identifiantDsiFilleul ?: "—"
                                val date = h.date ?: ""
                                "${h.type} · $m DSI · $who · $date"
                            },
                        )
                    }
                }
            }
        }
    }
}
