package com.guild.games.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guild.games.data.Repo
import com.guild.games.ui.theme.*
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
    var loading by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("← Kembali", color = NeonBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onBack() }.padding(vertical = 8.dp))
        Text(if (developer) "Daftar Developer" else "Daftar Akun", fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(if (developer) "Khusus pengembang Guild Games" else "Buat akun untuk bergabung ke guild",
            fontSize = 14.sp, color = TextDim)
        Spacer(Modifier.height(4.dp))

        NeonField(username, { username = it }, "Username")
        NeonField(gmail, { gmail = it }, "Gmail", KeyboardType.Email)
        PasswordField(password, { password = it }, "Password")
        PasswordField(confirm, { confirm = it }, "Konfirmasi Password")
        if (developer) NeonField(code, { code = it }, "Kode Developer")
        ErrorText(error)

        NeonButton("DAFTAR", loading) {
            when {
                username.isBlank() || gmail.isBlank() || password.isBlank() || confirm.isBlank() ||
                    (developer && code.isBlank()) -> error = "Harap isi semua kolom"
                developer && code.trim() != Repo.DEV_CODE -> error = "Kode Developer Salah"
                !Patterns.EMAIL_ADDRESS.matcher(gmail.trim()).matches() -> error = "Format Gmail tidak valid"
                password.length < 6 -> error = "Password minimal 6 karakter"
                password != confirm -> error = "Konfirmasi Password tidak sama"
                else -> scope.launch {
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
