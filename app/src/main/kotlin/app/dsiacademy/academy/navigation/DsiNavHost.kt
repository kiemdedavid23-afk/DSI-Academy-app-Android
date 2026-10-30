package app.dsiacademy.academy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import app.dsiacademy.academy.core.session.SessionEvent

@Composable
fun DsiNavHost(
    sessionVm: SessionNavViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

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
        publicNavGraph(
            navController = navController,
            hasSession = { sessionVm.hasSession() },
        )
        memberNavGraph(
            navController = navController,
            goMember = { goMember(it) },
            logout = { logout() },
        )
        contentNavGraph(
            navController = navController,
            goMember = { goMember(it) },
            logout = { logout() },
        )
    }
}
