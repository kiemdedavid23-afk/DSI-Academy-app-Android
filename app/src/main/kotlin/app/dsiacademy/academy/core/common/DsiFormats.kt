package app.dsiacademy.academy.core.common

import java.math.BigDecimal
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Formats partagés (Kotlin pur, API SDK 24+).
 * Alignés sur le comportement JS du site (Date / toLocaleDateString / Number).
 */
object DsiFormats {

    /**
     * Parse une date ISO partielle comme `new Date(...)` côté site.
     * @return epoch ms UTC, ou null si absent / illisible.
     */
    fun parseIsoMs(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()
        // Tronquer les fractions de seconde au-delà de 3 chiffres
        val normalized = normalizeFractionalSeconds(s)

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
        )
        for (pattern in patterns) {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val pos = ParsePosition(0)
            val date = sdf.parse(normalized, pos)
            if (date != null && pos.index == normalized.length) {
                return date.time
            }
        }
        return null
    }

    /**
     * Date courte : FR `jj/MM/aaaa`, EN `M/j/aaaa` (comme toLocaleDateString).
     */
    fun formatShortDate(
        epochMs: Long,
        english: Boolean,
        zone: TimeZone = TimeZone.getDefault(),
    ): String {
        val pattern = if (english) "M/d/yyyy" else "dd/MM/yyyy"
        val sdf = SimpleDateFormat(pattern, if (english) Locale.US else Locale.FRANCE).apply {
            isLenient = false
            timeZone = zone
        }
        return sdf.format(epochMs)
    }

    /**
     * Affichage nombre comme JS : pas de notation scientifique, pas de `.0` inutile.
     */
    fun formatDsiAmount(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }

    private fun normalizeFractionalSeconds(s: String): String {
        // …ss.123456Z ou …ss.123456+01:00 → tronquer la fraction à 3 chiffres
        val regex = Regex("""(\.\d{3})\d+""")
        return regex.replace(s) { it.groupValues[1] }
    }
}
