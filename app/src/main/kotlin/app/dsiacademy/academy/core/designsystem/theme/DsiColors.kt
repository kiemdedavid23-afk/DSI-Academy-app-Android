package app.dsiacademy.academy.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens couleur extraits strictement de `src/assets/styles/global.css` du site
 * (charte v3.0 institutionnelle : 70 % neutres / 20 % sombre / 5 % or).
 * Aucune valeur inventée.
 */
object DsiColors {

    // —— Thème clair (défaut :root) ——
    val BgApp = Color(0xFFF8FAFC)
    val BgSurface = Color(0xFFFFFFFF)
    val BgSurfaceAlt = Color(0xFFF1F5F9)
    val BgDarkPremium = Color(0xFF0F172A)
    val BgDarkCard = Color(0xFF1E293B)

    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextMuted = Color(0xFF94A3B8)
    val TextOnDark = Color(0xFFF8FAFC)

    val GoldPrestige = Color(0xFFD4AF37)
    val GoldLight = Color(0xFFFDF6E2)
    val GoldBorder = Color(0x59D4AF37) // rgba(212, 175, 55, 0.35)

    val BluePrimary = Color(0xFF1E40AF)
    val BlueLight = Color(0xFFEFF6FF)
    val BorderLight = Color(0xFFE2E8F0)

    // —— Thème sombre (html[data-theme="dark"]) ——
    val DarkBgApp = Color(0xFF0B1120)
    val DarkBgSurface = Color(0xFF111827)
    val DarkBgSurfaceAlt = Color(0xFF1E293B)
    val DarkBgDarkPremium = Color(0xFF020617)
    val DarkBgDarkCard = Color(0xFF0F172A)

    val DarkTextPrimary = Color(0xFFF8FAFC)
    val DarkTextSecondary = Color(0xFFCBD5E1)
    val DarkTextMuted = Color(0xFF94A3B8)
    val DarkBorderLight = Color(0xFF334155)
    val DarkBlueLight = Color(0xFF1E3A5F)

    // —— Verre dépoli (global.css) ——
    val GlassBgLight = Color(0xDBFFFFFF) // rgba(255,255,255,0.86)
    val GlassBorderLight = Color(0x8CFFFFFF) // rgba(255,255,255,0.55)
    val GlassBgDark = Color(0xC70F172A) // rgba(15,23,42,0.78)
    val GlassBorderDark = Color(0x4094A3B8) // rgba(148,163,184,0.25)

    // —— Sémantiques (fréquemment utilisés dans les CSS d'écran du site) ——
    val Error = Color(0xFFB91C1C)
    val ErrorSoft = Color(0xFFFCA5A5)
    val Success = Color(0xFF15803D)
    val SuccessSoft = Color(0xFF86EFAC)
    val Warning = Color(0xFFB45309)
    val Info = Color(0xFF2563EB)
}
