package app.dsiacademy.academy.feature.member.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.ContinueEntry
import app.dsiacademy.academy.core.session.FavoriteEntry
import app.dsiacademy.academy.core.session.LocalContentStores
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.RetraitItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberHomeUiState(
    val prenom: String = "",
    val soldeDsi: String = "—",
    val totalGagne: String = "—",
    val abonnementLabel: String = "…",
    val abonnementState: String = "none",
    val activite: List<String> = emptyList(),
    val continueEntry: ContinueEntry? = null,
    val favorites: List<FavoriteEntry> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val sessionExpired: Boolean = false,
)

@HiltViewModel
class MemberHomeViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val localContent: LocalContentStores,
) : ViewModel() {

    private val _state = MutableStateFlow(MemberHomeUiState())
    val state: StateFlow<MemberHomeUiState> = _state.asStateFlow()

    init {
        charger()
        viewModelScope.launch {
            localContent.continueFlow.collect { entry ->
                _state.update { it.copy(continueEntry = entry) }
            }
        }
        viewModelScope.launch {
            localContent.favoritesFlow.collect { list ->
                _state.update { it.copy(favorites = list.take(5)) }
            }
        }
    }

    fun charger() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, sessionExpired = false) }
            when (val result = memberRepository.getDashboard()) {
                is DsiResult.Error -> {
                    val msg = result.error.message
                    val expired = result.error is app.dsiacademy.academy.core.common.AppError.SessionExpired
                    _state.update {
                        it.copy(
                            loading = false,
                            error = msg ?: "Impossible de charger votre tableau de bord.",
                            sessionExpired = expired,
                        )
                    }
                }
                is DsiResult.Success -> {
                    val d = result.data
                    val prenom = d.profil.prenom.ifBlank {
                        d.profil.nom.ifBlank { "Membre DSI" }
                    }
                    val etat = d.abonnement?.etatAffiche ?: "none"
                    val jours = d.abonnement?.joursRestants
                    val fin = d.abonnement?.dateFin
                    val label = when (etat) {
                        "active" -> when {
                            jours == 1 -> "Actif · 1 jour restant"
                            jours != null && jours > 1 -> "Actif · $jours jours restants"
                            !fin.isNullOrBlank() -> "Actif · Fin: $fin"
                            else -> "Abonnement actif"
                        }
                        "expired" -> if (!fin.isNullOrBlank()) "Expiré le $fin" else "Abonnement expiré"
                        else -> "Sans abonnement"
                    }
                    val solde = d.solde?.soldeDsiActuel?.let { formatDsi(it) } ?: "—"
                    val total = d.solde?.totalDsiGagnes?.let { formatDsi(it) } ?: "—"
                    val activite = d.retraits.take(5).map { formatRetrait(it) }
                    _state.update {
                        it.copy(
                            prenom = prenom,
                            soldeDsi = solde,
                            totalGagne = total,
                            abonnementLabel = label,
                            abonnementState = etat,
                            activite = activite,
                            loading = false,
                            error = null,
                        )
                    }
                }
            }
        }
    }

    private fun formatRetrait(r: RetraitItem): String {
        val m = formatDsi(r.montantDsiNet)
        val date = r.dateDemande?.take(10) ?: ""
        return "💸 $m DSI · ${r.statut} · $date"
    }

    private fun formatDsi(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
}
