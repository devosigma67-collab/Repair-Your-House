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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.data.Gem
import com.example.game.engine.GameEngine

@Composable
fun SellDialog(
    engine: GameEngine,
    onDismiss: () -> Unit
) {
    val gems = engine.backpack.toList()
    val sifterLvl = engine.saveManager.getBuildingUpgradeLevel("gem_sifter")
    val bonusMultiplier = 1f + sifterLvl * 0.06f

    val totalValue = gems.sumOf { (it.value * bonusMultiplier).toLong() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 380.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232A)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💎 GEM APPRAISAL STAND", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 18.sp)
                }

                Text(
                    text = "Sell your haul to finance the house repairs!",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Gem List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(gems) { gem ->
                        GemSellItemRow(gem, bonusMultiplier)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Total Summary & Sell Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL PAYOUT:", color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val formattedTotal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalValue)
                        Text(
                            text = "+ $$formattedTotal",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                        if (sifterLvl > 0) {
                            Text("+${sifterLvl * 6}% Sifter Bonus included", color = Color(0xFF4DD0E1), fontSize = 10.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Keep")
                        }

                        Button(
                            onClick = {
                                engine.sellAllGems()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFA5D6A7))
                        ) {
                            Text("SELL ALL", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GemSellItemRow(gem: Gem, bonusMultiplier: Float) {
    val finalVal = (gem.value * bonusMultiplier).toLong()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF37474F), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(gem.primaryColor, CircleShape)
                    .border(1.dp, Color.White, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = gem.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Rarity Tier ${gem.rarity} (${gem.rarityTier.displayName})", color = gem.rarityTier.baseColor, fontSize = 10.sp)
            }
        }

        val formattedVal = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(finalVal)
        Text(
            text = "+ $$formattedVal",
            color = Color(0xFFFFD54F),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}
