package com.guild.games.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.guild.games.BuildConfig
import com.guild.games.data.Prefs
import com.guild.games.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit, onProfile: () -> Unit, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    var autoCheck by remember { mutableStateOf(Prefs.autoCheck(ctx)) }
    var notify by remember { mutableStateOf(Prefs.notifyUpdate(ctx)) }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        Modifier.gameBackground().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("← Kembali", color = NeonBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 8.dp))
        Text("Pengaturan", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

        SectionLabel("AKUN")
        Row(
            Modifier.fillMaxWidth().glass(20.dp).clickable { onProfile() }.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👤", fontSize = 22.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Profil", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Lihat akun yang sedang login", fontSize = 13.sp, color = TextDim)
            }
            Text("›", fontSize = 24.sp, color = TextDim)
        }

        SectionLabel("PEMBARUAN")
        UpdateCard()
        Column(Modifier.fillMaxWidth().glass(20.dp).padding(horizontal = 18.dp, vertical = 6.dp)) {
            SwitchRow("Cek pembaruan otomatis", "Saat aplikasi dibuka", autoCheck) {
                autoCheck = it
                Prefs.setAutoCheck(ctx, it)
            }
            RowDivider()
            SwitchRow("Notifikasi pembaruan", "Beritahu saat ada versi baru", notify) {
                notify = it
                Prefs.setNotifyUpdate(ctx, it)
                if (it && Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        SectionLabel("TENTANG")
        Column(Modifier.fillMaxWidth().glass(20.dp).padding(horizontal = 18.dp, vertical = 6.dp)) {
            InfoRow("Versi aplikasi", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            RowDivider()
            InfoRow("Paket", ctx.packageName)
        }

        Spacer(Modifier.height(4.dp))
        OutlineNeonButton("Keluar") { onLogout() }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextDim, letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = TextDim)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonPurple,
                uncheckedThumbColor = TextDim,
                uncheckedTrackColor = PanelHigh,
                uncheckedBorderColor = Stroke
            )
        )
    }
}
