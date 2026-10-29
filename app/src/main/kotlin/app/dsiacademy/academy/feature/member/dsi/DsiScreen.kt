package app.dsiacademy.academy.feature.member.dsi

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
private val Surface = Color(0xFFFFFFFF)
private val Border = Color(0xFFE2E8F0)

@Composable
fun DsiScreen(viewModel: DsiViewModel = hiltViewModel()) {
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
            Text("Mes DSI", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
                    .padding(24.dp),
            ) {
                Text("SOLDE ACTUEL", fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Gold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(state.soldeActuel, fontFamily = FrauncesFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("DSI", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gold, modifier = Modifier.padding(bottom = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Total gagné", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextMuted)
                Text("${state.totalGagne} DSI", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White)
                if (state.identifiant != null) {
                    Spacer(Modifier.height(12.dp))
                    Text("Matricule ${state.identifiant}", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextMuted)
                }
                if (state.dateMaj != null) {
                    Text("Mis à jour : ${state.dateMaj}", fontFamily = ManropeFamily, fontSize = 11.sp, color = TextMuted)
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp)).padding(16.dp),
            ) {
                Text("Informations", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Les montants affichés proviennent du Worker Paiement. Le serveur reste seul juge des opérations (retraits, renouvellements).",
                    fontFamily = ManropeFamily, fontSize = 13.sp, color = TextMuted, lineHeight = 18.sp,
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp)).padding(16.dp),
            ) {
                Text("Code cadeau", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(10.dp)).padding(14.dp)) {
                    BasicTextField(
                        value = state.giftCode,
                        onValueChange = viewModel::setGiftCode,
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 14.sp, color = TextPrimary),
                        cursorBrush = SolidColor(Gold),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (state.giftCode.isEmpty()) {
                                Text("Saisir un code…", fontFamily = ManropeFamily, fontSize = 14.sp, color = TextMuted)
                            }
                            inner()
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.fillMaxWidth().height(44.dp).clip(CircleShape)
                        .background(Gold)
                        .clickable(enabled = !state.giftLoading) { viewModel.soumettreCode() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.giftLoading) CircularProgressIndicator(Modifier.size(16.dp), Color(0xFF1A1300), strokeWidth = 2.dp)
                    else Text("Utiliser le code", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1300))
                }
                state.giftMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFF15803D))
                }
                state.giftError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFFB91C1C))
                }
            }
        }
    }
}
