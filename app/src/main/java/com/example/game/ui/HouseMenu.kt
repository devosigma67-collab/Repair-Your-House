package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.HouseRegistry
import com.example.game.data.HouseRepairItem
import com.example.game.engine.GameEngine

@Composable
fun HouseMenu(
    engine: GameEngine,
    onClose: () -> Unit
) {
    var completedRepairs by remember { mutableStateOf(engine.saveManager.getCompletedRepairs()) }
    var money by remember { mutableStateOf(engine.saveManager.money) }

    val currentStage = HouseRegistry.getStageForCompletedCount(completedRepairs.size)
    val progressPct = HouseRegistry.calculateProgressPercentage(completedRepairs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14181F))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠 HOUSE RESTORATION WORKSHOP", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF004D40))),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰 ", fontSize = 14.sp)
                            Text(
                                "FUNDS: $${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(money)}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF37474F), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Content: Left side = Stage Overview & Visual, Right side = Repair List
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT: Stage Card & House Preview
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .background(Color(0xFF1E242D), RoundedCornerShape(16.dp))
                        .border(1.5.dp, Color(0xFF37474F), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STAGE ${currentStage.stageNumber} / 8",
                            color = Color(0xFF4DD0E1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = currentStage.title,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            text = currentStage.subtitle,
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Progress Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("OVERALL REPAIR", color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("$progressPct% COMPLETE", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFFFF9800),
                            trackColor = Color(0xFF37474F)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (progressPct >= 100) "🎉 LITERAL MANSION UNLOCKED!" else "Target: 100% for Literal Mansion",
                            color = if (progressPct >= 100) Color(0xFF69F0AE) else Color(0xFF90CAF9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // House Stage Status Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFE65100), Color(0xFFBF360C))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (progressPct >= 100) "🏆 YOU BUILT A MANSION!" else "${18 - completedRepairs.size} Repairs Remaining",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // RIGHT: Repair Items Scrollable List
                LazyColumn(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HouseRegistry.REPAIR_ITEMS) { item ->
                        val isDone = completedRepairs.contains(item.id)
                        val canAfford = money >= item.cost
                        val isUnlocked = item.requiredStage <= currentStage.stageNumber

                        RepairItemCard(
                            item = item,
                            isDone = isDone,
                            isUnlocked = isUnlocked,
                            canAfford = canAfford,
                            onRepair = {
                                if (canAfford && !isDone && isUnlocked) {
                                    engine.saveManager.money -= item.cost
                                    engine.saveManager.addCompletedRepair(item.id)
                                    engine.audioEngine.playRepairHouse()
                                    engine.particleSystem.emitCoinBurst(engine.playerX, engine.playerY, 20)
                                    // Update state
                                    completedRepairs = engine.saveManager.getCompletedRepairs()
                                    money = engine.saveManager.money
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RepairItemCard(
    item: HouseRepairItem,
    isDone: Boolean,
    isUnlocked: Boolean,
    canAfford: Boolean,
    onRepair: () -> Unit
) {
    val bgColor = when {
        isDone -> Color(0xFF1B382B)
        !isUnlocked -> Color(0xFF1B1F26)
        else -> Color(0xFF262C36)
    }

    val borderColor = when {
        isDone -> Color(0xFF2E7D32)
        !isUnlocked -> Color(0xFF2E3540)
        canAfford -> Color(0xFFFFB74D)
        else -> Color(0xFF37474F)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    color = if (isDone) Color(0xFFA5D6A7) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "(${item.category})",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }
            Text(
                text = item.description,
                color = Color(0xFFB0BEC5),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        if (isDone) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("REPAIRED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        } else if (!isUnlocked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color(0xFF78909C), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Stage ${item.requiredStage}", color = Color(0xFF78909C), fontSize = 11.sp)
            }
        } else {
            Button(
                onClick = onRepair,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE65100),
                    disabledContainerColor = Color(0xFF37474F)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "$${item.cost}",
                    color = if (canAfford) Color.White else Color(0xFF90A4AE),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
