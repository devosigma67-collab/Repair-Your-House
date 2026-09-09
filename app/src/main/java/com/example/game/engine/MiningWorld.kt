package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.data.Gem
import com.example.game.data.GemRegistry
import kotlin.random.Random

enum class BlockType(
    val displayName: String,
    val baseHealth: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isDiggable: Boolean = true,
    val isSolid: Boolean = true
) {
    AIR("Air", 0f, Color.Transparent, Color.Transparent, isDiggable = false, isSolid = false),
    LADDER("Ladder", 0f, Color(0xFF8D6E63), Color(0xFFD7CCC8), isDiggable = false, isSolid = false),
    SOFT_DIRT("Soft Earth", 2.0f, Color(0xFF795548), Color(0xFF5D4037)),
    STONY_DIRT("Stony Loam", 3.5f, Color(0xFF6D4C41), Color(0xFF78909C)),
    CAVERN_GRANITE("Cavern Granite", 6.0f, Color(0xFF546E7A), Color(0xFF37474F)),
    CRYSTAL_STRATA("Crystal Strata", 10.0f, Color(0xFF4A148C), Color(0xFF7B1FA2)),
    MAGMA_BASALT("Magma Basalt", 16.0f, Color(0xFF263238), Color(0xFFFF3D00)),
    CELESTIAL_STONE("Celestial Core", 26.0f, Color(0xFF1A237E), Color(0xFF00E5FF)),
    BEDROCK("Bedrock", 9999f, Color(0xFF101010), Color(0xFF1E1E1E), isDiggable = false, isSolid = true)
}

data class Block(
    val x: Int,
    val y: Int, // 1 to 600
    var type: BlockType,
    var currentHealth: Float,
    val maxHealth: Float,
    var gem: Gem? = null,
    val gemProtrusionSide: Int = 0, // 0: top, 1: left, 2: right, 3: center cluster
    var hasChest: Boolean = false,
    var hasFossil: Boolean = false
) {
    val isDestroyed: Boolean get() = type == BlockType.AIR || currentHealth <= 0f
    val crackStage: Int get() {
        if (maxHealth <= 0f) return 0
        val ratio = (1f - (currentHealth / maxHealth)).coerceIn(0f, 1f)
        return (ratio * 4f).toInt().coerceIn(0, 4)
    }
}

data class CollectibleDrop(
    val id: Long,
    var x: Float,
    var y: Float,
    var vy: Float = -60f,
    val gem: Gem? = null,
    val isCoin: Boolean = false,
    val isFossil: Boolean = false,
    val coinValue: Long = 0L,
    var bounceCount: Int = 0,
    var pulseTimer: Float = 0f
)

class MiningWorld(private val onDiscovery: (Gem) -> Unit) {
    companion object {
        const val WIDTH_TILES = 14
        const val MAX_DEPTH_LEVELS = 600
        const val TILE_SIZE = 48f
    }

    // Sparse storage for modified/loaded blocks
    private val blocksMap = mutableMapOf<Long, Block>()
    val activeDrops = mutableListOf<CollectibleDrop>()
    private var dropCounter = 0L

    private fun coordKey(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)

    fun getBlock(x: Int, y: Int): Block {
        if (x < 0 || x >= WIDTH_TILES) {
            return Block(x, y, BlockType.BEDROCK, 9999f, 9999f)
        }
        if (y < 0) {
            return Block(x, y, BlockType.AIR, 0f, 0f)
        }
        if (y == 0) {
            // Surface row: mine shaft entrance at columns 10, 11
            return if (x == 10 || x == 11) {
                Block(x, y, BlockType.LADDER, 0f, 0f)
            } else {
                Block(x, y, BlockType.SOFT_DIRT, 2f, 2f)
            }
        }
        if (y > MAX_DEPTH_LEVELS) {
            return Block(x, y, BlockType.BEDROCK, 9999f, 9999f)
        }

        val key = coordKey(x, y)
        blocksMap[key]?.let { return it }

        val generated = generateBlock(x, y)
        blocksMap[key] = generated
        return generated
    }

