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


    private val MONTHS_FR = arrayOf(
        "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre",
    )
    private val MONTHS_EN = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    /**
     * Date longue : FR « 12 mars 2027 », EN « March 12, 2027 »
     * (comme toLocaleDateString day/month:long/year).
     */
    fun formatLongDate(
        epochMs: Long,
        english: Boolean,
        zone: TimeZone = TimeZone.getDefault(),
    ): String {
        val cal = java.util.Calendar.getInstance(zone).apply { timeInMillis = epochMs }
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month = cal.get(java.util.Calendar.MONTH) // 0-based
        val year = cal.get(java.util.Calendar.YEAR)
        return if (english) {
            "${MONTHS_EN[month]} $day, $year"
        } else {
            "$day ${MONTHS_FR[month]} $year"
        }
    }

    /**
     * Entier groupé : FR espace fine insécable U+202F, EN virgule.
     */
    fun formatGroupedInt(value: Long, english: Boolean): String {
        val negative = value < 0
        var n = if (negative) -value else value
        if (n < 1000) {
            return if (negative) "-$n" else n.toString()
        }
        val sep = if (english) "," else " "
        val parts = ArrayList<String>()
        while (n > 0) {
            val chunk = (n % 1000).toInt()
            n /= 1000
            if (n > 0) {
                parts.add(chunk.toString().padStart(3, '0'))
            } else {
                parts.add(chunk.toString())
            }
        }
        val body = parts.reversed().joinToString(sep)
        return if (negative) "-$body" else body
    }

    private fun normalizeFractionalSeconds(s: String): String {
        // …ss.123456Z ou …ss.123456+01:00 → tronquer la fraction à 3 chiffres
        val regex = Regex("""(\.\d{3})\d+""")
        return regex.replace(s) { it.groupValues[1] }
    }
}
