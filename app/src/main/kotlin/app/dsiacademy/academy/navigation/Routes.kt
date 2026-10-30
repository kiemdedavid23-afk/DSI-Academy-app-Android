package app.dsiacademy.academy.navigation

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER_BASE = "register"
    const val FORGOT_PASSWORD = "forgot-password"
    const val MEMBER_HOME = "member/home"
    const val MEMBER_SUBSCRIPTION = "member/subscription"
    const val MEMBER_DSI = "member/dsi"
    const val MEMBER_WITHDRAWALS = "member/withdrawals"
    const val MEMBER_PAYMENTS = "member/payments"
    const val MEMBER_REFERRAL = "member/referral"
    const val MEMBER_NOTIFICATIONS = "member/notifications"
    const val MEMBER_PROFILE = "member/profile"
    const val MEMBER_SETTINGS = "member/settings"
    const val HOUSE_COMMUNITY = "house/community"
    const val HOUSE_ACADEMY = "house/academy"
    const val HOUSE_CATALOG = "house/catalog/{type}?title={title}"
    const val HOUSE_SHOP = "house/shop"
    const val HOUSE_MARKETPLACE = "house/marketplace"
    const val BROWSER = "browser/{linkId}?ref={ref}"
    const val CONTENT_DETAIL = "content/detail/{contentId}?title={title}&type={type}"
    const val CONTENT_PLAYER = "content/player/{contentId}?title={title}&type={type}"

    fun browser(linkId: String, ref: String? = null): String =
        if (ref.isNullOrBlank()) "browser/$linkId" else "browser/$linkId?ref=$ref"

    fun register(ref: String? = null): String =
        if (ref.isNullOrBlank()) "register" else "register?ref=$ref"

    fun contentDetail(contentId: String, title: String, type: String): String {
        val encoded = android.net.Uri.encode(title)
        return "content/detail/$contentId?title=$encoded&type=$type"
    }

    fun contentPlayer(contentId: String, title: String, type: String): String {
        val encoded = android.net.Uri.encode(title)
        return "content/player/$contentId?title=$encoded&type=$type"
    }

    fun houseCatalog(type: String, title: String): String {
        val encoded = android.net.Uri.encode(title)
        return "house/catalog/$type?title=$encoded"
    }
}
