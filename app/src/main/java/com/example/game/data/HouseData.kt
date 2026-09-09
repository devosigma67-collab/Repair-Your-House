package com.example.game.data

enum class HouseStage(val stageNumber: Int, val title: String, val subtitle: String) {
    RUINED_SHACK(1, "Ruined Shack", "Rotten planks, broken glass, weeds, and a leaking roof."),
    BASIC_REPAIR(2, "Basic Shelter", "Structural timbers reinforced, foundation cleared, debris gone."),
    SMALL_HOUSE(3, "Small Cottage", "Sturdy oak siding, weatherproof roof, cozy stone chimney."),
    NICE_HOUSE(4, "Charming House", "Fresh paint, glazed bay windows, flower planters, paved path."),
    LARGE_HOME(5, "Suburban Estate", "Two-story framework, grand entrance porch, white picket fence."),
    LUXURY_HOME(6, "Luxury Villa", "Limestone facade, terrace balcony, garden lanterns, side garage."),
    HUGE_MANSION(7, "Grand Manor", "Pillared portico, arched French windows, manicured lawn."),
    LITERAL_MANSION(8, "LITERAL MANSION", "Opulent palace estate with crystal pool, marble pillars & gilded domes!")
}

data class HouseRepairItem(
    val id: String,
    val name: String,
    val category: String,
    val cost: Long,
    val requiredStage: Int,
    val description: String,
    val completionBonus: Float // contributes to 100%
)

object HouseRegistry {
    val REPAIR_ITEMS = listOf(
        HouseRepairItem("weeds_rubble", "Clear Weeds & Rubble", "Grounds", 50L, 1, "Remove thorny weeds, broken pottery, and rotten wood planks.", 5.5f),
        HouseRepairItem("foundation", "Reinforce Foundation", "Structure", 120L, 1, "Level the ground with poured mortar and solid stone piers.", 5.5f),
        HouseRepairItem("support_beams", "Replace Support Beams", "Structure", 250L, 1, "Install heavy cedar framing to stabilize the tilting house.", 5.5f),
        HouseRepairItem("patch_roof", "Patch Leaking Roof", "Roofing", 450L, 2, "Cover gaps with tar paper and fresh cedar shake shingles.", 5.5f),
        HouseRepairItem("exterior_walls", "Repair Exterior Walls", "Exterior", 750L, 2, "Board up damaged sidings with clean treated pine boards.", 5.5f),
        HouseRepairItem("sturdy_door", "Install Oak Front Door", "Carpentry", 1200L, 2, "Hang a secure solid oak door with brass hinges and handle.", 5.5f),
        HouseRepairItem("clean_windows", "Glaze Window Panes", "Glasswork", 1800L, 3, "Replace broken shards with crystal-clear insulated double-pane glass.", 5.5f),
        HouseRepairItem("stone_chimney", "Build Stone Chimney", "Masonry", 2600L, 3, "Construct an authentic riverstone fireplace and chimney.", 5.5f),
        HouseRepairItem("flower_garden", "Plant Flower Garden", "Landscaping", 3800L, 3, "Plant blooming roses, hydrangeas, and sweet lavender bushes.", 5.5f),
        HouseRepairItem("picket_fence", "Erect Picket Fence", "Grounds", 5400L, 4, "Surround the yard with an elegant crisp white picket fence.", 5.5f),
        HouseRepairItem("cobblestone_path", "Pave Cobblestone Walkway", "Grounds", 7500L, 4, "Lay rounded river cobblestones from entrance to the mine shaft.", 5.5f),
        HouseRepairItem("exterior_lighting", "Install Porch Lanterns", "Electrical", 11000L, 4, "Cast warm amber welcoming light along eaves and pathways.", 5.5f),
        HouseRepairItem("second_floor", "Construct Second Floor", "Architecture", 16000L, 5, "Expand the living quarters with an expansive upper level.", 5.5f),
        HouseRepairItem("master_balcony", "Build Master Balcony", "Architecture", 24000L, 5, "Open up a panoramic scenic balcony overlooking the landscape.", 5.5f),
        HouseRepairItem("workshop_garage", "Build Crafting Workshop", "Outbuildings", 36000L, 6, "Add a specialized side wing for advanced gear and gem display.", 5.5f),
        HouseRepairItem("swimming_pool", "Install Swimming Pool", "Luxury", 55000L, 6, "Dig out and tile a shimmering turquoise heated swimming pool.", 5.5f),
        HouseRepairItem("marble_fountain", "Carve Marble Fountain", "Luxury", 85000L, 7, "A grand three-tiered flowing centerpiece in the front courtyard.", 6.0f),
        HouseRepairItem("gilded_finish", "Gilded Royal Finishes", "Estate", 140000L, 7, "24k gold leaf accents, imperial crown molding, and crystal chandeliers.", 6.0f)
    )

    fun getStageForCompletedCount(completedCount: Int): HouseStage {
        return when {
            completedCount == 0 -> HouseStage.RUINED_SHACK
            completedCount in 1..3 -> HouseStage.BASIC_REPAIR
            completedCount in 4..6 -> HouseStage.SMALL_HOUSE
            completedCount in 7..9 -> HouseStage.NICE_HOUSE
            completedCount in 10..12 -> HouseStage.LARGE_HOME
            completedCount in 13..15 -> HouseStage.LUXURY_HOME
            completedCount in 16..17 -> HouseStage.HUGE_MANSION
            else -> HouseStage.LITERAL_MANSION
        }
    }

    fun calculateProgressPercentage(completedItemIds: Set<String>): Int {
        val totalItems = REPAIR_ITEMS.size
        val count = completedItemIds.size
        return ((count.toFloat() / totalItems.toFloat()) * 100f).toInt().coerceIn(0, 100)
    }
}
