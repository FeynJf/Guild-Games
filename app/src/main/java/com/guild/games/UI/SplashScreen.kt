package com.guild.games.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.guild.games.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinish: (loggedIn: Boolean) -> Unit) {
    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(700)) }
        progress.animateTo(1f, tween(2000, easing = LinearEasing))
        onFinish(FirebaseAuth.getInstance().currentUser != null)
    }
    Column(
        Modifier.gameBackground(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            alpha = ((scale.value - 0.7f) / 0.3f).coerceIn(0f, 1f)
        }) { LogoMark(110.dp) }
        Spacer(Modifier.height(24.dp))
        Text("GUILD GAMES", style = logoStyle(34))
        Spacer(Modifier.height(6.dp))
        Text("Main bareng, menang bareng", color = TextDim, fontSize = 13.sp)
        Spacer(Modifier.height(40.dp))
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier.width(180.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = NeonPurple,
            trackColor = Stroke
        )
    }
}
