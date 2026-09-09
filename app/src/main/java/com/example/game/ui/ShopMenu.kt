package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.game.data.UpgradeRegistry
import com.example.game.engine.GameEngine
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ShopMenu(
    engine: GameEngine,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Shovels, 1: Backpacks, 2: Boots, 3: Lamps, 4: Estate Tech
    var money by remember { mutableLongStateOf(engine.saveManager.money) }
    var shovelLvl by remember { mutableIntStateOf(engine.saveManager.shovelLevel) }
    var backpackLvl by remember { mutableIntStateOf(engine.saveManager.backpackLevel) }
    var bootsLvl by remember { mutableIntStateOf(engine.saveManager.bootsLevel) }
    var lampLvl by remember { mutableIntStateOf(engine.saveManager.headlampLevel) }

    val formattedMoney = NumberFormat.getNumberInstance(Locale.US).format(money)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🛒 MINER'S OUTFITTER & WORKSHOP",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.width(14.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF004D40))),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰 ", fontSize = 13.sp)
                            Text(
                                "FUNDS: $$formattedMoney",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFF334155), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // HORIZONTAL CATEGORY TAB BAR (5 COMPREHENSIVE TABS)
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopTabButton(title = "⛏️ SHOVELS (30)", selected = selectedTab == 0) { selectedTab = 0 }
                ShopTabButton(title = "🎒 BACKPACKS (30)", selected = selectedTab == 1) { selectedTab = 1 }
                ShopTabButton(title = "🥾 BOOTS (20)", selected = selectedTab == 2) { selectedTab = 2 }
                ShopTabButton(title = "🔦 LAMPS (15)", selected = selectedTab == 3) { selectedTab = 3 }
                ShopTabButton(title = "🏛️ ESTATE TECH (9)", selected = selectedTab == 4) { selectedTab = 4 }
            }

            Spacer(Modifier.height(8.dp))

            // TAB CONTENT LIST
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        // 30 SHOVEL TIERS
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(UpgradeRegistry.SHOVEL_UPGRADES) { item ->
                                val isOwned = item.level <= shovelLvl
                                val isNext = item.level == shovelLvl + 1
                                val canAfford = money >= item.cost

                                ShovelRow(
                                    item = item,
                                    isOwned = isOwned,
                                    isNext = isNext,
                                    canAfford = canAfford,
                                    onBuy = {
                                        if (isNext && canAfford) {
                                            engine.saveManager.money -= item.cost
                                            engine.saveManager.shovelLevel = item.level
                                            engine.audioEngine.playUpgradeFanfare()
                                            money = engine.saveManager.money
                                            shovelLvl = engine.saveManager.shovelLevel
                                        }
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        // 30 BACKPACK TIERS
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(UpgradeRegistry.BACKPACK_UPGRADES) { item ->
                                val isOwned = item.level <= backpackLvl
                                val isNext = item.level == backpackLvl + 1
                                val canAfford = money >= item.cost

                                BackpackRow(
                                    item = item,
                                    isOwned = isOwned,
                                    isNext = isNext,
                                    canAfford = canAfford,
                                    onBuy = {
                                        if (isNext && canAfford) {
                                            engine.saveManager.money -= item.cost
                                            engine.saveManager.backpackLevel = item.level
                                            engine.audioEngine.playUpgradeFanfare()
                                            money = engine.saveManager.money
                                            backpackLvl = engine.saveManager.backpackLevel
                                        }
                                    }
                                )
                            }
                        }
                    }
                    2 -> {
                        // 20 MINING BOOTS TIERS
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(UpgradeRegistry.BOOTS_UPGRADES) { item ->
                                val isOwned = item.level <= bootsLvl
                                val isNext = item.level == bootsLvl + 1
                                val canAfford = money >= item.cost

                                BootsRow(
                                    item = item,
                                    isOwned = isOwned,
                                    isNext = isNext,
                                    canAfford = canAfford,
                                    onBuy = {
                                        if (isNext && canAfford) {
                                            engine.saveManager.money -= item.cost
                                            engine.saveManager.bootsLevel = item.level
                                            engine.audioEngine.playUpgradeFanfare()
                                            money = engine.saveManager.money
                                            bootsLvl = engine.saveManager.bootsLevel
                                        }
                                    }
                                )
                            }
                        }
                    }
                    3 -> {
                        // 15 HEADLAMP TIERS
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(UpgradeRegistry.HEADLAMP_UPGRADES) { item ->
                                val isOwned = item.level <= lampLvl
                                val isNext = item.level == lampLvl + 1
                                val canAfford = money >= item.cost

                                HeadlampRow(
                                    item = item,
                                    isOwned = isOwned,
                                    isNext = isNext,
                                    canAfford = canAfford,
                                    onBuy = {
                                        if (isNext && canAfford) {
                                            engine.saveManager.money -= item.cost
                                            engine.saveManager.headlampLevel = item.level
                                            engine.audioEngine.playUpgradeFanfare()
                                            money = engine.saveManager.money
                                            lampLvl = engine.saveManager.headlampLevel
                                        }
                                    }
                                )
                            }
                        }
                    }
                    4 -> {
                        // 9 PERSISTENT ESTATE TECHNOLOGIES (10 LEVELS EACH = 90 UPGRADES)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(UpgradeRegistry.BUILDING_UPGRADES) { b ->
                                val currentLvl = engine.saveManager.getBuildingUpgradeLevel(b.id)
                                val isMax = currentLvl >= b.maxLevel
                                val nextCost = (b.baseCost * Math.pow(b.costMultiplier.toDouble(), currentLvl.toDouble())).toLong()
                                val canAfford = money >= nextCost

                                BuildingUpgradeRow(
                                    upgrade = b,
                                    currentLvl = currentLvl,
                                    isMax = isMax,
                                    nextCost = nextCost,
                                    canAfford = canAfford,
                                    onBuy = {
                                        if (!isMax && canAfford) {
                                            engine.saveManager.money -= nextCost
                                            engine.saveManager.setBuildingUpgradeLevel(b.id, currentLvl + 1)
                                            engine.audioEngine.playUpgradeFanfare()
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
    }
}

@Composable
fun ShopTabButton(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF1D4ED8) else Color(0xFF1E293B))
            .border(1.2.dp, if (selected) Color(0xFF60A5FA) else Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ShovelRow(
    item: com.example.game.data.ShovelUpgrade,
    isOwned: Boolean,
    isNext: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val formattedCost = NumberFormat.getNumberInstance(Locale.US).format(item.cost)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isOwned) Color(0xFF143424) else Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(1.dp, if (isOwned) Color(0xFF22C55E) else Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Lv.${item.level} ${item.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (item.canBreakHardRock) {
                    Spacer(Modifier.width(6.dp))
                    Text("⛏️ Granite Breaker", color = Color(0xFFFFD54F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "Dig Power: ${item.power}x  •  Speed: ${item.speedMultiplier}x",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = item.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        Spacer(Modifier.width(8.dp))

        if (isOwned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF15803D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Equipped", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("EQUIPPED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else if (isNext) {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("$$formattedCost", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        } else {
            Text("Locked", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BackpackRow(
    item: com.example.game.data.BackpackUpgrade,
    isOwned: Boolean,
    isNext: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val formattedCost = NumberFormat.getNumberInstance(Locale.US).format(item.cost)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isOwned) Color(0xFF143424) else Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(1.dp, if (isOwned) Color(0xFF22C55E) else Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Lv.${item.level} Haul Pack", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "Capacity: ${item.capacity} Gems",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = item.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        Spacer(Modifier.width(8.dp))

        if (isOwned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF15803D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Equipped", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("EQUIPPED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else if (isNext) {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("$$formattedCost", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        } else {
            Text("Locked", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BootsRow(
    item: com.example.game.data.BootsUpgrade,
    isOwned: Boolean,
    isNext: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val formattedCost = NumberFormat.getNumberInstance(Locale.US).format(item.cost)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isOwned) Color(0xFF143424) else Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(1.dp, if (isOwned) Color(0xFF22C55E) else Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Lv.${item.level} ${item.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "Move: +${((item.moveSpeedMultiplier - 1f) * 100).toInt()}%  •  Jump: +${((item.jumpImpulseMultiplier - 1f) * 100).toInt()}%  •  Climb: +${((item.climbSpeedMultiplier - 1f) * 100).toInt()}%",
                color = Color(0xFFA7F3D0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = item.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        Spacer(Modifier.width(8.dp))

        if (isOwned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF15803D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Equipped", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("EQUIPPED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else if (isNext) {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("$$formattedCost", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        } else {
            Text("Locked", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HeadlampRow(
    item: com.example.game.data.HeadlampUpgrade,
    isOwned: Boolean,
    isNext: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val formattedCost = NumberFormat.getNumberInstance(Locale.US).format(item.cost)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isOwned) Color(0xFF143424) else Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(1.dp, if (isOwned) Color(0xFF22C55E) else Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Lv.${item.level} ${item.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "Beam Radius: ${item.lightRadius.toInt()}px  •  Cavern Clarity: ${(item.beamBrightness * 100).toInt()}%",
                color = Color(0xFFFDE047),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = item.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        Spacer(Modifier.width(8.dp))

        if (isOwned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF15803D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Equipped", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("EQUIPPED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else if (isNext) {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("$$formattedCost", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        } else {
            Text("Locked", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BuildingUpgradeRow(
    upgrade: com.example.game.data.PersistentBuildingUpgrade,
    currentLvl: Int,
    isMax: Boolean,
    nextCost: Long,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val formattedCost = NumberFormat.getNumberInstance(Locale.US).format(nextCost)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = upgrade.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Lvl $currentLvl / ${upgrade.maxLevel}",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Text(text = upgrade.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
            if (currentLvl > 0) {
                Text(
                    text = "Current: " + upgrade.effectDescription(currentLvl),
                    color = Color(0xFF4ADE80),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        if (isMax) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF475569), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("MAX LEVEL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("$$formattedCost", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
}
