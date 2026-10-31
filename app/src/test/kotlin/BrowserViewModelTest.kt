package app.dsiacademy.academy.feature.browser

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import app.dsiacademy.academy.core.common.LinkDecision
import app.dsiacademy.academy.core.common.LinkPolicy
import app.dsiacademy.academy.core.common.PaymentReturnStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    private val testSiteUrl = "https://exemple.test"
    private val testExternalHosts = setOf("facebook.com", "wa.me")

    private fun createPolicy(siteUrl: String = testSiteUrl): LinkPolicy =
        LinkPolicy(siteUrl, testExternalHosts)

    private fun createViewModel(
        linkId: String? = "inscription",
        ref: String? = null,
        policy: LinkPolicy = createPolicy(),
    ): BrowserViewModel {
        val map = mutableMapOf<String, Any?>()
        if (linkId != null) map["linkId"] = linkId
        if (ref != null) map["ref"] = ref
        return BrowserViewModel(SavedStateHandle(map), policy)
    }

    // --- État initial ---

    @Test
    fun etatInitial_siteNonConfigure_phaseNotConfigured() {
        val vm = createViewModel(policy = createPolicy(siteUrl = ""))
        val state = vm.uiState.value
        assertEquals(BrowserPhase.NotConfigured, state.phase)
        assertNull(state.startUrl)
    }

    @Test
    fun etatInitial_lienInconnu_phaseUnknownLink() {
        val vm = createViewModel(linkId = "inconnu")
        val state = vm.uiState.value
        assertEquals(BrowserPhase.UnknownLink, state.phase)
        assertNull(state.startUrl)
    }

    @Test
    fun etatInitial_lienAbsent_phaseUnknownLink() {
        val vm = createViewModel(linkId = null)
        val state = vm.uiState.value
        assertEquals(BrowserPhase.UnknownLink, state.phase)
        assertNull(state.startUrl)
    }

    @Test
    fun etatInitial_lienConnu_phaseReadyAvecStartUrl() {
        val vm = createViewModel(linkId = "inscription")
        val state = vm.uiState.value
        assertEquals(BrowserPhase.Ready, state.phase)
        assertEquals("https://exemple.test/inscription", state.startUrl)
        assertEquals("exemple.test", state.host)
        assertTrue(state.isSecure)
        assertFalse(state.loading)
        assertEquals(0f, state.progress, 0.001f)
        assertFalse(state.hasError)
        assertFalse(state.canGoBack)
        assertEquals("", state.title)
    }

    @Test
    fun etatInitial_refPresent_estEncodeDansStartUrl() {
        val vm = createViewModel(linkId = "inscription", ref = "ABC 12")
        val state = vm.uiState.value
        assertEquals(BrowserPhase.Ready, state.phase)
        assertEquals("https://exemple.test/inscription?ref=ABC+12", state.startUrl)
    }

    @Test
    fun etatInitial_refBlanc_estIgnore() {
        val vm = createViewModel(linkId = "inscription", ref = "   ")
        val state = vm.uiState.value
        assertEquals("https://exemple.test/inscription", state.startUrl)
    }

    // --- onPageStarted ---

    @Test
    fun onPageStarted_metLoadingEtHost() {
        val vm = createViewModel()
        vm.onPageStarted("https://exemple.test/inscription")
        val state = vm.uiState.value
        assertTrue(state.loading)
        assertFalse(state.hasError)
        assertEquals(0f, state.progress, 0.001f)
        assertEquals("exemple.test", state.host)
        assertTrue(state.isSecure)
    }

    // --- onProgressChanged ---

    @Test
    fun onProgressChanged_borneEntre0Et1() {
        val vm = createViewModel()
        vm.onProgressChanged(50)
        assertEquals(0.5f, vm.uiState.value.progress, 0.001f)

        vm.onProgressChanged(-10)
        assertEquals(0f, vm.uiState.value.progress, 0.001f)

        vm.onProgressChanged(150)
        assertEquals(1f, vm.uiState.value.progress, 0.001f)
    }

    // --- onPageFinished ---

    @Test
    fun onPageFinished_metFinChargementEtCanGoBack() {
        val vm = createViewModel()
        vm.onPageStarted("https://exemple.test/inscription")
        vm.onPageFinished("https://exemple.test/inscription", canGoBack = true)
        val state = vm.uiState.value
        assertFalse(state.loading)
        assertEquals(1f, state.progress, 0.001f)
        assertTrue(state.canGoBack)
        assertEquals("exemple.test", state.host)
        assertTrue(state.isSecure)
    }

    // --- onTitleReceived ---

    @Test
    fun onTitleReceived_titreRecu_estAffiche() {
        val vm = createViewModel()
        vm.onPageStarted("https://exemple.test/inscription")
        vm.onTitleReceived("Inscription DSI")
        assertEquals("Inscription DSI", vm.uiState.value.title)
    }

    @Test
    fun onTitleReceived_titreVide_retombeSurHost() {
        val vm = createViewModel()
        vm.onPageStarted("https://exemple.test/inscription")
        vm.onTitleReceived("   ")
        assertEquals("exemple.test", vm.uiState.value.title)
    }

    // --- onMainFrameError + onRetry ---

    @Test
    fun onMainFrameError_puisOnRetry() {
        val vm = createViewModel()
        vm.onPageStarted("https://exemple.test/inscription")
        vm.onMainFrameError()
        var state = vm.uiState.value
        assertTrue(state.hasError)
        assertFalse(state.loading)

        vm.onRetry()
        state = vm.uiState.value
        assertFalse(state.hasError)
        assertTrue(state.loading)
        assertEquals(0f, state.progress, 0.001f)
    }

    // --- onNavigationRequest ---

    @Test
    fun onNavigationRequest_Allow_aucunEvenement() = runTest {
        val vm = createViewModel()
        vm.events.test {
            val decision = vm.onNavigationRequest("https://exemple.test/inscription")
            assertEquals(LinkDecision.Allow, decision)
            expectNoEvents()
        }
    }

    @Test
    fun onNavigationRequest_OpenExternal_aucunEvenement() = runTest {
        val vm = createViewModel()
        vm.events.test {
            val decision = vm.onNavigationRequest("https://www.facebook.com/share/1GVL3d7duA/")
            assertTrue(decision is LinkDecision.OpenExternal)
            expectNoEvents()
        }
    }

    @Test
    fun onNavigationRequest_Block_aucunEvenement() = runTest {
        val vm = createViewModel()
        vm.events.test {
            val decision = vm.onNavigationRequest("javascript:alert(1)")
            assertEquals(LinkDecision.Block, decision)
            expectNoEvents()
        }
    }

    @Test
    fun onNavigationRequest_PaymentReturn_emetUnSeulEvenement() = runTest {
        val vm = createViewModel()
        vm.events.test {
            val url = "https://exemple.test/paiement-retour?status=success&token=tok123"
            val decision1 = vm.onNavigationRequest(url)
            assertTrue(decision1 is LinkDecision.PaymentReturn)

            val event = awaitItem()
            assertTrue(event is BrowserEvent.PaymentReturned)
            val pr = event as BrowserEvent.PaymentReturned
            assertEquals(PaymentReturnStatus.Success, pr.status)
            assertEquals("tok123", pr.trackingToken)

            // Second appel : même décision, mais plus d'événement
            val decision2 = vm.onNavigationRequest(url)
            assertTrue(decision2 is LinkDecision.PaymentReturn)
            expectNoEvents()
        }
    }

    // --- onCloseRequested ---

    @Test
    fun onCloseRequested_emetClose() = runTest {
        val vm = createViewModel()
        vm.events.test {
            vm.onCloseRequested()
            assertEquals(BrowserEvent.Close, awaitItem())
        }
    }
}
