package com.guild.games.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.guild.games.data.Repo
import com.guild.games.data.UserProfile
import com.guild.games.ui.theme.*

/** Lingkaran avatar: foto akun kalau ada, kalau tidak huruf pertama username. */
@Composable
fun AvatarCircle(name: String, photoUrl: String, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CircleShape).background(NeonBrush).padding(2.dp)) {
        Box(
            Modifier.fillMaxSize().clip(CircleShape).background(PanelHigh),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNotBlank())
                AsyncImage(photoUrl, name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(
                name.trim().take(1).uppercase().ifEmpty { "?" },
                fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.ExtraBold, color = NeonPurple
            )
        }
    }
}

fun roleLabel(role: String) = when (role) {
    "developer" -> "Developer"
    "admin" -> "Admin"
    "anggota" -> "Anggota"
    else -> "Belum diketahui"
}

@Composable
fun ProfileScreen(onBack: () -> Unit, onSettings: () -> Unit, onLogout: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var loggedIn by remember { mutableStateOf(auth.currentUser != null) }
    DisposableEffect(Unit) {
        val l = FirebaseAuth.AuthStateListener { loggedIn = it.currentUser != null }
        auth.addAuthStateListener(l)
        onDispose { auth.removeAuthStateListener(l) }
    }

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(loggedIn) {
        loading = true
        profile = if (loggedIn) Repo.loadProfile() else null
        loading = false
    }
    var confirmLogout by remember { mutableStateOf(false) }

    // Tampilkan data dari akun login dulu supaya layar tidak kosong selagi data lengkap dimuat.
    val cu = auth.currentUser
    val name = profile?.username ?: cu?.displayName ?: cu?.email?.substringBefore("@").orEmpty()
    val gmail = profile?.gmail ?: cu?.email.orEmpty()
    val photo = profile?.photoUrl ?: cu?.photoUrl?.toString().orEmpty()

    Column(
        Modifier.gameBackground().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("← Kembali", color = NeonBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 8.dp))
        Text("Profil", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

        Column(
            Modifier.fillMaxWidth().glass(24.dp).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (loggedIn) AvatarCircle(name, photo, 96.dp)
            else Text("👤", fontSize = 64.sp)
            Text(
                if (loggedIn) name else "Belum login",
                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center
            )
            StatusChip(loggedIn)
        }

        if (loggedIn) {
            Column(Modifier.fillMaxWidth().glass(24.dp).padding(horizontal = 18.dp, vertical = 6.dp)) {
                InfoRow("Gmail", gmail.ifBlank { "-" })
                RowDivider()
                InfoRow("Peran", when {
                    profile != null -> roleLabel(profile!!.role)
                    loading -> "Memuat…"
                    else -> roleLabel("")
                })
                RowDivider()
                InfoRow("Masuk lewat", profile?.provider ?: "-")
                RowDivider()
                InfoRow("ID Akun", (cu?.uid ?: "").let { if (it.length > 12) it.take(12) + "…" else it.ifBlank { "-" } })
            }
        } else {
            Text("Login dulu untuk melihat dan memakai akun kamu.", color = TextDim, fontSize = 14.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            NeonButton("LOGIN") { onLogout() }
        }

        OutlineNeonButton("⚙️  Pengaturan & Pembaruan") { onSettings() }
        if (loggedIn) OutlineNeonButton("Keluar") { confirmLogout = true }
        Spacer(Modifier.height(12.dp))
    }

    if (confirmLogout) AlertDialog(
        onDismissRequest = { confirmLogout = false },
        containerColor = Panel,
        titleContentColor = Color.White,
        textContentColor = TextDim,
        title = { Text("Keluar dari akun?", fontWeight = FontWeight.Bold) },
        text = { Text("Kamu perlu login lagi untuk memakai aplikasi.") },
        confirmButton = {
            TextButton(onClick = { confirmLogout = false; onLogout() }) {
                Text("Keluar", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { confirmLogout = false }) { Text("Batal", color = NeonBlue) }
        }
    )
}

@Composable
private fun StatusChip(loggedIn: Boolean) {
    val c = if (loggedIn) Success else ErrorRed
    Row(
        Modifier.clip(RoundedCornerShape(20.dp)).background(c.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(c))
        Spacer(Modifier.width(8.dp))
        Text(if (loggedIn) "Sudah login" else "Belum login", color = c, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextDim, fontSize = 14.sp)
        Spacer(Modifier.width(16.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
fun RowDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Stroke))
}
