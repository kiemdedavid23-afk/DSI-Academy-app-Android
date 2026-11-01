package app.dsiacademy.academy.core.common

import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DsiFormatsTest {

    @Test
    fun parseIsoMs_dateSeule() {
        val ms = DsiFormats.parseIsoMs("2027-03-12")
        assertEquals(1_173_657_600_000L, ms) // 2027-03-12T00:00:00Z
    }

    @Test
    fun parseIsoMs_avecZ() {
        val ms = DsiFormats.parseIsoMs("2027-03-12T10:00:00Z")
        assertEquals(1_173_693_600_000L, ms)
    }

    @Test
    fun parseIsoMs_avecMillisZ() {
        val ms = DsiFormats.parseIsoMs("2027-03-12T10:00:00.000Z")
        assertEquals(1_173_693_600_000L, ms)
    }

    @Test
    fun parseIsoMs_fractionLongueTronquee() {
        val ms = DsiFormats.parseIsoMs("2027-03-12T10:00:00.123456Z")
        assertEquals(1_173_693_600_123L, ms)
    }

    @Test
    fun parseIsoMs_offset() {
        val ms = DsiFormats.parseIsoMs("2027-03-12T10:00:00+01:00")
        assertEquals(1_173_690_000_000L, ms) // 09:00 UTC
    }

    @Test
    fun parseIsoMs_invalide() {
        assertNull(DsiFormats.parseIsoMs(null))
        assertNull(DsiFormats.parseIsoMs(""))
        assertNull(DsiFormats.parseIsoMs("   "))
        assertNull(DsiFormats.parseIsoMs("not-a-date"))
    }

    @Test
    fun formatShortDate_fr_utc() {
        val ms = DsiFormats.parseIsoMs("2027-03-12")!!
        val s = DsiFormats.formatShortDate(ms, english = false, zone = TimeZone.getTimeZone("UTC"))
        assertEquals("12/03/2027", s)
    }

    @Test
    fun formatShortDate_en_utc() {
        val ms = DsiFormats.parseIsoMs("2027-03-12")!!
        val s = DsiFormats.formatShortDate(ms, english = true, zone = TimeZone.getTimeZone("UTC"))
        assertEquals("3/12/2027", s)
    }

    @Test
    fun formatDsiAmount_cas() {
        assertEquals("12", DsiFormats.formatDsiAmount(12.0))
        assertEquals("12.5", DsiFormats.formatDsiAmount(12.5))
        assertEquals("10000000", DsiFormats.formatDsiAmount(10_000_000.0))
        assertEquals("0", DsiFormats.formatDsiAmount(0.0))
        assertEquals("-3.5", DsiFormats.formatDsiAmount(-3.5))
        assertEquals("0", DsiFormats.formatDsiAmount(Double.NaN))
        assertEquals("0", DsiFormats.formatDsiAmount(Double.POSITIVE_INFINITY))
    }
}
