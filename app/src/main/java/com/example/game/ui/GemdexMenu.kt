package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.data.Gem
import com.example.game.data.GemRegistry
import com.example.game.engine.GameEngine

@Composable
fun GemdexMenu(
    engine: GameEngine,
    onClose: () -> Unit
) {
    val discoveredIds = engine.saveManager.getDiscoveredGemIds()
    var selectedGem by remember { mutableStateOf<Gem?>(null) }
    var filterTier by remember { mutableStateOf<String?>("ALL") }

    val allGems = GemRegistry.ALL_GEMS
    val filtered = if (filterTier == "ALL" || filterTier == null) {
        allGems
    } else {
        allGems.filter { it.rarityTier.displayName.equals(filterTier, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF11141B))
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
                    Text("💎 GEMDEX ARCHIVES", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4A148C), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFCE93D8), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${discoveredIds.size} / ${allGems.size} Discovered (${(discoveredIds.size * 100 / allGems.size)}%)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF004D40))),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "💰 $${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(engine.saveManager.money)}",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
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

            // Filter Tabs (Including new rarity tiers)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tiers = listOf("ALL", "Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic", "Celestial", "Primordial", "Cosmic")
                items(tiers.size) { idx ->
                    val tier = tiers[idx]
                    val isSel = filterTier == tier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { filterTier = tier }
                            .background(if (isSel) Color(0xFF7C3AED) else Color(0xFF1E293B))
                            .border(1.dp, if (isSel) Color(0xFFC4B5FD) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tier,
                            color = if (isSel) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Grid of 40 Gems
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items = filtered, key = { it.id }) { gem ->
                    val isDiscovered = discoveredIds.contains(gem.id)
                    val count = engine.saveManager.getGemDiscoveryCount(gem.id)

                    GemCardItem(
                        gem = gem,
                        isDiscovered = isDiscovered,
                        count = count,
                        onClick = {
                            if (isDiscovered) {
                                selectedGem = gem
                            }
                        }
                    )
                }
            }
        }

        // Gem Detail Modal
        selectedGem?.let { gem ->
            val count = engine.saveManager.getGemDiscoveryCount(gem.id)
            Dialog(onDismissRequest = { selectedGem = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232B)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, gem.primaryColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(gem.primaryColor, CircleShape)
                                .border(2.dp, gem.secondaryColor, CircleShape)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(text = gem.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            text = "Tier ${gem.rarity} - ${gem.rarityTier.displayName}",
                            color = gem.rarityTier.baseColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(text = gem.lore, color = Color(0xFFCFD8DC), fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BASE VALUE", color = Color(0xFF90A4AE), fontSize = 10.sp)
                                Text("$$${gem.value}", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DEPTH FOUND", color = Color(0xFF90A4AE), fontSize = 10.sp)
                                Text("Lvl ${gem.minDepth} - ${gem.maxDepth}", color = Color(0xFF4DD0E1), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FOUND", color = Color(0xFF90A4AE), fontSize = 10.sp)
                                Text("$count times", color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GemCardItem(
    gem: Gem,
    isDiscovered: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isDiscovered, onClick = onClick)
            .background(if (isDiscovered) Color(0xFF1E242E) else Color(0xFF181C24))
            .border(
                width = 1.dp,
                color = if (isDiscovered) gem.primaryColor.copy(alpha = 0.6f) else Color(0xFF2C323D),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            if (isDiscovered) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(gem.primaryColor, CircleShape)
                        .border(1.5.dp, gem.secondaryColor, CircleShape)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = gem.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = "Rarity ${gem.rarity} | $$${gem.value}",
                    color = gem.rarityTier.baseColor,
                    fontSize = 10.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF263238), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color(0xFF78909C), modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "??? (Undiscovered)",
                    color = Color(0xFF78909C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Depth: Lvl ${gem.minDepth}+",
                    color = Color(0xFF546E7A),
                    fontSize = 9.sp
                )
            }
        }
    }
}
