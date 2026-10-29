package app.dsiacademy.academy.feature.member.register

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import kotlinx.coroutines.delay

private val Ink = Color(0xFF0B0D12)
private val Gold = Color(0xFFC9A15A)
private val GoldBright = Color(0xFFE8C77E)
private val Paper = Color(0xFFF3F1EC)
private val TextCol = Color(0xFF1A1D24)
private val TextSoft = Color(0xFF565D6B)
private val Line = Color(0xFFE4E2DA)
private val UserBg = Color(0xFF0B0D12)
private val UserFg = Color(0xFFF2EFE6)
private val StatusGreen = Color(0xFF5FE3B3)
private val HeaderFg = Color(0xFFF2EFE6)
private val BtnText = Color(0xFF1A1300)
private val ErrorC = Color(0xFFC0392B)
private val Radius = 16.dp

private data class Etape(val champ: String, val question: String, val placeholder: String, val type: String, val optionnel: Boolean = false)

private val ETAPES = listOf(
    Etape("nom", "Quel est votre nom ?", "Ex: Traoré", "text"),
    Etape("prenom", "Et votre prénom ?", "Ex: Awa", "text"),
    Etape("pays", "Dans quel pays êtes-vous ?", "Code pays (ex: BF, CI, FR, SN…)", "text"),
    Etape("whatsapp", "Quel est votre numéro WhatsApp ?", "Ex: +226 78 62 75 30", "tel"),
    Etape("email", "Avez-vous un email à ajouter ? (facultatif)", "Optionnel", "email", true),
    Etape("mdp", "Créez un mot de passe (8 caractères minimum).", "Mot de passe", "password"),
    Etape("mdp2", "Confirmez votre mot de passe.", "Retapez le mot de passe", "password"),
)

private sealed class Msg {
    data class Bot(val id: Long, val text: String) : Msg()
    data class User(val id: Long, val text: String) : Msg()
    data class Typing(val id: Long = -1L) : Msg()
    data class Card(val id: Long, val index: Int) : Msg()
    data class Pay(val id: Long) : Msg()
    data class StartBtn(val id: Long = -2L) : Msg()
}

