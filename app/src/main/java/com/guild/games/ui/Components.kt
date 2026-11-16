package com.guild.games.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guild.games.R
import com.guild.games.ui.theme.*

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
fun logoStyle(size: Int) = TextStyle(
    brush = NeonBrush,
    fontSize = size.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = 3.sp
)

fun Modifier.gameBackground(): Modifier = this.fillMaxSize().background(BgBrush)

/** Kartu kaca: panel gelap dengan garis tepi gradien neon. */
fun Modifier.glass(radius: Dp = 20.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .background(Panel)
        .border(
            1.dp,
            Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.6f), Stroke, NeonBlue.copy(alpha = 0.4f))),
            shape
        )
}

@Composable
fun LogoMark(height: Dp) {
    Image(
        painterResource(R.drawable.ic_logo_mark),
        contentDescription = "Guild Games",
        modifier = Modifier.height(height).aspectRatio(60f / 70f)
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = Stroke,
    cursorColor = NeonBlue,
    focusedContainerColor = PanelHigh,
    unfocusedContainerColor = PanelHigh
)

@Composable
fun NeonField(
    value: String,
    onChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    multiline: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(hint, color = TextDim) },
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors()
    )
}

@Composable
fun PasswordField(value: String, onChange: (String) -> Unit, hint: String) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(hint, color = TextDim) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painterResource(if (visible) R.drawable.ic_eye else R.drawable.ic_eye_off),
                    contentDescription = "Tampilkan password",
                    tint = TextDim
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors()
    )
}

/** Tombol utama: gradien ungu-biru dengan glow. */
@Composable
fun NeonButton(text: String, loading: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier.fillMaxWidth().height(56.dp)
            .shadow(12.dp, shape, ambientColor = NeonPurple, spotColor = NeonPurple)
            .clip(shape)
            .background(ButtonBrush)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
    }
}

@Composable
fun OutlineNeonButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val c = if (enabled) NeonPurple else TextDim
    Box(
        Modifier.fillMaxWidth().height(52.dp)
            .clip(shape)
            .border(1.5.dp, c, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c)
    }
}

@Composable
fun WhiteButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().height(54.dp)
            .clip(shape)
            .background(if (enabled) Color.White else Color(0xFFCCCCCC))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF1F3F4)), contentAlignment = Alignment.Center) {
            Text("G", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
    }
}

@Composable
fun OrDivider() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(Stroke))
        Text("  Atau  ", color = TextDim, fontSize = 13.sp)
        Box(Modifier.weight(1f).height(1.dp).background(Stroke))
    }
}

@Composable
fun ErrorText(msg: String?) {
    if (msg != null) Text(
        msg, color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(ErrorRed.copy(alpha = 0.12f)).padding(12.dp)
    )
}

@Composable
fun InfoText(msg: String?) {
    if (msg != null) Text(
        msg, color = Success, fontSize = 14.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(Success.copy(alpha = 0.12f)).padding(12.dp)
    )
}
