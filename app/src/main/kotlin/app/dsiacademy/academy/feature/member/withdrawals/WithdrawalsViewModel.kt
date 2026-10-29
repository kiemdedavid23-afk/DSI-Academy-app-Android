package app.dsiacademy.academy.feature.member.withdrawals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.RetraitEvenement
import app.dsiacademy.academy.domain.member.RetraitItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ordre frise site : demande → validee → envoyee → payee (ou rejetee). */
val RETRAIT_STEPS = listOf("demande", "validee", "envoyee", "payee")

fun statutLabel(s: String): String = when (s.lowercase()) {
    "demande" -> "Demande"
    "validee" -> "Validée"
    "envoyee" -> "Envoyée"
    "payee" -> "Payée"
    "rejetee" -> "Rejetée"
    "traite" -> "Traitée"
    else -> s.ifBlank { "—" }
}

data class WithdrawalsUiState(
    val retraits: List<RetraitItem> = emptyList(),
    val moyens: List<String> = emptyList(),
    val pays: String? = null,
    val montant: String = "",
    val moyen: String = "",
    val numero: String = "",
    val selectedId: String? = null,
    val evenements: List<RetraitEvenement> = emptyList(),
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class WithdrawalsViewModel @Inject constructor(
    private val repo: MemberRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WithdrawalsUiState())
    val state: StateFlow<WithdrawalsUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val pays = (repo.getProfil() as? DsiResult.Success)?.data?.pays
            val liste = repo.getRetraits()
            val moyens = repo.getMoyensRetrait(pays)
            val retraits = (liste as? DsiResult.Success)?.data?.retraits ?: emptyList()
            val m = (moyens as? DsiResult.Success)?.data ?: emptyList()
            val err = (liste as? DsiResult.Error)?.error?.message
            _state.update {
                it.copy(
                    loading = false,
                    pays = pays,
                    retraits = retraits,
                    moyens = m,
                    moyen = m.firstOrNull() ?: it.moyen,
                    error = err,
                )
            }
        }
    }

    fun setMontant(v: String) = _state.update { it.copy(montant = v, successMessage = null) }
    fun setMoyen(v: String) = _state.update { it.copy(moyen = v) }
    fun setNumero(v: String) = _state.update { it.copy(numero = v) }
    fun selectRetrait(id: String?) {
        _state.update { it.copy(selectedId = id, evenements = emptyList()) }
        if (id != null) {
            viewModelScope.launch {
                when (val r = repo.getRetraitHistorique(id)) {
                    is DsiResult.Success -> _state.update { it.copy(evenements = r.data) }
                    else -> Unit
                }
            }
        }
    }

    fun soumettre() {
        val s = _state.value
        val montant = s.montant.toDoubleOrNull()
        if (montant == null || montant <= 0 || s.moyen.isBlank() || s.numero.isBlank()) {
            _state.update { it.copy(error = "Renseignez montant, moyen et numéro.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null, successMessage = null) }
            when (val r = repo.demanderRetrait(montant, s.moyen, s.numero.trim())) {
                is DsiResult.Error -> _state.update {
                    it.copy(submitting = false, error = r.error.message ?: "Demande refusée.")
                }
                is DsiResult.Success -> {
                    val msg = when (r.data.status) {
                        "demande_enregistree" -> r.data.message
                            ?: "Demande enregistrée (${r.data.idRetrait ?: ""})."
                        else -> r.data.message ?: "Demande refusée."
                    }
                    _state.update {
                        it.copy(
                            submitting = false,
                            successMessage = if (r.data.status == "demande_enregistree") msg else null,
                            error = if (r.data.status != "demande_enregistree") msg else null,
                            montant = if (r.data.status == "demande_enregistree") "" else it.montant,
                        )
                    }
                    if (r.data.status == "demande_enregistree") charger()
                }
            }
        }
    }
}
