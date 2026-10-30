package app.dsiacademy.academy.navigation

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.common.PaymentReturnStatus
import app.dsiacademy.academy.core.config.SiteLink
import app.dsiacademy.academy.feature.browser.BrowserScreen
import app.dsiacademy.academy.feature.member.login.ForgotPasswordScreen
import app.dsiacademy.academy.feature.member.login.LoginScreen
import app.dsiacademy.academy.feature.member.register.RegisterScreen
import app.dsiacademy.academy.feature.public.SplashScreen
import app.dsiacademy.academy.feature.public.WelcomeScreen

fun NavGraphBuilder.publicNavGraph(
    navController: NavHostController,
    hasSession: () -> Boolean,
) {
    composable(Routes.SPLASH) {
        SplashScreen(
            onFinished = {
                if (hasSession()) {
                    navController.navigate(Routes.MEMBER_HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            },
        )
    }

    composable(Routes.WELCOME) {
        WelcomeScreen(
            onLogin = { navController.navigate(Routes.LOGIN) },
            onRegister = {
                navController.navigate(Routes.browser(SiteLink.INSCRIPTION.id))
            },
        )
    }

    composable(Routes.LOGIN) {
        LoginScreen(
            onNavigateMemberHome = {
                navController.navigate(Routes.MEMBER_HOME) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateRegister = {
                navController.navigate(Routes.browser(SiteLink.INSCRIPTION.id))
            },
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
            },
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

    composable(
        route = Routes.BROWSER,
        arguments = listOf(
            navArgument("linkId") { type = NavType.StringType },
            navArgument("ref") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        val context = LocalContext.current
        BrowserScreen(
            onClose = { navController.popBackStack() },
            onPaymentReturn = { status, _ ->
                val messageRes = when (status) {
                    PaymentReturnStatus.Success -> R.string.payment_return_success
                    PaymentReturnStatus.Failed -> R.string.payment_return_error
                    PaymentReturnStatus.Unknown -> R.string.payment_return_pending
                }
                Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_LONG).show()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.WELCOME) { inclusive = false }
                    launchSingleTop = true
                }
            },
        )
    }
}
