package app.dsiacademy.academy.feature.member.home

import app.dsiacademy.academy.core.common.AppError
import app.dsiacademy.academy.core.common.DsiResult
import app.dsiacademy.academy.core.session.LocalContentStores
import app.dsiacademy.academy.domain.member.MemberAbonnement
import app.dsiacademy.academy.domain.member.MemberDashboard
import app.dsiacademy.academy.domain.member.MemberProfil
import app.dsiacademy.academy.domain.member.MemberRepository
import app.dsiacademy.academy.domain.member.MemberSolde
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class MemberHomeViewModelTest {

    private lateinit var repository: MemberRepository
    private lateinit var localContent: LocalContentStores

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        localContent = mockk {
            every { continueFlow } returns flowOf(null)
            every { favoritesFlow } returns flowOf(emptyList())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun profil(prenom: String = "", nom: String = "") = MemberProfil(
        prenom = prenom,
        nom = nom,
        identifiantDsi = "ID",
        niveau = "",
        codeParrainage = "",
        statut = "active",
        dateInscription = null,
        dateActivation = null,
        whatsapp = null,
        email = null,
        pays = null,
    )

    private fun dashboard(
        prenom: String = "Awa",
        nom: String = "Traore",
        solde: MemberSolde? = MemberSolde(null, 12.5, 100.0, null),
        abonnement: MemberAbonnement? = null,
    ) = MemberDashboard(
        profil = profil(prenom, nom),
        solde = solde,
        abonnement = abonnement,
        retraits = emptyList(),
    )

    private fun createVm(): MemberHomeViewModel =
        MemberHomeViewModel(repository, localContent)

    @Test
    fun prenom_utilisePrenom() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Success(dashboard(prenom = "Awa"))
        val vm = createVm()
        advanceUntilIdle()
        assertEquals("Awa", vm.state.value.prenom)
    }

    @Test
    fun prenom_repliSurNom() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Success(dashboard(prenom = "", nom = "Traore"))
        val vm = createVm()
        advanceUntilIdle()
        assertEquals("Traore", vm.state.value.prenom)
    }

    @Test
    fun prenom_repliNul() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Success(dashboard(prenom = "", nom = ""))
        val vm = createVm()
        advanceUntilIdle()
        assertNull(vm.state.value.prenom)
    }

    @Test
    fun soldeAbsent_afficheZero() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Success(dashboard(solde = null))
        val vm = createVm()
        advanceUntilIdle()
        assertEquals("0", vm.state.value.soldeDsi)
        assertEquals("0", vm.state.value.totalGagne)
    }

    @Test
    fun abonnement_troisEtats() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Success(
            dashboard(
                abonnement = MemberAbonnement(
                    statut = null,
                    dateDebut = null,
                    dateFin = "2099-12-31T00:00:00Z",
                    formule = null,
                    modePaiement = null,
                    renouvellements = null,
                ),
            ),
        )
        var vm = createVm()
        advanceUntilIdle()
        assertEquals(SubscriptionState.Active, vm.state.value.abonnementState)
        assertTrue(vm.state.value.abonnementFin != null)

        coEvery { repository.getDashboard() } returns DsiResult.Success(
            dashboard(
                abonnement = MemberAbonnement(
                    statut = null,
                    dateDebut = null,
                    dateFin = "2020-01-01T00:00:00Z",
                    formule = null,
                    modePaiement = null,
                    renouvellements = null,
                ),
            ),
        )
        vm = createVm()
        advanceUntilIdle()
        assertEquals(SubscriptionState.Expired, vm.state.value.abonnementState)

        coEvery { repository.getDashboard() } returns DsiResult.Success(
            dashboard(abonnement = null),
        )
        vm = createVm()
        advanceUntilIdle()
        assertEquals(SubscriptionState.None, vm.state.value.abonnementState)
        assertNull(vm.state.value.abonnementFin)
    }

    @Test
    fun sessionExpiree() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Error(AppError.SessionExpired())
        val vm = createVm()
        advanceUntilIdle()
        assertTrue(vm.state.value.sessionExpired)
        assertFalse(vm.state.value.loadFailed)
    }

    @Test
    fun autreErreur_loadFailed_messageServeur() = runTest {
        coEvery { repository.getDashboard() } returns DsiResult.Error(
            AppError.Network(message = "Serveur indisponible"),
        )
        val vm = createVm()
        advanceUntilIdle()
        assertFalse(vm.state.value.sessionExpired)
        assertTrue(vm.state.value.loadFailed)
        assertEquals("Serveur indisponible", vm.state.value.errorMessage)
    }

    @Test
    fun rechargement_apresErreur() = runTest {
        coEvery { repository.getDashboard() } returnsMany listOf(
            DsiResult.Error(AppError.Network()),
            DsiResult.Success(dashboard()),
        )
        val vm = createVm()
        advanceUntilIdle()
        assertTrue(vm.state.value.loadFailed)
        vm.charger()
        advanceUntilIdle()
        assertFalse(vm.state.value.loadFailed)
        assertEquals("Awa", vm.state.value.prenom)
    }
}
