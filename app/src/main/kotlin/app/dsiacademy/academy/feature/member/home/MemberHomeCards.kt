package app.dsiacademy.academy.feature.member.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.core.session.ContinueEntry
import app.dsiacademy.academy.core.session.FavoriteEntry

@Composable
fun ContinueCardSection(
    continueEntry: ContinueEntry?,
    onNavigate: (String) -> Unit,
) {
    GlassCard(title = stringResource(R.string.home_continue_title)) {
        val c = continueEntry
        if (c == null) {
            Text(
                stringResource(R.string.home_continue_empty),
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallChip(stringResource(R.string.home_library)) { onNavigate("house/academy") }
                SmallChip(stringResource(R.string.home_formations), gold = true) {
                    onNavigate("house/academy")
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.icon, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        c.title,
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    )
                    Text(c.house, fontFamily = ManropeFamily, fontSize = 12.sp, color = TextMuted)
                    c.progress?.let { pct ->
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFE2E8F0)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(pct / 100f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(HomeGold),
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.home_continue_cta),
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = HomeGold,
                    modifier = Modifier.clickable { onNavigate("house/academy") },
                )
            }
        }
    }
}

@Composable
fun ActivityCardSection(activite: List<String>) {
    GlassCard(title = stringResource(R.string.home_activity_title)) {
        if (activite.isEmpty()) {
            Text(
                stringResource(R.string.home_activity_empty),
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                color = TextMuted,
            )
        } else {
            activite.forEach { line ->
                Text(
                    line,
                    fontFamily = ManropeFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
fun FavoritesCardSection(
    favorites: List<FavoriteEntry>,
    onNavigate: (String) -> Unit,
) {
    GlassCard(title = stringResource(R.string.home_favorites_title)) {
        if (favorites.isEmpty()) {
            Text(
                stringResource(R.string.home_favorites_empty),
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp,
            )
        } else {
            favorites.forEach { f ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigate("house/academy") },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(f.icon, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        f.title,
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusLg))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(RadiusLg))
            .padding(16.dp),
    ) {
        Text(
            title,
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = TextPrimary,
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
fun SmallChip(label: String, gold: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (gold) GoldLight else Color(0xFFF1F5F9))
            .border(
                1.dp,
                if (gold) GoldBorder else Color(0xFFE2E8F0),
                RoundedCornerShape(999.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontFamily = ManropeFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (gold) HomeGold else TextPrimary,
        )
    }
}

@Preview(name = "Cards — clair", showBackground = true)
@Composable
private fun MemberHomeCardsPreview() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContinueCardSection(continueEntry = null, onNavigate = {})
        ActivityCardSection(activite = emptyList())
        FavoritesCardSection(favorites = emptyList(), onNavigate = {})
    }
}
