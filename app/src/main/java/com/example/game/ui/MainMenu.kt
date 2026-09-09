package com.example.game.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.data.HouseRegistry
import com.example.game.engine.GameEngine
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.sin

@Composable
fun MainMenu(
    engine: GameEngine,
    onStartGame: () -> Unit,
    onOpenHouse: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenGemdex: () -> Unit,
    onOpenSeasons: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val logoBitmap = rememberGameLogo()
    val completedRepairs = engine.saveManager.getCompletedRepairs()
    val currentStage = HouseRegistry.getStageForCompletedCount(completedRepairs.size)
    val progressPct = HouseRegistry.calculateProgressPercentage(completedRepairs)
    val discoveredGems = engine.saveManager.getDiscoveredGemIds().size
    val claimedChallenges = engine.saveManager.getClaimedChallenges().size
    var showHelpDialog by remember { mutableStateOf(false) }

    // Ambient floating particles animation
    val infiniteTransition = rememberInfiniteTransition(label = "menu_particles")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_float"
    )

    // Pulsing play button border
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0F1D),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
    ) {
        // AMBIENT DRIFTING EMBER & CRYSTAL PARTICLES CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 0..28) {
                val seed = i * 137.5f
                val speed = 0.4f + (i % 5) * 0.15f
                val px = (seed * 11f + animTime * speed * 8f) % w
                val py = (h - ((animTime * speed * 25f + seed * 19f) % (h + 80f)))
                val sway = sin(animTime * 0.05f + seed) * 14f
                val radius = 1.5f + (i % 4) * 1.2f
                val alpha = (0.25f + sin(animTime * 0.08f + i.toFloat()) * 0.2f).coerceIn(0.1f, 0.7f)
                val color = when (i % 5) {
                    0 -> Color(0xFFFFD54F) // Gold
                    1 -> Color(0xFF00E5FF) // Diamond Cyan
                    2 -> Color(0xFFE040FB) // Amethyst Violet
                    3 -> Color(0xFFFF5252) // Ruby Red
                    else -> Color(0xFF69F0AE) // Emerald
                }
                drawCircle(color.copy(alpha = alpha), radius = radius, center = Offset(px + sway, py))
            }
        }

        // MAIN CONTENT CONTAINER
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT COLUMN: Title / Logo, Elite House Progress, and Stats Ribbon
            Column(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                // TOP BRANDING & LOGO
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap,
                                contentDescription = "Game Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFFE65100), Color(0xFFFF9800))
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "⛏️ REPAIR YOUR HOUSE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .background(Color(0x33FFD54F), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v3.0 ELITE",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "DEEP CAVERNS • 60 GEMSTONES • 100+ UPGRADES • MANSION",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.6.sp
                    )
                }

                // ESTATE RENOVATION PROGRESS CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenHouse() }
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏠 ", fontSize = 16.sp)
                                Text(
                                    text = "Stage ${currentStage.stageNumber}: ${currentStage.title}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "$progressPct% RESTORED",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Dual-tone high-contrast progress bar
                        LinearProgressIndicator(
                            progress = { progressPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF1E293B),
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${completedRepairs.size}/18 Renovations Completed",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp
                            )
                            Text(
                                text = "TAP TO EXPAND ESTATE →",
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // 4-STAT DOCK (Glassmorphic mini-counters)
                val formattedMoney = NumberFormat.getNumberInstance(Locale.US).format(engine.saveManager.money)

                Row(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMiniCard(
                        label = "FUNDS",
                        value = "$$formattedMoney",
                        color = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "MAX DEPTH",
                        value = "${engine.saveManager.deepestDepth}m",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "GEMDEX",
                        value = "$discoveredGems/40",
                        color = Color(0xFFC084FC),
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniCard(
                        label = "SEASON",
                        value = "$claimedChallenges/8 Done",
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // RIGHT COLUMN: Big Play Button, Subsystem Grid, Utility Row
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.End
            ) {
                // HERO PLAY / RESUME MINING BUTTON
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        2.5.dp,
                        Color(0xFF4ADE80).copy(alpha = pulseGlow)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .height(58.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (engine.saveManager.totalBlocksDug > 0) "RESUME EXPEDITION" else "START EXPEDITION",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Dug: ${engine.saveManager.totalBlocksDug} Blocks • Max: ${engine.saveManager.deepestDepth}m",
                            fontSize = 10.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }

                // 2x2 SUBSYSTEM GRID (UPGRADES, REPAIRS, GEMDEX, SEASONS)
                Column(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MenuEliteButton(
                            icon = Icons.Filled.Home,
                            title = "ESTATE REPAIRS",
                            badge = "${completedRepairs.size}/18",
                            primaryColor = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenHouse
                        )
                        MenuEliteButton(
                            icon = Icons.Filled.ShoppingBag,
                            title = "OUTFITTER (100+)",
                            badge = "SHOP",
                            primaryColor = Color(0xFF2563EB),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenShop
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MenuEliteButton(
                            icon = Icons.Filled.Star,
                            title = "GEMDEX (40 GEMS)",
                            badge = "$discoveredGems/40",
                            primaryColor = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenGemdex
                        )
                        MenuEliteButton(
                            icon = Icons.Filled.EmojiEvents,
                            title = "SEASONS & RANKS",
                            badge = "TIERS",
                            primaryColor = Color(0xFF059669),
                            modifier = Modifier.weight(1f),
                            onClick = onOpenSeasons
                        )
                    }
                }

                // BOTTOM UTILITY DOCK (Leaderboards, Guide/Help, Settings)
                Row(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leaderboards direct button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenLeaderboard() }
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard", tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("LEADERBOARD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                    }

                    Spacer(Modifier.width(8.dp))

                    // How to Play / Guide
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = "Help Guide", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Spacer(Modifier.width(8.dp))

                    // Settings & Audio
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // ELITE MINING MANUAL / HOW TO PLAY DIALOG
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(Color(0xFF0F172A), RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "📖 MINER'S FIELD MANUAL & MECHANICS",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        HelpBullet(
                            title = "Depth Scaling & Hardness",
                            desc = "The deeper you descend, the harder rock becomes, but gem drop rates and cluster yields dramatically multiply (depth 40m+ grants colossal bonus clusters)!"
                        )
                        HelpBullet(
                            title = "100+ Upgrades & Automation",
                            desc = "Equip Shovels, Haul Packs, Boosted Boots & Headlamps. Build Estate Automation (Drone Hauler, Magma Smelter, Fossil Museum & Rental Cabin) for passive wealth!"
                        )
                        HelpBullet(
                            title = "Controls & Wall Climbing",
                            desc = "Move with D-Pad or virtual joystick. Hold JUMP against dirt/rock to scale cavern walls without ladders. Press DIG to excavate."
                        )
                        HelpBullet(
                            title = "Estate Renovation",
                            desc = "Return to surface, sell your minerals at the Merchant Counter, and rebuild the ruined shack into a 5-Star Luxury Sovereign Estate!"
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = { showHelpDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("UNDERSTOOD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpBullet(title: String, desc: String) {
    Column {
        Text(text = "• $title", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(text = desc, color = Color(0xFFCBD5E1), fontSize = 11.sp, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
fun MenuEliteButton(
    icon: ImageVector,
    title: String,
    badge: String,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(primaryColor.copy(alpha = 0.85f), primaryColor.copy(alpha = 0.65f))
                )
            )
            .border(1.2.dp, primaryColor.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        }
    }
}