    private fun generateBlock(x: Int, y: Int): Block {
        // Wooden ladder shaft down the right side (column 11)
        if (x == 11 && y in 1..MAX_DEPTH_LEVELS) {
            return Block(x, y, BlockType.LADDER, 0f, 0f)
        }

        // Milestone chambers every 100 levels or level 10, 25, 50
        val isMilestoneRoom = (y == 10 || y == 25 || y == 50 || y % 100 == 0) && (x in 4..8)
        if (isMilestoneRoom) {
            val hasTreasure = (x == 6)
            return Block(
                x = x,
                y = y,
                type = BlockType.AIR,
                currentHealth = 0f,
                maxHealth = 0f,
                hasChest = hasTreasure
            )
        }

        // Natural small caverns
        val caveNoise = kotlin.math.sin(x * 0.8 + y * 0.15) * kotlin.math.cos(x * 0.4 + y * 0.3)
        if (caveNoise > 0.82 && y > 5 && x != 10 && x != 11) {
            return Block(x, y, BlockType.AIR, 0f, 0f)
        }

        // Determine stratum based on 600 levels
        val blockType = when (y) {
            in 1..50 -> BlockType.SOFT_DIRT
            in 51..120 -> if (Random(x * 31 + y * 17).nextFloat() < 0.35f) BlockType.STONY_DIRT else BlockType.SOFT_DIRT
            in 121..220 -> if (Random(x * 31 + y * 17).nextFloat() < 0.65f) BlockType.CAVERN_GRANITE else BlockType.STONY_DIRT
            in 221..350 -> if (Random(x * 31 + y * 17).nextFloat() < 0.60f) BlockType.CRYSTAL_STRATA else BlockType.CAVERN_GRANITE
            in 351..480 -> if (Random(x * 31 + y * 17).nextFloat() < 0.70f) BlockType.MAGMA_BASALT else BlockType.CRYSTAL_STRATA
            else -> if (Random(x * 31 + y * 17).nextFloat() < 0.85f) BlockType.CELESTIAL_STONE else BlockType.MAGMA_BASALT
        }

        // Seeded random for gem and loot generation
        val rand = Random(x * 4999 + y * 1777)
        val gemRoll = rand.nextFloat()

        // Depth scaling factor (0.0 at surface to 1.0 at maximum depth)
        val depthProgress = (y.toFloat() / MAX_DEPTH_LEVELS).coerceIn(0f, 1f)

        // MORE LOOTS AS YOU GO DOWN: Gem chance scales from 24% at surface up to 72% deep below!
        val gemChance = 0.24f + depthProgress * 0.48f

        // First dig guarantee at (10, 1) or (9, 1) next to ladder
        var gem: Gem? = null
        if (x == 10 && y == 1) {
            gem = GemRegistry.getGemById(1) // Amber Pebble for tutorial!
        } else {
            if (gemRoll < gemChance) {
                val depthGems = GemRegistry.getGemsForDepth(y)
                if (depthGems.isNotEmpty()) {
                    gem = depthGems[rand.nextInt(depthGems.size)]
                }
            }
        }

        // Chests and fossils appear much more frequently at deeper strata
        val chestChance = 0.035f + depthProgress * 0.075f
        val fossilChance = 0.04f + depthProgress * 0.08f
        val hasChest = gem == null && rand.nextFloat() < chestChance && y > 8
        val hasFossil = gem == null && !hasChest && rand.nextFloat() < fossilChance && y > 12
        val protrusion = rand.nextInt(4)

        // HARDER TO DIG AS YOU GO DOWN: Strata become progressively denser, reinforced by rock pressure!
        // At depth 1: 1.0x | depth 50: ~1.45x | depth 150: ~2.4x | depth 300: ~4.5x | depth 600: ~9.5x!
        val depthHardnessMultiplier = 1.0f + Math.pow(y.toDouble() / 55.0, 1.25).toFloat() * 0.45f
        val calculatedHealth = blockType.baseHealth * depthHardnessMultiplier

        return Block(
            x = x,
            y = y,
            type = blockType,
            currentHealth = calculatedHealth,
            maxHealth = calculatedHealth,
            gem = gem,
            gemProtrusionSide = protrusion,
            hasChest = hasChest,
            hasFossil = hasFossil
        )
    }

