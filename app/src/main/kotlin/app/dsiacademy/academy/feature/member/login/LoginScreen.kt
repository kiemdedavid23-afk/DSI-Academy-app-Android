package app.dsiacademy.academy.feature.member.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.IbmPlexMonoFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily

private val Ink = Color(0xFF0B0D12)
private val Gold = Color(0xFFC9A15A)
private val GoldBright = Color(0xFFE8C77E)
private val Paper = Color(0xFFF3F1EC)
private val TextCol = Color(0xFF1A1D24)
private val TextSoft = Color(0xFF565D6B)
private val Line = Color(0xFFE4E2DA)
private val Emerald = Color(0xFF1F7A5C)
private val ErrorBorder = Color(0xFFC0392B)
private val ErrorBg = Color(0xFFFBEAE9)
private val ErrorText = Color(0xFF9A3B32)
private val InfoBg = Color(0xFFEFF6F2)
private val BtnText = Color(0xFF1A1300)

@Composable
fun LoginScreen(
    onNavigateMemberHome: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onNavigateInscriptionPaiement: (userId: String?, token: String?, url: String?) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginUiEvent.NavigateMemberHome -> onNavigateMemberHome()
                LoginUiEvent.NavigateRegister -> onNavigateRegister()
                LoginUiEvent.NavigateForgotPassword -> onNavigateForgotPassword()
                is LoginUiEvent.NavigateInscriptionPaiement ->
                    onNavigateInscriptionPaiement(event.userId, event.paymentTrackingToken, event.paymentUrl)
            }
        }
    }
    LoginContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun LoginContent(state: LoginUiState, onAction: (LoginUiAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_dsi),
                contentDescription = "DSI Academy",
                modifier = Modifier.height(28.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.width(10.dp))
            Text("DSI Academy", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.8.sp, color = Ink)
        }

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = true).padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp).fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x2E0B0D12), spotColor = Color(0x2E0B0D12))
                    .clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(20.dp))
                    .padding(horizontal = 28.dp, vertical = 36.dp),
            ) {
                Text("ESPACE MEMBRE", fontFamily = IbmPlexMonoFamily, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, letterSpacing = 1.5.sp, color = Gold)
                Spacer(Modifier.height(10.dp))
                Text("Bon retour", fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 27.2.sp, color = Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Connectez-vous pour accéder à vos formations, votre bibliothèque et votre solde DSI.",
                    fontFamily = ManropeFamily, fontSize = 14.4.sp, color = TextSoft, lineHeight = 21.6.sp,
                )
                Spacer(Modifier.height(26.dp))

                DsiLoginField("Email ou numéro WhatsApp", state.identifiant, { onAction(LoginUiAction.IdentifiantChanged(it)) },
                    "vous@exemple.com", state.idInvalide, "Renseignez votre email ou votre numéro WhatsApp.",
                    KeyboardType.Email, ImeAction.Next)
                Spacer(Modifier.height(16.dp))
                DsiLoginField("Mot de passe", state.motDePasse, { onAction(LoginUiAction.MotDePasseChanged(it)) },
                    "Votre mot de passe", state.mdpInvalide, "Renseignez votre mot de passe.",
                    KeyboardType.Password, ImeAction.Done, true, state.afficherMdp,
                    { onAction(LoginUiAction.ToggleAfficherMdp) }, { onAction(LoginUiAction.Submit) })
                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            onAction(LoginUiAction.SouvenirChanged(!state.souvenir))
                        },
                    ) {
                        Box(
                            Modifier.size(15.dp).border(1.5.dp, if (state.souvenir) Gold else TextSoft, RoundedCornerShape(3.dp))
                                .background(if (state.souvenir) Gold else Color.Transparent, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center,
                        ) { if (state.souvenir) Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(7.dp))
                        Text("Se souvenir de moi", fontFamily = ManropeFamily, fontSize = 13.3.sp, color = TextSoft)
                    }
                    Text("Mot de passe oublié ?", fontFamily = ManropeFamily, fontSize = 13.3.sp, color = TextSoft,
                        textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { onAction(LoginUiAction.ForgotPassword) })
                }
                Spacer(Modifier.height(18.dp))

                Box(
                    Modifier.fillMaxWidth().height(48.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GoldBright, Gold)))
                        .clickable(enabled = !state.chargement) { onAction(LoginUiAction.Submit) },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.chargement) {
                            CircularProgressIndicator(Modifier.size(16.dp), BtnText, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (state.chargement) "Connexion…" else "Se connecter", fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Bold, fontSize = 14.7.sp, color = BtnText.copy(alpha = if (state.chargement) 0.6f else 1f))
                    }
                }

                if (state.messageErreur != null) {
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ErrorBg).padding(horizontal = 14.dp, vertical = 11.dp)) {
                        Text(state.messageErreur!!, fontFamily = ManropeFamily, fontSize = 13.4.sp, color = ErrorText, lineHeight = 20.sp)
                    }
                }
            }
        }

        Text("·", color = TextSoft, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Pas encore de compte ? ", fontFamily = ManropeFamily, fontSize = 14.sp, color = TextSoft)
            Text("Créer un compte", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink,
                modifier = Modifier.clickable { onAction(LoginUiAction.GoRegister) })
        }
        Text("© DSI Academy", fontFamily = ManropeFamily, fontSize = 12.2.sp, color = TextSoft, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(18.dp))
    }
}

@Composable
private fun DsiLoginField(
    label: String, value: String, onValueChange: (String) -> Unit, placeholder: String,
    isError: Boolean, errorText: String, keyboardType: KeyboardType, imeAction: ImeAction,
    isPassword: Boolean = false, showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null, onDone: (() -> Unit)? = null,
) {
    Text(label, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextSoft)
    Spacer(Modifier.height(6.dp))
    Box(
        Modifier.fillMaxWidth().border(1.dp, if (isError) ErrorBorder else Line, RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(placeholder, fontFamily = ManropeFamily, fontSize = 15.2.sp, color = TextSoft.copy(alpha = 0.7f))
                BasicTextField(
                    value = value, onValueChange = onValueChange, singleLine = true,
                    textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 15.2.sp, color = TextCol),
                    cursorBrush = SolidColor(Gold),
                    visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                    keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isPassword && onTogglePassword != null) {
                IconButton(onClick = onTogglePassword, modifier = Modifier.size(28.dp)) {
                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Afficher le mot de passe", tint = TextSoft, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
    if (isError) {
        Spacer(Modifier.height(5.dp))
        Text(errorText, fontFamily = ManropeFamily, fontSize = 12.2.sp, color = ErrorBorder)
    }
}
