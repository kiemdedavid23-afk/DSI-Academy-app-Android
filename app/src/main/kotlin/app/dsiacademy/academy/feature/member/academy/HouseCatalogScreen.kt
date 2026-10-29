package app.dsiacademy.academy.feature.member.academy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import coil.compose.AsyncImage

private val Gold = Color(0xFFD4AF37)
private val TextP = Color(0xFF0F172A)
private val TextM = Color(0xFF94A3B8)
private val TextS = Color(0xFF475569)
private val Surface = Color.White
private val Border = Color(0xFFE2E8F0)
private val GoldLight = Color(0xFFFDF6E2)

@Composable
fun HouseCatalogScreen(
    onOpenContent: (id: String, title: String, type: String) -> Unit = { _, _, _ -> },
    viewModel: HouseCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(state.title, fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextP)
        Spacer(Modifier.height(4.dp))
        Text("type=${state.type}", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextM)
        Spacer(Modifier.height(12.dp))

        // Search
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                singleLine = true,
                textStyle = TextStyle(fontFamily = ManropeFamily, fontSize = 14.sp, color = TextP),
                cursorBrush = SolidColor(Gold),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (state.query.isEmpty()) {
                        Text("Rechercher…", fontFamily = ManropeFamily, fontSize = 14.sp, color = TextM)
                    }
                    inner()
                },
            )
            Text(
                "OK",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                color = Gold,
                modifier = Modifier.clickable { viewModel.search() }.padding(start = 8.dp),
            )
        }

        // Categories
        if (state.categories.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                CategoryChip("Tous", state.selectedCategoryId == null) { viewModel.selectCategory(null) }
                state.categories.forEach { cat ->
                    Spacer(Modifier.width(8.dp))
                    CategoryChip("${cat.name} (${cat.count})", state.selectedCategoryId == cat.id) {
                        viewModel.selectCategory(cat.id)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.loading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            state.error != null -> Text(state.error!!, fontFamily = ManropeFamily, color = Color(0xFFB91C1C), fontSize = 14.sp)
            state.items.isEmpty() -> Text(
                "Aucun contenu pour le moment.",
                fontFamily = ManropeFamily, fontSize = 13.sp, color = TextM,
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items, key = { it.id }) { item ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface)
                            .border(1.dp, Color(0x66D4AF37), RoundedCornerShape(14.dp))
                            .clickable { onOpenContent(item.id, item.title, item.type.ifBlank { state.type }) },
                    ) {
                        if (!item.coverUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.coverUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column(Modifier.padding(14.dp)) {
                            Text(item.title, fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextP)
                            item.summary?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, fontFamily = ManropeFamily, fontSize = 13.sp, color = TextS, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) GoldLight else Surface)
            .border(1.dp, if (selected) Gold else Border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontFamily = ManropeFamily,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Gold else TextP,
        )
    }
}
