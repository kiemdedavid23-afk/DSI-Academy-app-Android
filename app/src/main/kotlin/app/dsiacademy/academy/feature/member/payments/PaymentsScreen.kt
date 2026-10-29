package app.dsiacademy.academy.feature.member.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

private val Gold = Color(0xFFD4AF37)
private val TextP = Color(0xFF0F172A)
private val TextM = Color(0xFF94A3B8)
private val TextS = Color(0xFF475569)
private val Surface = Color.White
private val Border = Color(0xFFE2E8F0)
private val Green = Color(0xFF15803D)

@Composable
fun PaymentsScreen(vm: PaymentsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Historique paiements", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextP)
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            state.error != null -> Text(state.error!!, fontFamily = ManropeFamily, color = Color(0xFFB91C1C), fontSize = 14.sp)
            state.paiements.isEmpty() -> Text("Aucun paiement pour le moment.", fontFamily = ManropeFamily, color = TextM, fontSize = 13.sp)
            else -> state.paiements.forEach { p ->
                val paid = p.statut == "paid"
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp))
                        .background(Surface).border(1.dp, Border, RoundedCornerShape(12.dp)).padding(14.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${p.montant.toLong()} ${p.devise ?: ""}".trim(),
                            fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextP,
                        )
                        Text(
                            if (paid) "Payé" else "En attente",
                            fontFamily = ManropeFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = if (paid) Green else TextS,
                        )
                    }
                    p.typePaiement?.let { Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = TextS) }
                    p.plateforme?.let { Text(it, fontFamily = ManropeFamily, fontSize = 12.sp, color = TextM) }
                    Text(p.dateConfirmation ?: p.dateCreation ?: "", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextM)
                }
            }
        }

        if (state.totalPages > 1) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Page ${state.page} / ${state.totalPages}", fontFamily = ManropeFamily, fontSize = 13.sp, color = TextM)
                Row {
                    if (state.page > 1) {
                        Text("← Préc.", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, color = Gold,
                            modifier = Modifier.clickable { vm.load(state.page - 1) }.padding(8.dp))
                    }
                    if (state.page < state.totalPages) {
                        Text("Suiv. →", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, color = Gold,
                            modifier = Modifier.clickable { vm.load(state.page + 1) }.padding(8.dp))
                    }
                }
            }
        }
    }
}
