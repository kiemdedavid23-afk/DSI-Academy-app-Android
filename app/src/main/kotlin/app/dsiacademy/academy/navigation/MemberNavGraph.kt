package app.dsiacademy.academy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.feature.member.academy.AcademyScreen
import app.dsiacademy.academy.feature.member.dsi.DsiScreen
import app.dsiacademy.academy.feature.member.home.MemberHomeScreen
import app.dsiacademy.academy.feature.member.payments.PaymentsScreen
import app.dsiacademy.academy.feature.member.profile.ProfileScreen
import app.dsiacademy.academy.feature.member.referral.ReferralScreen
import app.dsiacademy.academy.feature.member.shell.MemberShell
import app.dsiacademy.academy.feature.member.subscription.SubscriptionScreen
import app.dsiacademy.academy.feature.member.withdrawals.WithdrawalsScreen

fun NavGraphBuilder.memberNavGraph(
    navController: NavHostController,
    goMember: (String) -> Unit,
    logout: () -> Unit,
) {
    val memberRoutes = listOf(
        Routes.MEMBER_HOME,
        Routes.MEMBER_SUBSCRIPTION,
        Routes.MEMBER_DSI,
        Routes.MEMBER_WITHDRAWALS,
        Routes.MEMBER_PAYMENTS,
        Routes.MEMBER_REFERRAL,
        Routes.MEMBER_NOTIFICATIONS,
        Routes.MEMBER_PROFILE,
        Routes.MEMBER_SETTINGS,
        Routes.HOUSE_COMMUNITY,
        Routes.HOUSE_ACADEMY,
        Routes.HOUSE_SHOP,
        Routes.HOUSE_MARKETPLACE,
    )

    memberRoutes.forEach { route ->
        composable(route) { entry ->
            val currentRoute = entry.destination.route ?: route
            MemberShell(
                currentRoute = currentRoute,
                onNavigate = { dest -> goMember(dest) },
                onLogout = { logout() },
            ) {
                when (route) {
                    Routes.MEMBER_HOME -> MemberHomeScreen(onNavigate = { goMember(it) })
                    Routes.MEMBER_PROFILE -> ProfileScreen()
                    Routes.MEMBER_DSI -> DsiScreen()
                    Routes.MEMBER_REFERRAL -> ReferralScreen()
                    Routes.MEMBER_WITHDRAWALS -> WithdrawalsScreen()
                    Routes.MEMBER_PAYMENTS -> PaymentsScreen()
                    Routes.MEMBER_SUBSCRIPTION -> SubscriptionScreen()
                    Routes.HOUSE_ACADEMY -> AcademyScreen(
                        onOpenHouse = { type, title ->
                            navController.navigate(Routes.houseCatalog(type, title))
                        },
                    )
                    else -> MemberPlaceholder(title = routeLabel(route))
                }
            }
        }
    }
}

private fun routeLabel(route: String): String = when (route) {
    Routes.MEMBER_SUBSCRIPTION -> "Mon abonnement"
    Routes.MEMBER_DSI -> "Mes DSI"
    Routes.MEMBER_WITHDRAWALS -> "Mes retraits"
    Routes.MEMBER_PAYMENTS -> "Historique paiements"
    Routes.MEMBER_REFERRAL -> "Parrainage"
    Routes.MEMBER_NOTIFICATIONS -> "Notifications"
    Routes.MEMBER_PROFILE -> "Mon Profil"
    Routes.MEMBER_SETTINGS -> "Paramètres"
    Routes.HOUSE_COMMUNITY -> "Communauté"
    Routes.HOUSE_ACADEMY -> "Academy"
    Routes.HOUSE_SHOP -> "Boutique DSI"
    Routes.HOUSE_MARKETPLACE -> "Marketplace"
    else -> route
}

@Composable
private fun MemberPlaceholder(title: String) {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$title\n(bientôt)",
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF475569),
        )
    }
}
