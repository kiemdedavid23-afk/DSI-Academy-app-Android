package app.dsiacademy.academy.feature.public

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.IbmPlexMonoFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

private val Ink = Color(0xFF0B0D12)
private val Gold = Color(0xFFC9A15A)
private val GoldBright = Color(0xFFE8C77E)
private val Paper = Color(0xFFF3F1EC)
private val TextSoft = Color(0xFF565D6B)
private val Line = Color(0xFFE4E2DA)
private val BtnText = Color(0xFF1A1300)

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Paper).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Image(painterResource(R.drawable.logo_dsi), "DSI", Modifier.height(48.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(16.dp))
        Text("ESPACE MEMBRE", fontFamily = IbmPlexMonoFamily, fontSize = 11.5.sp, letterSpacing = 1.5.sp, color = Gold)
        Spacer(Modifier.height(10.dp))
        Text("DSI Academy", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            "Formations, bibliothèque, communauté et solde DSI — accessible 24h/24 depuis votre téléphone.",
            fontFamily = ManropeFamily, fontSize = 15.sp, color = TextSoft, textAlign = TextAlign.Center, lineHeight = 22.sp,
        )
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.fillMaxWidth().height(50.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(GoldBright, Gold))).clickable(onClick = onLogin),
            contentAlignment = Alignment.Center,
        ) {
            Text("Se connecter", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BtnText)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(50.dp).clip(CircleShape)
                .border(1.dp, Line, CircleShape).background(Color.White).clickable(onClick = onRegister),
            contentAlignment = Alignment.Center,
        ) {
            Text("Créer un compte", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
        }
        Spacer(Modifier.height(32.dp))
        Text("© DSI Academy", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextSoft)
        Spacer(Modifier.height(24.dp))
    }
}