@Composable
fun RegisterScreen(
    codeParrain: String? = null,
    onNavigateLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val form by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var msgs by remember { mutableStateOf<List<Msg>>(emptyList()) }
    var idc by remember { mutableLongStateOf(0L) }
    fun nid() = (++idc)
    var localVal by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    var mdpRef by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf("accueil") } // accueil | inscription | paiement
    var pendingNext by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(codeParrain) { if (!codeParrain.isNullOrBlank()) viewModel.setCodeParrain(codeParrain) }
    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                RegisterUiEvent.NavigateLogin -> onNavigateLogin()
                is RegisterUiEvent.OpenPaymentUrl -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
            }
        }
    }

    // Reprise brouillon (comme le site)
    LaunchedEffect(Unit) {
        val draft = viewModel.restaurerBrouillonSiBesoin()
        if (draft != null) {
            phase = "paiement"
            msgs = listOf(
                Msg.Bot(nid(), "Vous aviez commencé votre inscription — reprenons là où vous en étiez 👋"),
                Msg.Pay(nid()),
            )
            return@LaunchedEffect
        }
        // Accueil
        msgs = listOf(Msg.Typing())
        delay(500)
        msgs = listOf(
            Msg.Bot(nid(), "Bonjour 👋 Bienvenue sur Digital Service International. Je peux répondre à vos questions, ou vous accompagner directement pour créer votre compte."),
        )
        msgs = msgs + Msg.Typing()
        delay(900)
        msgs = msgs.filterNot { it is Msg.Typing } + Msg.Bot(
            nid(),
            "DSI Academy vous donne accès à une bibliothèque numérique, des formations, des audios, des vidéos et des outils IA — accessible 24h/24 depuis votre téléphone.",
        ) + Msg.StartBtn()
    }

    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.lastIndex)
    }

    // Quand paymentUrl arrive → carte paiement
    LaunchedEffect(form.paymentUrl, form.messageErreur) {
        if (form.paymentUrl != null && phase == "inscription" && msgs.none { it is Msg.Pay }) {
            msgs = msgs.filterNot { it is Msg.Card || it is Msg.Typing } +
                Msg.Bot(nid(), "Merci ! Votre compte est presque prêt. Il reste uniquement l'activation de votre accès.") +
                Msg.Pay(nid())
            phase = "paiement"
        }
        if (form.messageErreur != null && form.paymentUrl == null && phase == "inscription" && form.chargement.not()) {
            // erreur création
        }
    }

    fun startSignup() {
        msgs = msgs.filterNot { it is Msg.StartBtn } + Msg.User(nid(), "Commencer mon inscription")
        phase = "inscription"
    }

    LaunchedEffect(phase) {
        if (phase != "inscription") return@LaunchedEffect
        if (msgs.any { it is Msg.Card }) return@LaunchedEffect
        delay(450)
        msgs = msgs.filterNot { it is Msg.Typing } +
            Msg.Bot(nid(), "Bienvenue dans DSI Academy. Commençons votre inscription.") +
            Msg.Bot(nid(), ETAPES[0].question) +
            Msg.Card(nid(), 0)
        localVal = ""
    }

    fun valider(etape: Etape, index: Int, valeur: String) {
        val v = valeur.trim()
        val ok = when (etape.champ) {
            "nom", "prenom" -> v.length >= 2
            "pays" -> v.isNotEmpty()
            "whatsapp" -> v.replace(Regex("[^0-9+]"), "").length >= 8
            "email" -> true
            "mdp" -> v.length >= 8
            "mdp2" -> v.length >= 8 && v == mdpRef
            else -> true
        }
        if (!ok) { invalid = true; return }
        invalid = false
        if (etape.champ == "mdp") mdpRef = v
        val display = if (etape.champ == "mdp" || etape.champ == "mdp2") "••••••••" else v.ifBlank { "(non renseigné)" }
        if (etape.champ != "mdp2") viewModel.setField(etape.champ, v)
        msgs = msgs.filterNot { it is Msg.Card } + Msg.User(nid(), display)

        val next = index + 1
        if (next < ETAPES.size) {
            msgs = msgs + Msg.Typing()
            // continue in coroutine via side effect flag
            pendingNext = next
        } else {
            viewModel.soumettreInscription()
            msgs = msgs + Msg.Typing()
        }
    }
    LaunchedEffect(pendingNext) {
        val n = pendingNext ?: return@LaunchedEffect
        delay(450)
        msgs = msgs.filterNot { it is Msg.Typing } + Msg.Bot(nid(), ETAPES[n].question) + Msg.Card(nid(), n)
        localVal = ""
        pendingNext = null
    }

    LaunchedEffect(form.chargement, form.paymentUrl, form.messageErreur) {
        if (!form.chargement && form.paymentUrl == null && form.messageErreur != null && phase == "inscription") {
            msgs = msgs.filterNot { it is Msg.Typing } + Msg.Bot(nid(), form.messageErreur ?: "Erreur")
        }
        if (!form.chargement && form.paymentUrl != null) {
            msgs = msgs.filterNot { it is Msg.Typing }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Fond image comme .dsi-signup-bg du site
        Image(
            painter = painterResource(R.drawable.inscription_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.35f,
        )
        Box(Modifier.fillMaxSize().background(Paper.copy(alpha = 0.82f)))
        Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Ink).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(R.drawable.logo_dsi), null, Modifier.height(30.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Assistant DSI", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 15.2.sp, color = HeaderFg)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(StatusGreen))
                    Spacer(Modifier.width(5.dp))
                    Text("En ligne", fontFamily = ManropeFamily, fontSize = 11.5.sp, color = StatusGreen)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(msgs.size) { i ->
                when (val m = msgs[i]) {
                    is Msg.Bot -> BotBubble(m.text)
                    is Msg.User -> UserBubble(m.text)
                    is Msg.Typing -> TypingRow()
                    is Msg.StartBtn -> GoldButton("Commencer mon inscription") { startSignup() }
                    is Msg.Card -> {
                        val etape = ETAPES[m.index]
                        StepCard(
                            etape = etape,
                            value = localVal,
                            onValueChange = { localVal = it; invalid = false },
                            invalid = invalid,
                            loading = form.chargement,
                            onSubmit = { valider(etape, m.index, localVal) },
                        )
                    }
                    is Msg.Pay -> PayCard(
                        url = form.paymentUrl,
                        loading = form.chargement,
                        error = form.messageErreur,
                        onOpen = {
                            form.paymentUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                        },
                        onCheck = { viewModel.verifierPaiement() },
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
            Text("Déjà un compte ? ", fontFamily = ManropeFamily, fontSize = 14.sp, color = TextSoft)
            Text("Se connecter", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink,
                modifier = Modifier.clickable { onNavigateLogin() })
        }
        } // Column
    } // Box
}

@Composable private fun BotBubble(text: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(Brush.radialGradient(listOf(GoldBright, Gold))),
            contentAlignment = Alignment.Center,
        ) { Text("✦", color = BtnText, fontSize = 13.sp) }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(Radius)).background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(Radius)).padding(horizontal = 16.dp, vertical = 13.dp),
        ) {
            Text(text, fontFamily = ManropeFamily, fontSize = 14.7.sp, color = TextCol, lineHeight = 22.sp)
        }
    }
}

@Composable private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
        Box(
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(Radius)).background(UserBg)
                .padding(horizontal = 16.dp, vertical = 13.dp),
        ) {
            Text(text, fontFamily = ManropeFamily, fontSize = 14.7.sp, color = UserFg, lineHeight = 22.sp)
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF333333)), contentAlignment = Alignment.Center) {
            Text("👤", fontSize = 13.sp)
        }
    }
}

