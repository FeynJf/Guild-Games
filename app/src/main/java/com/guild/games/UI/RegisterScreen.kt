package com.guild.games.ui

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guild.games.data.Config
import com.guild.games.data.DevCode
import com.guild.games.data.Repo
import com.guild.games.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onSuccess: () -> Unit, onBack: () -> Unit) =
    RegisterForm(developer = false, onSuccess = onSuccess, onBack = onBack)

@Composable
fun RegisterDeveloperScreen(onSuccess: () -> Unit, onBack: () -> Unit) =
    RegisterForm(developer = true, onSuccess = onSuccess, onBack = onBack)

@Composable
private fun RegisterForm(developer: Boolean, onSuccess: () -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var username by rememberSaveable { mutableStateOf("") }
    var gmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var cooldown by remember { mutableIntStateOf(0) }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    Column(
        Modifier.gameBackground().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("← Kembali", color = NeonBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LogoMark(48.dp)
            Column {
                Text(if (developer) "Daftar Developer" else "Daftar Akun", fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(if (developer) "Verifikasi lewat kode Gmail" else "Buat akun untuk gabung guild",
                    fontSize = 13.sp, color = TextDim)
            }
        }

        Column(Modifier.fillMaxWidth().glass(24.dp).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonField(username, { username = it }, "Username")
            NeonField(gmail, { gmail = it }, "Gmail", KeyboardType.Email)
            PasswordField(password, { password = it }, "Password")
            PasswordField(confirm, { confirm = it }, "Konfirmasi Password")

            if (developer) {
                OutlineNeonButton(
                    text = when {
                        sending -> "Mengirim..."
                        cooldown > 0 -> "Kirim ulang (${cooldown}s)"
                        else -> "Kirim Kode ke Gmail"
                    },
                    enabled = !sending && cooldown == 0
                ) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(gmail.trim()).matches()) {
                        error = "Isi Gmail yang valid dulu"
                        info = null
                    } else scope.launch {
                        error = null
                        info = null
                        sending = true
                        val e = DevCode.request(gmail)
                        sending = false
                        if (e == null) {
                            info = if (Config.APPROVER_EMAIL.isBlank())
                                "Kode dikirim ke ${gmail.trim()}. Cek Inbox atau Spam."
                            else "Kode dikirim ke pemilik aplikasi. Minta kodenya ke pemilik."
                            cooldown = 60
                        } else error = e
                    }
                }
                NeonField(code, { code = it.filter(Char::isDigit).take(6) }, "Kode Developer (6 angka)", KeyboardType.Number)
                InfoText(info)
            }

            ErrorText(error)

            NeonButton("DAFTAR", loading) {
                val err = when {
                    username.isBlank() || gmail.isBlank() || password.isBlank() || confirm.isBlank() ||
                        (developer && code.isBlank()) -> "Harap isi semua kolom"
                    !Patterns.EMAIL_ADDRESS.matcher(gmail.trim()).matches() -> "Format Gmail tidak valid"
                    password.length < 6 -> "Password minimal 6 karakter"
                    password != confirm -> "Konfirmasi Password tidak sama"
                    else -> null
                }
                if (err != null) {
                    error = err
                    return@NeonButton
                }
                if (developer) {
                    val codeErr = DevCode.verify(gmail, code)
                    if (codeErr != null) {
                        error = codeErr
                        return@NeonButton
                    }
                }
                scope.launch {
                    error = null
                    loading = true
                    val e = Repo.register(username, gmail, password, if (developer) "developer" else "anggota")
                    loading = false
                    if (e == null) onSuccess() else error = e
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
