package app.dsiacademy.academy.feature.member.withdrawals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.domain.member.RetraitItem

private val Gold = Color(0xFFD4AF37)
private val GoldB = Color(0xFFE8C77E)
private val TextP = Color(0xFF0F172A)
private val TextM = Color(0xFF94A3B8)
private val TextS = Color(0xFF475569)
private val Surface = Color.White
private val Border = Color(0xFFE2E8F0)
private val BtnT = Color(0xFF1A1300)
private val Green = Color(0xFF15803D)
private val Blue = Color(0xFF1E40AF)

@Composable
fun WithdrawalsScreen(vm: WithdrawalsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var menu by remember { mutableStateOf(false) }

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Mes retraits", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextP)
        state.pays?.let {
            Text("Pays profil : $it", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextM)
        }
        Spacer(Modifier.height(16.dp))

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface)
                .border(1.dp, Border, RoundedCornerShape(14.dp)).padding(16.dp),
        ) {
            Text("Nouvelle demande", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextP)
            Spacer(Modifier.height(12.dp))
            Field("Montant DSI", state.montant) { vm.setMontant(it) }
            Spacer(Modifier.height(10.dp))
            Text("Moyen de paiement", fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextM)
            Spacer(Modifier.height(4.dp))
            Box {
                Box(
                    Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(10.dp))
                        .clickable { menu = true }.padding(14.dp),
                ) {
                    Text(state.moyen.ifBlank { "Choisir…" }, fontFamily = ManropeFamily, fontSize = 14.sp, color = TextP)
                }
                DropdownMenu(menu, { menu = false }) {
                    state.moyens.forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { vm.setMoyen(m); menu = false })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Field("Numéro de paiement", state.numero) { vm.setNumero(it) }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GoldB, Gold)))
                    .clickable(enabled = !state.submitting) { vm.soumettre() },
                contentAlignment = Alignment.Center,
            ) {
                if (state.submitting) CircularProgressIndicator(Modifier.size(16.dp), BtnT, strokeWidth = 2.dp)
                else Text("Demander un retrait", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BtnT)
            }
            state.error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFFB91C1C))
            }
            state.successMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = Green)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "La validation serveur (plafond, fréquence) prime toujours.",
                fontFamily = ManropeFamily, fontSize = 11.sp, color = TextM,
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("Historique", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextP)
        Spacer(Modifier.height(8.dp))
        if (state.retraits.isEmpty()) {
            Text("Aucun retrait pour le moment.", fontFamily = ManropeFamily, fontSize = 13.sp, color = TextM)
        } else {
            state.retraits.forEach { r ->
                val selected = state.selectedId == r.idRetrait
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp))
                        .background(Surface).border(1.dp, if (selected) Gold else Border, RoundedCornerShape(12.dp))
                        .clickable { vm.selectRetrait(if (selected) null else r.idRetrait) }
                        .padding(14.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${r.montantDsiNet.toLong()} DSI", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextP)
                        StatusChip(r.statut)
                    }
                    Text(r.dateDemande ?: "", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextM)
                    r.moyenPaiement?.let {
                        Text(it, fontFamily = ManropeFamily, fontSize = 12.sp, color = TextS)
                    }
                    if (selected) {
                        Spacer(Modifier.height(12.dp))
                        Text("Progression", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextM)
                        Spacer(Modifier.height(8.dp))
                        StatusFrise(r.statut)
                        if (state.evenements.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("Historique", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextM)
                            state.evenements.forEach { e ->
                                Text(
                                    "${e.date ?: ""} · ${e.champ ?: ""} : ${e.ancienneValeur ?: "—"} → ${e.nouvelleValeur ?: "—"}",
                                    fontFamily = ManropeFamily,
                                    fontSize = 12.sp,
                                    color = TextS,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(statut: String) {
    val s = statut.lowercase()
    val bg = when (s) {
        "payee", "traite" -> Color(0xFFDCFCE7)
        "rejetee" -> Color(0xFFFEE2E2)
        "validee", "envoyee" -> Color(0xFFDBEAFE)
        else -> Color(0xFFF1F5F9)
    }
    val fg = when (s) {
        "payee", "traite" -> Green
        "rejetee" -> Color(0xFFB91C1C)
        "validee", "envoyee" -> Blue
        else -> TextS
    }
    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(statutLabel(statut), fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

/** Frise site ORDRE_ETAPES : demande → validee → envoyee → payee */
@Composable
private fun StatusFrise(statut: String) {
    val s = statut.lowercase()
    if (s == "rejetee") {
        Text("Demande rejetée", fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFFB91C1C))
        return
    }
    val idx = RETRAIT_STEPS.indexOf(s).let { if (it < 0) 0 else it }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RETRAIT_STEPS.forEachIndexed { i, step ->
            val done = i <= idx
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier.size(12.dp).clip(CircleShape)
                        .background(if (done) Gold else Color(0xFFE2E8F0)),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    statutLabel(step),
                    fontFamily = ManropeFamily,
                    fontSize = 10.sp,
                    color = if (done) TextP else TextM,
                )
            }
            if (i < RETRAIT_STEPS.lastIndex) {
                Box(
                    Modifier.weight(0.4f).height(2.dp).padding(bottom = 14.dp)
                        .background(if (i < idx) Gold else Color(0xFFE2E8F0)),
                )
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Text(label, fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextM)
    Spacer(Modifier.height(4.dp))
    Box(Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(10.dp)).padding(14.dp)) {
        BasicTextField(
            value = value, onValueChange = onChange, singleLine = true,
            textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 14.sp, color = TextP),
            cursorBrush = SolidColor(Gold),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
