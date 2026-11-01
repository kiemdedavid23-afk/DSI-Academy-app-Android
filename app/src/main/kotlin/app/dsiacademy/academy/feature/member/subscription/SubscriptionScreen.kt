package app.dsiacademy.academy.feature.member.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import app.dsiacademy.academy.core.designsystem.components.SkeletonGate
import app.dsiacademy.academy.core.designsystem.theme.DsiSubscriptionStyle as S
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.domain.member.MemberAbonnement

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.sessionExpired -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                MessageCard(stringResource(R.string.subscription_session_expired))
            }
        }
        state.loadFailed -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                MessageCard(
                    state.errorMessage
                        ?: stringResource(R.string.subscription_error_default),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.retry),
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = S.gold,
                    modifier = Modifier.clickable { viewModel.charger() },
                )
            }
        }
        else -> {
            SkeletonGate(
                loading = state.loading,
                hasData = state.abo != null,
                skeleton = { SubscriptionSkeleton() },
            ) {
                val abo = state.abo ?: return@SkeletonGate
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    SubscriptionContent(state, abo, viewModel)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionContent(
    state: SubscriptionUiState,
    abo: MemberAbonnement,
    viewModel: SubscriptionViewModel,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SubscriptionHeader()
        SubscriptionHero(abo)
        SubscriptionKeyFigures(abo)
        SubscriptionRenewBox(
            state = state,
            onOpenPanel = viewModel::openPanel,
            onClosePanel = viewModel::closePanel,
            onChooseDsi = viewModel::chooseDsi,
            onBackToChoice = viewModel::backToChoice,
            onRenewFcfa = viewModel::renewFcfa,
            onConfirmDsi = viewModel::confirmDsi,
        )
        SubscriptionHistory(abo.renouvellements)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MessageCard(text: String) {
    Text(
        text,
        fontFamily = ManropeFamily,
        fontSize = 14.sp,
        color = S.textSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(S.radius))
            .background(S.glassBg)
            .border(1.dp, S.glassBorder, RoundedCornerShape(S.radius))
            .padding(16.dp),
    )
}
