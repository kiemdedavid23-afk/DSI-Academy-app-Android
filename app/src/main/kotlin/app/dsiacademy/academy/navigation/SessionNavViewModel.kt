package app.dsiacademy.academy.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dsiacademy.academy.core.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SessionNavViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {
    val session = sessionStore.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), sessionStore.getSession())

    val events = sessionStore.events

    fun hasSession(): Boolean = sessionStore.hasSession()

    fun logout() {
        sessionStore.clearSession()
    }
}
