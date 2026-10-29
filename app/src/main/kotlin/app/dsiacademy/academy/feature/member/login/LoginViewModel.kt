package app.dsiacademy.academy.feature.member.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.MemberSession
import app.dsiacademy.academy.core.session.SessionStore
import app.dsiacademy.academy.domain.member.AuthRepository
import app.dsiacademy.academy.domain.member.LoginSuccess
import app.dsiacademy.academy.domain.member.PendingPayment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val identifiant: String = "",
    val motDePasse: String = "",
    val souvenir: Boolean = false,
    val afficherMdp: Boolean = false,
    val idInvalide: Boolean = false,
    val mdpInvalide: Boolean = false,
    val chargement: Boolean = false,
    val messageErreur: String? = null,
    val messageInfo: String? = null,
)

sealed interface LoginUiEvent {
    data object NavigateMemberHome : LoginUiEvent
    data class NavigateInscriptionPaiement(
        val userId: String?,
        val paymentTrackingToken: String?,
        val paymentUrl: String?,
    ) : LoginUiEvent
    data object NavigateRegister : LoginUiEvent
    data object NavigateForgotPassword : LoginUiEvent
}

sealed interface LoginUiAction {
    data class IdentifiantChanged(val value: String) : LoginUiAction
    data class MotDePasseChanged(val value: String) : LoginUiAction
    data class SouvenirChanged(val value: Boolean) : LoginUiAction
    data object ToggleAfficherMdp : LoginUiAction
    data object Submit : LoginUiAction
    data object ForgotPassword : LoginUiAction
    data object GoRegister : LoginUiAction
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.IdentifiantChanged -> _state.update {
                it.copy(identifiant = action.value, idInvalide = false, messageErreur = null)
            }
            is LoginUiAction.MotDePasseChanged -> _state.update {
                it.copy(motDePasse = action.value, mdpInvalide = false, messageErreur = null)
            }
            is LoginUiAction.SouvenirChanged -> _state.update { it.copy(souvenir = action.value) }
            LoginUiAction.ToggleAfficherMdp -> _state.update { it.copy(afficherMdp = !it.afficherMdp) }
            LoginUiAction.Submit -> submit()
            LoginUiAction.ForgotPassword -> viewModelScope.launch {
                _events.send(LoginUiEvent.NavigateForgotPassword)
            }
            LoginUiAction.GoRegister -> viewModelScope.launch {
                _events.send(LoginUiEvent.NavigateRegister)
            }
        }
    }

    private fun submit() {
        val s = _state.value
        val idPropre = s.identifiant.trim()
        val idInv = idPropre.isEmpty()
        val mdpInv = s.motDePasse.isEmpty()
        if (idInv || mdpInv) {
            _state.update { it.copy(idInvalide = idInv, mdpInvalide = mdpInv) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(chargement = true, messageErreur = null, messageInfo = null) }
            when (val result = authRepository.connecter(idPropre, s.motDePasse)) {
                is DsiResult.Error -> {
                    _state.update {
                        it.copy(
                            chargement = false,
                            messageErreur = result.error.message
                                ?: "Identifiant ou mot de passe incorrect.",
                        )
                    }
                }
                is DsiResult.Success -> {
                    when (val data = result.data) {
                        is LoginSuccess -> {
                            sessionStore.setSession(
                                MemberSession(
                                    token = data.token,
                                    accountStatus = data.accountStatus,
                                    subscriptionStatus = data.subscriptionStatus,
                                    subscriptionActive = data.subscriptionActive,
                                    dateFin = data.dateFin,
                                    accessProof = data.accessProof,
                                ),
                                souvenir = s.souvenir,
                            )
                            _state.update { it.copy(chargement = false) }
                            _events.send(LoginUiEvent.NavigateMemberHome)
                        }
                        is PendingPayment -> {
                            _state.update { it.copy(chargement = false) }
                            _events.send(
                                LoginUiEvent.NavigateInscriptionPaiement(
                                    userId = data.userId,
                                    paymentTrackingToken = data.paymentTrackingToken,
                                    paymentUrl = data.paymentUrl,
                                )
                            )
                        }
                        else -> {
                            _state.update {
                                it.copy(
                                    chargement = false,
                                    messageErreur = "Identifiant ou mot de passe incorrect.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
