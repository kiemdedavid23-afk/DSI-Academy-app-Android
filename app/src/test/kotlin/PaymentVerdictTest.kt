package app.dsiacademy.academy.domain.member

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentVerdictTest {

    @Test
    fun active_minuscule() {
        assertEquals(PaymentVerdict.Active, paymentVerdictFromStatus("active"))
    }

    @Test
    fun active_casseIgnoree() {
        assertEquals(PaymentVerdict.Active, paymentVerdictFromStatus("ACTIVE"))
        assertEquals(PaymentVerdict.Active, paymentVerdictFromStatus(" Active "))
    }

    @Test
    fun attente_retourneWaiting() {
        assertEquals(PaymentVerdict.Waiting, paymentVerdictFromStatus("attente"))
        assertEquals(PaymentVerdict.Waiting, paymentVerdictFromStatus("ATTENTE"))
    }

    @Test
    fun refuse_retourneRefused() {
        assertEquals(PaymentVerdict.Refused, paymentVerdictFromStatus("refuse"))
        assertEquals(PaymentVerdict.Refused, paymentVerdictFromStatus("REFUSE"))
    }

    @Test
    fun introuvable_retourneRefused() {
        assertEquals(PaymentVerdict.Refused, paymentVerdictFromStatus("introuvable"))
    }

    @Test
    fun valeurInconnue_retourneWaiting() {
        assertEquals(PaymentVerdict.Waiting, paymentVerdictFromStatus("pending"))
        assertEquals(PaymentVerdict.Waiting, paymentVerdictFromStatus("ok"))
        assertEquals(PaymentVerdict.Waiting, paymentVerdictFromStatus(""))
        assertEquals(PaymentVerdict.Waiting, paymentVerdictFromStatus(null))
    }
}
