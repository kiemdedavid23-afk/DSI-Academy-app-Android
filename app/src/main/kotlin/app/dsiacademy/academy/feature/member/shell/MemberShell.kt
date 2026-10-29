package app.dsiacademy.academy.feature.member.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.config.SocialLinks
import app.dsiacademy.academy.core.designsystem.icons.SocialIcon
import app.dsiacademy.academy.core.designsystem.components.MemberAvatar
import app.dsiacademy.academy.core.designsystem.icons.DsiIcon
import app.dsiacademy.academy.core.designsystem.icons.DsiIcons
import app.dsiacademy.academy.core.designsystem.theme.FrauncesFamily
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import kotlinx.coroutines.launch

private val Surface = Color(0xFFFFFFFF)
private val Border = Color(0xFFE2E8F0)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF475569)
private val TextMuted = Color(0xFF94A3B8)
private val Gold = Color(0xFFD4AF37)
private val BluePrimary = Color(0xFF1E40AF)
private val RoyalStart = Color(0xFF0A1B54)
private val RoyalEnd = Color(0xFF1D4ED8)
private val GoldText = Color(0xFF8C6A12)


@Composable
fun MemberShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit,
    shellVm: MemberShellViewModel = hiltViewModel(),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val shellState by shellVm.state.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }

    val welcome = when {
        currentRoute.startsWith("house/community") -> "Bienvenue parmi les vôtres"
        currentRoute.startsWith("house/academy") -> "Bienvenue, on avance ensemble"
        currentRoute.startsWith("house/shop") -> "Bienvenue, faites-vous plaisir"
        currentRoute.startsWith("house/marketplace") -> "Bienvenue, bonnes trouvailles"
        else -> "Bienvenue chez vous"
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Déconnexion", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment vous déconnecter ?", fontFamily = ManropeFamily) },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; onLogout() }) {
                    Text("Se déconnecter", color = Color(0xFFB91C1C))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Annuler") }
            },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Surface,
            ) {
                MemberDrawerContent(
                    currentRoute = currentRoute,
                    fullName = shellState.fullName,
                    initials = shellState.initials,
                    avatarUrl = shellState.avatarUrl,
                    subState = shellState.subState,
                    subLine = shellState.subLine,
                    subFoot = shellState.subFoot,
                    subPercent = shellState.subPercent,
                    subBadge = shellState.subBadge,
                    subBtn = shellState.subBtn,
                    isDark = shellState.isDark,
                    onToggleTheme = shellVm::toggleTheme,
                    onNavigate = {
                        scope.launch { drawerState.close() }
                        onNavigate(it)
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        confirmLogout = true
                    },
                    onClose = { scope.launch { drawerState.close() } },
                )
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.member_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // léger assombrissement bas/haut comme le site
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x330B0D12),
                                Color.Transparent,
                                Color.Transparent,
                                Color(0x400B0D12),
                            ),
                        ),
                    ),
            )

            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                // Header royal — structure site : hamburger | logo+welcome | cloche+horloge
                var clock by remember { mutableStateOf(formatClock()) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(15_000)
                        clock = formatClock()
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Brush.horizontalGradient(listOf(RoyalStart, RoyalEnd))),
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Hamburger 3 barres (comme .dsi-hamburger-bar)
                        Column(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { scope.launch { drawerState.open() } }
                                .padding(10.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            repeat(3) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(Color.White),
                                )
                            }
                        }
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                welcome,
                                fontFamily = ManropeFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFBFDBFE),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "DSI Academy",
                                fontFamily = FrauncesFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                        }
                        IconButton(onClick = { onNavigate("member/notifications") }) {
                            DsiIcon(
                                DsiIcons.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Text(
                            clock,
                            fontFamily = ManropeFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }

                Box(Modifier.weight(1f).fillMaxWidth()) {
                    content()
                }

                // Bottom nav
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Surface.copy(alpha = 0.96f))
                        .border(1.dp, Border)
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MemberNav.mainHouses.forEach { house ->
                        val active = when (house.id) {
                            "member-space" -> currentRoute.startsWith("member/")
                            else -> currentRoute == house.route || currentRoute.startsWith(house.route)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = house.ready) { onNavigate(house.route) }
                                .padding(vertical = 6.dp),
                        ) {
                            when {
                                house.id == "member-space" -> {
                                    Image(
                                        painter = painterResource(R.drawable.espace_membre),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                else -> {
                                    val navVec = DsiIcons.navIcon(house.id)
                                    if (navVec != null) {
                                        DsiIcon(
                                            navVec,
                                            tint = if (active) BluePrimary else TextMuted,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                house.shortLabel,
                                fontFamily = ManropeFamily,
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) BluePrimary else TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun houseIcon(id: String): String = when (id) {
    "community" -> "💬"
    "academy" -> "🎓"
    "member-space" -> "🏠"
    "shop" -> "🛍️"
    "marketplace" -> "🏪"
    else -> "•"
}

@Composable
private fun MemberDrawerContent(
    currentRoute: String,
    fullName: String,
    initials: String,
    avatarUrl: String?,
    subState: String,
    subLine: String,
    subFoot: String,
    subPercent: Int,
    subBadge: String,
    subBtn: String,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        // Head
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("DSI Academy", fontFamily = ManropeFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("Espace membre", fontFamily = ManropeFamily, fontSize = 12.sp, color = TextMuted)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
            }
        }

        // User card
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .clickable { onNavigate("member/profile") }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MemberAvatar(
                imageUrl = avatarUrl,
                initials = initials.ifBlank { "?" },
                size = 44.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Bonjour", fontFamily = ManropeFamily, fontSize = 11.sp, color = TextMuted)
                Text(
                    fullName.ifBlank { "Membre DSI" },
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Border)

        Text(
            "MENU",
            fontFamily = ManropeFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        MemberNav.rooms.forEach { room ->
            val active = currentRoute == room.route
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (active) Color(0xFFEFF6FF) else Color.Transparent)
                    .clickable { onNavigate(room.route) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val roomVec = DsiIcons.roomIcon(room.id)
                if (roomVec != null) {
                    DsiIcon(roomVec, tint = if (active) BluePrimary else TextSecondary, modifier = Modifier.size(20.dp))
                } else {
                    Text("•", fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    room.label,
                    fontFamily = ManropeFamily,
                    fontSize = 14.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) BluePrimary else TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (active) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Gold))
                }
            }
        }

        HorizontalDivider(color = Border, modifier = Modifier.padding(vertical = 8.dp))

        // Subscription card
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.verticalGradient(listOf(RoyalStart, RoyalEnd)))
                .padding(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Abonnement", fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                if (subBadge.isNotBlank()) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(subBadge, fontFamily = ManropeFamily, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(subLine, fontFamily = ManropeFamily, fontSize = 13.sp, color = Color(0xFFBFDBFE))
            Spacer(Modifier.height(8.dp))
            // Progress bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(subPercent / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Gold),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(subFoot, fontFamily = ManropeFamily, fontSize = 11.sp, color = Color(0xFF93C5FD))
                Text(
                    subBtn,
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Gold,
                    modifier = Modifier.clickable { onNavigate("member/subscription") },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Theme toggle
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("☀️", fontSize = 16.sp, color = if (!isDark) BluePrimary else TextMuted)
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .width(44.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, Border, RoundedCornerShape(999.dp))
                    .background(if (isDark) RoyalStart else Surface)
                    .clickable(onClick = onToggleTheme),
            ) {
                Box(
                    Modifier
                        .padding(2.dp)
                        .size(20.dp)
                        .align(if (isDark) Alignment.CenterEnd else Alignment.CenterStart)
                        .clip(CircleShape)
                        .background(if (isDark) Gold else BluePrimary),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text("🌙", fontSize = 16.sp, color = if (isDark) BluePrimary else TextMuted)
        }

        Spacer(Modifier.height(16.dp))

        // Social
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            SocialLinks.all.forEach { link ->
                val enabled = link.href != null
                Box(
                    Modifier
                        .padding(horizontal = 6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (enabled) Color(0xFFF1F5F9) else Color(0xFFF8FAFC))
                        .then(
                            if (enabled) Modifier.clickable {
                                try { uriHandler.openUri(link.href!!) } catch (_: Exception) {}
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    SocialIcon(id = link.id, enabled = enabled)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Border)
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onLogout)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DsiIcon(DsiIcons.Logout, tint = Color(0xFFB91C1C), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Se déconnecter", fontFamily = ManropeFamily, fontSize = 14.sp, color = Color(0xFFB91C1C))
        }
    }
}

private fun roomIcon(id: String): String = when (id) {
    "home" -> "📊"
    "subscription" -> "⭐"
    "dsi-holdings" -> "🪙"
    "withdrawals" -> "💸"
    "payments" -> "🧾"
    "referral" -> "🤝"
    "notifications" -> "🔔"
    "profile" -> "👤"
    "settings" -> "⚙️"
    else -> "•"
}

private fun formatClock(): String {
    val cal = java.util.Calendar.getInstance()
    return String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
}
