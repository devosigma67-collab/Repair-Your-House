package com.example.game.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameEngine

@Composable
fun SettingsDialog(
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    var sfxVol by remember { mutableFloatStateOf(engine.saveManager.sfxVolume) }
    var musicVol by remember { mutableFloatStateOf(engine.saveManager.musicVolume) }
    var vibration by remember { mutableStateOf(engine.saveManager.vibrationEnabled) }
    var controlOpacity by remember { mutableFloatStateOf(engine.saveManager.controlOpacity) }
    var controlScale by remember { mutableFloatStateOf(engine.saveManager.controlScale) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF455A64))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ SETTINGS", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF37474F), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Sound FX Volume
                SettingSliderRow(
                    label = "Sound Effects",
                    value = sfxVol,
                    onValueChange = {
                        sfxVol = it
                        engine.saveManager.sfxVolume = it
                        engine.audioEngine.sfxVolume = it
                    }
                )

                // Vibration Haptics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration Feedback", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = vibration,
                        onCheckedChange = {
                            vibration = it
                            engine.saveManager.vibrationEnabled = it
                            engine.audioEngine.vibrationEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFFE65100))
                    )
                }

                // Controls Opacity
                SettingSliderRow(
                    label = "D-Pad / Buttons Opacity",
                    value = controlOpacity,
                    valueRange = 0.3f..1.0f,
                    onValueChange = {
                        controlOpacity = it
                        engine.saveManager.controlOpacity = it
                    }
                )

                // Controls Size
                SettingSliderRow(
                    label = "D-Pad / Buttons Scale",
                    value = controlScale,
                    valueRange = 0.8f..1.3f,
                    onValueChange = {
                        controlScale = it
                        engine.saveManager.controlScale = it
                    }
                )

                Spacer(Modifier.height(10.dp))

                // Replay Tutorial & Reset Data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            engine.replayTutorial()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Replay Tutorial", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Progress?", fontWeight = FontWeight.Black) },
            text = { Text("This will permanently clear your money, shovel upgrades, discovered gems, and house repairs. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        engine.saveManager.resetAllProgress()
                        engine.backpack.clear()
                        showResetConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("YES, RESET")
                }
            },
            dismissButton = {
                Button(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("${(value * 100).toInt()}%", color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFD54F),
                activeTrackColor = Color(0xFFE65100),
                inactiveTrackColor = Color(0xFF37474F)
            )
        )
    }
}
