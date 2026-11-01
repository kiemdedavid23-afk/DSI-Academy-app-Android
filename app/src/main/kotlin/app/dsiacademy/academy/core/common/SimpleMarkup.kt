package app.dsiacademy.academy.core.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Interprète un sous-ensemble minimal de balises : uniquement &lt;b&gt;…&lt;/b&gt;.
 * Les autres balises sont ignorées (contenu conservé).
 */
object SimpleMarkup {
    private val boldRegex = Regex("""(?is)<b>(.*?)</b>""")

    fun toAnnotatedString(raw: String): AnnotatedString {
        return buildAnnotatedString {
            var index = 0
            for (match in boldRegex.findAll(raw)) {
                val start = match.range.first
                if (start > index) {
                    append(raw.substring(index, start))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                index = match.range.last + 1
            }
            if (index < raw.length) {
                append(raw.substring(index))
            }
        }
    }
}
