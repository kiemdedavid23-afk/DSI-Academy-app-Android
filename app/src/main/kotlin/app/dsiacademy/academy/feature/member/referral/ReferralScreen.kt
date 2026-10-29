package app.dsiacademy.academy.feature.member.referral

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

private val Gold = Color(0xFFD4AF37)
private val TextPrimary = Color(0xFF0F172A)
private val TextMuted = Color(0xFF94A3B8)
private val TextSecondary = Color(0xFF475569)
private val Surface = Color(0xFFFFFFFF)
private val Border = Color(0xFFE2E8F0)
private val GoldLight = Color(0xFFFDF6E2)

@Composable
fun ReferralScreen(viewModel: ReferralViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        state.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(state.error!!, fontFamily = ManropeFamily, color = Color(0xFFB91C1C), fontSize = 14.sp)
        }
        else -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            Text("Parrainage", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            // Code
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GoldLight)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(14.dp)).padding(16.dp),
            ) {
                Text("VOTRE CODE", fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Gold)
                Spacer(Modifier.height(6.dp))
                Text(
                    state.code.ifBlank { "—" },
                    fontFamily = FrauncesFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary,
                )
                if (state.code.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    val ctx = LocalContext.current
                    Row {
                        Text(
                            "Copier",
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Gold,
                            modifier = Modifier.clickable {
                                val cm = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("code", state.code))
                            }.padding(8.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Partager le lien",
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Gold,
                            modifier = Modifier.clickable {
                                val link = "https://dsiacademy.com/register?ref=${state.code}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Rejoins DSI Academy avec mon code ${state.code} : $link")
                                }
                                ctx.startActivity(Intent.createChooser(intent, "Partager"))
                            }.padding(8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stats
            Row(Modifier.fillMaxWidth()) {
                StatBox("Filleuls", "${state.filleulsTotal}", Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                StatBox("Actifs", "${state.filleulsActifs}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                StatBox("Gains N1", "${state.n1} DSI", Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                StatBox("Gains N2", "${state.n2} DSI", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            StatBox("Total parrainage", "${state.total} DSI", Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Historique", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            if (state.historique.isEmpty()) {
                Text(
                    "Aucun filleul pour le moment — état normal, pas de données fictives.",
                    fontFamily = ManropeFamily,
                    fontSize = 13.sp,
                    color = TextMuted,
                )
            } else {
                state.historique.forEach { line ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(10.dp)).background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(10.dp)).padding(12.dp),
                    ) {
                        Text(line, fontFamily = ManropeFamily, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Récompenses affichées (règles) : N1 = 500 DSI, N2 = 120 DSI — affichage seul ; le Worker attribue les gains réels.",
                fontFamily = ManropeFamily, fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(Surface)
            .border(1.dp, Border, RoundedCornerShape(12.dp)).padding(14.dp),
    ) {
        Text(label, fontFamily = ManropeFamily, fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
    }
}
