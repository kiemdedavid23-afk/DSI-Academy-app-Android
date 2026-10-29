package app.dsiacademy.academy.feature.member.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.AuthRepository
import app.dsiacademy.academy.core.session.InscriptionDraft
import app.dsiacademy.academy.core.session.InscriptionDraftStore
import app.dsiacademy.academy.domain.member.InscriptionForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormState(
    val nom: String = "",
    val prenom: String = "",
    val pays: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val motDePasse: String = "",
    val codeParrain: String = "",
    val chargement: Boolean = false,
    val messageErreur: String? = null,
    val paymentUrl: String? = null,
    val paymentTrackingToken: String? = null,
    val userId: String? = null,
    val paiementOk: Boolean = false,
)

sealed interface RegisterUiEvent {
    data object NavigateLogin : RegisterUiEvent
    data class OpenPaymentUrl(val url: String) : RegisterUiEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val draftStore: InscriptionDraftStore,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterFormState())
    val state: StateFlow<RegisterFormState> = _state.asStateFlow()

    private val _events = Channel<RegisterUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setCodeParrain(ref: String) {
        if (ref.isNotBlank()) _state.update { it.copy(codeParrain = ref) }
    }

    fun setField(champ: String, valeur: String) {
        _state.update { s ->
            when (champ) {
                "nom" -> s.copy(nom = valeur)
                "prenom" -> s.copy(prenom = valeur)
                "pays" -> s.copy(pays = valeur)
                "whatsapp" -> s.copy(whatsapp = valeur)
                "email" -> s.copy(email = valeur)
                "mdp" -> s.copy(motDePasse = valeur)
                else -> s
            }
        }
    }

    fun soumettreInscription() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(chargement = true, messageErreur = null) }
            val form = InscriptionForm(
                nom = s.nom.trim(),
                prenom = s.prenom.trim(),
                whatsapp = s.whatsapp.trim(),
                email = s.email.trim(),
                pays = s.pays,
                codeParrain = s.codeParrain,
                motDePasse = s.motDePasse,
            )
            when (val result = authRepository.creerInscription(form)) {
                is DsiResult.Error -> _state.update {
                    it.copy(
                        chargement = false,
                        messageErreur = result.error.message
                            ?: "La création de votre compte a échoué. Vérifiez votre connexion et réessayez.",
                    )
                }
                is DsiResult.Success -> {
                    val d = result.data
                    _state.update {
                        it.copy(
                            chargement = false,
                            userId = d.userId,
                            paymentTrackingToken = d.paymentTrackingToken,
                            paymentUrl = d.paymentUrl,
                        )
                    }
                    draftStore.saveDraft(
                        InscriptionDraft(
                            userId = d.userId,
                            paymentTrackingToken = d.paymentTrackingToken,
                            paymentUrl = d.paymentUrl,
                            etape = "paiement",
                        )
                    )
                    d.paymentUrl?.let { _events.send(RegisterUiEvent.OpenPaymentUrl(it)) }
                }
            }
        }
    }

    fun verifierPaiement() {
        val token = _state.value.paymentTrackingToken ?: return
        viewModelScope.launch {
            _state.update { it.copy(chargement = true, messageErreur = null) }
            when (val result = authRepository.verifierStatutPaiement(token)) {
                is DsiResult.Error -> _state.update {
                    it.copy(
                        chargement = false,
                        messageErreur = result.error.message
                            ?: "Impossible de vérifier votre paiement pour le moment.",
                    )
                }
                is DsiResult.Success -> when (result.data) {
                    "active" -> {
                        draftStore.clearDraft()
                        _state.update { it.copy(chargement = false, paiementOk = true, messageErreur = null) }
                        _events.send(RegisterUiEvent.NavigateLogin)
                    }
                    "attente" -> _state.update {
                        it.copy(chargement = false, messageErreur = "⏳ Paiement en attente. Nous vous préviendrons dès sa confirmation.")
                    }
                    "refuse", "introuvable" -> _state.update {
                        it.copy(chargement = false, messageErreur = "❌ Paiement non confirmé.")
                    }
                    else -> _state.update {
                        it.copy(chargement = false, messageErreur = "Statut : ${result.data}")
                    }
                }
            }
        }
    }

    suspend fun restaurerBrouillonSiBesoin(): InscriptionDraft? {
        val d = draftStore.getDraft()
        if (d != null && d.etape == "paiement" && !d.paymentTrackingToken.isNullOrBlank()) {
            _state.update {
                it.copy(
                    userId = d.userId,
                    paymentTrackingToken = d.paymentTrackingToken,
                    paymentUrl = d.paymentUrl,
                )
            }
            return d
        }
        return null
    }

    fun goLogin() {
        viewModelScope.launch { _events.send(RegisterUiEvent.NavigateLogin) }
    }
}
