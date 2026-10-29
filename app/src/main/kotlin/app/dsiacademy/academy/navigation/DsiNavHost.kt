package app.dsiacademy.academy.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.core.session.SessionEvent
import app.dsiacademy.academy.core.session.SessionStore
import app.dsiacademy.academy.feature.member.academy.AcademyScreen
import app.dsiacademy.academy.feature.member.academy.HouseCatalogScreen
import app.dsiacademy.academy.feature.content.ContentDetailScreen
import app.dsiacademy.academy.feature.content.ContentPlayerScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import app.dsiacademy.academy.feature.member.dsi.DsiScreen
import app.dsiacademy.academy.feature.member.payments.PaymentsScreen
import app.dsiacademy.academy.feature.member.subscription.SubscriptionScreen
import app.dsiacademy.academy.feature.member.withdrawals.WithdrawalsScreen
import app.dsiacademy.academy.feature.member.home.MemberHomeScreen
import app.dsiacademy.academy.feature.member.profile.ProfileScreen
import app.dsiacademy.academy.feature.member.referral.ReferralScreen
import app.dsiacademy.academy.feature.member.login.ForgotPasswordScreen
import app.dsiacademy.academy.feature.member.login.LoginScreen
import app.dsiacademy.academy.feature.member.register.RegisterScreen
import app.dsiacademy.academy.feature.member.shell.MemberShell
import app.dsiacademy.academy.feature.public.SplashScreen
import app.dsiacademy.academy.feature.public.WelcomeScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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

    fun register(ref: String? = null): String =
        if (ref.isNullOrBlank()) "register" else "register?ref=$ref"
}

@HiltViewModel
class SessionNavViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {
    val session = sessionStore.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), sessionStore.getSession())

    val events = sessionStore.events

    fun hasSession(): Boolean = sessionStore.hasSession()

    fun logout() {
        sessionStore.clearSession()
    }
}

@Composable
fun DsiNavHost(
    sessionVm: SessionNavViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Routes.SPLASH

    LaunchedEffect(Unit) {
        sessionVm.events.collect { event ->
            when (event) {
                is SessionEvent.Expired -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    fun goMember(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun logout() {
        sessionVm.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    if (sessionVm.hasSession()) {
                        navController.navigate(Routes.MEMBER_HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.register()) },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateMemberHome = {
                    navController.navigate(Routes.MEMBER_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateRegister = { navController.navigate(Routes.register()) },
                onNavigateForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onNavigateInscriptionPaiement = { _, _, _ ->
                    navController.navigate(Routes.register())
                },
            )
        }

        composable(
            route = "register?ref={ref}",
            arguments = listOf(
                navArgument("ref") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
        ) { entry ->
            RegisterScreen(
                codeParrain = entry.arguments?.getString("ref"),
                onNavigateLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
            )
        }

        composable(Routes.REGISTER_BASE) {
            RegisterScreen(onNavigateLogin = { navController.navigate(Routes.LOGIN) })
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackLogin = { navController.popBackStack() },
                onBackHome = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
            )
        }

        // —— Espace membre (shell) ——
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
            composable(route) {
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
                                val encoded = android.net.Uri.encode(title)
                                navController.navigate("house/catalog/$type?title=$encoded")
                            },
                        )
                        else -> MemberPlaceholder(title = routeLabel(route))
                    }
                }
            }
        }
    }
}

        composable(
            route = "house/catalog/{type}?title={title}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "Catalogue" },
            ),
        ) {
            MemberShell(
                currentRoute = currentRoute,
                onNavigate = { dest -> goMember(dest) },
                onLogout = { logout() },
            ) {
                HouseCatalogScreen(
                    onOpenContent = { id, title, type ->
                        val t = android.net.Uri.encode(title)
                        navController.navigate("content/detail/$id?title=$t&type=$type")
                    },
                )
            }
        }

        composable(
            route = "content/player/{contentId}?title={title}&type={type}",
            arguments = listOf(
                navArgument("contentId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "Contenu" },
                navArgument("type") { type = NavType.StringType; defaultValue = "book" },
            ),
        ) {
            ContentPlayerScreen(onClose = { navController.popBackStack() })
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
