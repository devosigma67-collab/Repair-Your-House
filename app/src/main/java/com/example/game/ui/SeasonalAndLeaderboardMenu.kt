package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.game.data.GemRegistry
import com.example.game.data.LeaderboardRegistry
import com.example.game.data.SeasonalChallenge
import com.example.game.data.SeasonalRegistry
import com.example.game.engine.GameEngine

@Composable
fun SeasonalAndLeaderboardMenu(
    engine: GameEngine,
    initialTab: Int = 0, // 0: Seasons, 1: Leaderboard
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var claimedIds by remember { mutableStateOf(engine.saveManager.getClaimedChallenges()) }

    val playerNetWorth = engine.saveManager.lifetimeEarnings
    val deepestLevel = engine.saveManager.deepestDepth
    val discoveredGems = engine.saveManager.getDiscoveredGemIds()
    val rarestGem = discoveredGems.mapNotNull { GemRegistry.getGemById(it) }.maxByOrNull { it.rarity }?.name ?: "None Yet"

    val standings = LeaderboardRegistry.getStandings(playerNetWorth, deepestLevel, rarestGem)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10141C))
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
                    Text("🏆 COMPETITIONS & ARCHIVES", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 20.sp)
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

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopTabButton(title = "⚡ SEASONAL CHALLENGES", selected = selectedTab == 0) { selectedTab = 0 }
                ShopTabButton(title = "🌍 GLOBAL HALL OF FAME", selected = selectedTab == 1) { selectedTab = 1 }
            }

            Spacer(Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Seasonal challenges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF4A148C), Color(0xFF311B92))),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(SeasonalRegistry.CURRENT_SEASON_NAME, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text("Complete limited-time expedition feats for huge rewards", color = Color(0xFFE1BEE7), fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(SeasonalRegistry.SEASON_TIME_REMAINING, color = Color(0xFF3E2723), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SeasonalRegistry.CHALLENGES) { ch ->
                            val currentVal = getChallengeProgress(ch.id, engine)
                            val isComplete = currentVal >= ch.target
                            val isClaimed = claimedIds.contains(ch.id)

                            SeasonalChallengeRow(
                                challenge = ch,
                                current = currentVal,
                                isComplete = isComplete,
                                isClaimed = isClaimed,
                                onClaim = {
                                    if (isComplete && !isClaimed) {
                                        engine.saveManager.money += ch.rewardMoney
                                        engine.saveManager.lifetimeEarnings += ch.rewardMoney
                                        engine.saveManager.claimChallenge(ch.id)
                                        engine.audioEngine.playUpgradeFanfare()
                                        claimedIds = engine.saveManager.getClaimedChallenges()
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Leaderboard
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(standings) { entry ->
                        LeaderboardRowItem(entry)
                    }
                }
            }
        }
    }
}

private fun getChallengeProgress(id: String, engine: GameEngine): Long {
    return when (id) {
        "dig_50", "dig_300" -> engine.saveManager.totalBlocksDug
        "depth_25", "depth_100", "depth_300" -> engine.saveManager.deepestDepth.toLong()
        "collect_10_rare" -> {
            val disc = engine.saveManager.getDiscoveredGemIds()
            disc.count { id -> (GemRegistry.getGemById(id)?.rarity ?: 0) >= 10 }.toLong()
        }
        "house_5_repairs", "house_complete" -> engine.saveManager.getCompletedRepairs().size.toLong()
        else -> 0L
    }
}

@Composable
fun SeasonalChallengeRow(
    challenge: SeasonalChallenge,
    current: Long,
    isComplete: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val progress = (current.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isClaimed) Color(0xFF1B2E24) else Color(0xFF1E242E), RoundedCornerShape(12.dp))
            .border(1.dp, if (isComplete && !isClaimed) Color(0xFFFFD54F) else Color(0xFF2C3542), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(challenge.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(challenge.description, color = Color(0xFFB0BEC5), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF37474F)
            )
            Text(
                text = "${minOf(current, challenge.target)} / ${challenge.target}",
                color = Color(0xFF90A4AE),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (isClaimed) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Claimed", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("CLAIMED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        } else if (isComplete) {
            val formattedReward = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(challenge.rewardMoney)
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("CLAIM $$formattedReward", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        } else {
            val formattedReward = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(challenge.rewardMoney)
            Box(
                modifier = Modifier
                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Reward: $$formattedReward", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LeaderboardRowItem(entry: com.example.game.data.LeaderboardEntry) {
    val isPlayer = entry.isPlayer
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFCFD8DC)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF90A4AE)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isPlayer) Color(0xFF2E3D2F) else Color(0xFF1B2028), RoundedCornerShape(10.dp))
            .border(if (isPlayer) 1.5.dp else 1.dp, if (isPlayer) Color(0xFF81C784) else Color(0xFF2B3340), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#${entry.rank}",
                color = rankColor,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                modifier = Modifier.width(36.dp)
            )
            Text(entry.countryFlag, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = entry.name,
                    color = if (isPlayer) Color(0xFF81C784) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Depth: ${entry.deepestLevel}m | Best: ${entry.rarestGemName}",
                    color = Color(0xFF90A4AE),
                    fontSize = 10.sp
                )
            }
        }

        val formattedNetWorth = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(entry.netWorth)
        Text(
            text = "$$formattedNetWorth",
            color = Color(0xFFFFD54F),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}
