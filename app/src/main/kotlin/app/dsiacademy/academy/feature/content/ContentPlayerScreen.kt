package app.dsiacademy.academy.feature.content

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

private val Gold = Color(0xFFD4AF37)
private val DarkPremium = Color(0xFF020817)
private val TextOnDark = Color.White
private val TextP = Color(0xFF0F172A)
private val TextM = Color(0xFF94A3B8)

@Composable
fun ContentPlayerScreen(
    onClose: () -> Unit,
    viewModel: ContentPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAudio = state.mode is PlayerMode.Audio
    val isVideo = state.mode is PlayerMode.Video

    Box(Modifier.fillMaxSize().background(if (isAudio || isVideo) DarkPremium else Color(0xFFF8FAFC))) {
        Column(Modifier.fillMaxSize()) {
            // ReaderChrome topbar — toujours sombre (site)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(DarkPremium)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("←", fontSize = 22.sp, color = TextOnDark, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(
                        state.title,
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextOnDark.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        state.contentType,
                        fontFamily = ManropeFamily,
                        fontSize = 11.sp,
                        color = TextOnDark.copy(alpha = 0.55f),
                    )
                }
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold, strokeWidth = 3.dp)
                }
                state.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, fontFamily = ManropeFamily, color = Color(0xFFFCA5A5), fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Réessayer",
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            modifier = Modifier.clickable { viewModel.open() },
                        )
                    }
                }
                state.mode != null -> when (val m = state.mode!!) {
                    is PlayerMode.Video -> Media3Player(
                        url = m.url, video = true, resumeMs = state.resumeMs,
                        onProgress = viewModel::saveMediaProgress,
                    )
                    is PlayerMode.Audio -> AudioFullScreen(
                        url = m.url, title = state.title, resumeMs = state.resumeMs,
                        onProgress = viewModel::saveMediaProgress,
                    )
                    is PlayerMode.Pdf -> PdfReader(
                        url = m.url, startPage = state.resumePage, onPage = viewModel::savePdfPage,
                    )
                    is PlayerMode.TextContent -> TextReader(url = m.url)
                    is PlayerMode.Image -> ImageReader(url = m.url)
                    is PlayerMode.External -> ExternalOpen(url = m.url, format = m.format)
                    is PlayerMode.Download -> ExternalOpen(url = m.url, format = m.format ?: "download")
                }
            }
        }
    }
}

