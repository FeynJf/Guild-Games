package com.guild.games.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.guild.games.data.Guild
import com.guild.games.data.Repo
import com.guild.games.data.UpdateManager
import com.guild.games.data.UpdateUi
import com.guild.games.data.UserProfile
import com.guild.games.ui.theme.*

@Composable
fun MainScreen(onCreateGuild: () -> Unit, onProfile: () -> Unit, onSettings: () -> Unit, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(Unit) {
        UpdateManager.autoCheckOnce(ctx)
        profile = Repo.loadProfile()
    }
    val role = profile?.role.orEmpty()
    val update by UpdateManager.state.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val guilds by remember { Repo.observeGuilds() }.collectAsState(initial = emptyList())
    val canCreate = role == "developer" || role == "admin"

    val banner: Pair<String, String>? = when (val u = update) {
        is UpdateUi.Available -> "Pembaruan ${u.info.versionName} tersedia" to "Ketuk untuk mengunduh"
        is UpdateUi.Downloading ->
            "Mengunduh pembaruan…" to "${if (u.total > 0) (u.done * 100 / u.total).toInt() else 0}%"
        is UpdateUi.Ready -> "Pembaruan siap dipasang" to "Ketuk untuk memasang"
        else -> null
    }

    Column(Modifier.gameBackground()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LogoMark(30.dp)
            Text("GUILD GAMES", style = logoStyle(18), modifier = Modifier.weight(1f))
            Box {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(PanelHigh).clickable { onSettings() },
                    contentAlignment = Alignment.Center
                ) { Text("⚙️", fontSize = 18.sp) }
                if (banner != null) Box(
                    Modifier.size(11.dp).align(Alignment.TopEnd).clip(CircleShape).background(ErrorRed)
                )
            }
            AvatarCircle(
                profile?.username.orEmpty(), profile?.photoUrl.orEmpty(), 40.dp,
                Modifier.clickable { onProfile() }
            )
        }
        if (banner != null) Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                .glass(16.dp).clickable { onSettings() }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🎉", fontSize = 22.sp)
            Column(Modifier.weight(1f)) {
                Text(banner.first, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(banner.second, color = TextDim, fontSize = 12.sp)
            }
            Text("›", color = TextDim, fontSize = 22.sp)
        }
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.weight(1f).glass(26.dp).padding(4.dp)) {
                listOf("Guild", "Announcement", "Helper").forEachIndexed { i, t ->
                    TabPill(t, tab == i, Modifier.weight(1f)) { tab = i }
                }
            }
            if (canCreate) PlusButton(onCreateGuild)
        }
        when (tab) {
            0 -> if (guilds.isEmpty()) EmptyState("🛡️", "Belum ada Guild",
                if (canCreate) "Ketuk tombol + untuk membuat guild pertama"
                else "Guild akan muncul di sini setelah dibuat")
            else LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 20.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) { items(guilds, key = { it.id }) { GuildCard(it) } }
            1 -> EmptyState("📢", "Belum ada Announcement", "Pengumuman guild akan tampil di sini")
            else -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🛠️", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("Helper segera hadir", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(28.dp))
                OutlineNeonButton("Keluar") { onLogout() }
            }
        }
    }
}

@Composable
private fun PlusButton(onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp)
            .shadow(10.dp, CircleShape, ambientColor = NeonPurple, spotColor = NeonPurple)
            .clip(CircleShape).background(ButtonBrush).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White) }
}

@Composable
private fun TabPill(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(22.dp))
            .then(if (selected) Modifier.background(ButtonBrush) else Modifier)
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, maxLines = 1, softWrap = false, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else TextDim)
    }
}

@Composable
private fun EmptyState(icon: String, title: String, hint: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 56.sp)
        Spacer(Modifier.height(10.dp))
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(hint, color = TextDim, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Chip(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun GuildCard(g: Guild) {
    val fraction = if (g.maxMembers > 0) (g.memberCount.toFloat() / g.maxMembers).coerceIn(0f, 1f) else 0f
    Column(Modifier.fillMaxWidth().glass().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(NeonBrush).padding(2.dp)) {
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(PanelHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (g.logoUrl.isNotEmpty())
                        AsyncImage(g.logoUrl, g.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Text(g.name.take(1).uppercase(), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                        color = NeonPurple)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(g.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row {
                    repeat(5) { i ->
                        Text("★", fontSize = 16.sp, color = if (i < g.stars) Gold else Stroke)
                    }
                }
            }
        }
        if (g.description.isNotBlank())
            Text(g.description, fontSize = 13.sp, color = TextDim, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("👥 ${g.memberCount}/${g.maxMembers}", NeonBlue)
            if (g.requirements.isNotBlank())
                Chip("📋 ${g.requirements}", NeonPurple, Modifier.weight(1f, fill = false))
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Stroke)) {
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(NeonBrush))
        }
    }
}
