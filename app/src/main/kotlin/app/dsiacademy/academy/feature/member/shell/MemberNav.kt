package app.dsiacademy.academy.feature.member.shell

/**
 * Navigation espace membre — alignée sur MAIN_HOUSES + MEMBER_ROOMS du site.
 */
data class MainHouse(
    val id: String,
    val label: String,
    val shortLabel: String,
    val route: String,
    val ready: Boolean = true,
)

data class MemberRoom(
    val id: String,
    val label: String,
    val route: String,
)

object MemberNav {
    val mainHouses = listOf(
        MainHouse("community", "Communauté", "Communauté", "house/community"),
        MainHouse("academy", "Academy", "Academy", "house/academy"),
        MainHouse("member-space", "Espace Membre", "Espace", "member/home"),
        MainHouse("shop", "Boutique DSI", "Boutique", "house/shop"),
        MainHouse("marketplace", "Marketplace", "Marketplace", "house/marketplace"),
    )

    val rooms = listOf(
        MemberRoom("home", "Tableau de bord", "member/home"),
        MemberRoom("subscription", "Mon abonnement", "member/subscription"),
        MemberRoom("dsi-holdings", "Mes DSI", "member/dsi"),
        MemberRoom("withdrawals", "Mes retraits", "member/withdrawals"),
        MemberRoom("payments", "Historique paiements", "member/payments"),
        MemberRoom("referral", "Parrainage", "member/referral"),
        MemberRoom("notifications", "Notifications", "member/notifications"),
        MemberRoom("profile", "Mon Profil", "member/profile"),
        MemberRoom("settings", "Paramètres", "member/settings"),
    )
}