@Composable
private fun Media3Player(
    url: String,
    video: Boolean,
    resumeMs: Long = 0,
    onProgress: (Long, Long) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val speeds = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
    var speedIdx by remember { mutableStateOf(1) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var playing by remember { mutableStateOf(true) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            if (resumeMs > 0) seekTo(resumeMs)
        }
    }
    DisposableEffect(player) {
        onDispose {
            onProgress(player.currentPosition, player.duration.coerceAtLeast(0))
            player.release()
        }
    }
    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0)
            playing = player.isPlaying
            kotlinx.coroutines.delay(500)
            if (player.isPlaying && position % 15000 < 600) {
                onProgress(player.currentPosition, player.duration.coerceAtLeast(0))
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        // Custom chrome overlay
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC020817)),
                    ),
                )
                .padding(16.dp),
        ) {
            // Scrubber
            val progress = if (duration > 0) position.toFloat() / duration else 0f
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable { /* scrub simplified: toggle play */ },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Gold),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "−10",
                    color = Color.White,
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
                    }.padding(8.dp),
                )
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Gold)
                        .clickable {
                            if (player.isPlaying) player.pause() else player.play()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (playing) "❚❚" else "▶",
                        color = Color(0xFF1A1300),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Text(
                    "+10",
                    color = Color.White,
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        player.seekTo(player.currentPosition + 10_000)
                    }.padding(8.dp),
                )
                Text(
                    "${speeds[speedIdx]}×",
                    color = Gold,
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        speedIdx = (speedIdx + 1) % speeds.size
                        player.setPlaybackSpeed(speeds[speedIdx])
                    }.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun AudioFullScreen(
    url: String,
    title: String,
    resumeMs: Long,
    onProgress: (Long, Long) -> Unit,
) {
    val context = LocalContext.current
    val speeds = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
    var speedIdx by remember { mutableStateOf(1) }
    var sleepMin by remember { mutableStateOf(0) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            if (resumeMs > 0) seekTo(resumeMs)
        }
    }
    DisposableEffect(player) {
        onDispose {
            onProgress(player.currentPosition, player.duration.coerceAtLeast(0))
            player.release()
        }
    }
    LaunchedEffect(player) {
        while (true) {
            kotlinx.coroutines.delay(15_000)
            if (player.isPlaying) onProgress(player.currentPosition, player.duration.coerceAtLeast(0))
        }
    }
    LaunchedEffect(sleepMin) {
        if (sleepMin > 0) {
            kotlinx.coroutines.delay(sleepMin * 60_000L)
            player.pause()
            sleepMin = 0
        }
    }

    // Palette disc dérivée du titre (site DISC_PALETTE)
    val palette = remember(title) {
        val palettes = listOf(
            Color(0xFF0F172A) to Gold,
            Color(0xFF1E3A5F) to Color(0xFFE8C77E),
            Color(0xFF3F2D1A) to Gold,
            Color(0xFF1E4620) to Color(0xFFE8C77E),
            Color(0xFF4A1E3D) to Color(0xFFE8C77E),
        )
        palettes[kotlin.math.abs(title.hashCode()) % palettes.size]
    }

    Box(Modifier.fillMaxSize().background(DarkPremium)) {
        // Backdrop blur approximation (radial glow)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(palette.first.copy(alpha = 0.45f), DarkPremium),
                        radius = 900f,
                    ),
                ),
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Spacer(Modifier.height(24.dp))
        // Disc
        Box(
            Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(palette.first, DarkPremium)),
                )
                .border(3.dp, Gold.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("🎧", fontSize = 64.sp)
        }
        Spacer(Modifier.height(16.dp))
        // Equalizer 48 barres (site BAR_COUNT)
        AudioEqualizer(active = true)
        Spacer(Modifier.height(20.dp))
        Text(
            title,
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = TextOnDark,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text("Audiothèque DSI", fontFamily = ManropeFamily, fontSize = 13.sp, color = TextOnDark.copy(alpha = 0.55f))
        Spacer(Modifier.weight(1f))

        // Controls
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CtrlBtn("−10") { player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0)) }
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Gold)
                    .clickable {
                        if (player.isPlaying) player.pause() else player.play()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("▶/❚❚", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1300), fontSize = 14.sp)
            }
            CtrlBtn("+10") { player.seekTo(player.currentPosition + 10_000) }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CtrlBtn("${speeds[speedIdx]}×") {
                speedIdx = (speedIdx + 1) % speeds.size
                player.setPlaybackSpeed(speeds[speedIdx])
            }
            CtrlBtn(if (sleepMin > 0) "Sleep ${sleepMin}m" else "Sleep") {
                sleepMin = when (sleepMin) {
                    0 -> 5
                    5 -> 15
                    15 -> 30
                    else -> 0
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    controllerShowTimeoutMs = 0
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
        Spacer(Modifier.height(16.dp))
    }
    }
}


@Composable
private fun AudioEqualizer(active: Boolean) {
    val bars = 48
    val anim = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(active) {
        while (active) {
            anim.animateTo(
                1f,
                animationSpec = androidx.compose.animation.core.tween(400),
            )
            anim.animateTo(
                0f,
                animationSpec = androidx.compose.animation.core.tween(400),
            )
        }
    }
    Canvas(Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 24.dp)) {
        val barWidth = size.width / (bars * 1.6f)
        val gap = barWidth * 0.6f
        for (i in 0 until bars) {
            val seed = ((i * 37) % 17) / 17f
            val h = (0.25f + seed * 0.75f * (0.4f + anim.value * 0.6f)) * size.height
            val x = i * (barWidth + gap)
            drawRoundRect(
                color = Gold.copy(alpha = 0.85f),
                topLeft = Offset(x, size.height - h),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
            )
        }
    }
}

@Composable
private fun CtrlBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gold)
    }
}

