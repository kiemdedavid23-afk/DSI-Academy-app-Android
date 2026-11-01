package app.dsiacademy.academy.feature.member.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.common.DsiFormats
import app.dsiacademy.academy.core.designsystem.icons.DsiIcons
import app.dsiacademy.academy.core.designsystem.theme.DsiSubscriptionStyle as S
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.domain.member.MemberAbonnement
import app.dsiacademy.academy.domain.member.PaymentModeKind
import app.dsiacademy.academy.domain.member.SubscriptionTone
import app.dsiacademy.academy.domain.member.paymentModeOf
import java.util.Locale

@Composable
fun SubscriptionHeader() {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(S.goldLight)
                .border(1.dp, S.goldBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DsiIcons.Subscription,
                contentDescription = null,
                tint = S.gold,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                stringResource(R.string.subscription_title),
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = S.textPrimary,
            )
            Text(
                stringResource(R.string.subscription_subtitle),
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                color = S.textMuted,
            )
        }
    }
}

@Composable
fun SubscriptionHero(abo: MemberAbonnement) {
    val english = Locale.getDefault().language.startsWith("en")
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(S.radius))
            .background(S.heroBg)
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.subscription_current_access),
            fontFamily = ManropeFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = S.goldLight,
        )
        Spacer(Modifier.height(8.dp))
        if (abo.hasFormula) {
            Text(
                abo.formule.orEmpty(),
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = S.heroText,
            )
            val statut = abo.statut?.takeIf { it.isNotBlank() }
            if (statut != null) {
                Spacer(Modifier.height(8.dp))
                StatusPill(statut, abo.tone)
            }
        } else {
            Text(
                stringResource(R.string.subscription_empty),
                fontFamily = ManropeFamily,
                fontSize = 14.sp,
                color = S.heroText.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
fun StatusPill(label: String, tone: SubscriptionTone) {
    val dot = when (tone) {
        SubscriptionTone.Active -> S.toneActive
        SubscriptionTone.Warning -> S.toneWarning
        SubscriptionTone.Neutral -> S.toneNeutral
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(6.dp))
        Text(label, fontFamily = ManropeFamily, fontSize = 12.sp, color = S.heroText)
    }
}

@Composable
fun SubscriptionKeyFigures(abo: MemberAbonnement) {
    if (!abo.hasFormula) return
    val english = Locale.getDefault().language.startsWith("en")
    fun longOrDash(raw: String?): String {
        val ms = DsiFormats.parseIsoMs(raw) ?: return "—"
        return DsiFormats.formatLongDate(ms, english)
    }
    val mode = paymentModeOf(abo.modePaiement)
    val modeLabel = when (mode.kind) {
        PaymentModeKind.None -> "—"
        PaymentModeKind.Fcfa -> "FCFA"
        PaymentModeKind.Dsi -> "DSI"
        PaymentModeKind.Founder -> stringResource(R.string.subscription_mode_founder)
        PaymentModeKind.Other -> mode.raw ?: "—"
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KeyCell(
            stringResource(R.string.subscription_start_date),
            longOrDash(abo.dateDebut),
            Modifier.weight(1f),
        )
        KeyCell(
            stringResource(R.string.subscription_end_date),
            longOrDash(abo.dateFin),
            Modifier.weight(1f),
            highlight = true,
        )
        KeyCell(
            stringResource(R.string.subscription_payment_mode),
            modeLabel,
            Modifier.weight(1f),
            badge = true,
        )
    }
}

@Composable
private fun KeyCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    badge: Boolean = false,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(S.radiusSm))
            .background(S.glassBg)
            .border(1.dp, S.glassBorder, RoundedCornerShape(S.radiusSm))
            .padding(10.dp),
    ) {
        Text(label, fontFamily = ManropeFamily, fontSize = 10.sp, color = S.textMuted)
        Spacer(Modifier.height(4.dp))
        if (badge) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(S.goldLight)
                    .border(1.dp, S.goldBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(value, fontFamily = ManropeFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = S.gold)
            }
        } else {
            Text(
                value,
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (highlight) S.gold else S.textPrimary,
            )
        }
    }
}
