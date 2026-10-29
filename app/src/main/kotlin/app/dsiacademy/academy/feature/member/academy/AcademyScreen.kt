package app.dsiacademy.academy.feature.member.academy

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.IbmPlexMonoFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import app.dsiacademy.academy.core.session.LocalContentStores
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private val Gold = Color(0xFFD4AF37)
private val GoldBorder = Color(0x66D4AF37)
private val TextOnDark = Color(0xFFFFFFFF)
private val DarkPremium = Color(0xFF020817)

data class AcademyHouse(
    val id: String,
    val contentType: String,
    @DrawableRes val imageRes: Int,
    val title: String,
    val tagline: String,
)

private val HOUSES = listOf(
    AcademyHouse("library", "book", R.drawable.house_library, "Bibliothèque", "Livres et documents"),
    AcademyHouse("videos", "video", R.drawable.house_videos, "Vidéothèque", "Cours et replays"),
    AcademyHouse("formations", "training", R.drawable.house_formations, "Formations", "Parcours structurés"),
    AcademyHouse("audio", "audio", R.drawable.house_audio, "Audiothèque", "Podcasts et livres audio"),
)

@HiltViewModel
class AcademyViewModel @Inject constructor(
    localContent: LocalContentStores,
) : ViewModel() {
    val continueEntry = localContent.continueFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun AcademyScreen(
    onOpenHouse: (contentType: String, title: String) -> Unit = { _, _ -> },
    viewModel: AcademyViewModel = hiltViewModel(),
) {
    val continueEntry by viewModel.continueEntry.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // HÉROS — calqué .dsi-ac-hero
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, GoldBorder, RoundedCornerShape(16.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.hub_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x26020817), Color(0xE6020817)),
                        ),
                    ),
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (continueEntry != null) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x24D4AF37))
                            .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "CONTINUER",
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Gold,
                        )
                    }
                    Text(
                        continueEntry!!.title,
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,
                        color = TextOnDark,
                        lineHeight = 30.sp,
                    )
                    Text(
                        continueEntry!!.house,
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = TextOnDark.copy(alpha = 0.85f),
                    )
                    continueEntry!!.progress?.let { pct ->
                        Box(
                            Modifier
                                .widthIn(max = 320.dp)
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(pct / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(Gold),
                            )
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x24D4AF37))
                            .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "GRANDE MAISON DSI",
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Gold,
                        )
                    }
                    Text(
                        "Academy",
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        color = TextOnDark,
                    )
                    Text(
                        "Bienvenue, on avance ensemble. Explorez les quatre maisons du savoir.",
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = TextOnDark.copy(alpha = 0.85f),
                        lineHeight = 20.sp,
                    )
                }
            }
        }

        Text(
            "Les quatre maisons",
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF0F172A),
        )

        HOUSES.forEach { h ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GoldBorder, RoundedCornerShape(16.dp))
                    .clickable { onOpenHouse(h.contentType, h.title) },
            ) {
                Image(
                    painter = painterResource(h.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x33020817), Color(0xE0020817)),
                            ),
                        ),
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text(
                        h.title,
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = TextOnDark,
                    )
                    Text(
                        h.tagline,
                        fontFamily = ManropeFamily,
                        fontSize = 13.sp,
                        color = TextOnDark.copy(alpha = 0.85f),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