@Composable
private fun PdfReader(url: String, startPage: Int = 0, onPage: (Int, Int) -> Unit = { _, _ -> }) {
    var pageCount by remember { mutableStateOf(0) }
    var pageIndex by remember { mutableStateOf(startPage) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(1f) }
    var themeMode by remember { mutableStateOf(0) } // 0 clair 1 sépia 2 nuit
    val context = LocalContext.current
    val pageBg = when (themeMode) {
        1 -> Color(0xFFF4ECD8)
        2 -> Color(0xFF0F172A)
        else -> Color(0xFFF8FAFC)
    }

    LaunchedEffect(url, pageIndex) {
        loading = true
        error = null
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "dsi_read_${url.hashCode()}.pdf")
                if (!file.exists() || file.length() == 0L) {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 20000
                    conn.readTimeout = 30000
                    conn.inputStream.use { input ->
                        FileOutputStream(file).use { output -> input.copyTo(output) }
                    }
                    conn.disconnect()
                }
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
                val idx = pageIndex.coerceIn(0, (renderer.pageCount - 1).coerceAtLeast(0))
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(idx)
                    val bmp = Bitmap.createBitmap(
                        (page.width * 1.5f).toInt().coerceAtLeast(1),
                        (page.height * 1.5f).toInt().coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888,
                    )
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmap = bmp
                }
                renderer.close()
                pfd.close()
                onPage(idx, pageCount)
            } catch (e: Exception) {
                error = e.message ?: "Impossible d'ouvrir le PDF."
            } finally {
                loading = false
            }
        }
    }

    Column(Modifier.fillMaxSize().background(pageBg)) {
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = Color(0xFFFCA5A5), fontFamily = ManropeFamily)
            }
            else -> {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(pageBg)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale),
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(DarkPremium)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "←",
                        color = if (pageIndex > 0) Gold else TextOnDark.copy(alpha = 0.3f),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = pageIndex > 0) { pageIndex--; scale = 1f }
                            .padding(12.dp),
                    )
                    Text(
                        when (themeMode) { 1 -> "Sépia"; 2 -> "Nuit"; else -> "Clair" },
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { themeMode = (themeMode + 1) % 3 },
                    )
                    Text(
                        "${pageIndex + 1} / ${pageCount.coerceAtLeast(1)}",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (themeMode == 2) TextOnDark else TextP,
                        fontSize = 13.sp,
                    )
                    Text(
                        "→",
                        color = if (pageIndex < pageCount - 1) Gold else TextOnDark.copy(alpha = 0.3f),
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = pageIndex < pageCount - 1) { pageIndex++; scale = 1f }
                            .padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TextReader(url: String) {
    var text by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                text = URL(url).readText()
            } catch (e: Exception) {
                error = e.message
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color(0xFFFFFBF0))) {
        when {
            text == null && error == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = Color(0xFFB91C1C))
            }
            else -> Text(
                text!!,
                fontFamily = ManropeFamily,
                fontSize = 16.sp,
                color = TextP,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            )
        }
    }
}

@Composable
private fun ImageReader(url: String) {
    var scale by remember { mutableStateOf(1f) }
    Box(
        Modifier
            .fillMaxSize()
            .background(DarkPremium)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale),
        )
    }
}

@Composable
private fun ExternalOpen(url: String, format: String?) {
    val ctx = LocalContext.current
    val isYt = url.contains("youtube", true) || url.contains("youtu.be", true)
    val isEpub = format?.equals("epub", true) == true || url.endsWith(".epub", true)
    Column(
        Modifier.fillMaxSize().background(DarkPremium).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            when {
                isYt -> "Vidéo YouTube"
                isEpub -> "Livre EPUB"
                else -> "Ouverture externe"
            },
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = TextOnDark,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            when {
                isYt -> "Lecture via YouTube ou le navigateur"
                isEpub -> "Ouvrir avec une application EPUB"
                else -> format?.let { "Format : $it" } ?: "Lien provider"
            },
            fontFamily = ManropeFamily,
            fontSize = 13.sp,
            color = TextOnDark.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .clip(CircleShape)
                .background(Gold)
                .clickable {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                        if (isYt) intent.setPackage("com.google.android.youtube")
                        ctx.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
                    }
                }
                .padding(horizontal = 28.dp, vertical = 14.dp),
        ) {
            Text(
                if (isYt) "Ouvrir YouTube" else "Ouvrir",
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1300),
            )
        }
    }
}
