package app.dsiacademy.academy.feature.member.subscription

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.common.DsiFormats
import app.dsiacademy.academy.core.common.SimpleMarkup
import app.dsiacademy.academy.core.config.Pricing
import app.dsiacademy.academy.core.designsystem.theme.DsiSubscriptionStyle as S
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import java.util.Locale

@Composable
fun SubscriptionRenewBox(
    state: SubscriptionUiState,
    onOpenPanel: () -> Unit,
    onClosePanel: () -> Unit,
    onChooseDsi: () -> Unit,
    onBackToChoice: () -> Unit,
    onRenewFcfa: () -> Unit,
    onConfirmDsi: () -> Unit,
) {
    val english = Locale.getDefault().language.startsWith("en")
    val price = DsiFormats.formatGroupedInt(Pricing.RENEWAL_PRICE_DSI, english)
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(S.radius))
            .background(S.glassBg)
            .border(1.dp, S.glassBorder, RoundedCornerShape(S.radius))
            .padding(16.dp),
    ) {
        when (state.panel) {
            RenewPanel.Closed -> {
                ActionButton(
                    text = stringResource(R.string.subscription_renew_button),
                    enabled = !state.renewing,
                    onClick = onOpenPanel,
                    leading = {
                        Icon(Icons.Outlined.Refresh, null, tint = S.gold)
                    },
                )
            }
            RenewPanel.ChooseMode -> {
                Text(
                    stringResource(R.string.subscription_choose_mode),
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = S.textPrimary,
                )
                Spacer(Modifier.height(10.dp))
                ActionButton(
                    stringResource(R.string.subscription_pay_fcfa),
                    enabled = !state.renewing,
                    onClick = onRenewFcfa,
                )
                Spacer(Modifier.height(8.dp))
                ActionButton(
                    stringResource(R.string.subscription_pay_dsi),
                    enabled = !state.renewing,
                    onClick = onChooseDsi,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(stringResource(R.string.common_cancel), onClosePanel)
            }
            RenewPanel.ConfirmDsi -> {
                Icon(Icons.Outlined.Warning, null, tint = S.toneWarning)
                Spacer(Modifier.height(8.dp))
                val warning = stringResource(R.string.subscription_confirm_warning, price)
                Text(
                    SimpleMarkup.toAnnotatedString(warning),
                    fontFamily = ManropeFamily,
                    fontSize = 13.sp,
                    color = S.textSecondary,
                )
                Spacer(Modifier.height(12.dp))
                ActionButton(
                    text = if (state.renewing) {
                        stringResource(R.string.subscription_processing)
                    } else {
                        stringResource(R.string.subscription_confirm_debit, price)
                    },
                    enabled = !state.renewing,
                    onClick = onConfirmDsi,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(stringResource(R.string.common_back), onBackToChoice)
            }
        }

        state.paymentLink?.let { link ->
            Spacer(Modifier.height(10.dp))
            ActionButton(
                stringResource(R.string.subscription_continue_payment),
                enabled = true,
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    } catch (_: ActivityNotFoundException) {
                    }
                },
            )
        }

        when (val fb = state.feedback) {
            is RenewFeedback.Success -> {
                Spacer(Modifier.height(10.dp))
                FeedbackBox(
                    fb.serverMessage
                        ?: stringResource(R.string.subscription_renew_success),
                    success = true,
                )
            }
            is RenewFeedback.Error -> {
                Spacer(Modifier.height(10.dp))
                val msg = when (fb.kind) {
                    FeedbackKind.Refused ->
                        fb.serverMessage ?: stringResource(R.string.subscription_renew_refused)
                    FeedbackKind.Unexpected ->
                        stringResource(R.string.subscription_unexpected_response)
                    FeedbackKind.Server ->
                        fb.serverMessage ?: stringResource(R.string.subscription_error_default)
                }
                FeedbackBox(msg, success = false)
            }
            null -> Unit
        }

        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.subscription_action_note),
            fontFamily = ManropeFamily,
            fontSize = 11.sp,
            color = S.textMuted,
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(S.radiusSm))
            .background(if (enabled) S.gold else S.gold.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp),
    ) {
        Text(
            text,
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
        )
    }
}

@Composable
private fun TextButton(text: String, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = ManropeFamily,
        fontSize = 13.sp,
        color = S.textSecondary,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
    )
}

@Composable
private fun FeedbackBox(text: String, success: Boolean) {
    val bg = if (success) S.successBg else S.errorBg
    val fg = if (success) S.successText else S.errorText
    Text(
        text,
        fontFamily = ManropeFamily,
        fontSize = 13.sp,
        color = fg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(S.radiusSm))
            .background(bg)
            .padding(10.dp),
    )
}

// local import for Color.White
