package app.dsiacademy.academy.feature.member.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

/**
 * Home membre — aligné MemberHomeScreen.jsx/css
 * Verre dépoli sur fond photo (géré par MemberShell).
 */
private val HomeGold = Color(0xFF8C6A12)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF475569)
private val TextMuted = Color(0xFF94A3B8)
private val GlassBg = Color(0xF2FFFFFF) // ~95% white
private val GlassBorder = Color(0x40FFFFFF)
private val GoldLight = Color(0xFFFDF6E2)
private val GoldBorder = Color(0xFFE8D5A3)
private val ErrorRed = Color(0xFFB91C1C)
private val Blue = Color(0xFF1E40AF)
private val RadiusLg = 16.dp

@Composable
fun MemberHomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: MemberHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                HomeSkeleton()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                GlassBox {
                    Text(state.error!!, fontFamily = ManropeFamily, color = ErrorRed, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Réessayer",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        color = Blue,
                        modifier = Modifier.clickable { viewModel.charger() },
                    )
                }
            }
        }
        else -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Hero
                Column(Modifier.fillMaxWidth()) {
                    GlassBox {
                        // Hello
                        Text(
                            "Bonjour",
                            fontFamily = FrauncesFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = HomeGold,
                        )
                        Text(
                            state.prenom,
                            fontFamily = FrauncesFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary,
                            lineHeight = 28.sp,
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Solde DSI disponible",
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = TextSecondary,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                state.soldeDsi,
                                fontFamily = FrauncesFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp,
                                color = TextPrimary,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "DSI",
                                fontFamily = FrauncesFamily,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                                color = HomeGold,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        SubBadge(state.abonnementLabel, state.abonnementState)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Total DSI gagnés", fontFamily = ManropeFamily, fontSize = 13.sp, color = TextSecondary)
                            Text(
                                "${state.totalGagne} DSI",
                                fontFamily = ManropeFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Actions 3 colonnes
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickAction("💸", "Mes retraits", Modifier.weight(1f)) { onNavigate("member/withdrawals") }
                        QuickAction("🤝", "Parrainage", Modifier.weight(1f)) { onNavigate("member/referral") }
                        QuickAction("🪙", "Mes DSI", Modifier.weight(1f)) { onNavigate("member/dsi") }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Continuer
                GlassCard(title = "Continuer") {
                    val c = state.continueEntry
                    if (c == null) {
                        Text(
                            "Explorez une maison (Bibliothèque, Formations…) puis ajoutez une catégorie en favori pour reprendre ici.",
                            fontFamily = ManropeFamily,
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallChip("Bibliothèque") { onNavigate("house/academy") }
                            SmallChip("Formations", gold = true) { onNavigate("house/academy") }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.icon, fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.title, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(c.house, fontFamily = ManropeFamily, fontSize = 12.sp, color = TextMuted)
                                c.progress?.let { pct ->
                                    Spacer(Modifier.height(6.dp))
                                    Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE2E8F0))) {
                                        Box(Modifier.fillMaxWidth(pct / 100f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(HomeGold))
                                    }
                                }
                            }
                            Text(
                                "Reprendre",
                                fontFamily = ManropeFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = HomeGold,
                                modifier = Modifier.clickable { onNavigate("house/academy") },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Activité
                GlassCard(title = "Activité récente") {
                    if (state.activite.isEmpty()) {
                        Text("Aucune activité récente", fontFamily = ManropeFamily, fontSize = 13.sp, color = TextMuted)
                    } else {
                        state.activite.forEach { line ->
                            Text(line, fontFamily = ManropeFamily, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Favoris
                GlassCard(title = "Favoris") {
                    if (state.favorites.isEmpty()) {
                        Text(
                            "Aucun favori pour l’instant. Sur une maison, touchez ★ pour en enregistrer.",
                            fontFamily = ManropeFamily,
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp,
                        )
                    } else {
                        state.favorites.forEach { f ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onNavigate("house/academy") },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(f.icon, fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(f.title, fontFamily = ManropeFamily, fontSize = 14.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomeSkeleton() {
    Column {
        GlassBox {
            Box(Modifier.width(64.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2E8F0)))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(140.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2E8F0)))
            Spacer(Modifier.height(16.dp))
            Box(Modifier.width(110.dp).height(11.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2E8F0)))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(160.dp).height(40.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2E8F0)))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(
                    Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(RadiusLg)).background(GlassBg),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        repeat(2) {
            Box(
                Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(RadiusLg)).background(GlassBg),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GlassBox(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RadiusLg))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(RadiusLg))
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) { content() }
}

@Composable
private fun GlassCard(title: String, content: @Composable () -> Unit) {
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
private fun SubBadge(label: String, state: String) {
    val bg = when (state) {
        "active" -> GoldLight
        "expired" -> Color(0xFFFEE2E2)
        else -> Color(0xFFE2E8F0)
    }
    val fg = when (state) {
        "active" -> Color(0xFF6B4F00)
        "expired" -> Color(0xFF991B1B)
        else -> Color(0xFF334155)
    }
    val border = if (state == "active") GoldBorder else Color.Transparent
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, fontFamily = ManropeFamily, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun QuickAction(icon: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(RadiusLg))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(RadiusLg))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(label, fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun SmallChip(label: String, gold: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (gold) GoldLight else Color(0xFFF1F5F9))
            .border(1.dp, if (gold) GoldBorder else Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
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
