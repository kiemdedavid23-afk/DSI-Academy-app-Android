package app.dsiacademy.academy.domain.member

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentWithdrawalsTest {

    private fun item(id: String, date: String?) = RetraitItem(
        idRetrait = id,
        statut = "pending",
        moyenPaiement = null,
        montantDsiDemande = 10.0,
        montantDsiNet = 10.0,
        montantFcfa = null,
        dateDemande = date,
    )

    @Test
    fun triDecroissant() {
        val list = listOf(
            item("a", "2026-01-01T00:00:00Z"),
            item("b", "2027-06-01T00:00:00Z"),
            item("c", "2025-01-01T00:00:00Z"),
        )
        val r = recentWithdrawals(list, limit = 5)
        assertEquals(listOf("b", "a", "c"), r.map { it.idRetrait })
    }

    @Test
    fun limiteCinq() {
        val list = (1..8).map { item("id$it", "2027-01-%02dT00:00:00Z".format(it)) }
        val r = recentWithdrawals(list, limit = 5)
        assertEquals(5, r.size)
        assertEquals("id8", r.first().idRetrait)
    }

    @Test
    fun moinsDeCinq() {
        val list = listOf(item("x", "2027-01-01T00:00:00Z"))
        assertEquals(1, recentWithdrawals(list).size)
    }

    @Test
    fun listeVide() {
        assertTrue(recentWithdrawals(emptyList()).isEmpty())
    }

    @Test
    fun datesAbsentesOuIllisibles_enFin() {
        val list = listOf(
            item("ok", "2027-01-01T00:00:00Z"),
            item("bad", "pas-une-date"),
            item("null", null),
            item("ok2", "2026-06-01T00:00:00Z"),
        )
        val r = recentWithdrawals(list, limit = 10)
        assertEquals(listOf("ok", "ok2", "bad", "null"), r.map { it.idRetrait })
    }

    @Test
    fun stabiliteDatesEgales() {
        val list = listOf(
            item("first", "2027-01-01T00:00:00Z"),
            item("second", "2027-01-01T00:00:00Z"),
            item("third", "2027-01-01T00:00:00Z"),
        )
        val r = recentWithdrawals(list, limit = 5)
        assertEquals(listOf("first", "second", "third"), r.map { it.idRetrait })
    }
}
