package com.example.game.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine

@Composable
fun ControlsOverlay(
    engine: GameEngine,
    opacity: Float,
    scaleFactor: Float,
    modifier: Modifier = Modifier
) {
    val canCollect by engine.canCollect.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .alpha(opacity)
    ) {
        // --- LEFT: ERGONOMIC CONTROLLER (UP REMOVED, LEFT / RIGHT / DOWN) ---
        // Sleek arcade pod: Left & Right side-by-side with Down centered below
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .scale(scaleFactor)
                .padding(bottom = 6.dp, start = 6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(
                        color = Color(0x99101820),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF546E7A), Color(0xFF263238))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(8.dp)
            ) {
                // Horizontal Row: LEFT and RIGHT
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArcadeDirectionButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "LEFT",
                        width = 66.dp,
                        height = 54.dp,
                        accentColor = Color(0xFF4FC3F7),
                        onPressChange = { pressed ->
                            engine.inputLeft = pressed
                        }
                    )

                    ArcadeDirectionButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        label = "RIGHT",
                        width = 66.dp,
                        height = 54.dp,
                        accentColor = Color(0xFF4FC3F7),
                        onPressChange = { pressed ->
                            engine.inputRight = pressed
                        }
                    )
                }

                // Centered below: DOWN button (for ladders and targeting down)
                ArcadeDirectionButton(
                    icon = Icons.Filled.ArrowDownward,
                    label = "DOWN",
                    width = 76.dp,
                    height = 50.dp,
                    accentColor = Color(0xFFFFB74D),
                    onPressChange = { pressed ->
                        engine.inputDown = pressed
                    }
                )
            }
        }

        // --- RIGHT: ACTION BUTTONS (JUMP / CLIMB, DIG, COLLECT) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .scale(scaleFactor)
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            // COLLECT BUTTON (Positioned cleanly above/left of Jump)
            Box(
                modifier = Modifier
                    .offset(x = (-18).dp, y = (-88).dp)
                    .align(Alignment.BottomEnd)
            ) {
                CollectActionButton(
                    canCollect = canCollect,
                    onPress = {
                        engine.inputCollectPress = true
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                // DIG BUTTON
                DigActionButton(
                    onPressChange = { pressed ->
                        engine.inputDigPress = pressed
                    }
                )

                // DUAL JUMP & CLIMB BUTTON (Takes over UP button's job completely)
                JumpClimbActionButton(
                    onPressChange = { pressed ->
                        engine.inputJumpHold = pressed
                        engine.inputUp = pressed // dual mapping ensures 100% ladder climbing responsiveness
                        if (pressed) {
                            engine.jump()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ArcadeDirectionButton(
    icon: ImageVector,
    label: String,
    width: Dp,
    height: Dp,
    accentColor: Color,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = width, height = height)
            .scale(if (isPressed) 0.93f else 1.0f)
            .background(
                brush = if (isPressed) {
                    Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.5f), Color(0xFF1E293B)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = if (isPressed) accentColor else Color(0xFF64748B),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChange(false)
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) Color.White else accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun JumpClimbActionButton(
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(92.dp)
            .scale(if (isPressed) 0.90f else 1.0f)
            .shadow(elevation = if (isPressed) 2.dp else 8.dp, shape = CircleShape)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        if (isPressed) Color(0xFF00E5FF) else Color(0xFF2979FF),
                        Color(0xFF1565C0),
                        Color(0xFF0D47A1)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = if (isPressed) 3.dp else 2.5.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE1F5FE), Color(0xFF81D4FA))
                ),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChange(false)
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "JUMP",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "CLIMB & UP",
                color = Color(0xFFB3E5FC),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun DigActionButton(
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .scale(if (isPressed) 0.90f else 1.0f)
            .shadow(elevation = if (isPressed) 2.dp else 6.dp, shape = CircleShape)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        if (isPressed) Color(0xFFFFD54F) else Color(0xFFFF9800),
                        Color(0xFFE65100),
                        Color(0xFFBF360C)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = if (isPressed) 3.dp else 2.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFFFF8E1), Color(0xFFFFB74D))
                ),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChange(false)
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⛏️",
                fontSize = 18.sp
            )
            Text(
                text = "DIG",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun CollectActionButton(
    canCollect: Boolean,
    onPress: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "collect_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (canCollect) 1.10f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .scale(if (isPressed) 0.90f else pulseScale)
            .shadow(elevation = if (canCollect) 10.dp else 4.dp, shape = CircleShape)
            .background(
                brush = Brush.radialGradient(
                    if (canCollect) {
                        listOf(Color(0xFF69F0AE), Color(0xFF00E676), Color(0xFF00B0FF))
                    } else {
                        listOf(Color(0xFF37474F), Color(0xFF263238))
                    }
                ),
                shape = CircleShape
            )
            .border(
                width = if (canCollect) 2.5.dp else 1.dp,
                color = if (canCollect) Color(0xFFB9F6CA) else Color(0xFF78909C),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "💎", fontSize = 16.sp)
            Text(
                text = "LOOT",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
