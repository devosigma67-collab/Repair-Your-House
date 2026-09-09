package com.example.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import com.example.game.data.Gem
import com.example.game.data.GemSilhouette
import com.example.game.data.HouseRegistry
import com.example.game.data.HouseStage
import com.example.game.engine.Block
import com.example.game.engine.BlockType
import com.example.game.engine.GameEngine
import com.example.game.engine.MiningWorld
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun drawWorld(
        drawScope: DrawScope,
        engine: GameEngine,
        screenWidth: Float,
        screenHeight: Float
    ) {
        val tileSize = MiningWorld.TILE_SIZE
        val camX = engine.cameraX
        val camY = engine.cameraY
        val shake = if (engine.screenShakeAmount > 0f) {
            (Math.random().toFloat() * 2f - 1f) * engine.screenShakeAmount
        } else 0f

        // Screen center offset
        val offsetX = screenWidth / 2f - camX + shake
        val offsetY = screenHeight / 2f - camY + shake

        // 1. Draw Atmospheric Sky & Subterranean Depths
        drawAtmosphere(drawScope, screenWidth, screenHeight, camY, offsetX, offsetY)

        // 2. Draw Surface Elements (Grass, House, Headframe, Merchant Stand)
        drawSurface(drawScope, engine, offsetX, offsetY, tileSize)

        // 3. Draw Underground Blocks
        val minVisibleTileX = maxOf(0, ((camX - screenWidth / 2f) / tileSize).toInt() - 1)
        val maxVisibleTileX = minOf(MiningWorld.WIDTH_TILES - 1, ((camX + screenWidth / 2f) / tileSize).toInt() + 1)
        val minVisibleTileY = maxOf(0, ((camY - screenHeight / 2f) / tileSize).toInt() - 1)
        val maxVisibleTileY = minOf(MiningWorld.MAX_DEPTH_LEVELS, ((camY + screenHeight / 2f) / tileSize).toInt() + 1)

        for (ty in minVisibleTileY..maxVisibleTileY) {
            for (tx in minVisibleTileX..maxVisibleTileX) {
                val block = engine.miningWorld.getBlock(tx, ty)
                val bx = tx * tileSize + offsetX
                val by = ty * tileSize + offsetY
                drawBlock(drawScope, block, bx, by, tileSize, engine)
            }
        }

        // 4. Draw Collectible Drops
        for (drop in engine.miningWorld.activeDrops) {
            val dx = drop.x + offsetX
            val dy = drop.y + offsetY
            drawDrop(drawScope, drop, dx, dy)
        }

        // 5. Draw Player Character & Shovel
        val px = engine.playerX + offsetX
        val py = engine.playerY + offsetY
        drawPlayer(drawScope, engine, px, py)

        // 6. Draw Particles, Smoke & Floating Text
        drawParticles(drawScope, engine, offsetX, offsetY)
    }

    private fun drawAtmosphere(
        drawScope: DrawScope,
        w: Float,
        h: Float,
        camY: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        val groundScreenY = offsetY

        // When viewing near surface: Draw lush sky, sun, clouds, distant mountains
        if (camY < 180f) {
            // Celestial daylight gradient
            drawScope.drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF93C5FD), Color(0xFFFEF08A))
                ),
                size = Size(w, h)
            )

            // Radiant Sun with glowing corona
            val sunX = w * 0.78f
            val sunY = 50f
            drawScope.drawCircle(
                color = Color(0x33FFEB3B),
                radius = 65f,
                center = Offset(sunX, sunY)
            )
            drawScope.drawCircle(
                color = Color(0x66FFF59D),
                radius = 42f,
                center = Offset(sunX, sunY)
            )
            drawScope.drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 24f,
                center = Offset(sunX, sunY)
            )

            // Fluffy procedural clouds drifting across the sky
            val time = (System.currentTimeMillis() % 60000L) / 60000f
            val cloudOffset1 = (time * w * 1.5f) % (w + 200f) - 100f
            val cloudOffset2 = ((time * 0.7f + 0.5f) * w * 1.5f) % (w + 200f) - 100f
            drawFluffyCloud(drawScope, cloudOffset1, 40f, 1.2f)
            drawFluffyCloud(drawScope, cloudOffset2, 85f, 0.9f)

            // Distant Purple-Blue Mountain Ridges (Atmospheric perspective)
            val mountainPath = Path().apply {
                moveTo(-50f, groundScreenY)
                lineTo(w * 0.15f, groundScreenY - 140f)
                lineTo(w * 0.35f, groundScreenY - 90f)
                lineTo(w * 0.55f, groundScreenY - 170f)
                lineTo(w * 0.75f, groundScreenY - 100f)
                lineTo(w * 0.95f, groundScreenY - 150f)
                lineTo(w + 50f, groundScreenY)
                close()
            }
            drawScope.drawPath(
                mountainPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF64748B).copy(alpha = 0.5f), Color(0xFF94A3B8).copy(alpha = 0.7f)),
                    startY = groundScreenY - 170f,
                    endY = groundScreenY
                )
            )

            // Midground Pine Forest Silhouettes
            var pineX = -20f
            while (pineX < w + 30f) {
                val pHeight = 28f + (sin(pineX * 0.05f) * 8f)
                val pinePath = Path().apply {
                    moveTo(pineX, groundScreenY)
                    lineTo(pineX + 12f, groundScreenY - pHeight)
                    lineTo(pineX + 24f, groundScreenY)
                    close()
                }
                drawScope.drawPath(pinePath, Color(0xFF1E3A1E).copy(alpha = 0.65f))
                pineX += 18f
            }
        } else {
            // Deep subterranean atmosphere based on depth zone
            val depthRatio = (camY / (MiningWorld.MAX_DEPTH_LEVELS * MiningWorld.TILE_SIZE)).coerceIn(0f, 1f)
            val topColor = Color(0xFF0F172A)
            val bottomColor = when {
                depthRatio < 0.25f -> Color(0xFF1C1917) // Warm loam & stone
                depthRatio < 0.50f -> Color(0xFF0F172A) // Granite caverns
                depthRatio < 0.70f -> Color(0xFF2E1065) // Bioluminescent crystal deeps
                depthRatio < 0.88f -> Color(0xFF450A0A) // Scorched magma mantle
                else -> Color(0xFF030712) // Cosmic primordial void
            }

            drawScope.drawRect(
                brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
                size = Size(w, h)
            )

            // Ethereal cavern dust motes floating in dark air
            val animTick = System.currentTimeMillis() * 0.001f
            for (i in 0 until 12) {
                val dx = (sin(i * 1.7f + animTick * 0.4f) * 0.5f + 0.5f) * w
                val dy = (cos(i * 2.3f + animTick * 0.3f) * 0.5f + 0.5f) * h
                val alpha = (sin(i + animTick) * 0.3f + 0.4f).coerceIn(0.1f, 0.7f)
                val color = if (depthRatio > 0.6f) Color(0xFF818CF8) else Color(0xFFE2E8F0)
                drawScope.drawCircle(color.copy(alpha = alpha), 2f, Offset(dx, dy))
            }
        }
    }

    private fun drawFluffyCloud(drawScope: DrawScope, x: Float, y: Float, scale: Float) {
        val cloudColor = Color.White.copy(alpha = 0.82f)
        drawScope.drawCircle(cloudColor, 20f * scale, Offset(x, y))
        drawScope.drawCircle(cloudColor, 26f * scale, Offset(x + 22f * scale, y - 8f * scale))
        drawScope.drawCircle(cloudColor, 24f * scale, Offset(x + 46f * scale, y - 4f * scale))
        drawScope.drawCircle(cloudColor, 18f * scale, Offset(x + 66f * scale, y))
        drawScope.drawRoundRect(
            cloudColor,
            topLeft = Offset(x - 8f * scale, y),
            size = Size(82f * scale, 14f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
    }

    private fun drawSurface(
        drawScope: DrawScope,
        engine: GameEngine,
        offsetX: Float,
        offsetY: Float,
        tileSize: Float
    ) {
        val groundY = 0f + offsetY

        // Multi-layered lush grass with vibrant blades
        val surfaceLength = MiningWorld.WIDTH_TILES * tileSize + 100f

        // Dark soil underbed
        drawScope.drawRect(
            color = Color(0xFF3E2723),
            topLeft = Offset(-50f, groundY - 4f),
            size = Size(surfaceLength, 12f)
        )

        // Vibrant grass turf
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)),
                startY = groundY - 14f,
                endY = groundY
            ),
            topLeft = Offset(-50f, groundY - 14f),
            size = Size(surfaceLength, 12f)
        )

        // Animated waving grass blades & scattered wildflowers
        val timeAnim = System.currentTimeMillis() * 0.003f
        var gx = -20f
        var bladeIdx = 0
        while (gx < surfaceLength) {
            val sway = sin(timeAnim + gx * 0.1f) * 3f
            val bladeHeight = 7f + (sin(gx * 0.3f) * 3f)
            drawScope.drawLine(
                color = if (bladeIdx % 2 == 0) Color(0xFF81C784) else Color(0xFF43A047),
                start = Offset(gx, groundY - 13f),
                end = Offset(gx + sway, groundY - 13f - bladeHeight),
                strokeWidth = 2f
            )

            // Wildflowers every few paces
            if (bladeIdx % 11 == 0 && (gx !in (7.5f * tileSize)..(11.5f * tileSize))) {
                val flowerColor = when ((bladeIdx / 11) % 4) {
                    0 -> Color(0xFFFFEB3B) // Yellow buttercup
                    1 -> Color(0xFFF43F5E) // Red poppy
                    2 -> Color(0xFFC084FC) // Lavender
                    else -> Color(0xFFFFFFFF) // White daisy
                }
                drawScope.drawCircle(flowerColor, 3.5f, Offset(gx + sway, groundY - 14f - bladeHeight))
                drawScope.drawCircle(Color(0xFFFFD54F), 1.5f, Offset(gx + sway, groundY - 14f - bladeHeight))
            }

            gx += 7f
            bladeIdx++
        }

        // Cobblestone walkway connecting house, merchant stall, and mineshaft
        var stoneX = 1f * tileSize + offsetX
        val endWalkX = 11f * tileSize + offsetX
        while (stoneX < endWalkX) {
            drawScope.drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(stoneX, groundY - 8f),
                size = Size(16f, 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            stoneX += 22f
        }

        // Draw The House at columns 1 to 7
        val houseX = 1f * tileSize + offsetX
        drawHouse(drawScope, engine, houseX, groundY)

        // Draw Gem Merchant Stand at column 8 to 10
        val stallX = 7.8f * tileSize + offsetX
        drawMerchantStall(drawScope, stallX, groundY, engine)

        // Draw Mine Shaft Headframe at column 10 to 11
        val shaftX = 10.2f * tileSize + offsetX
        drawMineHeadframe(drawScope, shaftX, groundY)
    }

    private fun drawHouse(drawScope: DrawScope, engine: GameEngine, x: Float, groundY: Float) {
        val completedRepairs = engine.saveManager.getCompletedRepairs()
        val stage = HouseRegistry.getStageForCompletedCount(completedRepairs.size)

        val houseWidth = 240f
        val isMansion = stage.stageNumber >= 6
        val isMultiStory = stage.stageNumber >= 4
        val houseHeight = when {
            isMansion -> 175f
            isMultiStory -> 140f
            else -> 110f
        }
        val houseTop = groundY - houseHeight

        // 1. Foundation: Stone masonry with carved brick lines
        val hasFoundation = completedRepairs.contains("foundation")
        val foundationColor = if (hasFoundation) Color(0xFF546E7A) else Color(0xFF4E342E)
        drawScope.drawRect(
            color = foundationColor,
            topLeft = Offset(x - 4f, groundY - 16f),
            size = Size(houseWidth + 8f, 16f)
        )
        // Foundation masonry mortar lines
        drawScope.drawLine(Color(0xFF37474F), Offset(x, groundY - 8f), Offset(x + houseWidth, groundY - 8f), 1.5f)

        // 2. Exterior Walls
        val wallColor = when (stage.stageNumber) {
            1 -> Color(0xFF6D4C41) // Rotten weathered timbers
            2 -> Color(0xFF8D6E63) // Repaired pine planks
            3 -> Color(0xFFFFB74D) // Cozy amber cedar siding
            4 -> Color(0xFFFFE082) // Painted warm cottage
            5 -> Color(0xFFECEFF1) // Polished country manor
            else -> Color(0xFFFFFDE7) // Stately gilded mansion marble
        }

        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(wallColor.copy(alpha = 0.95f), wallColor),
                startY = houseTop,
                endY = groundY - 16f
            ),
            topLeft = Offset(x + 10f, houseTop),
            size = Size(houseWidth - 20f, houseHeight - 16f)
        )

        // Wood Siding Horizontal Planks
        for (plankY in (houseTop.toInt() + 14)..(groundY.toInt() - 18) step 14) {
            drawScope.drawLine(
                color = Color.Black.copy(alpha = 0.15f),
                start = Offset(x + 10f, plankY.toFloat()),
                end = Offset(x + houseWidth - 10f, plankY.toFloat()),
                strokeWidth = 1.2f
            )
        }

        // Ruined cracks & splintered wood if Stage 1
        if (stage.stageNumber == 1) {
            drawScope.drawLine(Color(0xFF2E1C14), Offset(x + 40f, houseTop + 20f), Offset(x + 65f, houseTop + 65f), 3.5f)
            drawScope.drawLine(Color(0xFF2E1C14), Offset(x + 120f, houseTop + 10f), Offset(x + 145f, houseTop + 55f), 3f)
            drawScope.drawLine(Color(0xFF2E1C14), Offset(x + 180f, houseTop + 30f), Offset(x + 160f, houseTop + 70f), 2.5f)
        }

        // 3. Roof & Gables
        val hasRoofPatch = completedRepairs.contains("patch_roof")
        val roofColor = when {
            stage.stageNumber >= 7 -> Color(0xFFF59E0B) // Royal gilded gold
            stage.stageNumber >= 5 -> Color(0xFF1E293B) // Slate navy tiles
            hasRoofPatch -> Color(0xFFB91C1C) // Deep terracotta cedar shingles
            else -> Color(0xFF3E2723) // Broken mossy timber roof
        }

        val roofPeakY = houseTop - (if (isMansion) 65f else 50f)
        val roofPath = Path().apply {
            moveTo(x - 8f, houseTop)
            lineTo(x + houseWidth / 2f, roofPeakY)
            lineTo(x + houseWidth + 8f, houseTop)
            close()
        }
        drawScope.drawPath(roofPath, roofColor, style = Fill)
        // Roof fascia border trim
        drawScope.drawPath(roofPath, Color.White.copy(alpha = 0.4f), style = Stroke(3f))

        // Hole in roof if Stage 1
        if (stage.stageNumber == 1) {
            drawScope.drawCircle(Color(0xFF18181B), 18f, Offset(x + houseWidth * 0.38f, houseTop - 16f))
        }

        // 4. Stately Chimney with Animated Smoke
        if (completedRepairs.contains("stone_chimney")) {
            val chimX = x + houseWidth - 45f
            val chimY = houseTop - 65f
            // Brick chimney stack
            drawScope.drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFB91C1C), Color(0xFF7F1D1D)),
                    startX = chimX,
                    endX = chimX + 24f
                ),
                topLeft = Offset(chimX, chimY),
                size = Size(24f, 55f)
            )
            // Chimney cap
            drawScope.drawRect(
                color = Color(0xFF450A0A),
                topLeft = Offset(chimX - 3f, chimY - 6f),
                size = Size(30f, 6f)
            )
            // Procedural smoke puffs drifting into sky
            val smokeTime = (System.currentTimeMillis() % 4000L) / 4000f
            for (si in 0..2) {
                val sProg = (smokeTime + si * 0.33f) % 1.0f
                val sy = chimY - 10f - sProg * 50f
                val sx = chimX + 12f + sin(sProg * 4f) * 12f + sProg * 20f
                val sRadius = 7f + sProg * 14f
                val sAlpha = (1f - sProg) * 0.5f
                drawScope.drawCircle(Color.White.copy(alpha = sAlpha), sRadius, Offset(sx, sy))
            }
        }

        // 5. Grand Entrance Door
        val hasSturdyDoor = completedRepairs.contains("sturdy_door")
        val doorWidth = if (isMansion) 42f else 32f
        val doorHeight = if (isMansion) 62f else 54f
        val doorX = x + houseWidth / 2f - doorWidth / 2f
        val doorY = groundY - 16f - doorHeight

        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(
                if (hasSturdyDoor) {
                    listOf(Color(0xFF78350F), Color(0xFF451A03))
                } else {
                    listOf(Color(0xFF451A03), Color(0xFF1C1917))
                }
            ),
            topLeft = Offset(doorX, doorY),
            size = Size(doorWidth, doorHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Door frame trim
        drawScope.drawRoundRect(
            color = if (stage.stageNumber >= 5) Color.White else Color(0xFFD97706),
            topLeft = Offset(doorX, doorY),
            size = Size(doorWidth, doorHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            style = Stroke(2f)
        )
        // Brass door knob
        drawScope.drawCircle(Color(0xFFFFD54F), 3.5f, Offset(doorX + doorWidth - 8f, doorY + doorHeight * 0.55f))

        // 6. Glowing Windows with Interior Amber Light
        val hasCleanWindows = completedRepairs.contains("clean_windows")
        val windowGlowColor = if (hasCleanWindows) Color(0xFFFDE047) else Color(0xFF64748B)

        // Left window
        drawWindowWithGlow(drawScope, x + 35f, groundY - 80f, 32f, 32f, windowGlowColor, hasCleanWindows)
        // Right window
        drawWindowWithGlow(drawScope, x + houseWidth - 67f, groundY - 80f, 32f, 32f, windowGlowColor, hasCleanWindows)

        // Window flower boxes if unlocked
        if (completedRepairs.contains("flower_garden")) {
            drawFlowerBox(drawScope, x + 33f, groundY - 48f, 36f)
            drawFlowerBox(drawScope, x + houseWidth - 69f, groundY - 48f, 36f)
        }

        // 7. Upper Floor & Balcony (Stages 4+)
        if (isMultiStory) {
            val upperY = houseTop + 35f
            // Upper dormer windows
            drawWindowWithGlow(drawScope, x + 50f, upperY, 26f, 26f, windowGlowColor, hasCleanWindows)
            drawWindowWithGlow(drawScope, x + houseWidth - 76f, upperY, 26f, 26f, windowGlowColor, hasCleanWindows)

            // Balcony with decorative balustrade
            val balcX = x + houseWidth / 2f - 44f
            drawScope.drawRect(Color.White, Offset(balcX, upperY + 36f), Size(88f, 6f))
            for (bx in balcX.toInt()..(balcX + 88f).toInt() step 10) {
                drawScope.drawLine(Color.White, Offset(bx.toFloat(), upperY + 16f), Offset(bx.toFloat(), upperY + 36f), 2f)
            }
            drawScope.drawLine(Color.White, Offset(balcX, upperY + 16f), Offset(balcX + 88f, upperY + 16f), 3f)
        }

        // 8. Mansion Features: Classical Colonnade, Spire & Swimming Pool
        if (isMansion) {
            // Classical White Columns flanking front entrance
            drawScope.drawRect(Color.White, Offset(doorX - 16f, doorY - 8f), Size(8f, doorHeight + 8f))
            drawScope.drawRect(Color.White, Offset(doorX + doorWidth + 8f, doorY - 8f), Size(8f, doorHeight + 8f))

            // Rooftop Spire / Cupola Tower with Golden Rooster Weather Vane
            val towerX = x + houseWidth / 2f - 16f
            val towerY = roofPeakY - 32f
            drawScope.drawRect(Color(0xFFFFFBEB), Offset(towerX, towerY), Size(32f, 32f))
            // Golden Spire Pyramid
            val spirePath = Path().apply {
                moveTo(towerX - 4f, towerY)
                lineTo(towerX + 16f, towerY - 24f)
                lineTo(towerX + 36f, towerY)
                close()
            }
            drawScope.drawPath(spirePath, Color(0xFFFFD54F))
            // Golden Weather Vane Star
            drawScope.drawCircle(Color(0xFFFFD54F), 5f, Offset(towerX + 16f, towerY - 26f))
        }

        // Swimming Pool on left lawn if unlocked
        if (completedRepairs.contains("swimming_pool")) {
            val poolX = x - 75f
            val poolWidth = 65f
            drawScope.drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(poolX, groundY - 12f),
                size = Size(poolWidth, 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Sparkling turquoise water
            drawScope.drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))),
                topLeft = Offset(poolX + 3f, groundY - 10f),
                size = Size(poolWidth - 6f, 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            // Diving board
            drawScope.drawRect(Color(0xFF94A3B8), Offset(poolX - 6f, groundY - 16f), Size(18f, 3f))
        }

        // Marble Fountain on right lawn if unlocked
        if (completedRepairs.contains("marble_fountain")) {
            val fx = x + houseWidth + 30f
            // Tiered marble basin
            drawScope.drawCircle(Color(0xFFE2E8F0), 18f, Offset(fx, groundY - 16f))
            drawScope.drawCircle(Color(0xFF38BDF8), 12f, Offset(fx, groundY - 16f))
            drawScope.drawCircle(Color(0xFFFFFFFF), 6f, Offset(fx, groundY - 26f))
            // Water spray jets
            val animF = sin(System.currentTimeMillis() * 0.008f) * 4f
            drawScope.drawLine(Color(0xFFBAE6FD), Offset(fx, groundY - 26f), Offset(fx - 10f + animF, groundY - 40f), 2f)
            drawScope.drawLine(Color(0xFFBAE6FD), Offset(fx, groundY - 26f), Offset(fx + 10f - animF, groundY - 40f), 2f)
        }

        // White Picket Fence
        if (completedRepairs.contains("picket_fence")) {
            for (fx in (x - 25f).toInt()..(x + houseWidth + 15f).toInt() step 14) {
                // Pointed picket
                val px = fx.toFloat()
                val p = Path().apply {
                    moveTo(px, groundY - 16f)
                    lineTo(px + 2f, groundY - 26f)
                    lineTo(px + 4f, groundY - 16f)
                    close()
                }
                drawScope.drawPath(p, Color.White)
                drawScope.drawRect(Color.White, Offset(px, groundY - 16f), Size(4f, 16f))
            }
        }
    }

    private fun drawWindowWithGlow(
        drawScope: DrawScope,
        wx: Float,
        wy: Float,
        w: Float,
        h: Float,
        glowColor: Color,
        hasCleanWindows: Boolean
    ) {
        // Window glass with warm ambient glow
        drawScope.drawRoundRect(
            color = glowColor,
            topLeft = Offset(wx, wy),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Window frame
        drawScope.drawRoundRect(
            color = Color(0xFF451A03),
            topLeft = Offset(wx, wy),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            style = Stroke(2f)
        )
        // Window panes cross
        drawScope.drawLine(Color(0xFF451A03), Offset(wx + w / 2f, wy), Offset(wx + w / 2f, wy + h), 1.5f)
        drawScope.drawLine(Color(0xFF451A03), Offset(wx, wy + h / 2f), Offset(wx + w, wy + h / 2f), 1.5f)

        // Warm light beam shining onto the ground from clean windows
        if (hasCleanWindows) {
            drawScope.drawCircle(Color.White.copy(alpha = 0.5f), 3f, Offset(wx + 6f, wy + 6f))
        }
    }

    private fun drawFlowerBox(drawScope: DrawScope, bx: Float, by: Float, width: Float) {
        // Wooden planter box
        drawScope.drawRect(Color(0xFF78350F), Offset(bx, by), Size(width, 8f))
        // Blooming blossoms
        drawScope.drawCircle(Color(0xFFF43F5E), 3.5f, Offset(bx + 6f, by - 2f))
        drawScope.drawCircle(Color(0xFFFDE047), 3.5f, Offset(bx + 18f, by - 3f))
        drawScope.drawCircle(Color(0xFFA855F7), 3.5f, Offset(bx + 30f, by - 2f))
    }

    private fun drawMerchantStall(drawScope: DrawScope, x: Float, groundY: Float, engine: GameEngine) {
        // Polished oak trade counter
        drawScope.drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF78350F), Color(0xFF451A03))),
            topLeft = Offset(x, groundY - 34f),
            size = Size(82f, 34f)
        )
        // Scalloped Red/White Striped Awning
        val awningY = groundY - 65f
        for (stripe in 0..7) {
            val sx = x - 6f + stripe * 12f
            val sColor = if (stripe % 2 == 0) Color(0xFFDC2626) else Color(0xFFFFFBEB)
            drawScope.drawRect(sColor, Offset(sx, awningY), Size(12f, 18f))
            drawScope.drawCircle(sColor, 6f, Offset(sx + 6f, awningY + 18f)) // scalloped bottom
        }

        // Trade sign: "OLD PETE'S GEMS"
        drawScope.drawRect(Color(0xFFD97706), Offset(x + 2f, awningY - 14f), Size(78f, 14f))
        drawScope.drawRect(Color.White, Offset(x + 2f, awningY - 14f), Size(78f, 14f), style = Stroke(1f))

        // Crates of glittering gems & gold on the counter
        drawScope.drawCircle(Color(0xFFFFD700), 5.5f, Offset(x + 20f, groundY - 38f))
        drawScope.drawCircle(Color(0xFF00E5FF), 5f, Offset(x + 40f, groundY - 37f))
        drawScope.drawCircle(Color(0xFFE11D48), 4.5f, Offset(x + 60f, groundY - 38f))

        // Balance scales
        drawScope.drawLine(Color(0xFFD4AF37), Offset(x + 50f, groundY - 46f), Offset(x + 50f, groundY - 34f), 2f)
        drawScope.drawLine(Color(0xFFD4AF37), Offset(x + 42f, groundY - 46f), Offset(x + 58f, groundY - 46f), 2f)
    }

    private fun drawMineHeadframe(drawScope: DrawScope, x: Float, groundY: Float) {
        val hfHeight = 90f
        val hfY = groundY - hfHeight

        // Heavy timber A-frame gantry
        drawScope.drawLine(Color(0xFF451A03), Offset(x, groundY), Offset(x + 26f, hfY), strokeWidth = 6f)
        drawScope.drawLine(Color(0xFF451A03), Offset(x + 52f, groundY), Offset(x + 26f, hfY), strokeWidth = 6f)
        // Cross-braces
        drawScope.drawLine(Color(0xFF78350F), Offset(x + 10f, groundY - 45f), Offset(x + 42f, groundY - 45f), strokeWidth = 4f)
        drawScope.drawLine(Color(0xFF78350F), Offset(x + 10f, groundY - 45f), Offset(x + 42f, groundY), strokeWidth = 2.5f)
        drawScope.drawLine(Color(0xFF78350F), Offset(x + 42f, groundY - 45f), Offset(x + 10f, groundY), strokeWidth = 2.5f)

        // Spinning Pulley wheel at the apex
        val rotAngle = (System.currentTimeMillis() % 2000L) * 0.18f
        drawScope.drawCircle(Color(0xFF1E293B), 14f, Offset(x + 26f, hfY + 12f), style = Stroke(3.5f))
        // Steel hoist cable descending down into the shaft
        drawScope.drawLine(Color(0xFFE2E8F0), Offset(x + 26f, hfY + 26f), Offset(x + 26f, groundY + 40f), strokeWidth = 2f)

        // Hanging Miner's Brass Lantern (Casts a pulsing warm light pool)
        val lanternX = x + 44f
        val lanternY = groundY - 55f
        val pulse = (sin(System.currentTimeMillis() * 0.006f) * 0.2f + 0.8f)
        drawScope.drawCircle(Color(0x55FFEB3B), 38f * pulse, Offset(lanternX, lanternY))
        drawScope.drawCircle(Color(0xAAFFF59D), 14f * pulse, Offset(lanternX, lanternY))
        drawScope.drawRect(Color(0xFFD97706), Offset(lanternX - 4f, lanternY - 7f), Size(8f, 14f))
        drawScope.drawCircle(Color(0xFFFFD54F), 3f, Offset(lanternX, lanternY))
    }

    private fun drawBlock(
        drawScope: DrawScope,
        block: Block,
        bx: Float,
        by: Float,
        tileSize: Float,
        engine: GameEngine
    ) {
        if (block.type == BlockType.AIR) {
            if (block.hasChest) {
                // Golden treasure chest in cavern cavity
                drawScope.drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFD97706))),
                    topLeft = Offset(bx + 10f, by + 16f),
                    size = Size(28f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Iron band
                drawScope.drawRect(Color(0xFF1E293B), Offset(bx + 8f, by + 24f), Size(32f, 4f))
                drawScope.drawCircle(Color.White, 2.5f, Offset(bx + 24f, by + 26f))
            }
            return
        }

        if (block.type == BlockType.LADDER) {
            // High-definition wooden ladder
            // Left & Right oak rails with grain
            drawScope.drawRect(Color(0xFF451A03), Offset(bx + 10f, by), Size(6f, tileSize))
            drawScope.drawRect(Color(0xFF78350F), Offset(bx + 12f, by), Size(2f, tileSize)) // rail highlight
            drawScope.drawRect(Color(0xFF451A03), Offset(bx + tileSize - 16f, by), Size(6f, tileSize))
            drawScope.drawRect(Color(0xFF78350F), Offset(bx + tileSize - 14f, by), Size(2f, tileSize))

            // Rungs with 3D drop shadow
            for (step in 6..tileSize.toInt() - 6 step 12) {
                drawScope.drawRect(Color(0xFF1C1917), Offset(bx + 12f, by + step + 3f), Size(tileSize - 24f, 2f)) // shadow
                drawScope.drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFD7CCC8), Color(0xFFA1887F))),
                    topLeft = Offset(bx + 12f, by + step),
                    size = Size(tileSize - 24f, 4.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
                )
            }
            return
        }

        val isTarget = block.x == engine.targetBlockX && block.y == engine.targetBlockY

        // 1. Base Block with 3D Bevel Lighting (Giving blocks tactile voxel volume)
        drawScope.drawRect(
            color = block.type.primaryColor,
            topLeft = Offset(bx, by),
            size = Size(tileSize, tileSize)
        )

        // Top and Left Highlights (Brightened light edge)
        val highlightColor = block.type.secondaryColor.copy(alpha = 0.55f)
        drawScope.drawLine(highlightColor, Offset(bx, by), Offset(bx + tileSize, by), strokeWidth = 2.5f)
        drawScope.drawLine(highlightColor, Offset(bx, by), Offset(bx, by + tileSize), strokeWidth = 2.5f)

        // Bottom and Right Shadows (Darkened occlusion edge)
        val shadowColor = Color.Black.copy(alpha = 0.40f)
        drawScope.drawLine(shadowColor, Offset(bx, by + tileSize), Offset(bx + tileSize, by + tileSize), strokeWidth = 2.5f)
        drawScope.drawLine(shadowColor, Offset(bx + tileSize, by), Offset(bx + tileSize, by + tileSize), strokeWidth = 2.5f)

        // Biome Specific Textures
        when (block.type) {
            BlockType.SOFT_DIRT -> {
                // Pebble flecks and root strands
                drawScope.drawCircle(Color(0xFF3E2723), 2f, Offset(bx + 14f, by + 16f))
                drawScope.drawCircle(Color(0xFF3E2723), 2.5f, Offset(bx + 32f, by + 28f))
                drawScope.drawLine(Color(0xFF795548), Offset(bx + 8f, by + 6f), Offset(bx + 16f, by + 18f), 1.2f)
            }
            BlockType.STONY_DIRT -> {
                // Chiseled strata lines
                drawScope.drawLine(Color.White.copy(alpha = 0.15f), Offset(bx + 6f, by + 18f), Offset(bx + tileSize - 8f, by + 18f), 1.2f)
                drawScope.drawLine(Color.Black.copy(alpha = 0.25f), Offset(bx + 10f, by + 32f), Offset(bx + tileSize - 6f, by + 32f), 1.2f)
            }
            BlockType.CAVERN_GRANITE -> {
                // Sparkling quartz flecks
                drawScope.drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.6f), 1.5f, Offset(bx + 12f, by + 14f))
                drawScope.drawCircle(Color(0xFFE2E8F0).copy(alpha = 0.6f), 1.5f, Offset(bx + 34f, by + 26f))
            }
            BlockType.CRYSTAL_STRATA -> {
                // Glowing crystalline veins
                val pulse = (sin(System.currentTimeMillis() * 0.005f + bx) * 0.2f + 0.8f)
                drawScope.drawLine(Color(0xFF00E5FF).copy(alpha = 0.6f * pulse), Offset(bx + 6f, by + 10f), Offset(bx + 24f, by + 24f), 2f)
                drawScope.drawLine(Color(0xFFE040FB).copy(alpha = 0.6f * pulse), Offset(bx + 24f, by + 24f), Offset(bx + 42f, by + 36f), 2f)
            }
            BlockType.MAGMA_BASALT -> {
                // Glowing orange lava fissure
                val pulse = (sin(System.currentTimeMillis() * 0.006f + by) * 0.25f + 0.75f)
                drawScope.drawLine(Color(0xFFFF3D00).copy(alpha = 0.75f * pulse), Offset(bx + 8f, by + 8f), Offset(bx + 26f, by + 22f), 2.5f)
                drawScope.drawLine(Color(0xFFFFD600).copy(alpha = 0.9f * pulse), Offset(bx + 26f, by + 22f), Offset(bx + 40f, by + 40f), 1.8f)
            }
            BlockType.CELESTIAL_STONE -> {
                // Embedded cosmic stardust
                drawScope.drawCircle(Color.White, 1.5f, Offset(bx + 16f, by + 14f))
                drawScope.drawCircle(Color(0xFF80D8FF), 2f, Offset(bx + 34f, by + 30f))
            }
            else -> {}
        }

        // 2. GEMS PROTRUDING OUT OF ROCK!
        block.gem?.let { gem ->
            drawProtrudingGem(drawScope, gem, bx, by, tileSize, block.gemProtrusionSide)
        }

        // 3. Fossil or Chest embedded in rock
        if (block.hasFossil) {
            // Spiral ammonite shell fossil
            drawScope.drawCircle(Color(0xFFE2E8F0), 7f, Offset(bx + tileSize * 0.5f, by + tileSize * 0.5f))
            drawScope.drawCircle(Color(0xFF78350F), 4f, Offset(bx + tileSize * 0.5f, by + tileSize * 0.5f), style = Stroke(1.5f))
        }
        if (block.hasChest) {
            drawScope.drawRect(Color(0xFFFFD54F), Offset(bx + 14f, by + 16f), Size(20f, 16f))
            drawScope.drawCircle(Color(0xFF1E293B), 2.5f, Offset(bx + 24f, by + 24f))
        }

        // 4. RADAR BEACON SONAR PING (Seismic crystal detection)
        val radarLvl = engine.saveManager.getBuildingUpgradeLevel("radar_beacon")
        val gem = block.gem
        if (radarLvl > 0 && gem != null) {
            val playerTileX = (engine.playerX / tileSize).toInt()
            val playerTileY = (engine.playerY / tileSize).toInt()
            val dist = kotlin.math.hypot((block.x - playerTileX).toFloat(), (block.y - playerTileY).toFloat())
            val maxRadarDist = (radarLvl + 2).toFloat()
            if (dist <= maxRadarDist) {
                val pingTime = (System.currentTimeMillis() % 1400L) / 1400f
                val pingRadius = pingTime * (tileSize * 0.75f)
                val pingAlpha = (1f - pingTime) * 0.85f
                drawScope.drawCircle(
                    color = gem.primaryColor.copy(alpha = pingAlpha),
                    radius = pingRadius,
                    center = Offset(bx + tileSize / 2f, by + tileSize / 2f),
                    style = Stroke(width = 2.2f)
                )
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = pingAlpha),
                    radius = 3.5f,
                    center = Offset(bx + tileSize / 2f, by + tileSize / 2f)
                )
            }
        }

        // 4. Damage crack stage overlay
        val cracks = block.crackStage
        if (cracks > 0) {
            drawBlockCracks(drawScope, bx, by, tileSize, cracks)
        }

        // 5. Target selection pulse border
        if (isTarget) {
            val pulse = (sin(System.currentTimeMillis() * 0.012f) * 0.2f + 0.8f)
            drawScope.drawRect(
                color = Color(0xFFFFEB3B).copy(alpha = pulse),
                topLeft = Offset(bx + 1f, by + 1f),
                size = Size(tileSize - 2f, tileSize - 2f),
                style = Stroke(3f)
            )
        }
    }

    private fun drawProtrudingGem(
        drawScope: DrawScope,
        gem: Gem,
        bx: Float,
        by: Float,
        tileSize: Float,
        protrusionSide: Int
    ) {
        val color1 = gem.primaryColor
        val color2 = gem.secondaryColor
        val isRare = gem.rarity >= 15
        val isLegendary = gem.rarity >= 40

        val cx = bx + tileSize / 2f
        val cy = by + tileSize / 2f

        // Glowing colored radial aura illuminating the rock face
        val auraRadius = if (isLegendary) tileSize * 0.75f else if (isRare) tileSize * 0.55f else tileSize * 0.4f
        val auraAlpha = if (isLegendary) 0.55f else if (isRare) 0.38f else 0.22f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                listOf(color1.copy(alpha = auraAlpha), color2.copy(alpha = auraAlpha * 0.5f), Color.Transparent),
                center = Offset(cx, cy),
                radius = auraRadius
            ),
            radius = auraRadius,
            center = Offset(cx, cy)
        )

        // Faceted crystal clusters jutting out
        when (protrusionSide) {
            0 -> {
                // Juts out of top surface
                val p1 = Path().apply {
                    moveTo(bx + 12f, by + 8f)
                    lineTo(bx + 22f, by - 14f) // Shard points up out of block!
                    lineTo(bx + 32f, by + 8f)
                    close()
                }
                drawScope.drawPath(p1, color1)
                val p2 = Path().apply {
                    moveTo(bx + 26f, by + 6f)
                    lineTo(bx + 38f, by - 10f)
                    lineTo(bx + 44f, by + 6f)
                    close()
                }
                drawScope.drawPath(p2, color2)
                drawScope.drawCircle(Color.White, 2.5f, Offset(bx + 22f, by - 12f)) // specular gleam
            }
            1 -> {
                // Juts out of left wall
                val p = Path().apply {
                    moveTo(bx + 8f, by + 12f)
                    lineTo(bx - 14f, by + 24f) // Points left into cavern air!
                    lineTo(bx + 8f, by + 36f)
                    close()
                }
                drawScope.drawPath(p, color1)
                drawScope.drawCircle(Color.White, 2.5f, Offset(bx - 12f, by + 24f))
            }
            2 -> {
                // Juts out of right wall
                val p = Path().apply {
                    moveTo(bx + tileSize - 8f, by + 12f)
                    lineTo(bx + tileSize + 14f, by + 24f) // Points right into cavern air!
                    lineTo(bx + tileSize - 8f, by + 36f)
                    close()
                }
                drawScope.drawPath(p, color1)
                drawScope.drawCircle(Color.White, 2.5f, Offset(bx + tileSize + 12f, by + 24f))
            }
            else -> {
                // Embedded faceted jewel cluster in center of block
                val p = Path().apply {
                    moveTo(cx, cy - 14f)
                    lineTo(cx + 12f, cy)
                    lineTo(cx, cy + 14f)
                    lineTo(cx - 12f, cy)
                    close()
                }
                drawScope.drawPath(p, color1)
                // Inner facet reflection
                val pInner = Path().apply {
                    moveTo(cx, cy - 8f)
                    lineTo(cx + 7f, cy)
                    lineTo(cx, cy + 8f)
                    lineTo(cx - 7f, cy)
                    close()
                }
                drawScope.drawPath(pInner, color2)
                drawScope.drawCircle(Color.White, 3f, Offset(cx - 3f, cy - 4f)) // bright white shine
            }
        }
    }

    private fun drawBlockCracks(drawScope: DrawScope, bx: Float, by: Float, size: Float, stage: Int) {
        val dark = Color(0xFF101010).copy(alpha = 0.85f)
        val stroke = 2.5f

        // Progressive spiderweb crack lines
        drawScope.drawLine(dark, Offset(bx + size * 0.5f, by + 2f), Offset(bx + size * 0.45f, by + size * 0.45f), stroke)
        if (stage >= 2) {
            drawScope.drawLine(dark, Offset(bx + size * 0.45f, by + size * 0.45f), Offset(bx + 6f, by + size * 0.7f), stroke)
        }
        if (stage >= 3) {
            drawScope.drawLine(dark, Offset(bx + size * 0.45f, by + size * 0.45f), Offset(bx + size - 6f, by + size * 0.6f), stroke)
        }
        if (stage >= 4) {
            drawScope.drawLine(dark, Offset(bx + size * 0.45f, by + size * 0.45f), Offset(bx + size * 0.55f, by + size - 4f), stroke)
            drawScope.drawLine(dark, Offset(bx + size * 0.45f, by + size * 0.45f), Offset(bx + 4f, by + 12f), stroke)
        }
    }

    private fun drawDrop(drawScope: DrawScope, drop: com.example.game.engine.CollectibleDrop, dx: Float, dy: Float) {
        val hoverOffset = sin(drop.pulseTimer * 6f) * 4f
        val y = dy + hoverOffset

        if (drop.gem != null) {
            val gem = drop.gem
            val isRare = gem.rarity >= 15
            val isLegendary = gem.rarity >= 40

            // Glowing halo
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    listOf(gem.primaryColor.copy(alpha = if (isLegendary) 0.65f else 0.4f), Color.Transparent),
                    center = Offset(dx, y),
                    radius = if (isLegendary) 26f else 16f
                ),
                radius = if (isLegendary) 26f else 16f,
                center = Offset(dx, y)
            )

            // Diamond gem silhouette
            val size = 11f
            val p = Path().apply {
                moveTo(dx, y - size)
                lineTo(dx + size, y)
                lineTo(dx, y + size)
                lineTo(dx - size, y)
                close()
            }
            drawScope.drawPath(p, gem.primaryColor)
            // Facet reflection
            drawScope.drawCircle(gem.secondaryColor, 5f, Offset(dx + 2f, y + 2f))
            drawScope.drawCircle(Color.White, 3f, Offset(dx - 3f, y - 3f))
        } else if (drop.isCoin) {
            drawScope.drawCircle(Color(0xFFFFD54F), 10f, Offset(dx, y))
            drawScope.drawCircle(Color(0xFFFFA000), 8f, Offset(dx, y), style = Stroke(2f))
            drawScope.drawCircle(Color.White, 2f, Offset(dx - 3f, y - 3f))
        } else if (drop.isFossil) {
            drawScope.drawCircle(Color(0xFFD7CCC8), 10f, Offset(dx, y))
            drawScope.drawCircle(Color(0xFF5D4037), 6f, Offset(dx, y), style = Stroke(2f))
        }
    }

    private fun drawPlayer(drawScope: DrawScope, engine: GameEngine, px: Float, py: Float) {
        val w = engine.playerWidth
        val h = engine.playerHeight
        val facingRight = engine.facingRight

        // 1. Dynamic Flashlight Beam (Scales with Headlamp Upgrade tier!)
        val headlamp = engine.headlamp
        val beamDir = if (facingRight) 1f else -1f
        val headX = px + w / 2f
        val headY = py + 12f
        val beamLength = headlamp.lightRadius * 1.55f
        val beamSpread = headlamp.lightRadius * 0.42f
        val beamPath = Path().apply {
            moveTo(headX, headY)
            lineTo(headX + beamDir * beamLength, headY - beamSpread)
            lineTo(headX + beamDir * beamLength, headY + beamSpread * 1.15f)
            close()
        }
        val alphaBase = (headlamp.beamBrightness * 0.65f).coerceIn(0.25f, 0.95f)
        drawScope.drawPath(
            beamPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFFF9C4).copy(alpha = alphaBase),
                    Color(0xFFFFF59D).copy(alpha = alphaBase * 0.45f),
                    Color.Transparent
                ),
                startX = headX,
                endX = headX + beamDir * beamLength
            )
        )
        // Ambient illumination bubble around the miner
        val bubbleRadius = headlamp.lightRadius * 0.65f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFDE7).copy(alpha = alphaBase * 0.35f), Color.Transparent),
                center = Offset(headX, headY),
                radius = bubbleRadius
            ),
            radius = bubbleRadius,
            center = Offset(headX, headY)
        )

        // 2. Miner Backpack (Visibly displays gemstones sticking out!)
        val bpX = if (facingRight) px - 6f else px + w - 4f
        val bpY = py + 16f
        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF78350F), Color(0xFF451A03))),
            topLeft = Offset(bpX, bpY),
            size = Size(10f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        // If backpack has gems, render colorful gem facets peeking out the top!
        if (engine.backpack.isNotEmpty()) {
            val topGem = engine.backpack.last()
            drawScope.drawCircle(topGem.primaryColor, 4.5f, Offset(bpX + 5f, bpY - 2f))
            drawScope.drawCircle(Color.White, 1.5f, Offset(bpX + 4f, bpY - 3f))
        }

        // 3. Miner Body: Feet & Boots (Reflects Boots Upgrade Tier!)
        val bootsColor = when (engine.saveManager.bootsLevel) {
            in 1..3 -> Color(0xFF422006) // Sturdy brown work leather
            in 4..7 -> Color(0xFF475569) // Pneumatic reinforced steel
            in 8..11 -> Color(0xFF0284C7) // Kinetic cyber cyan
            in 12..15 -> Color(0xFF9333EA) // Anti-grav cosmic violet
            in 16..19 -> Color(0xFFF59E0B) // Solar rocket gold
            else -> Color(0xFF00F5D4) // Transcendent light-speed turquoise
        }
        val walkCycle = if (abs(engine.playerVx) > 10f) sin(System.currentTimeMillis() * 0.018f) * 4.5f else 0f
        drawScope.drawRoundRect(
            color = bootsColor,
            topLeft = Offset(px + 4f + walkCycle, py + h - 6f),
            size = Size(9f, 6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawScope.drawRoundRect(
            color = bootsColor,
            topLeft = Offset(px + w - 13f - walkCycle, py + h - 6f),
            size = Size(9f, 6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )

        // Blue Denim Dungarees / Overalls
        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF1E40AF))),
            topLeft = Offset(px + 4f, py + 18f),
            size = Size(w - 8f, h - 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Brass suspender buckles
        drawScope.drawCircle(Color(0xFFFFD54F), 2f, Offset(px + 7f, py + 20f))
        drawScope.drawCircle(Color(0xFFFFD54F), 2f, Offset(px + w - 7f, py + 20f))

        // Red Flannel Shirt Underneath
        drawScope.drawRect(
            color = Color(0xFFDC2626),
            topLeft = Offset(px + 6f, py + 14f),
            size = Size(w - 12f, 6f)
        )

        // Face & Skin
        drawScope.drawCircle(
            color = Color(0xFFFFCC80),
            radius = 9.5f,
            center = Offset(headX, headY)
        )
        // Eye looking in facing direction
        val eyeX = if (facingRight) headX + 4f else headX - 4f
        drawScope.drawCircle(Color(0xFF1E293B), 2f, Offset(eyeX, headY - 1f))
        drawScope.drawCircle(Color.White, 0.8f, Offset(eyeX - 0.5f, headY - 1.5f))

        // Safety Hardhat
        drawScope.drawArc(
            brush = Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFEAB308))),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(headX - 11f, headY - 12f),
            size = Size(22f, 15f)
        )
        // Cap visor
        val visorX = if (facingRight) headX - 2f else headX - 14f
        drawScope.drawRoundRect(
            color = Color(0xFFCA8A04),
            topLeft = Offset(visorX, headY - 2f),
            size = Size(16f, 3.5f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
        )

        // High-power Headlamp Lens
        val lampX = if (facingRight) headX + 8f else headX - 10f
        drawScope.drawCircle(Color(0xFFFFFBEB), 4f, Offset(lampX, headY - 6f))
        drawScope.drawCircle(Color(0xFFEAB308), 5f, Offset(lampX, headY - 6f), style = Stroke(1.5f))

        // 4. Shovel in Hand with Dynamic Swing Arc
        val shovelAngle = if (engine.isDigging) {
            val progress = engine.digSwingProgress
            if (facingRight) -30f + progress * 95f else 30f - progress * 95f
        } else {
            if (facingRight) 15f else -15f
        }

        val handX = if (facingRight) px + w - 2f else px + 2f
        val handY = py + 24f

        drawScope.rotate(shovelAngle, pivot = Offset(handX, handY)) {
            // Shovel handle
            drawLine(
                color = Color(0xFF78350F),
                start = Offset(handX, handY - 16f),
                end = Offset(handX, handY + 18f),
                strokeWidth = 3.5f
            )
            // Shovel blade
            val bladeColor = when (engine.saveManager.shovelLevel) {
                1 -> Color(0xFF94A3B8)
                2 -> Color(0xFFCBD5E1)
                3, 4 -> Color(0xFFF1F5F9)
                5, 6 -> Color(0xFFFFD54F) // Gold
                7, 8 -> Color(0xFF00E5FF) // Diamond/Plasma
                in 9..12 -> Color(0xFF8B5CF6) // Antimatter Purple
                in 13..16 -> Color(0xFFEC4899) // Magma / Singularity Pink
                in 17..20 -> Color(0xFF06B6D4) // Pulsar Cyan
                in 21..25 -> Color(0xFFF43F5E) // Quasar Ruby
                else -> Color(0xFFFFD700) // Transcendent Creation Gold
            }
            drawRoundRect(
                color = bladeColor,
                topLeft = Offset(handX - 5f, handY + 16f),
                size = Size(10f, 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }

    private fun drawParticles(drawScope: DrawScope, engine: GameEngine, offsetX: Float, offsetY: Float) {
        val ps = engine.particleSystem

        // 1. Sparks, Dirt & Chimney Smoke
        for (p in ps.particles) {
            val px = p.x + offsetX
            val py = p.y + offsetY
            val alpha = (p.life).coerceIn(0f, 1f)

            if (p.isSmoke) {
                drawScope.drawCircle(p.color.copy(alpha = alpha * 0.45f), p.size * (2f - alpha), Offset(px, py))
            } else if (p.isSparkle) {
                drawScope.drawCircle(p.color.copy(alpha = alpha), p.size, Offset(px, py))
                drawScope.drawCircle(Color.White.copy(alpha = alpha), p.size * 0.5f, Offset(px, py))
            } else {
                drawScope.drawRect(p.color.copy(alpha = alpha), Offset(px, py), Size(p.size, p.size))
            }
        }

        // 2. Floating Text using Native Canvas with Drop Shadow
        val nativeCanvas = drawScope.drawContext.canvas.nativeCanvas
        val paint = android.graphics.Paint().apply {
            textSize = 30f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        for (ft in ps.floatingTexts) {
            val fx = ft.x + offsetX
            val fy = ft.y + offsetY
            val alpha = (ft.life * 255f).toInt().coerceIn(0, 255)

            // Outline shadow
            paint.color = android.graphics.Color.argb(alpha, 0, 0, 0)
            paint.strokeWidth = 4.5f
            paint.style = android.graphics.Paint.Style.STROKE
            nativeCanvas.drawText(ft.text, fx, fy, paint)

            // Fill
            val c = ft.color
            paint.color = android.graphics.Color.argb(
                alpha,
                (c.red * 255).toInt(),
                (c.green * 255).toInt(),
                (c.blue * 255).toInt()
            )
            paint.style = android.graphics.Paint.Style.FILL
            nativeCanvas.drawText(ft.text, fx, fy, paint)
        }
    }
}
