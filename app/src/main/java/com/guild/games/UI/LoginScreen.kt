package com.guild.games.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.guild.games.R
import com.guild.games.data.Repo
import com.guild.games.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onSuccess: () -> Unit, onRegister: () -> Unit, onRegisterDev: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by rememberSaveable { mutableStateOf("") }
    var gmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ctx.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(ctx, gso) }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        try {
            val acc = GoogleSignIn.getSignedInAccountFromIntent(r.data).getResult(ApiException::class.java)
            val token = acc.idToken
            if (token == null) {
                error = "Login Google gagal: token kosong"
            } else scope.launch {
                loading = true
                val e = Repo.googleLogin(token)
                loading = false
                if (e == null) onSuccess() else error = e
            }
        } catch (e: ApiException) {
            if (e.statusCode != 12501) error = "Login Google gagal (kode ${e.statusCode})"
        }
    }

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("GUILD GAMES", style = logoStyle(30))
        Spacer(Modifier.height(8.dp))
        Text("Selamat Datang di Guild Games", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
        Text("Login untuk lanjut main bareng guild", fontSize = 14.sp, color = TextDim)
        Spacer(Modifier.height(8.dp))

        NeonField(username, { username = it }, "Masukkan Username")
        NeonField(gmail, { gmail = it }, "Masukkan Gmail", KeyboardType.Email)
        PasswordField(password, { password = it }, "Masukkan Password")
        ErrorText(error)

        NeonButton("LOGIN", loading) {
            if (username.isBlank() || gmail.isBlank() || password.isBlank()) {
                error = "Harap isi semua kolom"
            } else scope.launch {
                error = null
                loading = true
                val e = Repo.login(username, gmail, password)
                loading = false
                if (e == null) onSuccess() else error = e
            }
        }

        Text("Atau", color = TextDim, fontSize = 14.sp)

        WhiteButton("Login dengan Gmail", enabled = !loading) {
            error = null
            googleClient.signOut().addOnCompleteListener { googleLauncher.launch(googleClient.signInIntent) }
        }

        Spacer(Modifier.height(8.dp))
        Row {
            Text("Belum punya akun? ", color = TextDim, fontSize = 14.sp)
            Text("Daftar Akun", color = NeonBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegister() })
        }
        OutlineNeonButton("Daftar Sebagai Developer", onRegisterDev)
        Spacer(Modifier.height(16.dp))
    }
}
