package app.dsiacademy.academy.feature.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.domain.content.FormationNode
import coil.compose.AsyncImage

private val Gold = Color(0xFFD4AF37)
private val GoldB = Color(0xFFE8C77E)
private val TextP = Color(0xFF0F172A)
private val TextM = Color(0xFF94A3B8)
private val TextS = Color(0xFF475569)
private val Surface = Color.White
private val Border = Color(0xFFE2E8F0)
private val BtnT = Color(0xFF1A1300)

@Composable
fun ContentDetailScreen(
    onPlay: (id: String, title: String, type: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ContentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("← Retour", fontFamily = ManropeFamily, color = TextS, modifier = Modifier.clickable(onClick = onBack).padding(bottom = 12.dp))

        when {
            state.loading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            state.error != null -> Text(state.error!!, color = Color(0xFFB91C1C), fontFamily = ManropeFamily)
            state.tree != null -> {
                val tree = state.tree!!
                Text(tree.title, fontFamily = FrauncesFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextP)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Formation — ouvrez un module (jamais deliver sur l'id formation).",
                    fontFamily = ManropeFamily, fontSize = 13.sp, color = TextM,
                )
                Spacer(Modifier.height(16.dp))
                FormationTreeList(tree.children, onPlay)
            }
            state.item != null -> {
                val item = state.item!!
                item.coverUrl?.let {
                    AsyncImage(
                        model = it, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.height(14.dp))
                }
                Text(item.title, fontFamily = FrauncesFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextP)
                Spacer(Modifier.height(6.dp))
                Text(item.type, fontFamily = ManropeFamily, fontSize = 12.sp, color = TextM)
                item.summary?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, fontFamily = ManropeFamily, fontSize = 14.sp, color = TextS, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier.fillMaxWidth().height(48.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GoldB, Gold)))
                        .clickable { onPlay(item.id, item.title, item.type) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Lire / Écouter", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, color = BtnT)
                }
                if (state.downloadable) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier.fillMaxWidth().height(48.dp).clip(CircleShape)
                            .border(1.dp, Border, CircleShape)
                            .clickable { onPlay(item.id, item.title, "download") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Télécharger", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, color = TextP)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormationTreeList(nodes: List<FormationNode>, onPlay: (String, String, String) -> Unit, depth: Int = 0) {
    nodes.forEach { node ->
        Column(Modifier.padding(start = (depth * 12).dp, bottom = 8.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                    .clickable(enabled = node.id.isNotBlank() && node.type != "training") {
                        onPlay(node.id, node.title, node.type)
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    when (node.type) {
                        "video" -> "🎬"
                        "audio" -> "🎧"
                        "book" -> "📄"
                        else -> "📁"
                    },
                    fontSize = 18.sp,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(node.title, fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextP)
                    Text(node.type, fontFamily = ManropeFamily, fontSize = 11.sp, color = TextM)
                }
                if (node.type != "training" && node.id.isNotBlank()) {
                    Text("Lire", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gold)
                }
            }
            if (node.children.isNotEmpty()) {
                FormationTreeList(node.children, onPlay, depth + 1)
            }
        }
    }
}
