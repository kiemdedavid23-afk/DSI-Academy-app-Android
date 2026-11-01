package app.dsiacademy.academy.feature.member.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.common.DsiFormats
import app.dsiacademy.academy.core.designsystem.theme.DsiSubscriptionStyle as S
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.domain.member.PaymentModeKind
import app.dsiacademy.academy.domain.member.RenouvellementItem
import app.dsiacademy.academy.domain.member.paymentModeOf
import java.util.Locale

@Composable
fun SubscriptionHistory(items: List<RenouvellementItem>) {
    val english = Locale.getDefault().language.startsWith("en")
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(S.radius))
            .background(S.glassBg)
            .border(1.dp, S.glassBorder, RoundedCornerShape(S.radius))
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.subscription_history_title),
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = S.textPrimary,
        )
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text(
                stringResource(R.string.subscription_history_empty),
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                color = S.textMuted,
            )
        } else {
            items.forEach { item ->
                HistoryEntry(item, english)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HistoryEntry(item: RenouvellementItem, english: Boolean) {
    fun longOrDash(raw: String?): String {
        val ms = DsiFormats.parseIsoMs(raw) ?: return "—"
        return DsiFormats.formatLongDate(ms, english)
    }
    val mode = paymentModeOf(item.modePaiement)
    val modeLabel = when (mode.kind) {
        PaymentModeKind.None -> "—"
        PaymentModeKind.Fcfa -> "FCFA"
        PaymentModeKind.Dsi -> "DSI"
        PaymentModeKind.Founder -> stringResource(R.string.subscription_mode_founder)
        PaymentModeKind.Other -> mode.raw ?: "—"
    }
    val statut = item.statut?.takeIf { it.isNotBlank() } ?: "—"
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(S.gold),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                longOrDash(item.date),
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = S.textPrimary,
            )
            Text(
                "${stringResource(R.string.subscription_renewal_label)} · $statut",
                fontFamily = ManropeFamily,
                fontSize = 12.sp,
                color = S.textSecondary,
            )
            Text(
                "${stringResource(R.string.subscription_mode_label)} $modeLabel · " +
                    "${stringResource(R.string.subscription_new_due_date)} ${longOrDash(item.nouvelleDateFin)}",
                fontFamily = ManropeFamily,
                fontSize = 12.sp,
                color = S.textMuted,
            )
        }
    }
}
