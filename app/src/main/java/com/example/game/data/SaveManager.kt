package com.example.game.data

import android.content.Context
import android.content.SharedPreferences

class SaveManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("repair_your_house_save_v1", Context.MODE_PRIVATE)

    var money: Long
        get() = prefs.getLong("money", 0L)
        set(value) = prefs.edit().putLong("money", value).apply()

    var lifetimeEarnings: Long
        get() = prefs.getLong("lifetime_earnings", 0L)
        set(value) = prefs.edit().putLong("lifetime_earnings", value).apply()

    var shovelLevel: Int
        get() = prefs.getInt("shovel_level", 1)
        set(value) = prefs.edit().putInt("shovel_level", value).apply()

    var backpackLevel: Int
        get() = prefs.getInt("backpack_level", 1)
        set(value) = prefs.edit().putInt("backpack_level", value).apply()

    var bootsLevel: Int
        get() = prefs.getInt("boots_level", 1)
        set(value) = prefs.edit().putInt("boots_level", value).apply()

    var headlampLevel: Int
        get() = prefs.getInt("headlamp_level", 1)
        set(value) = prefs.edit().putInt("headlamp_level", value).apply()

    var deepestDepth: Int
        get() = prefs.getInt("deepest_depth", 0)
        set(value) = prefs.edit().putInt("deepest_depth", value).apply()

    var totalBlocksDug: Long
        get() = prefs.getLong("total_blocks_dug", 0L)
        set(value) = prefs.edit().putLong("total_blocks_dug", value).apply()

    var tutorialCompleted: Boolean
        get() = prefs.getBoolean("tutorial_completed", false)
        set(value) = prefs.edit().putBoolean("tutorial_completed", value).apply()

    var sfxVolume: Float
        get() = prefs.getFloat("sfx_volume", 1.0f)
        set(value) = prefs.edit().putFloat("sfx_volume", value).apply()

    var musicVolume: Float
        get() = prefs.getFloat("music_volume", 0.8f)
        set(value) = prefs.edit().putFloat("music_volume", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    var controlOpacity: Float
        get() = prefs.getFloat("control_opacity", 0.75f)
        set(value) = prefs.edit().putFloat("control_opacity", value).apply()

    var controlScale: Float
        get() = prefs.getFloat("control_scale", 1.0f)
        set(value) = prefs.edit().putFloat("control_scale", value).apply()

    // Completed Repairs
    fun getCompletedRepairs(): Set<String> {
        return prefs.getStringSet("completed_repairs", emptySet()) ?: emptySet()
    }

    fun addCompletedRepair(id: String) {
        val current = getCompletedRepairs().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("completed_repairs", current).apply()
    }

    // Discovered Gems
    fun getDiscoveredGemIds(): Set<Int> {
        val raw = prefs.getStringSet("discovered_gem_ids", emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun recordGemDiscovery(gemId: Int) {
        val current = prefs.getStringSet("discovered_gem_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(gemId.toString())
        val countKey = "gem_count_$gemId"
        val count = prefs.getInt(countKey, 0) + 1
        prefs.edit()
            .putStringSet("discovered_gem_ids", current)
            .putInt(countKey, count)
            .apply()
    }

    fun getGemDiscoveryCount(gemId: Int): Int {
        return prefs.getInt("gem_count_$gemId", 0)
    }

    // Building Upgrades
    fun getBuildingUpgradeLevel(id: String): Int {
        return prefs.getInt("building_upgrade_$id", 0)
    }

    fun setBuildingUpgradeLevel(id: String, level: Int) {
        prefs.edit().putInt("building_upgrade_$id", level).apply()
    }

    // Claimed Seasonal Challenges
    fun getClaimedChallenges(): Set<String> {
        return prefs.getStringSet("claimed_challenges", emptySet()) ?: emptySet()
    }

    fun claimChallenge(id: String) {
        val current = getClaimedChallenges().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("claimed_challenges", current).apply()
    }

    // Reset Progress
    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }
}
