package app.dsiacademy.academy.feature.member.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.IbmPlexMonoFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mot de passe oublié — aligné sur ForgotPasswordScreen.jsx du site.
 * Le site n'a pas encore de route Worker : même comportement placeholder
 * (succès générique après court délai).
 */
private val Ink = Color(0xFF0B0D12)
private val Gold = Color(0xFFC9A15A)
private val GoldBright = Color(0xFFE8C77E)
private val Paper = Color(0xFFF3F1EC)
private val TextCol = Color(0xFF1A1D24)
private val TextSoft = Color(0xFF565D6B)
private val Line = Color(0xFFE4E2DA)
private val ErrorBg = Color(0xFFFBEAE9)
private val ErrorText = Color(0xFF9A3B32)
private val OkBg = Color(0xFFEFF6F2)
private val OkText = Color(0xFF1F7A5C)
private val BtnText = Color(0xFF1A1300)

@Composable
fun ForgotPasswordScreen(
    onBackLogin: () -> Unit,
    onBackHome: () -> Unit,
) {
    var identifiant by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) } // null | pending | sent | error
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Column(
            Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(20.dp))
                .padding(28.dp),
        ) {
            Text(
                "DSI ACADEMY",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.5.sp,
                letterSpacing = 1.5.sp,
                color = Gold,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Mot de passe oublié",
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Indiquez votre identifiant DSI. Nous vous indiquerons la marche à suivre pour récupérer l’accès à votre compte.",
                fontFamily = ManropeFamily,
                fontSize = 14.sp,
                color = TextSoft,
                lineHeight = 21.sp,
            )
            Spacer(Modifier.height(20.dp))

            if (status == "sent") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(OkBg)
                        .padding(12.dp),
                ) {
                    Text(message, fontFamily = ManropeFamily, fontSize = 13.5.sp, color = OkText, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
            if (status == "error") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorBg)
                        .padding(12.dp),
                ) {
                    Text(message, fontFamily = ManropeFamily, fontSize = 13.5.sp, color = ErrorText)
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                "Identifiant DSI",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = TextSoft,
            )
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, Line, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
            ) {
                if (identifiant.isEmpty()) {
                    Text(
                        "Ex. votre ID membre",
                        fontFamily = ManropeFamily,
                        fontSize = 15.sp,
                        color = TextSoft.copy(alpha = 0.7f),
                    )
                }
                BasicTextField(
                    value = identifiant,
                    onValueChange = { identifiant = it },
                    singleLine = true,
                    enabled = status != "pending",
                    textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 15.sp, color = TextCol),
                    cursorBrush = SolidColor(Gold),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(18.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GoldBright, Gold)))
                    .clickable(enabled = status != "pending") {
                        val v = identifiant.trim()
                        if (v.isEmpty()) {
                            status = "error"
                            message = "Veuillez saisir votre identifiant."
                            return@clickable
                        }
                        scope.launch {
                            status = "pending"
                            message = ""
                            delay(600)
                            status = "sent"
                            message =
                                "Si un compte correspond à cet identifiant, les instructions de réinitialisation seront communiquées par le support DSI. (Branchement Worker à venir.)"
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (status == "pending") {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp).padding(0.dp),
                        color = BtnText,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        "Demander la réinitialisation",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = BtnText,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Retour à la connexion",
                fontFamily = ManropeFamily,
                fontSize = 14.sp,
                color = Ink,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackLogin),
            )
        }
    }
}
