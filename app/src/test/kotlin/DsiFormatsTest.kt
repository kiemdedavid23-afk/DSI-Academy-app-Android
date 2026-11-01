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


    @Test
    fun formatLongDate_fr_jour12_mars_utc() {
        val ms = DsiFormats.parseIsoMs("2027-03-12")!!
        val s = DsiFormats.formatLongDate(ms, english = false, zone = TimeZone.getTimeZone("UTC"))
        assertEquals("12 mars 2027", s)
    }

    @Test
    fun formatLongDate_fr_jour1_janvier_utc() {
        val ms = DsiFormats.parseIsoMs("2027-01-01")!!
        val s = DsiFormats.formatLongDate(ms, english = false, zone = TimeZone.getTimeZone("UTC"))
        assertEquals("1 janvier 2027", s)
    }

    @Test
    fun formatLongDate_en_mars_utc() {
        val ms = DsiFormats.parseIsoMs("2027-03-12")!!
        val s = DsiFormats.formatLongDate(ms, english = true, zone = TimeZone.getTimeZone("UTC"))
        assertEquals("March 12, 2027", s)
    }

    @Test
    fun formatLongDate_en_decembre_utc() {
        val ms = DsiFormats.parseIsoMs("2027-12-01")!!
        val s = DsiFormats.formatLongDate(ms, english = true, zone = TimeZone.getTimeZone("UTC"))
        assertEquals("December 1, 2027", s)
    }

    @Test
    fun formatGroupedInt_fr() {
        assertEquals("0", DsiFormats.formatGroupedInt(0, english = false))
        assertEquals("999", DsiFormats.formatGroupedInt(999, english = false))
        assertEquals("2 500", DsiFormats.formatGroupedInt(2500, english = false))
        assertEquals("1 234 567", DsiFormats.formatGroupedInt(1_234_567, english = false))
    }

    @Test
    fun formatGroupedInt_en() {
        assertEquals("0", DsiFormats.formatGroupedInt(0, english = true))
        assertEquals("999", DsiFormats.formatGroupedInt(999, english = true))
        assertEquals("2,500", DsiFormats.formatGroupedInt(2500, english = true))
        assertEquals("1,234,567", DsiFormats.formatGroupedInt(1_234_567, english = true))
    }
}
