package app.dsiacademy.academy.feature.public

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import kotlinx.coroutines.delay

private val Ink = Color(0xFF0B0D12)
private val Gold = Color(0xFFC9A15A)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onFinished()
    }
    Box(
        Modifier.fillMaxSize().background(Ink),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo_dsi),
                contentDescription = null,
                modifier = Modifier.height(56.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "DSI Academy",
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = Gold,
            )
        }
    }
}
