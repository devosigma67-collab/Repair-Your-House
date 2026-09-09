package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.HouseRegistry
import com.example.game.engine.GameEngine
import com.example.game.engine.MiningWorld
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HUDOverlay(
    engine: GameEngine,
    onOpenHouse: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenGemdex: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenSellDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backpackCount = engine.backpack.size
    val backpackCap = engine.backpackCapacity
    val money = engine.saveManager.money
    val lifetimeEarnings = engine.saveManager.lifetimeEarnings
    val sifterLvl = engine.saveManager.getBuildingUpgradeLevel("gem_sifter")
    val bonusMultiplier = 1f + sifterLvl * 0.06f
    val pendingHaulValue = engine.backpack.sumOf { (it.value * bonusMultiplier).toLong() }
    val formattedPendingValue = NumberFormat.getNumberInstance(Locale.US).format(pendingHaulValue)
    val currentLevel = maxOf(0, (engine.playerY / MiningWorld.TILE_SIZE).toInt())
    val depthMeters = (currentLevel * 1.5).toInt()
    val completedRepairs = engine.saveManager.getCompletedRepairs()
    val housePercent = HouseRegistry.calculateProgressPercentage(completedRepairs)
    val houseStage = HouseRegistry.getStageForCompletedCount(completedRepairs.size)

    val backpackFull by engine.backpackFullWarning.collectAsState()
    val milestoneText by engine.milestoneAnnouncement.collectAsState()
    val showSellStand by engine.showSellStand.collectAsState()

    val formattedMoney = NumberFormat.getNumberInstance(Locale.US).format(money)
    val formattedLifetime = NumberFormat.getNumberInstance(Locale.US).format(lifetimeEarnings)

    val infiniteTransition = rememberInfiniteTransition(label = "gold_coin_shine")
    val coinScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // TOP HUD BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- LEFT: ULTRA-PROMINENT GOLD VAULT & MONEY DISPLAY ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // LUXURY GOLD VAULT BADGE (Prominently shows exactly how much money the player has!)
                Box(
                    modifier = Modifier
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF1B5E20), Color(0xFF004D40), Color(0xFF1E3A1F))
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFFFD54F), Color(0xFFFFA000), Color(0xFFFFECB3))
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 3D Coin Badge
                        Box(
                            modifier = Modifier
                                .scale(coinScale)
                                .size(28.dp)
                                .shadow(4.dp, CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFF8F00))
                                    ),
                                    shape = CircleShape
                                )
                                .border(1.5.dp, Color(0xFFFFF8E1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = Color(0xFF4E342E),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = formattedMoney,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "NET: $$formattedLifetime",
                                color = Color(0xFFA5D6A7),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Depth Meter Pill
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF1E293B).copy(alpha = 0.9f), Color(0xFF0F172A).copy(alpha = 0.9f))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(1.2.dp, Color(0xFF38BDF8), RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⛏️ ",
                            fontSize = 11.sp
                        )
                        Column {
                            Text(
                                text = "${depthMeters}m (Lvl $currentLevel)",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "PICKAXE LV.${engine.saveManager.shovelLevel}",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            // --- RIGHT: BACKPACK, HOUSE RESTORATION & MENUS ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Backpack Pill
                val isPackFull = backpackCount >= backpackCap
                Box(
                    modifier = Modifier
                        .shadow(if (isPackFull) 8.dp else 2.dp, RoundedCornerShape(18.dp))
                        .background(
                            brush = if (isPackFull) {
                                Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFD32F2F)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                            },
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(
                            width = 1.2.dp,
                            color = if (isPackFull) Color(0xFFFF8A80) else Color(0xFF64748B),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎒", fontSize = 13.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$backpackCount/$backpackCap",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                // House Restoration Progress Pill (Clickable to open house overhaul)
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenHouse() }
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFE65100), Color(0xFFF57C00))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFFE082), RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏡", fontSize = 13.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$housePercent%",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                // Quick Menu Buttons (Shop, Gemdex, Pause Menu)
                IconButton(
                    onClick = onOpenShop,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color(0xFF0284C7), CircleShape)
                        .border(1.2.dp, Color(0xFF7DD3FC), CircleShape)
                ) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = "Shop", tint = Color.White, modifier = Modifier.size(19.dp))
                }

                IconButton(
                    onClick = onOpenGemdex,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color(0xFF7C3AED), CircleShape)
                        .border(1.2.dp, Color(0xFFC4B5FD), CircleShape)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = "Gemdex", tint = Color.White, modifier = Modifier.size(19.dp))
                }

                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color(0xFF475569), CircleShape)
                        .border(1.2.dp, Color(0xFF94A3B8), CircleShape)
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
        }

        // QUICK SELL POPUP BANNER WHEN NEAR SURFACE STAND
        AnimatedVisibility(
            visible = showSellStand,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            onOpenSellDialog()
                        }
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF004D40))
                            )
                        )
                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(text = "💰 ", fontSize = 16.sp)
                    Text(
                        text = "MERCHANT STAND: TAP TO APPRAISE & SELL (+$${formattedPendingValue})",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // BACKPACK FULL WARNING BANNER
        AnimatedVisibility(
            visible = backpackFull,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { engine.dismissBackpackWarning() }
                        .background(Color(0xFFDC2626))
                        .border(2.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⚠️ BACKPACK FULL! Return to surface stand to sell gems.",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // MILESTONE ANNOUNCEMENT BANNER
        AnimatedVisibility(
            visible = milestoneText != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            milestoneText?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { engine.dismissMilestone() }
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF581C87), Color(0xFF1E1B4B))
                                )
                            )
                            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = text,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "(Tap to dismiss)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