@Composable private fun TypingRow() {
    val t = rememberInfiniteTransition(label = "dots")
    val a by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "a")
    Row(verticalAlignment = Alignment.Bottom) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Brush.radialGradient(listOf(GoldBright, Gold))), contentAlignment = Alignment.Center) {
            Text("✦", color = BtnText, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Row(
            Modifier.clip(RoundedCornerShape(Radius)).background(Color.White).border(1.dp, Line, RoundedCornerShape(Radius)).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(3) { Box(Modifier.size(6.dp).clip(CircleShape).background(TextSoft.copy(alpha = a))) }
        }
    }
}

@Composable private fun GoldButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(48.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(GoldBright, Gold))).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.7.sp, color = BtnText)
    }
}

@Composable
private fun StepCard(
    etape: Etape, value: String, onValueChange: (String) -> Unit, invalid: Boolean, loading: Boolean, onSubmit: () -> Unit,
) {
    val isPwd = etape.type == "password"
    val isPays = etape.champ == "pays"
    var menuOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius)).background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(Radius)).padding(16.dp),
    ) {
        if (isPays) {
            val selectedLabel = PAYS_OPTIONS.find { it.value == value }?.label ?: ""
            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().border(1.dp, if (invalid) ErrorC else Line, RoundedCornerShape(10.dp))
                        .clickable { menuOpen = true }.padding(horizontal = 14.dp, vertical = 13.dp),
                ) {
                    Text(
                        if (value.isEmpty()) "Sélectionner un pays" else selectedLabel,
                        fontFamily = ManropeFamily, fontSize = 15.sp,
                        color = if (value.isEmpty()) TextSoft.copy(alpha = 0.7f) else TextCol,
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, modifier = Modifier.heightIn(max = 320.dp).fillMaxWidth(0.9f)) {
                    // Filtre rapide
                    Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth()) {
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 14.sp, color = TextCol),
                            cursorBrush = SolidColor(Gold),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(8.dp)).padding(10.dp),
                            decorationBox = { inner ->
                                Box {
                                    if (query.isEmpty()) Text("Rechercher…", fontFamily = ManropeFamily, fontSize = 14.sp, color = TextSoft)
                                    inner()
                                }
                            },
                        )
                    }
                    val filtered = PAYS_OPTIONS.filter {
                        it.value.isNotEmpty() && (query.isBlank() || it.label.contains(query, true) || it.value.contains(query, true))
                    }
                    filtered.take(80).forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.label, fontFamily = ManropeFamily, fontSize = 14.sp) },
                            onClick = {
                                onValueChange(opt.value)
                                menuOpen = false
                                query = ""
                            },
                        )
                    }
                }
            }
        } else {
        Box(
            Modifier.fillMaxWidth().border(1.dp, if (invalid) ErrorC else Line, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
        ) {
            if (value.isEmpty()) {
                Text(etape.placeholder, fontFamily = ManropeFamily, fontSize = 15.sp, color = TextSoft.copy(alpha = 0.7f))
            }
            BasicTextField(
                value = value, onValueChange = onValueChange, singleLine = true,
                textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 15.sp, color = TextCol),
                cursorBrush = SolidColor(Gold),
                visualTransformation = if (isPwd) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (etape.type) {
                        "tel" -> KeyboardType.Phone
                        "email" -> KeyboardType.Email
                        "password" -> KeyboardType.Password
                        else -> KeyboardType.Text
                    }
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        } // else not pays
        if (invalid) {
            Spacer(Modifier.height(6.dp))
            Text(
                if (etape.champ == "mdp2") "Les mots de passe ne correspondent pas." else "Merci de compléter ce champ correctement.",
                fontFamily = ManropeFamily, fontSize = 12.sp, color = ErrorC,
            )
        }
        Spacer(Modifier.height(12.dp))
        GoldButton(if (etape.optionnel && value.isBlank()) "Passer" else "Suivant") {
            if (!loading) onSubmit()
        }
    }
}

@Composable
private fun PayCard(url: String?, loading: Boolean, error: String?, onOpen: () -> Unit, onCheck: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius)).background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(Radius)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("2 000 FCFA", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink)
        Spacer(Modifier.height(6.dp))
        Text("Chariow · Swinmo Shop", fontFamily = ManropeFamily, fontSize = 13.sp, color = TextSoft)
        Spacer(Modifier.height(16.dp))
        if (url != null) {
            GoldButton("Ouvrir le paiement", onOpen)
            Spacer(Modifier.height(10.dp))
        }
        Box(
            Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).background(Ink).clickable(enabled = !loading, onClick = onCheck),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(16.dp), Color.White, strokeWidth = 2.dp)
            else Text("J'ai payé — vérifier", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 14.7.sp, color = Color.White)
        }
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, fontFamily = ManropeFamily, fontSize = 13.sp, color = ErrorC)
        }
    }
}
