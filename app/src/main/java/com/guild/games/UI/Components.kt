package com.guild.games.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guild.games.R
import com.guild.games.ui.theme.*

val NeonBrush = Brush.horizontalGradient(listOf(NeonPurple, NeonBlue))
fun logoStyle(size: Int) = TextStyle(brush = NeonBrush, fontSize = size.sp, fontWeight = FontWeight.ExtraBold)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = Color(0xFF3A3A55),
    cursorColor = NeonBlue,
    focusedContainerColor = Surface,
    unfocusedContainerColor = Surface
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
        shape = RoundedCornerShape(14.dp),
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
        shape = RoundedCornerShape(14.dp),
        colors = fieldColors()
    )
}

@Composable
fun NeonButton(text: String, loading: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonPurple,
            disabledContainerColor = NeonPurple.copy(alpha = 0.5f)
        )
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun OutlineNeonButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, NeonPurple)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
    }
}

@Composable
fun WhiteButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, disabledContainerColor = Color(0xFFCCCCCC))
    ) {
        Text("G", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
    }
}

@Composable
fun ErrorText(msg: String?) {
    if (msg != null) Text(msg, color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Medium)
}
