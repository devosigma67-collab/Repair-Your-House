package com.example.game.data

import androidx.compose.ui.graphics.Color

/**
 * Rarity tier: Level 1 (Common) to Level 60 (Extremely Rare / Primordial / Cosmic)
 */
enum class GemRarityTier(val displayName: String, val baseColor: Color) {
    COMMON("Common", Color(0xFFB0BEC5)),
    UNCOMMON("Uncommon", Color(0xFF81C784)),
    RARE("Rare", Color(0xFF64B5F6)),
    EPIC("Epic", Color(0xFFBA68C8)),
    MYTHIC("Mythic", Color(0xFFFFB74D)),
    CELESTIAL("Celestial", Color(0xFF4DD0E1)),
    LEGENDARY("Legendary", Color(0xFFFFD54F)),
    PRIMORDIAL("Primordial", Color(0xFFFF4081)),
    COSMIC("Cosmic", Color(0xFFE040FB))
}

/**
 * Shape archetype for rendering protruding crystals and Gemdex icons
 */
enum class GemSilhouette {
    SPIRE,
    CLUSTER,
    FACETED,
    DROPLET,
    GEODE,
    LOTUS,
    STAR,
    RING_ORB,
    BISMUTH_STEP,
    PRISM_HEART
}

data class Gem(
    val id: Int,
    val name: String,
    val rarity: Int, // 1 to 60
    val value: Long,
    val minDepth: Int, // Level 1 to 600
    val maxDepth: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val silhouette: GemSilhouette,
    val lore: String
) {
    val rarityTier: GemRarityTier get() = when (rarity) {
        in 1..4 -> GemRarityTier.COMMON
        in 5..10 -> GemRarityTier.UNCOMMON
        in 11..20 -> GemRarityTier.RARE
        in 21..32 -> GemRarityTier.EPIC
        in 33..42 -> GemRarityTier.MYTHIC
        in 43..50 -> GemRarityTier.CELESTIAL
        in 51..55 -> GemRarityTier.LEGENDARY
        in 56..58 -> GemRarityTier.PRIMORDIAL
        else -> GemRarityTier.COSMIC
    }
}