    fun damageBlock(x: Int, y: Int, damage: Float): Boolean {
        val block = getBlock(x, y)
        if (!block.type.isDiggable || block.isDestroyed) return false

        block.currentHealth -= damage
        if (block.currentHealth <= 0f) {
            destroyBlock(x, y)
            return true // broke
        }
        return false // damaged
    }

    private fun destroyBlock(x: Int, y: Int) {
        val key = coordKey(x, y)
        val block = getBlock(x, y)
        block.type = BlockType.AIR
        block.currentHealth = 0f
        blocksMap[key] = block

        val dropWorldX = x * TILE_SIZE + TILE_SIZE / 2f
        val dropWorldY = y * TILE_SIZE + TILE_SIZE / 2f

        // Drop gem if present
        block.gem?.let { gem ->
            activeDrops.add(
                CollectibleDrop(
                    id = ++dropCounter,
                    x = dropWorldX,
                    y = dropWorldY,
                    gem = gem
                )
            )
            onDiscovery(gem)

            // Deep cavern bonus: At depth 40+, chance to unearth an extra cluster gem drop!
            val depthProgress = (y.toFloat() / MAX_DEPTH_LEVELS).coerceIn(0f, 1f)
            val rand = Random(x * 1337 + y * 7331)
            if (y >= 40 && rand.nextFloat() < (0.15f + depthProgress * 0.30f)) {
                val depthGems = GemRegistry.getGemsForDepth(y)
                if (depthGems.isNotEmpty()) {
                    val bonusGem = depthGems[rand.nextInt(depthGems.size)]
                    activeDrops.add(
                        CollectibleDrop(
                            id = ++dropCounter,
                            x = dropWorldX + rand.nextInt(-12, 13),
                            y = dropWorldY - 14f,
                            vy = -90f,
                            gem = bonusGem
                        )
                    )
                    onDiscovery(bonusGem)
                }
            }
        }

        // Drop chest coins (scales substantially with depth!)
        if (block.hasChest) {
            val chestReward = (y * 120L + 250L)
            activeDrops.add(
                CollectibleDrop(
                    id = ++dropCounter,
                    x = dropWorldX,
                    y = dropWorldY,
                    isCoin = true,
                    coinValue = chestReward
                )
            )
        }

        // Drop fossil (scales with depth!)
        if (block.hasFossil) {
            val fossilReward = (y * 85L + 150L)
            activeDrops.add(
                CollectibleDrop(
                    id = ++dropCounter,
                    x = dropWorldX,
                    y = dropWorldY,
                    isFossil = true,
                    coinValue = fossilReward
                )
            )
        }
    }

    fun updateDrops(dt: Float) {
        val iter = activeDrops.iterator()
        while (iter.hasNext()) {
            val drop = iter.next()
            drop.pulseTimer += dt
            drop.vy += 380f * dt // Gravity
            drop.y += drop.vy * dt

            // Ground collision for drop
            val tileX = (drop.x / TILE_SIZE).toInt()
            val tileY = (drop.y / TILE_SIZE).toInt()
            val belowBlock = getBlock(tileX, tileY)
            if (belowBlock.type.isSolid && !belowBlock.isDestroyed) {
                drop.y = tileY * TILE_SIZE - 6f
                if (drop.bounceCount < 2) {
                    drop.vy = -drop.vy * 0.35f
                    drop.bounceCount++
                } else {
                    drop.vy = 0f
                }
            }
        }
    }

    fun removeDrop(id: Long) {
        activeDrops.removeAll { it.id == id }
    }
}
