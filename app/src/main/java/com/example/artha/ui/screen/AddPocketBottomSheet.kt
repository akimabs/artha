// ui/AddPocketBottomSheet.kt
package com.example.artha.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import com.example.artha.model.PocketData

@Composable
fun AddPocketBottomSheet(
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    onDismiss: () -> Unit,
    onAddPocket: (PocketData) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var target by remember { mutableStateOf(TextFieldValue("")) }

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Tambah Saku Baru", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                label = { Text("Nama Saku") },
                value = title,
                onValueChange = { title = it },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            OutlinedTextField(
                label = { Text("Target Keuangan") },
                value = target,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.text.replace(Regex("[^\\d]"), "")
                    val formatted = if (digitsOnly.isNotEmpty()) {
                        val number = digitsOnly.toLongOrNull() ?: 0L
                        "Rp " + "%,d".format(number).replace(',', '.')
                    } else {
                        ""
                    }
                    target = newValue.copy(text = formatted, selection = TextRange(formatted.length))
                },
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Pilih Warna")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                listOf(
                    Color(0xFFFFFFCC), Color(0xFFCFFFE0),
                    Color(0xFFD1E8FF), Color(0xFFFFD1DC),
                    Color(0xFFE2ECEB), Color(0xFFE0F7FA)
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, shape = CircleShape)
                            .border(2.dp, if (selectedColor == color) Color(0xFF5AB0F6) else Color.Transparent, CircleShape)
                            .clickable { onColorChange(color) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val pocket = PocketData(
                        title = title.text,
                        amount = 0,
                        backgroundColor = selectedColor,
                        percentage = 0
                    )
                    onAddPocket(pocket)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AB0F6))
            ) {
                Text("Simpan", color = Color.White)
            }
        }
    }
}