package com.example.artha.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import com.example.artha.R
import com.example.artha.model.PocketData
import com.example.artha.util.extractDigitsOnly

@Composable
fun AddPocketBottomSheet(
    selectedColor: Int,
    onColorChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onAddPocket: (PocketData) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var target by remember { mutableStateOf(TextFieldValue("")) }
    val isFormValid = title.text.isNotBlank() && target.text.contains(Regex("\\d"))

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(stringResource(R.string.add_new_pocket), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                label = { Text(stringResource(R.string.pocket_name)) },
                value = title,
                onValueChange = { title = it },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = Color(0xFF5AB0F6),
                    focusedBorderColor = Color(0xFF5AB0F6),
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                label = { Text(stringResource(R.string.target_amount)) },
                value = target,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.text.replace(Regex("[^\\d]"), "")
                    val formatted = if (digitsOnly.isNotEmpty()) {
                        val number = digitsOnly.toLongOrNull() ?: 0L
                        "Rp " + "%,d".format(number).replace(',', '.')
                    } else {
                        ""
                    }
                    target =
                        newValue.copy(text = formatted, selection = TextRange(formatted.length))
                },
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = Color(0xFF5AB0F6),
                    focusedBorderColor = Color(0xFF5AB0F6),
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.select_color))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                listOf(
                    0xFFFFFFCC.toInt(), 0xFFCFFFE0.toInt(),
                    0xFFD1E8FF.toInt(), 0xFFFFD1DC.toInt(),
                    0xFFE2ECEB.toInt(), 0xFFE0F7FA.toInt()
                ).forEach { colorInt ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                2.dp,
                                if (selectedColor == colorInt) Color(0xFF5AB0F6) else Color.Transparent,
                                CircleShape
                            )
                            .clickable(
                                indication = rememberRipple(bounded = true, radius = 20.dp),
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onColorChange(colorInt)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            val backgroundColor = if (isFormValid) Color(0xFF5AB0F6) else Color(0xFFB0BEC5)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable(
                        enabled = isFormValid,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                        onClick = {
                            val pocket = PocketData(
                                title = title.text,
                                amount = 0,
                                backgroundColorInt = selectedColor,
                                targetAmount = extractDigitsOnly(target.text)
                            )
                            onAddPocket(pocket)
                            onDismiss()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.save_pocket), color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}