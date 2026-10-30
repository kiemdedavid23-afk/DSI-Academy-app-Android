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
import app.dsiacademy.academy.feature.member.pendingpayment.PendingPaymentScreen
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
                navController.navigate(Routes.PENDING_PAYMENT)
            },
        )
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

    composable(Routes.PENDING_PAYMENT) {
        val context = LocalContext.current
        PendingPaymentScreen(
            onBack = { navController.popBackStack() },
            onPaid = {
                navigateToLoginAfterPaymentSuccess(navController, context)
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
                navigateToLoginAfterPaymentSuccess(navController, context)
            },
        )
    }
}

/**
 * Toast de succès + pile [Welcome, Login].
 * Utilisé après paiement confirmé (navigateur ou écran pending-payment).
 */
private fun navigateToLoginAfterPaymentSuccess(
    navController: NavHostController,
    context: android.content.Context,
) {
    // Toast déjà affiché par l'appelant pour le navigateur ;
    // pour PendingPayment onPaid, on affiche le succès ici.
    if (navController.currentDestination?.route == Routes.PENDING_PAYMENT) {
        Toast.makeText(
            context,
            context.getString(R.string.payment_return_success),
            Toast.LENGTH_LONG,
        ).show()
    }
    navController.navigate(Routes.LOGIN) {
        popUpTo(Routes.WELCOME) { inclusive = false }
        launchSingleTop = true
    }
}
