package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TerminalLogData
import androidx.compose.material.icons.filled.MenuBook
import com.example.ui.theme.*

@Composable
fun TerminalHackModal(
    terminal: TerminalLogData,
    onHackSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .border(3.dp, AgedGold, RoundedCornerShape(12.dp))
                .testTag("modal_terminal_hacker"),
            color = Parchment
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MedievalCrimson.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Ancient Scroll",
                                tint = MedievalCrimson,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = terminal.terminalName.replace("Terminal", "Ancient Cipher"),
                                color = MedievalCrimson,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                            Text(
                                text = "Origin: ${terminal.locationName}",
                                color = MedievalTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MedievalCrimson.copy(alpha = 0.1f))
                            .testTag("btn_close_terminal")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MedievalCrimson)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scroll Display Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Bronze, RoundedCornerShape(8.dp)),
                    color = Color(0xFFF5E6D3) // Aged paper
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (terminal.isHacked) {
                            Text(
                                text = "📜 SACRED WISDOM UNVEILED",
                                color = MedievalCrimsonDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = terminal.logText,
                                color = MedievalCrimsonDark,
                                fontSize = 12.sp,
                                fontFamily = AlmendraFontFamily
                            )
                        } else {
                            Text(
                                text = "🔒 SEALED BY ANCIENT MAGIC\nPassphrase Hint: The name of the forgotten ritual (7 Letters)",
                                color = MedievalCrimson,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MedievalSharpFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!terminal.isHacked) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("UNSEAL WITH PASSPHRASE", color = MedievalCrimson, fontFamily = MedievalSharpFontFamily, fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgedGold,
                            unfocusedBorderColor = Bronze,
                            focusedTextColor = MedievalCrimson,
                            unfocusedTextColor = MedievalCrimson
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_terminal_password")
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage!!,
                            color = MedievalCrimson,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (passwordInput.trim().uppercase() == terminal.passwordAnswer.uppercase()) {
                                    onHackSuccess(terminal.id)
                                } else {
                                    errorMessage = "THE SEAL REMAINS: INCORRECT PASSPHRASE"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedievalCrimson),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_submit_terminal_pass")
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = "Unlock", tint = Parchment, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BREAK THE SEAL", color = Parchment, fontWeight = FontWeight.Bold, fontFamily = MedievalSharpFontFamily)
                        }
                    }
                }
            }
        }
    }
}
