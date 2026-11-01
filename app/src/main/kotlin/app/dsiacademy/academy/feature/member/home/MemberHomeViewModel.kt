package app.dsiacademy.academy.feature.member.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiFormats
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.ContinueEntry
import app.dsiacademy.academy.core.session.FavoriteEntry
import app.dsiacademy.academy.core.session.LocalContentStores
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.RetraitItem
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SubscriptionState {
    Active,
    Expired,
    None,
}

data class MemberHomeUiState(
    val prenom: String? = null,
    val soldeDsi: String = "0",
    val totalGagne: String = "0",
    val abonnementState: SubscriptionState = SubscriptionState.None,
    val abonnementFin: String? = null,
    val activite: List<String> = emptyList(),
    val continueEntry: ContinueEntry? = null,
    val favorites: List<FavoriteEntry> = emptyList(),
    val loading: Boolean = true,
    val sessionExpired: Boolean = false,
    val loadFailed: Boolean = false,
    val errorMessage: String? = null,
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
            _state.update {
                it.copy(
                    loading = true,
                    sessionExpired = false,
                    loadFailed = false,
                    errorMessage = null,
                )
            }
            when (val result = memberRepository.getDashboard()) {
                is DsiResult.Error -> {
                    val expired = result.error is AppError.SessionExpired
                    _state.update {
                        it.copy(
                            loading = false,
                            sessionExpired = expired,
                            loadFailed = !expired,
                            errorMessage = if (expired) null else result.error.message,
                        )
                    }
                }
                is DsiResult.Success -> {
                    val d = result.data
                    val prenom = d.profil.prenom.takeIf { it.isNotBlank() }
                        ?: d.profil.nom.takeIf { it.isNotBlank() }
                    val etatRaw = d.abonnement?.etatAffiche ?: "none"
                    val subState = when (etatRaw) {
                        "active" -> SubscriptionState.Active
                        "expired" -> SubscriptionState.Expired
                        else -> SubscriptionState.None
                    }
                    val english = Locale.getDefault().language.startsWith("en")
                    val finMs = d.abonnement?.dateFin?.let { DsiFormats.parseIsoMs(it) }
                    val finTexte = finMs?.let { DsiFormats.formatShortDate(it, english) }
                    val solde = d.solde?.soldeDsiActuel?.let { DsiFormats.formatDsiAmount(it) } ?: "0"
                    val total = d.solde?.totalDsiGagnes?.let { DsiFormats.formatDsiAmount(it) } ?: "0"
                    val activite = d.retraits.take(5).map { formatRetrait(it) }
                    _state.update {
                        it.copy(
                            prenom = prenom,
                            soldeDsi = solde,
                            totalGagne = total,
                            abonnementState = subState,
                            abonnementFin = finTexte,
                            activite = activite,
                            loading = false,
                            sessionExpired = false,
                            loadFailed = false,
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    private fun formatRetrait(r: RetraitItem): String {
        val montant = DsiFormats.formatDsiAmount(r.montantDsiDemande)
        return "Retrait $montant DSI · ${r.statut}"
    }
}
