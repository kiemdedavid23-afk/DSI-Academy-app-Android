package app.dsiacademy.academy.feature.member.subscription

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

private val Gold = Color(0xFFD4AF37)
private val GoldB = Color(0xFFE8C77E)
private val TextP = Color(0xFF0F172A)
private val TextM = Color(0xFF94A3B8)
private val TextS = Color(0xFF475569)
private val Surface = Color.White
private val Border = Color(0xFFE2E8F0)
private val BtnT = Color(0xFF1A1300)
private val Ink = Color(0xFF0B0D12)
private val RoyalStart = Color(0xFF0A1B54)
private val RoyalEnd = Color(0xFF1D4ED8)
private val GoldLight = Color(0xFFFDF6E2)

@Composable
fun SubscriptionScreen(vm: SubscriptionViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        vm.events.collect {
            when (it) {
                is SubEvent.OpenUrl -> ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Mon abonnement", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextP)
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            state.error != null && state.abo == null -> Text(state.error!!, fontFamily = ManropeFamily, color = Color(0xFFB91C1C))
            else -> {
                val a = state.abo
                val etat = a?.etatAffiche ?: "none"
                val jours = a?.joursRestants
                val percent = a?.progressPercent ?: 0
                val title = when (etat) {
                    "active" -> "Abonnement actif"
                    "expired" -> "Abonnement expiré"
                    else -> "Sans abonnement"
                }
                val line = when (etat) {
                    "active" -> when (jours) {
                        1 -> "1 jour restant"
                        null -> "Abonnement actif"
                        else -> "$jours jours restants"
                    }
                    "expired" -> "Abonnement expiré"
                    else -> "Aucun abonnement actif"
                }

                // Carte statut (même logique drawer / computeSubscription)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(RoyalStart, RoyalEnd)))
                        .padding(16.dp),
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(title, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f))
                        val badgeBg = when (etat) {
                            "active" -> GoldLight
                            "expired" -> Color(0xFFFEE2E2)
                            else -> Color.White.copy(alpha = 0.2f)
                        }
                        val badgeFg = when (etat) {
                            "active" -> Color(0xFF6B4F00)
                            "expired" -> Color(0xFF991B1B)
                            else -> Color.White
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                when (etat) {
                                    "active" -> "Actif"
                                    "expired" -> "Expiré"
                                    else -> "Aucun"
                                },
                                fontFamily = ManropeFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeFg,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(line, fontFamily = ManropeFamily, fontSize = 14.sp, color = Color(0xFFBFDBFE))
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(percent / 100f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Gold),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    a?.dateFin?.let {
                        Text(
                            if (etat == "expired") "Expiré le $it" else "Expire le $it",
                            fontFamily = ManropeFamily,
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD),
                        )
                    }
                    a?.formule?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("Formule : $it", fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFFBFDBFE))
                    }
                    a?.modePaiement?.let {
                        Text("Mode : $it", fontFamily = ManropeFamily, fontSize = 12.sp, color = Color(0xFF93C5FD))
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Renouveler", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextP)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tarif affiché : 2 500 DSI (constante d'affichage). Le Worker reste seul juge.",
                    fontFamily = ManropeFamily,
                    fontSize = 12.sp,
                    color = TextM,
                )
                Spacer(Modifier.height(12.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GoldB, Gold)))
                        .clickable(enabled = !state.renewing) { vm.renouveler("dsi") },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.renewing) CircularProgressIndicator(Modifier.size(16.dp), BtnT, strokeWidth = 2.dp)
                    else Text("Payer en DSI", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BtnT)
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Ink)
                        .clickable(enabled = !state.renewing) { vm.renouveler("fcfa") },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Payer en FCFA", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }

                state.message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFF15803D))
                }
                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFFB91C1C))
                }
            }
        }
    }
}
