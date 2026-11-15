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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.guild.games.data.Guild
import com.guild.games.data.Repo
import com.guild.games.ui.theme.*

@Composable
fun MainScreen(onCreateGuild: () -> Unit, onLogout: () -> Unit) {
    var role by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { role = Repo.currentRole() }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val guilds by remember { Repo.observeGuilds() }.collectAsState(initial = emptyList())
    val canCreate = role == "developer" || role == "admin"

    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().background(Surface).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Guild", "Announcement", "Helper").forEachIndexed { i, t ->
                TabItem(t, tab == i, Modifier.weight(1f)) { tab = i }
            }
            if (canCreate) {
                Box(
                    Modifier.padding(start = 4.dp).size(44.dp).clip(CircleShape)
                        .background(NeonPurple).clickable { onCreateGuild() },
                    contentAlignment = Alignment.Center
                ) { Text("+", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
        when (tab) {
            0 -> if (guilds.isEmpty()) Centered("Belum ada Guild") else
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) { items(guilds, key = { it.id }) { GuildCard(it) } }
            1 -> Centered("Belum ada Announcement")
            else -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Helper segera hadir", color = TextDim, fontSize = 16.sp)
                Spacer(Modifier.height(24.dp))
                OutlineNeonButton("Keluar", onLogout)
            }
        }
    }
}

@Composable
private fun TabItem(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clickable { onClick() }.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, maxLines = 1, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = if (selected) NeonPurple else TextDim)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.height(3.dp).width(28.dp).clip(RoundedCornerShape(2.dp))
            .background(if (selected) NeonPurple else Color.Transparent))
    }
}

@Composable
private fun Centered(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = TextDim, fontSize = 16.sp)
    }
}

@Composable
private fun GuildCard(g: Guild) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF22223A)),
            contentAlignment = Alignment.Center) {
            if (g.logoUrl.isNotEmpty())
                AsyncImage(g.logoUrl, g.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(g.name.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NeonPurple)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(g.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Row {
                repeat(5) { i -> Text("★", fontSize = 14.sp, color = if (i < g.stars) Gold else Color(0xFF3A3A55)) }
                Spacer(Modifier.width(8.dp))
                Text("${g.memberCount}/${g.maxMembers} anggota", fontSize = 12.sp, color = NeonBlue)
            }
            Text(g.description, fontSize = 13.sp, color = TextDim, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (g.requirements.isNotBlank())
                Text("Syarat: ${g.requirements}", fontSize = 12.sp, color = TextDim, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
        }
    }
}
