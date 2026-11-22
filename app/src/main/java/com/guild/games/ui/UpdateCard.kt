package com.guild.games.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.guild.games.BuildConfig
import com.guild.games.data.Prefs
import com.guild.games.data.UpdateManager
import com.guild.games.data.UpdateUi
import com.guild.games.ui.theme.*
import java.util.Locale

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val mb = bytes / 1_048_576.0
    return if (mb >= 1) String.format(Locale.getDefault(), "%.1f MB", mb)
    else String.format(Locale.getDefault(), "%d KB", (bytes / 1024).coerceAtLeast(1))
}

@Composable
fun UpdateCard() {
    val ctx = LocalContext.current
    val state by UpdateManager.state.collectAsState()

    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Diizinkan atau tidak, unduhan tetap jalan; hanya notifikasinya yang tidak muncul.
        UpdateManager.download(ctx)
    }
    val startDownload: () -> Unit = {
        val needsPermission = Build.VERSION.SDK_INT >= 33 && Prefs.notifyUpdate(ctx) &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else UpdateManager.download(ctx)
    }

    Column(
        Modifier.fillMaxWidth().glass(24.dp).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(ButtonBrush), contentAlignment = Alignment.Center) {
                Text("⬆", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
            Column {
                Text("Pembaruan Aplikasi", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Versi terpasang ${BuildConfig.VERSION_NAME}", fontSize = 13.sp, color = TextDim)
            }
        }

        when (val s = state) {
            is UpdateUi.Idle -> {
                Text("Cek apakah ada versi Guild Games yang lebih baru.", fontSize = 14.sp, color = TextDim)
                NeonButton("CEK PEMBARUAN") { UpdateManager.check(ctx) }
            }
            is UpdateUi.Checking -> {
                Text("Memeriksa versi terbaru…", fontSize = 14.sp, color = TextDim)
                NeonButton("", loading = true) {}
            }
            is UpdateUi.UpToDate -> {
                InfoText("✓ Kamu sudah memakai versi terbaru")
                OutlineNeonButton("Cek lagi") { UpdateManager.check(ctx) }
            }
            is UpdateUi.Available -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "BARU", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(NeonBrush)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Text("Versi ${s.info.versionName}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, modifier = Modifier.weight(1f))
                    if (s.info.sizeBytes > 0) Text(formatBytes(s.info.sizeBytes), fontSize = 13.sp, color = TextDim)
                }
                if (s.info.notes.isNotBlank()) Text(
                    s.info.notes.trim(), fontSize = 13.sp, color = TextDim, maxLines = 8, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(PanelHigh).padding(12.dp)
                )
                NeonButton("UNDUH PEMBARUAN") { startDownload() }
            }
            is UpdateUi.Downloading -> {
                val fraction = if (s.total > 0) (s.done.toFloat() / s.total).coerceIn(0f, 1f) else 0f
                Text("Mengunduh versi ${s.info.versionName}…", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                GradientProgress(fraction)
                Row(Modifier.fillMaxWidth()) {
                    Text("${(fraction * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonBlue)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${formatBytes(s.done)} / ${formatBytes(s.total)}  •  ${formatBytes(s.bytesPerSec)}/dtk",
                        fontSize = 12.sp, color = TextDim
                    )
                }
                OutlineNeonButton("Batalkan") { UpdateManager.cancelDownload() }
            }
            is UpdateUi.Ready -> {
                InfoText("✓ Unduhan versi ${s.info.versionName} selesai")
                NeonButton("PASANG SEKARANG") {
                    if (!UpdateManager.install(ctx)) Toast.makeText(
                        ctx, "Izinkan \"Pasang aplikasi tak dikenal\" untuk Guild Games, lalu tekan Pasang lagi.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            is UpdateUi.Error -> {
                ErrorText(s.message)
                NeonButton(if (s.info != null) "COBA UNDUH LAGI" else "COBA LAGI") {
                    if (s.info != null) startDownload() else UpdateManager.check(ctx)
                }
            }
        }
    }
}

@Composable
private fun GradientProgress(fraction: Float) {
    val f by animateFloatAsState(fraction, label = "progress")
    Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Stroke)) {
        Box(Modifier.fillMaxWidth(f).fillMaxHeight().background(NeonBrush))
    }
}
