package app.dsiacademy.academy.core.config

/**
 * Identique à src/app/config/socialLinks.js du site.
 * href null = « à venir » (non cliquable).
 */
data class SocialLink(val id: String, val label: String, val href: String?)

object SocialLinks {
    val all = listOf(
        SocialLink("facebook", "Facebook", "https://www.facebook.com/share/1GVL3d7duA/"),
        SocialLink("whatsapp", "WhatsApp", "https://wa.me/22678627530"),
        SocialLink("tiktok", "TikTok", "https://www.tiktok.com/@dsi169"),
        SocialLink("telegram", "Telegram", null),
    )
}
