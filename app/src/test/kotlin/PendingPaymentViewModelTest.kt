package app.dsiacademy.academy.feature.member.pendingpayment

import app.cash.turbine.test
import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.InscriptionDraft
import app.dsiacademy.academy.core.session.InscriptionDraftStore
import app.dsiacademy.academy.domain.member.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PendingPaymentViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var draftStore: InscriptionDraftStore

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepository = mockk()
        draftStore = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVm(): PendingPaymentViewModel =
        PendingPaymentViewModel(authRepository, draftStore)

    @Test
    fun pasDeBrouillon_phaseNoPending() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns null
        val vm = createVm()
        advanceUntilIdle()
        assertEquals(PendingPaymentPhase.NoPending, vm.uiState.value.phase)
    }

    @Test
    fun brouillonSansJeton_phaseNoPending() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            userId = "u1",
            paymentTrackingToken = null,
            paymentUrl = "https://pay.example/x",
        )
        val vm = createVm()
        advanceUntilIdle()
        assertEquals(PendingPaymentPhase.NoPending, vm.uiState.value.phase)
    }

    @Test
    fun brouillonValide_phaseReady() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            userId = "u1",
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        val vm = createVm()
        advanceUntilIdle()
        assertEquals(PendingPaymentPhase.Ready, vm.uiState.value.phase)
        assertEquals("https://pay.example/x", vm.uiState.value.paymentUrl)
    }

    @Test
    fun ouvrirPaiement_urlHttps_emetOpenPaymentUrl() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.events.test {
            vm.onOpenPaymentClicked()
            val event = awaitItem()
            assertTrue(event is PendingPaymentEvent.OpenPaymentUrl)
            assertEquals("https://pay.example/x", (event as PendingPaymentEvent.OpenPaymentUrl).url)
        }
    }

    @Test
    fun ouvrirPaiement_sansUrl_messageUnavailable() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = null,
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.onOpenPaymentClicked()
        assertEquals(PendingPaymentMessage.Unavailable, vm.uiState.value.message)
    }

    @Test
    fun ouvrirPaiement_urlNonHttps_messageUnavailable() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "http://insecure.example",
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.onOpenPaymentClicked()
        assertEquals(PendingPaymentMessage.Unavailable, vm.uiState.value.message)
    }

    @Test
    fun verifier_active_effaceBrouillonEtEmetPaid() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        coEvery { authRepository.verifierStatutPaiement("tok") } returns DsiResult.Success("active")
        val vm = createVm()
        advanceUntilIdle()
        vm.events.test {
            vm.verifierPaiement()
            advanceUntilIdle()
            assertEquals(PendingPaymentEvent.Paid, awaitItem())
        }
        coVerify { draftStore.clearDraft() }
        assertFalse(vm.uiState.value.checking)
    }

    @Test
    fun verifier_attente_messageWaiting() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        coEvery { authRepository.verifierStatutPaiement("tok") } returns DsiResult.Success("attente")
        val vm = createVm()
        advanceUntilIdle()
        vm.verifierPaiement()
        advanceUntilIdle()
        assertEquals(PendingPaymentMessage.Waiting, vm.uiState.value.message)
        coVerify(exactly = 0) { draftStore.clearDraft() }
    }

    @Test
    fun verifier_refuse_messageRefused_brouillonConserve() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        coEvery { authRepository.verifierStatutPaiement("tok") } returns DsiResult.Success("refuse")
        val vm = createVm()
        advanceUntilIdle()
        vm.verifierPaiement()
        advanceUntilIdle()
        assertEquals(PendingPaymentMessage.Refused, vm.uiState.value.message)
        coVerify(exactly = 0) { draftStore.clearDraft() }
    }

    @Test
    fun verifier_erreur_messageUnavailable() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        coEvery { authRepository.verifierStatutPaiement("tok") } returns
            DsiResult.Error(AppError.Network())
        val vm = createVm()
        advanceUntilIdle()
        vm.verifierPaiement()
        advanceUntilIdle()
        assertEquals(PendingPaymentMessage.Unavailable, vm.uiState.value.message)
    }

    @Test
    fun verifier_doubleAppuiPendantChecking_ignore() = runTest(dispatcher) {
        coEvery { draftStore.getDraft() } returns InscriptionDraft(
            paymentTrackingToken = "tok",
            paymentUrl = "https://pay.example/x",
        )
        // Réponse différée : checking reste true tant que l'appel n'a pas fini
        coEvery { authRepository.verifierStatutPaiement("tok") } coAnswers {
            kotlinx.coroutines.delay(5_000)
            DsiResult.Success("attente")
        }
        val vm = createVm()
        advanceUntilIdle()
        vm.verifierPaiement()
        // Laisse le premier appel poser checking = true et suspendre sur delay
        advanceTimeBy(1)
        assertTrue(vm.uiState.value.checking)
        vm.verifierPaiement() // doit être ignoré
        advanceTimeBy(5_000)
        advanceUntilIdle()
        coVerify(exactly = 1) { authRepository.verifierStatutPaiement("tok") }
    }
}
