package app.dsiacademy.academy.feature.member.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.components.MemberAvatar
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

private val Surface = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0F172A)
private val TextMuted = Color(0xFF94A3B8)
private val Border = Color(0xFFE2E8F0)
private val Gold = Color(0xFFD4AF37)
private val Blue = Color(0xFF1E40AF)
private val Green = Color(0xFF15803D)

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    fun copy(key: String, value: String) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(key, value))
        viewModel.markCopied(key)
    }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        state.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(state.error!!, fontFamily = ManropeFamily, color = Color(0xFFB91C1C), fontSize = 14.sp)
        }
        else -> {
            val p = state.profil!!
            val initials = buildString {
                if (p.prenom.isNotBlank()) append(p.prenom.first().uppercaseChar())
                if (p.nom.isNotBlank()) append(p.nom.first().uppercaseChar())
            }.ifBlank { "M" }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            ) {
                Text("Mon Profil", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextPrimary)
                Spacer(Modifier.height(16.dp))

                // Avatar + name
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(14.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MemberAvatar(imageUrl = state.avatarUrl, initials = initials, size = 64.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "${p.prenom} ${p.nom}".trim().ifBlank { "Membre DSI" },
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                        )
                        Text(p.niveau, fontFamily = ManropeFamily, fontSize = 13.sp, color = TextMuted)
                        val actif = p.statut.lowercase() in listOf("actif", "active")
                        Text(
                            p.statut,
                            fontFamily = ManropeFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (actif) Green else Color(0xFFB91C1C),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Card {
                    Section("Identité")
                    RowLine("Prénom", p.prenom.ifBlank { "—" })
                    RowLine("Nom", p.nom.ifBlank { "—" })
                    RowCopy(
                        "Identifiant DSI",
                        p.identifiantDsi.ifBlank { "—" },
                        copyable = p.identifiantDsi.isNotBlank(),
                        copied = state.copiedKey == "dsiId",
                        gold = true,
                    ) { copy("dsiId", p.identifiantDsi) }
                    RowCopy(
                        "Code parrainage",
                        p.codeParrainage.ifBlank { "—" },
                        copyable = p.codeParrainage.isNotBlank(),
                        copied = state.copiedKey == "code",
                        gold = true,
                    ) { copy("code", p.codeParrainage) }
                }

                Spacer(Modifier.height(12.dp))
                Card {
                    Section("Contact")
                    RowCopy("Email", p.email ?: "—", copyable = !p.email.isNullOrBlank(), copied = state.copiedKey == "email") {
                        copy("email", p.email!!)
                    }
                    RowCopy("WhatsApp", p.whatsapp ?: "—", copyable = !p.whatsapp.isNullOrBlank(), copied = state.copiedKey == "whatsapp") {
                        copy("whatsapp", p.whatsapp!!)
                    }
                    RowLine("Pays", p.pays ?: "—")
                }

                Spacer(Modifier.height(12.dp))
                Card {
                    Section("Compte")
                    RowLine("Niveau", p.niveau)
                    RowLine("Statut", p.statut)
                    RowLine("Inscription", p.dateInscription ?: "—")
                    RowLine("Activation", p.dateActivation ?: "—")
                }
            }
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)).padding(16.dp),
    ) { content() }
}

@Composable
private fun Section(title: String) {
    Text(title, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextMuted, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun RowLine(label: String, value: String, gold: Boolean = false) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, fontFamily = ManropeFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (gold) Gold else TextPrimary)
    }
}

@Composable
private fun RowCopy(
    label: String,
    value: String,
    copyable: Boolean,
    copied: Boolean,
    gold: Boolean = false,
    onCopy: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontFamily = ManropeFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(value, fontFamily = ManropeFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (gold) Gold else TextPrimary)
        }
        if (copyable) {
            Text(
                if (copied) "Copié" else "Copier",
                fontFamily = ManropeFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (copied) Green else Blue,
                modifier = Modifier.clickable(onClick = onCopy).padding(8.dp),
            )
        }
    }
}
