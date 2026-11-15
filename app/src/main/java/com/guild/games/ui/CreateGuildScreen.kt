package com.guild.games.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.guild.games.data.Repo
import com.guild.games.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CreateGuildScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    var req by rememberSaveable { mutableStateOf("") }
    var maxStr by rememberSaveable { mutableStateOf("") }
    var stars by rememberSaveable { mutableIntStateOf(3) }
    var logo by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) logo = it }

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("← Kembali", color = NeonBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 8.dp))
        Text("Buat Guild", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

        Box(
            Modifier.align(Alignment.CenterHorizontally).size(104.dp).clip(RoundedCornerShape(20.dp))
                .background(Surface).clickable { picker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (logo != null) AsyncImage(logo, "Logo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text("Pilih\nLogo", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        NeonField(name, { name = it }, "Nama Guild")
        NeonField(desc, { desc = it }, "Deskripsi", multiline = true)
        NeonField(req, { req = it }, "Syarat Masuk", multiline = true)

        Text("Bintang", color = TextDim, fontSize = 14.sp)
        Row {
            for (i in 1..5) {
                Text("★", fontSize = 38.sp, color = if (i <= stars) Gold else Color(0xFF3A3A55),
                    modifier = Modifier.clickable { stars = i }.padding(end = 8.dp))
            }
        }

        NeonField(maxStr, { maxStr = it.filter(Char::isDigit) }, "Max Anggota", KeyboardType.Number)
        ErrorText(error)

        NeonButton("SIMPAN GUILD", loading) {
            val max = maxStr.toIntOrNull()
            when {
                name.isBlank() || desc.isBlank() || req.isBlank() || maxStr.isBlank() -> error = "Harap isi semua kolom"
                max == null || max < 1 -> error = "Max Anggota minimal 1"
                else -> scope.launch {
                    error = null
                    loading = true
                    val e = Repo.createGuild(name, logo, desc, req, stars, max)
                    loading = false
                    if (e == null) onBack() else error = e
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
