package app.dsiacademy.academy.data.member

import app.dsiacademy.academy.data.member.dto.toRenouvellementItems
import app.dsiacademy.academy.domain.member.MemberAbonnement
import app.dsiacademy.academy.domain.member.SubscriptionTone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemberAbonnementMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun renouvellements_tableauDeuxObjets() {
        val el = json.parseToJsonElement(
            """[{"date":"2026-01-01","statut":"ok","modePaiement":"dsi","nouvelleDateFin":"2027-01-01"},
               {"date":"2025-01-01","statut":null,"modePaiement":"fcfa","nouvelleDateFin":"2026-01-01"}]""",
        )
        val list = el.toRenouvellementItems()
        assertEquals(2, list.size)
        assertEquals("2026-01-01", list[0].date)
        assertEquals("ok", list[0].statut)
        assertEquals("dsi", list[0].modePaiement)
        assertEquals("2027-01-01", list[0].nouvelleDateFin)
        assertEquals("fcfa", list[1].modePaiement)
    }

    @Test
    fun renouvellements_tableauVide() {
        assertTrue(json.parseToJsonElement("[]").toRenouvellementItems().isEmpty())
    }

    @Test
    fun renouvellements_absentOuNull() {
        assertTrue(null.toRenouvellementItems().isEmpty())
        assertTrue(JsonNull.toRenouvellementItems().isEmpty())
    }

    @Test
    fun renouvellements_nombre_listeVide() {
        assertTrue(JsonPrimitive(3).toRenouvellementItems().isEmpty())
    }

    @Test
    fun renouvellements_texte_listeVide() {
        assertTrue(JsonPrimitive("trois").toRenouvellementItems().isEmpty())
    }

    @Test
    fun renouvellements_champsManquantsOuTypesInattendus() {
        val el = json.parseToJsonElement(
            """[{"date":12,"statut":{"x":1},"modePaiement":"dsi"},"pas-un-objet"]""",
        )
        val list = el.toRenouvellementItems()
        // un objet (champs illisibles → null) + une string ignorée
        assertEquals(1, list.size)
        assertEquals(null, list[0].date)
        assertEquals(null, list[0].statut)
        assertEquals("dsi", list[0].modePaiement)
    }

    @Test
    fun hasFormula() {
        fun abo(formule: String?) = MemberAbonnement(
            statut = null, dateDebut = null, dateFin = null,
            formule = formule, modePaiement = null,
        )
        assertTrue(abo("Premium").hasFormula)
        assertFalse(abo("").hasFormula)
        assertFalse(abo("   ").hasFormula)
        assertFalse(abo(null).hasFormula)
    }

    @Test
    fun tone_troisEtats() {
        val active = MemberAbonnement(
            statut = null, dateDebut = null, dateFin = "2099-12-31T00:00:00Z",
            formule = "X", modePaiement = null,
        )
        assertEquals(SubscriptionTone.Active, active.tone)

        val expired = MemberAbonnement(
            statut = null, dateDebut = null, dateFin = "2020-01-01T00:00:00Z",
            formule = "X", modePaiement = null,
        )
        assertEquals(SubscriptionTone.Warning, expired.tone)

        val none = MemberAbonnement(
            statut = "inactive", dateDebut = null, dateFin = null,
            formule = null, modePaiement = null,
        )
        assertEquals(SubscriptionTone.Neutral, none.tone)
    }
}
