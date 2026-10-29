package app.dsiacademy.academy.feature.member.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.data.community.CommunityRepository
import app.dsiacademy.academy.domain.member.MemberProfil
import app.dsiacademy.academy.domain.member.MemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profil: MemberProfil? = null,
    val avatarUrl: String? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val copiedKey: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            when (val r = memberRepository.getProfil()) {
                is DsiResult.Error -> _state.update {
                    it.copy(loading = false, error = r.error.message ?: "Impossible de charger le profil.")
                }
                is DsiResult.Success -> {
                    _state.update { it.copy(loading = false, profil = r.data) }
                    when (val c = communityRepository.getMyProfile()) {
                        is DsiResult.Success -> {
                            val p = c.data
                            _state.update {
                                it.copy(avatarUrl = p?.avatarThumbUrl ?: p?.avatarUrl)
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    fun markCopied(key: String) {
        _state.update { it.copy(copiedKey = key) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _state.update { if (it.copiedKey == key) it.copy(copiedKey = null) else it }
        }
    }
}
