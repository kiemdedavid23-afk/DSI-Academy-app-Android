package app.dsiacademy.academy.domain.member

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentModeTest {

    @Test
    fun nullOuVide_none() {
        assertEquals(PaymentModeKind.None, paymentModeOf(null).kind)
        assertEquals(PaymentModeKind.None, paymentModeOf("").kind)
    }

    @Test
    fun fcfa_exact() {
        val m = paymentModeOf("fcfa")
        assertEquals(PaymentModeKind.Fcfa, m.kind)
        assertEquals("fcfa", m.raw)
    }

    @Test
    fun dsi_exact() {
        assertEquals(PaymentModeKind.Dsi, paymentModeOf("dsi").kind)
    }

    @Test
    fun fondateur_exact() {
        assertEquals(PaymentModeKind.Founder, paymentModeOf("fondateur").kind)
    }

    @Test
    fun casseDifferente_other() {
        val m = paymentModeOf("FCFA")
        assertEquals(PaymentModeKind.Other, m.kind)
        assertEquals("FCFA", m.raw)
    }

    @Test
    fun valeurInconnue_other() {
        val m = paymentModeOf("crypto")
        assertEquals(PaymentModeKind.Other, m.kind)
        assertEquals("crypto", m.raw)
    }
}
