package app.dsiacademy.academy.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.dsiacademy.academy.feature.content.ContentDetailScreen
import app.dsiacademy.academy.feature.content.ContentPlayerScreen
import app.dsiacademy.academy.feature.member.academy.HouseCatalogScreen
import app.dsiacademy.academy.feature.member.shell.MemberShell

fun NavGraphBuilder.contentNavGraph(
    navController: NavHostController,
    goMember: (String) -> Unit,
    logout: () -> Unit,
) {
    composable(
        route = Routes.HOUSE_CATALOG,
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
            navArgument("title") { type = NavType.StringType; defaultValue = "Catalogue" },
        ),
    ) { entry ->
        val currentRoute = entry.destination.route ?: Routes.HOUSE_CATALOG
        MemberShell(
            currentRoute = currentRoute,
            onNavigate = { dest -> goMember(dest) },
            onLogout = { logout() },
        ) {
            HouseCatalogScreen(
                onOpenContent = { id, title, type ->
                    navController.navigate(Routes.contentDetail(id, title, type))
                },
            )
        }
    }

    composable(
        route = Routes.CONTENT_DETAIL,
        arguments = listOf(
            navArgument("contentId") { type = NavType.StringType },
            navArgument("title") { type = NavType.StringType; defaultValue = "Contenu" },
            navArgument("type") { type = NavType.StringType; defaultValue = "book" },
        ),
    ) {
        ContentDetailScreen(
            onPlay = { id, title, type ->
                navController.navigate(Routes.contentPlayer(id, title, type))
            },
            onBack = { navController.popBackStack() },
        )
    }

    composable(
        route = Routes.CONTENT_PLAYER,
        arguments = listOf(
            navArgument("contentId") { type = NavType.StringType },
            navArgument("title") { type = NavType.StringType; defaultValue = "Contenu" },
            navArgument("type") { type = NavType.StringType; defaultValue = "book" },
        ),
    ) {
        ContentPlayerScreen(onClose = { navController.popBackStack() })
    }
}
