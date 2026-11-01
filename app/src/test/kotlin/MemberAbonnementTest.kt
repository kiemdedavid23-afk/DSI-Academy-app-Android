package app.dsiacademy.academy.domain.member

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemberAbonnementTest {

    private fun abonnement(
        statut: String? = null,
        dateDebut: String? = null,
        dateFin: String? = null,
    ) = MemberAbonnement(
        statut = statut,
        dateDebut = dateDebut,
        dateFin = dateFin,
        formule = null,
        modePaiement = null,
        renouvellements = null,
    )

    @Test
    fun etatAffiche_dateFuture_active() {
        val a = abonnement(dateFin = "2099-12-31T00:00:00Z")
        assertEquals("active", a.etatAffiche)
        assertTrue((a.joursRestants ?: 0) > 0)
    }

    @Test
    fun etatAffiche_datePassee_expired() {
        val a = abonnement(dateFin = "2020-01-01T00:00:00Z")
        assertEquals("expired", a.etatAffiche)
        assertEquals(0, a.joursRestants)
    }

    @Test
    fun etatAffiche_dateIllisible_statutActive() {
        val a = abonnement(statut = "active", dateFin = "pas-une-date")
        assertEquals("active", a.etatAffiche)
        assertNull(a.joursRestants)
    }

    @Test
    fun etatAffiche_dateAbsente_statutAutre_none() {
        val a = abonnement(statut = "inactive", dateFin = null)
        assertEquals("none", a.etatAffiche)
    }
}
