package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine

@Composable
fun TutorialOverlay(
    engine: GameEngine,
    step: Int,
    modifier: Modifier = Modifier
) {
    if (step !in 1..10) return

    val (title, instruction) = when (step) {
        1 -> "Step 1: Your Broken House" to "Look at your house! It is ruined and abandoned. You need money to repair it."
        2 -> "Step 2: The Mine Nearby" to "Notice the underground mine shaft to the right. Deep riches lie buried below!"
        3 -> "Step 3: Walk to the Mine" to "Use the D-Pad [RIGHT] to walk toward the wooden mine shaft."
        4 -> "Step 4: Climb Down" to "Hold [DOWN] on the wooden ladder to descend underground."
        5 -> "Step 5: Dig Your First Block" to "Stand next to the dirt block and press the [DIG] button."
        6 -> "Step 6: Reveal the Gem" to "Look! A glowing crystal is protruding from the dirt! Keep digging to free it."
        7 -> "Step 7: Collect the Gem" to "Walk near the dropped gem and press the green [COLLECT] button."
        8 -> "Step 8: Backpack Capacity" to "Your gem is safely stored in your backpack! Watch your 3-slot capacity."
        9 -> "Step 9: Climb Back Up" to "Hold [JUMP] against the dirt wall to climb up, or climb the ladder to surface."
        10 -> "Step 10: Sell to Repair House" to "Walk near the Merchant Stand to sell your gems and fund your house repairs!"
        else -> "" to ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    // Skip or advance on tap
                    engine.advanceTutorial(step)
                }
                .background(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))
                )
                .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFFD54F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$step", color = Color(0xFF263238), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = instruction,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.width(12.dp))
            Text("(Tap to skip)", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
        }
    }
}
