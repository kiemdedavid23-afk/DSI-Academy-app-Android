package app.dsiacademy.academy.feature.member.subscription

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.domain.member.MemberAbonnement
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.RenouvelerResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private lateinit var repo: MemberRepository

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repo = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun abo() = MemberAbonnement(
        statut = "active",
        dateDebut = "2026-01-01",
        dateFin = "2099-12-31",
        formule = "Premium",
        modePaiement = "dsi",
    )

    private fun createVm(): SubscriptionViewModel = SubscriptionViewModel(repo)

    @Test
    fun charger_succes() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Success(abo())
        val vm = createVm()
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertEquals("Premium", vm.state.value.abo?.formule)
    }

    @Test
    fun charger_sessionExpiree() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Error(AppError.SessionExpired())
        val vm = createVm()
        advanceUntilIdle()
        assertTrue(vm.state.value.sessionExpired)
        assertFalse(vm.state.value.loadFailed)
    }

    @Test
    fun charger_autreErreur() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Error(AppError.Network("down"))
        val vm = createVm()
        advanceUntilIdle()
        assertTrue(vm.state.value.loadFailed)
        assertEquals("down", vm.state.value.errorMessage)
    }

    @Test
    fun renewFcfa_pendingHttps_paymentLink() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Success(abo())
        coEvery { repo.renouvelerAbonnement("fcfa") } returns DsiResult.Success(
            RenouvelerResult("pending_payment", "https://pay.example/x", null),
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.renewFcfa()
        advanceUntilIdle()
        assertEquals("https://pay.example/x", vm.state.value.paymentLink)
        assertNull(vm.state.value.feedback)
    }

    @Test
    fun renewFcfa_httpNonHttps_unexpected() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Success(abo())
        coEvery { repo.renouvelerAbonnement("fcfa") } returns DsiResult.Success(
            RenouvelerResult("pending_payment", "http://insecure", null),
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.renewFcfa()
        advanceUntilIdle()
        val fb = vm.state.value.feedback as RenewFeedback.Error
        assertEquals(FeedbackKind.Unexpected, fb.kind)
        assertNull(vm.state.value.paymentLink)
    }

    @Test
    fun confirmDsi_active_successEtRecharge() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Success(abo())
        coEvery { repo.renouvelerAbonnement("dsi") } returns DsiResult.Success(
            RenouvelerResult("active", null, "OK serveur"),
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.confirmDsi()
        advanceUntilIdle()
        val fb = vm.state.value.feedback as RenewFeedback.Success
        assertEquals("OK serveur", fb.serverMessage)
        assertEquals(RenewPanel.Closed, vm.state.value.panel)
    }

    @Test
    fun confirmDsi_refuse() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Success(abo())
        coEvery { repo.renouvelerAbonnement("dsi") } returns DsiResult.Success(
            RenouvelerResult("refuse", null, "solde insuffisant"),
        )
        val vm = createVm()
        advanceUntilIdle()
        vm.confirmDsi()
        advanceUntilIdle()
        val fb = vm.state.value.feedback as RenewFeedback.Error
        assertEquals(FeedbackKind.Refused, fb.kind)
        assertEquals("solde insuffisant", fb.serverMessage)
    }

    @Test
    fun panels_openChooseBackClose() = runTest {
        coEvery { repo.getAbonnement() } returns DsiResult.Success(abo())
        val vm = createVm()
        advanceUntilIdle()
        vm.openPanel()
        assertEquals(RenewPanel.ChooseMode, vm.state.value.panel)
        vm.chooseDsi()
        assertEquals(RenewPanel.ConfirmDsi, vm.state.value.panel)
        vm.backToChoice()
        assertEquals(RenewPanel.ChooseMode, vm.state.value.panel)
        vm.closePanel()
        assertEquals(RenewPanel.Closed, vm.state.value.panel)
    }
}
