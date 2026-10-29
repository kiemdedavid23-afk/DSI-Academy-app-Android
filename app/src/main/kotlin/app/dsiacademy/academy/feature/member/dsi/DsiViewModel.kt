package app.dsiacademy.academy.feature.member.dsi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.GiftCodeResult
import app.dsiacademy.academy.domain.member.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DsiUiState(
    val soldeActuel: String = "—",
    val totalGagne: String = "—",
    val identifiant: String? = null,
    val dateMaj: String? = null,
    val giftCode: String = "",
    val giftMessage: String? = null,
    val giftError: String? = null,
    val giftLoading: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class DsiViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DsiUiState())
    val state: StateFlow<DsiUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = memberRepository.getSolde()) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Impossible de récupérer votre solde.")
                }
                is DsiResult.Success -> {
                    fun fmt(v: Double) = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
                    _state.update {
                        it.copy(
                            loading = false,
                            soldeActuel = fmt(r.data.soldeDsiActuel),
                            totalGagne = fmt(r.data.totalDsiGagnes),
                            identifiant = r.data.identifiantDsi,
                            dateMaj = r.data.dateMiseAJour,
                        )
                    }
                }
            }
        }
    }

    fun setGiftCode(v: String) = _state.update { it.copy(giftCode = v, giftError = null, giftMessage = null) }

    fun soumettreCode() {
        viewModelScope.launch {
            _state.update { it.copy(giftLoading = true, giftError = null, giftMessage = null) }
            when (val r = memberRepository.utiliserCode(_state.value.giftCode)) {
                is DsiResult.Error -> _state.update {
                    it.copy(giftLoading = false, giftError = r.error.message ?: "Indisponible.")
                }
                is DsiResult.Success -> when (val g = r.data) {
                    is GiftCodeResult.Success -> {
                        fun fmt(v: Double) = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
                        _state.update {
                            it.copy(
                                giftLoading = false,
                                giftCode = "",
                                giftMessage = g.message ?: "+${fmt(g.montantDsi)} DSI crédités.",
                            )
                        }
                        charger()
                    }
                    is GiftCodeResult.Failure -> {
                        val msg = when (g.reason) {
                            "alreadyUsed" -> "Vous avez déjà utilisé ce code."
                            "empty" -> "Saisissez un code."
                            "session" -> "Votre session a expiré. Reconnectez-vous."
                            else -> "Ce code est invalide ou a expiré."
                        }
                        _state.update { it.copy(giftLoading = false, giftError = msg) }
                    }
                }
            }
        }
    }
}
