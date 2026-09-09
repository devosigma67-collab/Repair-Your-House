package com.example.game.data

data class SeasonalChallenge(
    val id: String,
    val title: String,
    val description: String,
    val target: Long,
    val rewardMoney: Long,
    val icon: String
)

object SeasonalRegistry {
    const val CURRENT_SEASON_NAME = "Season 1: Subterranean Genesis"
    const val SEASON_TIME_REMAINING = "18 Days Left"

    val CHALLENGES = listOf(
        SeasonalChallenge("dig_50", "Pioneer Miner", "Dig 50 blocks underground", 50L, 500L, "dig"),
        SeasonalChallenge("dig_300", "Tunnel Master", "Dig 300 blocks underground", 300L, 3500L, "tunnel"),
        SeasonalChallenge("depth_25", "First Plunge", "Reach underground Level 25", 25L, 1200L, "depth"),
        SeasonalChallenge("depth_100", "Rock Crusher", "Reach underground Level 100", 100L, 8000L, "cave"),
        SeasonalChallenge("depth_300", "Abyssal Diver", "Reach underground Level 300", 300L, 35000L, "deep"),
        SeasonalChallenge("collect_10_rare", "Gem Hunter", "Collect 10 Rare gems (Rarity >= 10)", 10L, 10000L, "gem"),
        SeasonalChallenge("house_5_repairs", "Handyman", "Complete 5 House repairs", 5L, 6000L, "repair"),
        SeasonalChallenge("house_complete", "Palace Architect", "Reach 100% House Repair (Literal Mansion)", 18L, 250000L, "crown")
    )
}

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val countryFlag: String,
    val netWorth: Long,
    val deepestLevel: Int,
    val rarestGemName: String,
    val isPlayer: Boolean = false
)

object LeaderboardRegistry {
    private val NPC_ENTRIES = listOf(
        LeaderboardEntry(1, "Old Pete 'The Drill'", "🇺🇸", 9500000L, 600, "Heart of Gaia"),
        LeaderboardEntry(2, "Lady Seraphina Ruby", "🇬🇧", 7800000L, 598, "Genesis Shard"),
        LeaderboardEntry(3, "Dr. Kazuto Tanaka", "🇯🇵", 6200000L, 592, "Infinity Amethyst"),
        LeaderboardEntry(4, "Gunter Von Basalt", "🇩🇪", 4900000L, 580, "Quantum Crystal"),
        LeaderboardEntry(5, "Elena 'Viper' Rostova", "🇨🇦", 3800000L, 565, "Astral Aegis"),
        LeaderboardEntry(6, "Mateo Gold-Finder", "🇲🇽", 2900000L, 540, "Solar Flare Ruby"),
        LeaderboardEntry(7, "Chloe 'Sparkle' Dubois", "🇫🇷", 2100000L, 510, "Aurora Diamond"),
        LeaderboardEntry(8, "Jin-Woo Cave-Walker", "🇰🇷", 1500000L, 470, "Dragon Tear Gem"),
        LeaderboardEntry(9, "Aarav Stone-Whisperer", "🇮🇳", 980000L, 420, "Starlight Core"),
        LeaderboardEntry(10, "Sven Deep-Delver", "🇸🇪", 650000L, 380, "Volcanic Ember"),
        LeaderboardEntry(11, "Liam Pickaxe-Pro", "🇦🇺", 420000L, 330, "Frost Diamond"),
        LeaderboardEntry(12, "Mia Crystal-Seeker", "🇧🇷", 280000L, 270, "Obsidian Core"),
        LeaderboardEntry(13, "Niko Quarry-Runner", "🇬🇷", 160000L, 210, "Emerald Heart"),
        LeaderboardEntry(14, "Zoe Shovel-Cadet", "🇳🇿", 75000L, 140, "Moon Shard"),
        LeaderboardEntry(15, "Barnaby New-Digger", "🇿🇦", 25000L, 80, "Deep Amethyst")
    )

    fun getStandings(playerNetWorth: Long, playerDeepestLevel: Int, playerRarestGem: String): List<LeaderboardEntry> {
        val playerEntry = LeaderboardEntry(
            rank = 999,
            name = "You (Master Miner)",
            countryFlag = "⭐",
            netWorth = playerNetWorth,
            deepestLevel = playerDeepestLevel,
            rarestGemName = playerRarestGem,
            isPlayer = true
        )

        val combined = (NPC_ENTRIES + playerEntry).sortedByDescending { it.netWorth }
        return combined.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }
}
