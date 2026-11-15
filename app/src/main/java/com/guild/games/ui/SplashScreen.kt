package com.guild.games.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.guild.games.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: (loggedIn: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish(FirebaseAuth.getInstance().currentUser != null)
    }
    Column(
        Modifier.fillMaxSize().background(Bg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GUILD", style = logoStyle(48))
        Text("GAMES", style = logoStyle(48))
        Spacer(Modifier.height(32.dp))
        CircularProgressIndicator(color = NeonPurple, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
    }
}
