package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.GameAudioEngine
import com.example.game.data.Gem
import com.example.game.data.HouseRegistry
import com.example.game.data.SaveManager
import com.example.game.data.UpgradeRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GameEngine(
    val saveManager: SaveManager,
    val audioEngine: GameAudioEngine
) {
    // Player physical bounding box
    var playerX: Float = 11f * MiningWorld.TILE_SIZE // start at top of shaft
    var playerY: Float = 0f
    var playerVx: Float = 0f
    var playerVy: Float = 0f
    val playerWidth: Float = 28f
    val playerHeight: Float = 40f
    var facingRight: Boolean = false
    var isGrounded: Boolean = false
    var isClimbingWall: Boolean = false
    var isOnLadder: Boolean = false
    var isDigging: Boolean = false
    var digSwingProgress: Float = 0f // 0f to 1f

    // Input states
    var inputLeft: Boolean = false
    var inputRight: Boolean = false
    var inputUp: Boolean = false
    var inputDown: Boolean = false
    var inputJumpHold: Boolean = false
    var inputDigPress: Boolean = false
    var inputCollectPress: Boolean = false

    // World & Systems
    val particleSystem = ParticleSystem()
    val miningWorld = MiningWorld { gem ->
        onGemDiscovered(gem)
    }

    // Backpack inventory
    val backpack = mutableListOf<Gem>()
    val backpackCapacity: Int
        get() = UpgradeRegistry.getBackpackForLevel(saveManager.backpackLevel).capacity

    // Shovel properties
    val shovelPower: Float
        get() {
            val base = UpgradeRegistry.getShovelForLevel(saveManager.shovelLevel).power
            val grindstoneLvl = saveManager.getBuildingUpgradeLevel("tool_grindstone")
            return base * (1f + grindstoneLvl * 0.12f)
        }

    val shovelSpeedMultiplier: Float
        get() = UpgradeRegistry.getShovelForLevel(saveManager.shovelLevel).speedMultiplier

    // Boots & Agility properties
    val boots: com.example.game.data.BootsUpgrade
        get() = UpgradeRegistry.getBootsForLevel(saveManager.bootsLevel)

    // Headlamp & Sensor Lamp properties
    val headlamp: com.example.game.data.HeadlampUpgrade
        get() = UpgradeRegistry.getHeadlampForLevel(saveManager.headlampLevel)

    // Automation & Technology timers
    private var passiveRevenueTimer = 0f
    private var droneTimer = 0f
    private var museumTimer = 0f

    // Camera
    var cameraX: Float = playerX
    var cameraY: Float = playerY
    var screenShakeAmount: Float = 0f

    // UI & Dialog states exposed via Flow
    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    private val _backpackFullWarning = MutableStateFlow(false)
    val backpackFullWarning: StateFlow<Boolean> = _backpackFullWarning.asStateFlow()

    private val _canCollect = MutableStateFlow(false)
    val canCollect: StateFlow<Boolean> = _canCollect.asStateFlow()

    private val _showSellStand = MutableStateFlow(false)
    val showSellStand: StateFlow<Boolean> = _showSellStand.asStateFlow()

    private val _tutorialStep = MutableStateFlow(if (saveManager.tutorialCompleted) -1 else 1)
    val tutorialStep: StateFlow<Int> = _tutorialStep.asStateFlow()

    // Milestone announcement
    private val _milestoneAnnouncement = MutableStateFlow<String?>(null)
    val milestoneAnnouncement: StateFlow<String?> = _milestoneAnnouncement.asStateFlow()

    // Target block currently highlighted for digging
    var targetBlockX: Int = -1
    var targetBlockY: Int = -1

    private fun onGemDiscovered(gem: Gem) {
        saveManager.recordGemDiscovery(gem.id)
        val isRare = gem.rarity >= 15
        audioEngine.playGemCollect(isRare)
        particleSystem.addFloatingText(playerX, playerY - 30f, "Discovered: ${gem.name}!", gem.primaryColor)
        if (gem.rarity >= 30) {
            _milestoneAnnouncement.value = "🌟 LEGENDARY GEM REVEALED: ${gem.name}! (Rarity ${gem.rarity})"
        }
    }

    fun dismissMilestone() {
        _milestoneAnnouncement.value = null
    }

    fun dismissBackpackWarning() {
        _backpackFullWarning.value = false
    }

    fun update(dt: Float) {
        val clampedDt = min(dt, 0.05f)

        // 1. Estate Passive Automation
        // Rental Cabin: visiting geologists & tourists pay rent
        val rentalLvl = saveManager.getBuildingUpgradeLevel("rental_cabin")
        if (rentalLvl > 0) {
            passiveRevenueTimer += clampedDt
            if (passiveRevenueTimer >= 10f) {
                passiveRevenueTimer = 0f
                val income = rentalLvl * 30L
                saveManager.money += income
                saveManager.lifetimeEarnings += income
                audioEngine.playButtonClick()
                particleSystem.addFloatingText(playerX, playerY - 20f, "+$$income (Rent)", Color(0xFF81C784))
            }
        }

        // Prospector Drone: autonomous drone gathers minerals
        val droneLvl = saveManager.getBuildingUpgradeLevel("prospector_drone")
        if (droneLvl > 0) {
            droneTimer += clampedDt
            if (droneTimer >= 15f) {
                droneTimer = 0f
                val droneHaul = droneLvl * 45L
                saveManager.money += droneHaul
                saveManager.lifetimeEarnings += droneHaul
                audioEngine.playButtonClick()
                particleSystem.addFloatingText(playerX, playerY - 32f, "+$$droneHaul (Drone Haul)", Color(0xFF4DD0E1))
            }
        }

        // Fossil Museum: visitor admission ticket fees
        val museumLvl = saveManager.getBuildingUpgradeLevel("fossil_museum")
        if (museumLvl > 0) {
            museumTimer += clampedDt
            if (museumTimer >= 20f) {
                museumTimer = 0f
                val ticketFees = museumLvl * 20L
                saveManager.money += ticketFees
                saveManager.lifetimeEarnings += ticketFees
                particleSystem.addFloatingText(playerX, playerY - 26f, "+$$ticketFees (Museum Tickets)", Color(0xFFCE93D8))
            }
        }

        // 2. Ladder check
        val currentTileX = ((playerX + playerWidth / 2f) / MiningWorld.TILE_SIZE).toInt()
        val currentTileY = ((playerY + playerHeight / 2f) / MiningWorld.TILE_SIZE).toInt()
        val currentBlock = miningWorld.getBlock(currentTileX, currentTileY)
        isOnLadder = currentBlock.type == BlockType.LADDER

        // 3. Wall climb check: is player touching a solid dirt/rock wall to left or right?
        val leftTileX = ((playerX - 4f) / MiningWorld.TILE_SIZE).toInt()
        val rightTileX = ((playerX + playerWidth + 4f) / MiningWorld.TILE_SIZE).toInt()
        val midTileY = ((playerY + playerHeight / 2f) / MiningWorld.TILE_SIZE).toInt()
        val leftBlock = miningWorld.getBlock(leftTileX, midTileY)
        val rightBlock = miningWorld.getBlock(rightTileX, midTileY)
        val touchingClimbableWall = (leftBlock.type.isSolid && !leftBlock.isDestroyed) ||
                (rightBlock.type.isSolid && !rightBlock.isDestroyed)

        // 4. Horizontal movement (scaled by boots agility upgrade!)
        val moveSpeed = 160f * boots.moveSpeedMultiplier
        if (inputLeft) {
            playerVx = -moveSpeed
            facingRight = false
            if (isGrounded && Math.random() < 0.08) audioEngine.playFootstep()
            advanceTutorial(4)
        } else if (inputRight) {
            playerVx = moveSpeed
            facingRight = true
            if (isGrounded && Math.random() < 0.08) audioEngine.playFootstep()
            advanceTutorial(4)
        } else {
            playerVx *= 0.7f // friction
            if (abs(playerVx) < 5f) playerVx = 0f
        }

        // 5. Jump & Wall / Ladder Climbing (scaled by boots climbing speed!)
        val climbSpeed = 135f * boots.climbSpeedMultiplier
        if (isOnLadder) {
            // Ladder vertical movement: Jump button OR Up ascends, Down descends
            if (inputUp || inputJumpHold) {
                playerVy = -climbSpeed
                if (Math.random() < 0.1) audioEngine.playClimb()
            } else if (inputDown) {
                playerVy = climbSpeed
                if (Math.random() < 0.1) audioEngine.playClimb()
            } else {
                playerVy = 0f
            }
            isClimbingWall = false
        } else if (touchingClimbableWall && inputJumpHold) {
            // SPECIAL WALL CLIMB: Player holds jump while touching wall -> climbs upward!
            isClimbingWall = true
            playerVy = -130f * boots.climbSpeedMultiplier // climb speed
            if (Math.random() < 0.07) audioEngine.playClimb()
            particleSystem.emitDirtChunks(playerX + (if (facingRight) playerWidth else 0f), playerY + playerHeight * 0.6f, Color(0xFF8D6E63), 1)
            advanceTutorial(9)
        } else {
            isClimbingWall = false
            // Normal gravity
            val gravity = 550f
            playerVy += gravity * clampedDt
            if (playerVy > 450f) playerVy = 450f // Terminal velocity
        }

        // 6. Integrate position with collision
        moveAndCollide(playerVx * clampedDt, playerVy * clampedDt)

        // 7. Check depth record
        val currentDepth = max(0, (playerY / MiningWorld.TILE_SIZE).toInt())
        if (currentDepth > saveManager.deepestDepth) {
            saveManager.deepestDepth = currentDepth
            checkDepthMilestones(currentDepth)
        }

        // 8. Determine target block for digging
        updateTargetBlock()

        // 9. Digging action
        if (inputDigPress || isDigging) {
            handleDigging(clampedDt)
        }

        // 10. Collectible drops check
        miningWorld.updateDrops(clampedDt)
        val nearbyDrop = findNearbyDrop()
        _canCollect.value = nearbyDrop != null
        if (inputCollectPress && nearbyDrop != null) {
            collectDrop(nearbyDrop)
            inputCollectPress = false
        }

        // 11. Surface Selling stand detection
        val nearSellStall = (playerY <= MiningWorld.TILE_SIZE * 0.8f) && (playerX in (7f * MiningWorld.TILE_SIZE)..(11f * MiningWorld.TILE_SIZE))
        _showSellStand.value = nearSellStall && backpack.isNotEmpty()

        // 12. Particles, chimney smoke & camera shake
        val completedRepairs = saveManager.getCompletedRepairs()
        if (completedRepairs.contains("stone_chimney") && Math.random() < 0.08) {
            val chimX = 1f * MiningWorld.TILE_SIZE + 205f
            val chimY = -65f
            particleSystem.emitChimneySmoke(chimX, chimY)
        }
        particleSystem.update(clampedDt)
        if (screenShakeAmount > 0f) {
            screenShakeAmount -= clampedDt * 30f
            if (screenShakeAmount < 0f) screenShakeAmount = 0f
        }

        // Camera follow
        val targetCamX = playerX + playerWidth / 2f
        val targetCamY = playerY + playerHeight / 2f
        cameraX += (targetCamX - cameraX) * 0.12f
        cameraY += (targetCamY - cameraY) * 0.12f
    }

    private fun updateTargetBlock() {
        val centerTileX = ((playerX + playerWidth / 2f) / MiningWorld.TILE_SIZE).toInt()
        val centerTileY = ((playerY + playerHeight / 2f) / MiningWorld.TILE_SIZE).toInt()

        // Priority 1: If Down is held, target block directly underneath
        if (inputDown) {
            val belowY = ((playerY + playerHeight + 6f) / MiningWorld.TILE_SIZE).toInt()
            val b = miningWorld.getBlock(centerTileX, belowY)
            if (b.type.isDiggable && !b.isDestroyed) {
                targetBlockX = centerTileX
                targetBlockY = belowY
                return
            }
        }

        // Priority 2: Block in facing direction
        val frontTileX = if (facingRight) {
            ((playerX + playerWidth + 8f) / MiningWorld.TILE_SIZE).toInt()
        } else {
            ((playerX - 8f) / MiningWorld.TILE_SIZE).toInt()
        }
        val frontBlock = miningWorld.getBlock(frontTileX, centerTileY)
        if (frontBlock.type.isDiggable && !frontBlock.isDestroyed) {
            targetBlockX = frontTileX
            targetBlockY = centerTileY
            return
        }

        // Priority 3: Diagonal down-front
        val diagBelowY = ((playerY + playerHeight + 4f) / MiningWorld.TILE_SIZE).toInt()
        val diagBlock = miningWorld.getBlock(frontTileX, diagBelowY)
        if (diagBlock.type.isDiggable && !diagBlock.isDestroyed) {
            targetBlockX = frontTileX
            targetBlockY = diagBelowY
            return
        }

        // Priority 4: Directly below as fallback
        val defaultBelowY = ((playerY + playerHeight + 4f) / MiningWorld.TILE_SIZE).toInt()
        val defaultBelow = miningWorld.getBlock(centerTileX, defaultBelowY)
        if (defaultBelow.type.isDiggable && !defaultBelow.isDestroyed) {
            targetBlockX = centerTileX
            targetBlockY = defaultBelowY
            return
        }

        targetBlockX = -1
        targetBlockY = -1
    }

    private fun handleDigging(dt: Float) {
        if (targetBlockX == -1 || targetBlockY == -1) {
            isDigging = false
            digSwingProgress = 0f
            return
        }

        isDigging = true
        // Swing cycle time scales with shovel speed
        val swingDuration = 0.28f / shovelSpeedMultiplier
        digSwingProgress += dt / swingDuration

        if (digSwingProgress >= 1.0f) {
            digSwingProgress = 0f
            // Strike the block!
            val target = miningWorld.getBlock(targetBlockX, targetBlockY)
            val blockCenterX = targetBlockX * MiningWorld.TILE_SIZE + MiningWorld.TILE_SIZE / 2f
            val blockCenterY = targetBlockY * MiningWorld.TILE_SIZE + MiningWorld.TILE_SIZE / 2f

            screenShakeAmount = 6f
            audioEngine.playDigHit()
            particleSystem.emitDirtChunks(blockCenterX, blockCenterY, target.type.primaryColor, count = 7)

            val broke = miningWorld.damageBlock(targetBlockX, targetBlockY, shovelPower)
            advanceTutorial(5)

            if (broke) {
                audioEngine.playBlockBreak()
                particleSystem.emitDirtChunks(blockCenterX, blockCenterY, target.type.secondaryColor, count = 16)
                saveManager.totalBlocksDug += 1
                if (target.gem != null) {
                    advanceTutorial(6)
                }

                // Magma Smelter Upgrade: Instant gold reward for every destroyed block
                val smelterLvl = saveManager.getBuildingUpgradeLevel("magma_smelter")
                if (smelterLvl > 0) {
                    val smelterBonus = smelterLvl * 15L
                    saveManager.money += smelterBonus
                    saveManager.lifetimeEarnings += smelterBonus
                    particleSystem.addFloatingText(blockCenterX, blockCenterY - 18f, "+$$smelterBonus Gold", Color(0xFFFFD54F))
                }

                // Dynamite Blast Workshop: Chance to blast adjacent blocks!
                val dynamiteLvl = saveManager.getBuildingUpgradeLevel("dynamite_quarry")
                if (dynamiteLvl > 0 && Math.random() < (dynamiteLvl * 0.05)) {
                    screenShakeAmount = 14f
                    audioEngine.playBlockBreak()
                    particleSystem.emitCoinBurst(blockCenterX, blockCenterY, count = 12)
                    particleSystem.addFloatingText(blockCenterX, blockCenterY - 32f, "💥 BLAST!", Color(0xFFFF3D00))
                    val neighbors = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                    for ((dx, dy) in neighbors) {
                        val nx = targetBlockX + dx
                        val ny = targetBlockY + dy
                        val nb = miningWorld.getBlock(nx, ny)
                        if (nb.type.isDiggable && !nb.isDestroyed) {
                            miningWorld.damageBlock(nx, ny, shovelPower * 0.85f)
                        }
                    }
                }
            }

            if (!inputDigPress) {
                isDigging = false
            }
        }
    }

    private fun findNearbyDrop(): CollectibleDrop? {
        val px = playerX + playerWidth / 2f
        val py = playerY + playerHeight / 2f
        val pickupDist = 55f
        return miningWorld.activeDrops.firstOrNull { drop ->
            val dx = drop.x - px
            val dy = drop.y - py
            (dx * dx + dy * dy) < (pickupDist * pickupDist)
        }
    }

    private fun collectDrop(drop: CollectibleDrop) {
        if (drop.gem != null) {
            if (backpack.size >= backpackCapacity) {
                _backpackFullWarning.value = true
                audioEngine.playBackpackFull()
                particleSystem.addFloatingText(playerX, playerY - 30f, "BACKPACK FULL!", Color(0xFFFF5252))
                return
            }

            backpack.add(drop.gem)
            miningWorld.removeDrop(drop.id)
            val isRare = drop.gem.rarity >= 15
            audioEngine.playGemCollect(isRare)
            particleSystem.emitGemSparkles(drop.x, drop.y, drop.gem.primaryColor, count = 14)
            particleSystem.addFloatingText(drop.x, drop.y - 15f, "+ ${drop.gem.name}", drop.gem.primaryColor)
            advanceTutorial(7)
            advanceTutorial(8)
        } else if (drop.isCoin || drop.isFossil) {
            miningWorld.removeDrop(drop.id)
            val museumLvl = saveManager.getBuildingUpgradeLevel("fossil_museum")
            val rewardValue = if (drop.isFossil && museumLvl > 0) {
                (drop.coinValue * (1f + museumLvl * 0.25f)).toLong()
            } else {
                drop.coinValue
            }
            saveManager.money += rewardValue
            saveManager.lifetimeEarnings += rewardValue
            audioEngine.playSellReward()
            particleSystem.emitCoinBurst(drop.x, drop.y, count = 12)
            val label = if (drop.isFossil) "+ Fossil ($${rewardValue})!" else "+ $${rewardValue}!"
            particleSystem.addFloatingText(drop.x, drop.y - 15f, label, Color(0xFFFFD54F))
        }
    }

    fun sellAllGems(): Long {
        if (backpack.isEmpty()) return 0L
        val sifterLvl = saveManager.getBuildingUpgradeLevel("gem_sifter")
        val bonusMultiplier = 1f + sifterLvl * 0.06f

        var totalEarned = 0L
        for (gem in backpack) {
            val payout = (gem.value * bonusMultiplier).toLong()
            totalEarned += payout
        }

        saveManager.money += totalEarned
        saveManager.lifetimeEarnings += totalEarned
        backpack.clear()

        audioEngine.playSellReward()
        particleSystem.emitCoinBurst(playerX, playerY, count = 25)
        particleSystem.addFloatingText(playerX, playerY - 40f, "+ $$totalEarned Sold!", Color(0xFFFFD54F))
        advanceTutorial(10)
        return totalEarned
    }

    fun jump() {
        val bootsJump = boots.jumpImpulseMultiplier
        if (isOnLadder) {
            playerVy = -180f * bootsJump
            audioEngine.playClimb()
        } else if (isGrounded) {
            playerVy = -275f * bootsJump
            isGrounded = false
            audioEngine.playJump()
        } else if (isClimbingWall) {
            playerVy = -220f * bootsJump
            playerVx = (if (facingRight) -130f else 130f) * boots.moveSpeedMultiplier
            audioEngine.playJump()
        }
    }

    private fun moveAndCollide(dx: Float, dy: Float) {
        // Horizontal check
        playerX += dx
        resolveHorizontalCollision(dx)

        // Clamp to world width boundaries
        val minX = MiningWorld.TILE_SIZE * 0.5f
        val maxX = (MiningWorld.WIDTH_TILES - 1.5f) * MiningWorld.TILE_SIZE
        if (playerX < minX) playerX = minX
        if (playerX > maxX) playerX = maxX

        // Vertical check
        playerY += dy
        resolveVerticalCollision(dy)
    }

    private fun resolveHorizontalCollision(dx: Float) {
        if (dx == 0f) return
        val startTileY = (playerY / MiningWorld.TILE_SIZE).toInt()
        val endTileY = ((playerY + playerHeight - 1f) / MiningWorld.TILE_SIZE).toInt()

        if (dx > 0) {
            val rightTileX = ((playerX + playerWidth) / MiningWorld.TILE_SIZE).toInt()
            for (ty in startTileY..endTileY) {
                val block = miningWorld.getBlock(rightTileX, ty)
                if (block.type.isSolid && !block.isDestroyed) {
                    playerX = rightTileX * MiningWorld.TILE_SIZE - playerWidth
                    playerVx = 0f
                    break
                }
            }
        } else {
            val leftTileX = (playerX / MiningWorld.TILE_SIZE).toInt()
            for (ty in startTileY..endTileY) {
                val block = miningWorld.getBlock(leftTileX, ty)
                if (block.type.isSolid && !block.isDestroyed) {
                    playerX = (leftTileX + 1) * MiningWorld.TILE_SIZE
                    playerVx = 0f
                    break
                }
            }
        }
    }

    private fun resolveVerticalCollision(dy: Float) {
        isGrounded = false
        val startTileX = (playerX / MiningWorld.TILE_SIZE).toInt()
        val endTileX = ((playerX + playerWidth - 1f) / MiningWorld.TILE_SIZE).toInt()

        if (dy >= 0) {
            val bottomTileY = ((playerY + playerHeight) / MiningWorld.TILE_SIZE).toInt()
            for (tx in startTileX..endTileX) {
                val block = miningWorld.getBlock(tx, bottomTileY)
                if (block.type.isSolid && !block.isDestroyed) {
                    playerY = bottomTileY * MiningWorld.TILE_SIZE - playerHeight
                    playerVy = 0f
                    isGrounded = true
                    break
                }
            }
        } else {
            val topTileY = (playerY / MiningWorld.TILE_SIZE).toInt()
            for (tx in startTileX..endTileX) {
                val block = miningWorld.getBlock(tx, topTileY)
                if (block.type.isSolid && !block.isDestroyed) {
                    playerY = (topTileY + 1) * MiningWorld.TILE_SIZE
                    playerVy = 0f
                    break
                }
            }
        }

        // Clamp to surface top
        if (playerY < -MiningWorld.TILE_SIZE * 2f) {
            playerY = -MiningWorld.TILE_SIZE * 2f
            playerVy = 0f
        }
    }

    private fun checkDepthMilestones(depth: Int) {
        val milestones = listOf(10, 25, 50, 100, 150, 200, 300, 400, 500, 600)
        if (depth in milestones) {
            val bonus = depth * 50L
            saveManager.money += bonus
            saveManager.lifetimeEarnings += bonus
            audioEngine.playUpgradeFanfare()
            _milestoneAnnouncement.value = "🏆 MILESTONE REACHED: LEVEL $depth!\nBonus: +$$bonus"
        }
    }

    fun advanceTutorial(step: Int) {
        if (saveManager.tutorialCompleted) return
        if (_tutorialStep.value == step) {
            val next = step + 1
            if (next > 10) {
                saveManager.tutorialCompleted = true
                _tutorialStep.value = -1
            } else {
                _tutorialStep.value = next
            }
        }
    }

    fun replayTutorial() {
        saveManager.tutorialCompleted = false
        _tutorialStep.value = 1
    }
}