object GemRegistry {
    // 60 rich, unique gems spanning Levels 1 to 600
    val ALL_GEMS: List<Gem> = listOf(
        // COMMON (Levels 1 - 60)
        Gem(1, "Amber Pebble", 1, 35L, 1, 45, Color(0xFFFFB300), Color(0xFFFFE082), GemSilhouette.DROPLET, "A warm fossilized resin found in soft topsoil."),
        Gem(2, "Pyrite Shard", 2, 60L, 2, 55, Color(0xFFD4AF37), Color(0xFFFFF59D), GemSilhouette.FACETED, "Fool's gold that glitters brightly in torchlight."),
        Gem(3, "Copper Spire", 3, 95L, 5, 70, Color(0xFFD87A54), Color(0xFFFFCCBC), GemSilhouette.SPIRE, "Sharp native copper needle extracted from gravel."),
        Gem(4, "Rose Quartz", 4, 140L, 8, 85, Color(0xFFF48FB1), Color(0xFFFCE4EC), GemSilhouette.CLUSTER, "Soothing pale pink crystal prized by early crafters."),

        // UNCOMMON (Levels 15 - 130)
        Gem(5, "Malachite Cluster", 5, 190L, 15, 100, Color(0xFF2E7D32), Color(0xFFA5D6A7), GemSilhouette.CLUSTER, "Deep forest-green banded mineral hidden in clay."),
        Gem(6, "Citrine Geode", 6, 260L, 22, 115, Color(0xFFFF8F00), Color(0xFFFFE082), GemSilhouette.GEODE, "Vibrant sunny quartz formed within basalt pockets."),
        Gem(7, "Lapis Lazuli", 7, 350L, 30, 130, Color(0xFF1E88E5), Color(0xFF90CAF9), GemSilhouette.FACETED, "Deep celestial blue flecked with golden grains."),
        Gem(8, "Garnet Tear", 8, 460L, 40, 145, Color(0xFFC2185B), Color(0xFFF8BBD0), GemSilhouette.DROPLET, "Rich wine-red droplet found along underground aquifers."),
        Gem(9, "Peridot Crystal", 9, 590L, 50, 160, Color(0xFF7CB342), Color(0xFFDCEDC8), GemSilhouette.FACETED, "Luminescent olive olivine formed under tectonic heat."),
        Gem(10, "Fluorite Prism", 10, 750L, 65, 180, Color(0xFF26A69A), Color(0xFF80CBC4), GemSilhouette.SPIRE, "Naturally cubes and glows under cavern radiation."),

        // RARE (Levels 70 - 260)
        Gem(11, "Aquamarine Spire", 11, 950L, 75, 200, Color(0xFF00ACC1), Color(0xFF80DEEA), GemSilhouette.SPIRE, "Clear beryl crystal reminiscent of calm glacial waters."),
        Gem(12, "Tourmaline Needle", 12, 1200L, 88, 215, Color(0xFF00897B), Color(0xFF80CBC4), GemSilhouette.SPIRE, "Slender dual-colored crystal with pyroelectric traits."),
        Gem(13, "Topaz Prism", 13, 1500L, 100, 230, Color(0xFFFB8C00), Color(0xFFFFE0B2), GemSilhouette.FACETED, "Dense golden gem that easily scores hard granite."),
        Gem(14, "Deep Amethyst", 14, 1850L, 115, 245, Color(0xFF7B1FA2), Color(0xFFCE93D8), GemSilhouette.GEODE, "Resplendent purple geode blooming in cavern hollows."),
        Gem(15, "Bloodstone Core", 15, 2300L, 130, 260, Color(0xFF880E4F), Color(0xFFEF5350), GemSilhouette.FACETED, "Jasper patterned with bright scarlet iron deposits."),
        Gem(16, "Moon Shard", 16, 2800L, 145, 275, Color(0xFFECEFF1), Color(0xFF80DEEA), GemSilhouette.SPIRE, "Chilled silvery stone with faint ethereal adularescence."),
        Gem(17, "Sun Crystal", 17, 3400L, 160, 290, Color(0xFFFF6F00), Color(0xFFFFF176), GemSilhouette.STAR, "Trapped ancient sunlight that maintains a warm glow."),
        Gem(18, "Cobalt Star", 18, 4100L, 175, 305, Color(0xFF1565C0), Color(0xFF64B5F6), GemSilhouette.STAR, "Metallic indigo star crystal of remarkable density."),
        Gem(19, "Jadeite Lotus", 19, 4900L, 190, 320, Color(0xFF388E3C), Color(0xFFC8E6C9), GemSilhouette.LOTUS, "Imperial jade carved by subterranean water currents."),
        Gem(20, "Opal Fire", 20, 5800L, 205, 335, Color(0xFFFF5722), Color(0xFF80DEEA), GemSilhouette.CLUSTER, "Flashes a rainbow prism when viewed from any angle."),

        // EPIC (Levels 210 - 390)
        Gem(21, "Bismuth Staircase", 21, 6800L, 215, 345, Color(0xFF00E5FF), Color(0xFFFF4081), GemSilhouette.BISMUTH_STEP, "Intricate geometric spiral staircase with iridescent oxide hues."),
        Gem(22, "Tanzanite Crown", 22, 8000L, 225, 355, Color(0xFF512DA8), Color(0xFF9FA8DA), GemSilhouette.FACETED, "Trichroic violet-blue treasure from ancient rift valleys."),
        Gem(23, "Emerald Heart", 23, 9500L, 240, 370, Color(0xFF00796B), Color(0xFF4DB6AC), GemSilhouette.PRISM_HEART, "Deep emerald crystal formed within schist matrix."),
        Gem(24, "Ruby Eye", 24, 11500L, 255, 385, Color(0xFFD32F2F), Color(0xFFFF8A80), GemSilhouette.DROPLET, "Gleams like a living ember in subterranean shadows."),
        Gem(25, "Sapphire Eclipse", 25, 14000L, 270, 400, Color(0xFF0D47A1), Color(0xFF42A5F5), GemSilhouette.STAR, "Flawless corundum of twilight navy blue brilliance."),
        Gem(26, "Alexandrite Shifter", 26, 17000L, 285, 415, Color(0xFF004D40), Color(0xFF880E4F), GemSilhouette.FACETED, "Shifts from deep teal in natural light to ruby red in torchlight."),
        Gem(27, "Obsidian Core", 27, 20500L, 300, 430, Color(0xFF212121), Color(0xFF7E57C2), GemSilhouette.SPIRE, "Vitreous volcanic core carrying magnetic resonance."),
        Gem(28, "Frost Diamond", 28, 25000L, 315, 445, Color(0xFF00BCD4), Color(0xFFE0F7FA), GemSilhouette.FACETED, "Sub-zero diamond that condenses frost on adjacent stone."),
        Gem(29, "Nebula Quartz", 29, 31000L, 330, 460, Color(0xFF6A1B9A), Color(0xFFEA80FC), GemSilhouette.CLUSTER, "Swirling gaseous violet hues sealed inside pure quartz."),
        Gem(30, "Moldavite Shard", 30, 38000L, 345, 475, Color(0xFF558B2F), Color(0xFFAED581), GemSilhouette.DROPLET, "Glassy olive impactite formed by a prehistoric meteorite."),
        Gem(31, "Benitoite Spark", 31, 46000L, 360, 490, Color(0xFF2979FF), Color(0xFF82B1FF), GemSilhouette.STAR, "Electric neon sapphire-blue gemstone of immense clarity."),
        Gem(32, "Volcanic Ember", 32, 55000L, 375, 505, Color(0xFFFF3D00), Color(0xFFFFAB91), GemSilhouette.SPIRE, "Searing magma core that steadily radiates thermal warmth."),

        // MYTHIC (Levels 380 - 490)
        Gem(33, "Celestial Pearl", 33, 66000L, 385, 515, Color(0xFFB39DDB), Color(0xFFEDE7F6), GemSilhouette.RING_ORB, "Silvery orb said to have seeded from fallen meteors."),
        Gem(34, "Void Resonance", 34, 79000L, 395, 525, Color(0xFF311B92), Color(0xFF7C4DFF), GemSilhouette.STAR, "Gravitationally bends light waves into a violet halo."),
        Gem(35, "Chrono Geode", 35, 94000L, 410, 535, Color(0xFFF57F17), Color(0xFFFFF59D), GemSilhouette.GEODE, "Spins microscopic dust motes in reverse time."),
        Gem(36, "Prismatic Blossom", 36, 112000L, 425, 545, Color(0xFFE91E63), Color(0xFF00E676), GemSilhouette.LOTUS, "Petals of solid crystal refracting seven clean wavelengths."),
        Gem(37, "Starlight Core", 37, 134000L, 440, 555, Color(0xFF26C6DA), Color(0xFFFFFFFF), GemSilhouette.STAR, "Condensed stellar fragment pulsing in quiet cadence."),
        Gem(38, "Dragon Tear Gem", 38, 160000L, 455, 565, Color(0xFFFF5252), Color(0xFFFFD740), GemSilhouette.DROPLET, "Mythic gemstone that survived billions of years of mantle pressure."),
        Gem(39, "Grand Red Beryl", 39, 192000L, 470, 575, Color(0xFFB71C1C), Color(0xFFFF8A80), GemSilhouette.FACETED, "One of the rarest red emerald varieties in existence."),
        Gem(40, "Taaffeite Crystal", 40, 230000L, 480, 580, Color(0xFF8E24AA), Color(0xFFE1BEE7), GemSilhouette.FACETED, "A mauve wonder million times rarer than diamond."),
        Gem(41, "Grandidierite Spire", 41, 275000L, 490, 585, Color(0xFF00897B), Color(0xFF80DEEA), GemSilhouette.SPIRE, "Sea-foam green pleochroic mineral of legendary rarity."),
        Gem(42, "Painite Needle", 42, 330000L, 500, 590, Color(0xFFBF360C), Color(0xFFFFAB91), GemSilhouette.SPIRE, "Dark reddish-orange crystal once crowned rarest on Earth."),

        // CELESTIAL (Levels 490 - 570)
        Gem(43, "Aurora Diamond", 43, 395000L, 505, 592, Color(0xFF18FFFF), Color(0xFF69F0AE), GemSilhouette.FACETED, "Ribbons of polar aurora trapped forever inside diamond."),
        Gem(44, "Solar Flare Ruby", 44, 470000L, 515, 594, Color(0xFFFF1744), Color(0xFFFF9100), GemSilhouette.SPIRE, "Blazing crimson crystal burning with solar fusion energy."),
        Gem(45, "Astral Aegis", 45, 560000L, 525, 596, Color(0xFF304FFE), Color(0xFF00B0FF), GemSilhouette.RING_ORB, "Surrounded by a microscopic orbiting ring of stardust."),
        Gem(46, "Quantum Crystal", 46, 670000L, 535, 598, Color(0xFF651FFF), Color(0xFF00E5FF), GemSilhouette.FACETED, "Superposed simultaneously across multiple light states."),
        Gem(47, "Infinity Amethyst", 47, 800000L, 545, 600, Color(0xFFAA00FF), Color(0xFFFF4081), GemSilhouette.CLUSTER, "Infinite self-similar fractal patterns in pure violet."),
        Gem(48, "Supernova Citrine", 48, 960000L, 555, 600, Color(0xFFFFD600), Color(0xFFFF6D00), GemSilhouette.STAR, "Radiates the blinding gold luminance of a dying sun."),
        Gem(49, "Black Opal Lightning", 49, 1150000L, 565, 600, Color(0xFF212121), Color(0xFF00E5FF), GemSilhouette.CLUSTER, "Dark obsidian base electrified with neon lightning streaks."),
        Gem(50, "Genesis Shard", 50, 1380000L, 572, 600, Color(0xFFFFF9C4), Color(0xFFFFFFFF), GemSilhouette.STAR, "One of the primordial sparks present at the birth of Earth."),

        // LEGENDARY (Levels 570 - 595)
        Gem(51, "Heart of Gaia", 51, 1650000L, 578, 600, Color(0xFFFF0055), Color(0xFF00FFFF), GemSilhouette.LOTUS, "Living crystal pulsing with the tectonic heartbeat of the planet."),
        Gem(52, "Void Singularity", 52, 2000000L, 582, 600, Color(0xFF1A237E), Color(0xFF000000), GemSilhouette.RING_ORB, "Infinitely dense orb that distorts time and space around it."),
        Gem(53, "Phoenix Core", 53, 2450000L, 586, 600, Color(0xFFFF3D00), Color(0xFFFFD600), GemSilhouette.PRISM_HEART, "Reborn from thermal ashes every time it is struck by a shovel."),
        Gem(54, "Hypercube Tesseract", 54, 3000000L, 590, 600, Color(0xFF00E5FF), Color(0xFF7C4DFF), GemSilhouette.FACETED, "4-dimensional crystal casting 3D shadows onto the rock face."),
        Gem(55, "Dark Matter Monolith", 55, 3700000L, 592, 600, Color(0xFF0D0D15), Color(0xFFD500F9), GemSilhouette.SPIRE, "Composed of invisible mass that interacts only through gravity."),

        // PRIMORDIAL (Levels 593 - 599)
        Gem(56, "Zero-Point Spark", 56, 4600000L, 594, 600, Color(0xFF84FFFF), Color(0xFFFFFFFF), GemSilhouette.STAR, "Draws infinite energy directly from quantum vacuum fluctuations."),
        Gem(57, "Ethereal Chrono-Sphere", 57, 5700000L, 596, 600, Color(0xFFFF80AB), Color(0xFF82B1FF), GemSilhouette.RING_ORB, "Allows the bearer to glimpse moments from past and future eons."),
        Gem(58, "Cosmic Tree Seed", 58, 7000000L, 597, 600, Color(0xFF00E676), Color(0xFFFFD700), GemSilhouette.LOTUS, "Ancient botanical seed mineralized into an everlasting diamond jewel."),

        // COSMIC (Level 598 - 600 - THE ULTIMATE TREASURES)
        Gem(59, "Event Horizon Diamond", 59, 8800000L, 598, 600, Color(0xFF7C4DFF), Color(0xFF18FFFF), GemSilhouette.STAR, "Forms at the threshold where light can never escape."),
        Gem(60, "Omniverse Crown", 60, 12000000L, 600, 600, Color(0xFFFFD700), Color(0xFFFF007F), GemSilhouette.LOTUS, "The absolute supreme treasure of all 600 levels of the earth.")
    )

    fun getGemById(id: Int): Gem = ALL_GEMS.firstOrNull { it.id == id } ?: ALL_GEMS[0]

    fun getGemsForDepth(depth: Int): List<Gem> {
        val matching = ALL_GEMS.filter { depth in it.minDepth..it.maxDepth }
        return if (matching.isNotEmpty()) matching else listOf(ALL_GEMS[0])
    }
}
