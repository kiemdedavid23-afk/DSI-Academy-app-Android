package app.dsiacademy.academy.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
        BrowserScreen(
            onClose = { navController.popBackStack() },
            onPaymentReturn = { status, token ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("browser_payment_status", status.name)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("browser_payment_token", token)
                navController.popBackStack()
            },
        )
    }
}
