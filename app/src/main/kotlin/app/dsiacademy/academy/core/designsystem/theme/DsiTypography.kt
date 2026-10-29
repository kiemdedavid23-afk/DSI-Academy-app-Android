package app.dsiacademy.academy.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R

/**
 * Typographie strictement alignée sur le site :
 * - Fraunces (serif « royal ») : titres, salutations, grands chiffres.
 *   Jamais < 14 sp, jamais sur boutons / formulaires / tableaux.
 * - Manrope (sans) : police par défaut de toute l'app.
 * - IBM Plex Mono : petits libellés techniques.
 *
 * Les fichiers de police doivent être placés dans res/font/ :
 *   fraunces_regular, fraunces_medium, fraunces_semibold, fraunces_bold
 *   (+ italiques si disponibles)
 *   manrope_regular, manrope_medium, manrope_semibold, manrope_bold, manrope_extrabold
 *   ibm_plex_mono_medium
 *
 * En attendant le téléchargement des polices, on retombe sur les familles système
 * avec les mêmes graisses (le rendu final doit embarquer les polices du site).
 */

val FrauncesFamily = FontFamily(
    Font(R.font.fraunces_regular, FontWeight.Normal),
    Font(R.font.fraunces_medium, FontWeight.Medium),
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
    Font(R.font.fraunces_bold, FontWeight.Bold),
)

val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

val IbmPlexMonoFamily = FontFamily(
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
)

@Immutable
data class DsiTypography(
    // Titres royaux (Fraunces)
    val displayLarge: TextStyle = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    val displayMedium: TextStyle = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    val titleLarge: TextStyle = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    val titleSmall: TextStyle = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    // Corps (Manrope)
    val bodyLarge: TextStyle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    val labelLarge: TextStyle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    val labelMedium: TextStyle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    val labelSmall: TextStyle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),

    // Technique (IBM Plex Mono)
    val mono: TextStyle = TextStyle(
        fontFamily = IbmPlexMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

val DefaultDsiTypography = DsiTypography()
