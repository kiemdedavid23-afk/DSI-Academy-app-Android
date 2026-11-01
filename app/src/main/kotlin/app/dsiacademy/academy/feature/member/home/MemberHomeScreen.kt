package app.dsiacademy.academy.feature.member.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

/**
 * Home membre — aligné MemberHomeScreen.jsx (données / textes).
 * Verre dépoli sur fond photo (géré par MemberShell).
 */
@Composable
fun MemberHomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: MemberHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> MemberHomeSkeleton()
        state.sessionExpired -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                GlassBox {
                    Text(
                        text = stringResource(R.string.home_session_expired),
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = ErrorRed,
                    )
                }
            }
        }
        state.loadFailed -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                GlassBox {
                    Text(
                        text = state.errorMessage
                            ?: stringResource(R.string.home_error_default),
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.retry),
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HomeGold,
                        modifier = Modifier.clickable { viewModel.charger() },
                    )
                }
            }
        }
        else -> {
            val displayName = state.prenom
                ?: stringResource(R.string.home_welcome_default)
            val badgeBase = when (state.abonnementState) {
                SubscriptionState.Active -> stringResource(R.string.home_badge_active)
                SubscriptionState.Expired -> stringResource(R.string.home_badge_expired)
                SubscriptionState.None -> stringResource(R.string.home_badge_none)
            }
            val badgeLabel = state.abonnementFin?.let { fin ->
                "$badgeBase · ${stringResource(R.string.home_subscription_end, fin)}"
            } ?: badgeBase

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                MemberHomeHero(
                    displayName = displayName,
                    soldeDsi = state.soldeDsi,
                    totalGagne = state.totalGagne,
                    badgeLabel = badgeLabel,
                    abonnementState = state.abonnementState,
                    onNavigate = onNavigate,
                )
                Spacer(Modifier.height(16.dp))
                ContinueCardSection(
                    continueEntry = state.continueEntry,
                    onNavigate = onNavigate,
                )
                Spacer(Modifier.height(12.dp))
                ActivityCardSection(activite = state.activite)
                Spacer(Modifier.height(12.dp))
                FavoritesCardSection(
                    favorites = state.favorites,
                    onNavigate = onNavigate,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
