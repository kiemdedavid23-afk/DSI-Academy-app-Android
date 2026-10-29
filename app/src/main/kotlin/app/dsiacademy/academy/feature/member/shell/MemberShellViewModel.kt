package app.dsiacademy.academy.feature.member.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.data.community.CommunityRepository
import app.dsiacademy.academy.core.session.PreferencesStore
import app.dsiacademy.academy.domain.member.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberShellUiState(
    val fullName: String = "",
    val initials: String = "",
    val avatarUrl: String? = null,
    val subState: String = "none",
    val subLine: String = "Sans abonnement",
    val subFoot: String = "",
    val subPercent: Int = 0,
    val subBadge: String = "Aucun",
    val subBtn: String = "S'abonner",
    val isDark: Boolean = false,
)

@HiltViewModel
class MemberShellViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val communityRepository: CommunityRepository,
    private val preferencesStore: PreferencesStore,
) : ViewModel() {

    private val _state = MutableStateFlow(MemberShellUiState())
    val state: StateFlow<MemberShellUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesStore.isDarkFlow.collect { dark ->
                _state.update { it.copy(isDark = dark) }
            }
        }

        viewModelScope.launch {
            when (val r = memberRepository.getProfil()) {
                is DsiResult.Success -> {
                    val p = r.data
                    val prenom = p.prenom
                    val nom = p.nom
                    val full = "$prenom $nom".trim()
                    val ini = buildString {
                        if (prenom.isNotBlank()) append(prenom.first().uppercaseChar())
                        if (nom.isNotBlank()) append(nom.first().uppercaseChar())
                    }
                    _state.update {
                        it.copy(fullName = full.ifBlank { "Membre DSI" }, initials = ini.ifBlank { "M" })
                    }
                }
                else -> Unit
            }
            // Avatar communauté — en mémoire seulement (URL courte durée)
            when (val r = communityRepository.getMyProfile()) {
                is DsiResult.Success -> {
                    val profile = r.data
                    if (profile != null) {
                        _state.update {
                            it.copy(
                                avatarUrl = profile.avatarThumbUrl ?: profile.avatarUrl,
                                fullName = profile.displayName?.takeIf { n -> n.isNotBlank() } ?: it.fullName,
                            )
                        }
                    }
                }
                else -> Unit
            }
            when (val r = memberRepository.getAbonnement()) {
                is DsiResult.Success -> {
                    val a = r.data
                    val etat = a.etatAffiche
                    val jours = a.joursRestants
                    val fin = a.dateFin
                    when (etat) {
                        "active" -> {
                            val l = when (jours) {
                                1 -> "1 jour restant"
                                null -> "Abonnement actif"
                                else -> "$jours jours restants"
                            }
                            _state.update {
                                it.copy(
                                    subState = etat,
                                    subBadge = "Actif",
                                    subLine = l,
                                    subFoot = if (!fin.isNullOrBlank()) "Expire le $fin" else "",
                                    subBtn = "Renouveler",
                                    subPercent = a.progressPercent,
                                )
                            }
                        }
                        "expired" -> _state.update {
                            it.copy(
                                subState = etat,
                                subBadge = "Expiré",
                                subLine = "Abonnement expiré",
                                subFoot = if (!fin.isNullOrBlank()) "Expiré le $fin" else "",
                                subBtn = "Renouveler",
                                subPercent = 0,
                            )
                        }
                        else -> _state.update {
                            it.copy(
                                subState = "none",
                                subBadge = "Aucun",
                                subLine = "Sans abonnement",
                                subFoot = "",
                                subBtn = "S'abonner",
                                subPercent = 0,
                            )
                        }
                    }
                }
                is DsiResult.Error -> {
                    _state.update {
                        it.copy(subLine = "Indisponible", subBtn = "Voir", subBadge = "")
                    }
                }
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val next = !_state.value.isDark
            preferencesStore.setDark(next)
        }
    }

    /** Appelé si Coil échoue sur l'URL avatar (expiration). */
    fun clearAvatar() {
        _state.update { it.copy(avatarUrl = null) }
    }
}
