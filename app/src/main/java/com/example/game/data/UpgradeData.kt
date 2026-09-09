package com.example.game.data

data class ShovelUpgrade(
    val level: Int,
    val name: String,
    val cost: Long,
    val power: Float, // Damage dealt per dig swing
    val speedMultiplier: Float, // Dig cycle time factor (higher = faster)
    val canBreakHardRock: Boolean,
    val description: String
)

data class BackpackUpgrade(
    val level: Int,
    val capacity: Int,
    val cost: Long,
    val description: String
)

data class BootsUpgrade(
    val level: Int,
    val name: String,
    val cost: Long,
    val moveSpeedMultiplier: Float,
    val jumpImpulseMultiplier: Float,
    val climbSpeedMultiplier: Float,
    val description: String
)

data class HeadlampUpgrade(
    val level: Int,
    val name: String,
    val cost: Long,
    val lightRadius: Float, // Pixels radius of cone illumination
    val beamBrightness: Float, // 0.0 to 1.0
    val description: String
)

data class PersistentBuildingUpgrade(
    val id: String,
    val name: String,
    val maxLevel: Int,
    val baseCost: Long,
    val costMultiplier: Float,
    val iconName: String,
    val description: String,
    val effectDescription: (Int) -> String
)

object UpgradeRegistry {
    // 30 SHOVEL TIERS
    val SHOVEL_UPGRADES: List<ShovelUpgrade> = listOf(
        ShovelUpgrade(1, "Rusty Garden Trowel", 0L, 1.0f, 1.0f, false, "A rickety trowel from the old shed. Slow but works."),
        ShovelUpgrade(2, "Reinforced Iron Spade", 150L, 1.8f, 1.25f, false, "Thick iron blade that cuts through soft dirt with ease."),
        ShovelUpgrade(3, "Tempered Steel Shovel", 450L, 2.8f, 1.55f, true, "Forged high-carbon steel capable of chipping hard rocks."),
        ShovelUpgrade(4, "Titanium Trench Spade", 1200L, 4.2f, 1.9f, true, "Aerospace-grade alloy with exceptional balance and cutting edge."),
        ShovelUpgrade(5, "Gilded Miner's Shovel", 3200L, 6.5f, 2.3f, true, "Plated in resonant gold that softens stone upon impact."),
        ShovelUpgrade(6, "Diamond-Tipped Excavator", 7500L, 10.0f, 2.7f, true, "Industrial synthetic diamonds embed the spade bevel."),
        ShovelUpgrade(7, "Obsidian Core Shovel", 16000L, 15.0f, 3.2f, true, "Volcanic glass edge with razor sharpness and dark energy."),
        ShovelUpgrade(8, "Plasma Sonic Shovel", 35000L, 23.0f, 3.8f, true, "High-frequency ultrasonic vibrations pulverize tough strata."),
        ShovelUpgrade(9, "Ancient World-Breaker", 75000L, 35.0f, 4.4f, true, "Carved from titan relics buried beneath the crust."),
        ShovelUpgrade(10, "Celestial Terra-Drill", 150000L, 52.0f, 5.0f, true, "Harnesses cosmic gravity to melt through bedrock."),
        ShovelUpgrade(11, "Antimatter Chisel", 280000L, 75.0f, 5.6f, true, "Micro-annihilation pulses disintegrate dense cavern granite."),
        ShovelUpgrade(12, "Neutronium Sledge-Spade", 480000L, 105.0f, 6.2f, true, "Infused with degenerate neutron matter for staggering impact."),
        ShovelUpgrade(13, "Hyper-Thermal Magma Bore", 750000L, 145.0f, 6.8f, true, "Superheated core instantly vaporizes basalt and subterranean slate."),
        ShovelUpgrade(14, "Dark Matter Pulverizer", 1100000L, 195.0f, 7.4f, true, "Bends gravitational local vectors to shatter deep mineral veins."),
        ShovelUpgrade(15, "Singularity Displacer", 1600000L, 260.0f, 8.0f, true, "Collapses target rock into miniature temporary spatial singularities."),
        ShovelUpgrade(16, "Void-Rift Mattock", 2300000L, 340.0f, 8.6f, true, "Opens micro-void rifts that tear rock strata apart at molecular levels."),
        ShovelUpgrade(17, "Pulsar Beam Excavator", 3200000L, 435.0f, 9.2f, true, "Emits focused rhythmic gamma sweeps slicing through crystal core."),
        ShovelUpgrade(18, "Tachyon Sonic Auger", 4400000L, 550.0f, 9.8f, true, "Faster-than-light resonant vibrations dissolve tough bedrock strata."),
        ShovelUpgrade(19, "Supernova Pick-Spear", 6000000L, 685.0f, 10.4f, true, "Concentrates starburst stellar plasma at the digging tip."),
        ShovelUpgrade(20, "Chrono-Drill of Aeons", 8200000L, 840.0f, 11.0f, true, "Ages target stones billions of years in milliseconds to crumble them."),
        ShovelUpgrade(21, "Astral Quasar Breaker", 11000000L, 1020.0f, 11.5f, true, "Channeling raw galactic core energy into each swing."),
        ShovelUpgrade(22, "Cosmic String Splitter", 15000000L, 1240.0f, 12.0f, true, "Slices spatial dimensions to cleanly extract gemstones."),
        ShovelUpgrade(23, "Quantum Flux Cleaver", 20000000L, 1500.0f, 12.5f, true, "Modulates probability fields so stone dissolves into dust."),
        ShovelUpgrade(24, "Event Horizon Harvester", 27000000L, 1800.0f, 13.0f, true, "Pulls minerals out from beneath reality itself."),
        ShovelUpgrade(25, "Genesis Bedrock Piercer", 36000000L, 2150.0f, 13.5f, true, "Forged at the genesis of the tectonic realm."),
        ShovelUpgrade(26, "Omniverse Sunderer", 48000000L, 2550.0f, 14.0f, true, "Sunders subterranean planetary layers with effortless ease."),
        ShovelUpgrade(27, "Infinity Core Dredge", 65000000L, 3050.0f, 14.5f, true, "Pours unending zero-point kinetic energy through the chisel."),
        ShovelUpgrade(28, "Eternal Titan Mattock", 90000000L, 3650.0f, 15.0f, true, "Wielded by primeval titans who carved mountains and oceans."),
        ShovelUpgrade(29, "Godforge Terra-Obliterator", 125000000L, 4400.0f, 15.5f, true, "Alchemical perfection that liquefies bedrock in an instant."),
        ShovelUpgrade(30, "Transcendent Creation Catalyst", 180000000L, 5500.0f, 16.0f, true, "Transcends earthly physics to strip deep strata clean of all riches.")
    )

    // 30 BACKPACK TIERS
    val BACKPACK_UPGRADES: List<BackpackUpgrade> = listOf(
        BackpackUpgrade(1, 3, 0L, "Starter worn burlap pouch."),
        BackpackUpgrade(2, 5, 100L, "Stitched canvas knapsack."),
        BackpackUpgrade(3, 8, 300L, "Reinforced leather rucksack."),
        BackpackUpgrade(4, 12, 750L, "Multi-pocket miner's pack."),
        BackpackUpgrade(5, 16, 1800L, "Expedition heavy duffel."),
        BackpackUpgrade(6, 22, 4200L, "Alloy-frame mountain pack."),
        BackpackUpgrade(7, 28, 9500L, "Expanded tactical carrier."),
        BackpackUpgrade(8, 36, 20000L, "Reinforced modular chest-pack."),
        BackpackUpgrade(9, 45, 42000L, "Sub-dimension void sack."),
        BackpackUpgrade(10, 58, 85000L, "Quantum spatial compression pack."),
        BackpackUpgrade(11, 75, 160000L, "Gravitational well pouch that fits an entire mine cart."),
        BackpackUpgrade(12, 95, 290000L, "Hyper-spatial pocket weaving non-Euclidean storage folds."),
        BackpackUpgrade(13, 120, 500000L, "Dark energy haul siphon compressing mineral volume."),
        BackpackUpgrade(14, 150, 850000L, "Tesseract dimensional duffel holding dozens of tons."),
        BackpackUpgrade(15, 190, 1400000L, "Nebula vacuum satchel with frictionless internal stasis."),
        BackpackUpgrade(16, 240, 2200000L, "Micro-blackhole collector neutralizing ore mass safely."),
        BackpackUpgrade(17, 300, 3400000L, "Event horizon storage cask with infinite crystal shelving."),
        BackpackUpgrade(18, 375, 5000000L, "Quantum foam reservoir expanding proportionally with haul."),
        BackpackUpgrade(19, 465, 7200000L, "Cosmic dust archive with self-categorizing gem matrix."),
        BackpackUpgrade(20, 575, 10000000L, "Warp-field containment rig suspending deep magma crystals."),
        BackpackUpgrade(21, 700, 14000000L, "Sub-atomic matrix vault dematerializing ores on entry."),
        BackpackUpgrade(22, 850, 19500000L, "Celestial super-cluster sack weaving stellar fabric."),
        BackpackUpgrade(23, 1025, 27000000L, "Continuum phase container bridging multiple dimensional folds."),
        BackpackUpgrade(24, 1225, 37000000L, "Dyson sphere pocket cell storing miniature planetary wealth."),
        BackpackUpgrade(25, 1450, 50000000L, "Void horizon repository holding colossal mining hauls."),
        BackpackUpgrade(26, 1700, 68000000L, "Universal matter compactor capable of swallowing entire mountains."),
        BackpackUpgrade(27, 2000, 92000000L, "Multiverse cargo shifter folding space across infinite planes."),
        BackpackUpgrade(28, 2350, 125000000L, "Infinity spatial vault with limitless quantum compartments."),
        BackpackUpgrade(29, 2750, 170000000L, "God-tier relic coffer carved from the heart of a fallen star."),
        BackpackUpgrade(30, 3500, 240000000L, "Omega Transcendent Infinity Pouch: haul an endless mountain of gems!")
    )

    // 20 MINING BOOTS & AGILITY GEAR TIERS
    val BOOTS_UPGRADES: List<BootsUpgrade> = listOf(
        BootsUpgrade(1, "Worn Work Boots", 0L, 1.0f, 1.0f, 1.0f, "Standard leather work boots. Good enough to start."),
        BootsUpgrade(2, "Steel-Toe Miner Boots", 200L, 1.08f, 1.05f, 1.10f, "Reinforced steel caps that protect toes and improve grip."),
        BootsUpgrade(3, "Spiked Climbing Cleats", 550L, 1.16f, 1.10f, 1.25f, "Carbide studs dig firmly into shaft walls and wooden rungs."),
        BootsUpgrade(4, "Spring-Loaded Traversal Boots", 1400L, 1.24f, 1.18f, 1.40f, "Internal coiled springs grant an extra spring to every step."),
        BootsUpgrade(5, "Heavy Cavern Striders", 3500L, 1.32f, 1.25f, 1.55f, "Shock-absorbing vulcanized tread for traversing steep mine shafts."),
        BootsUpgrade(6, "Pneumatic Piston Greaves", 8000L, 1.40f, 1.32f, 1.70f, "Compressed air canisters assist vertical leaps and wall dashes."),
        BootsUpgrade(7, "Hydraulic Jump Treads", 18000L, 1.48f, 1.40f, 1.85f, "High-pressure fluid actuators double ascent and climb speed."),
        BootsUpgrade(8, "Mag-Lev Hover Sabatons", 38000L, 1.56f, 1.48f, 2.00f, "Magnetic levitation coils reduce terrain friction substantially."),
        BootsUpgrade(9, "Sonic Velocity Soles", 75000L, 1.64f, 1.56f, 2.15f, "Ultrasonic pulse pads propel the miner smoothly across cavern floors."),
        BootsUpgrade(10, "Geothermal Heat Treads", 150000L, 1.72f, 1.64f, 2.30f, "Thermal dissipation plates withstand boiling magma floor friction."),
        BootsUpgrade(11, "Kinetic Energy Recoil Boots", 280000L, 1.80f, 1.72f, 2.45f, "Harvests impact force to power explosive vertical springboards."),
        BootsUpgrade(12, "Titanium Strut Exo-Boots", 500000L, 1.88f, 1.80f, 2.60f, "Motorized exoskeleton struts eliminate all climbing fatigue."),
        BootsUpgrade(13, "Jet-Assisted Strata Jumpers", 900000L, 1.96f, 1.88f, 2.75f, "Miniature thrusters boost ladder ascension and wall climbs."),
        BootsUpgrade(14, "Anti-Grav Leap Cushions", 1500000L, 2.04f, 1.96f, 2.90f, "Inverts local gravitational pull during jumps and leaps."),
        BootsUpgrade(15, "Warp-Pulse Agility Greaves", 2500000L, 2.12f, 2.05f, 3.05f, "Micro-teleport pulses allow blazing fast sprint speeds."),
        BootsUpgrade(16, "Chrono-Dash Striders", 4000000L, 2.20f, 2.15f, 3.20f, "Accelerates the wearer's personal timeframe in deep caves."),
        BootsUpgrade(17, "Solar Flare Rocket Boots", 6500000L, 2.28f, 2.25f, 3.35f, "Harnesses ionized rocket exhaust for soaring cavern jumps."),
        BootsUpgrade(18, "Quantum Leap Sabatons", 10000000L, 2.36f, 2.35f, 3.50f, "Effortless quantum tunneling through vertical subterranean air."),
        BootsUpgrade(19, "Star-Walker Cosmic Treads", 16000000L, 2.45f, 2.45f, 3.70f, "Forged in zero gravity; climb walls and ladders at blinding speed."),
        BootsUpgrade(20, "Transcendent Light-Speed Greaves", 25000000L, 2.60f, 2.60f, 4.00f, "Ultimate mobility: glide, jump, and scale deep caverns like a streak of light!")
    )

    // 15 HEADLAMP & SENSOR LAMP TIERS
    val HEADLAMP_UPGRADES: List<HeadlampUpgrade> = listOf(
        HeadlampUpgrade(1, "Flickering Candle Cap", 0L, 85f, 0.45f, "A simple wax candle affixed to a miner's cap. Dim but better than pitch black."),
        HeadlampUpgrade(2, "Brass Carbide Mining Lamp", 180L, 115f, 0.55f, "Acetylene gas burner producing a bright warm flare through dirt shafts."),
        HeadlampUpgrade(3, "Halogen Hardhat Beam", 500L, 145f, 0.65f, "Reliable halogen bulb with battery pack cutting through deep mist."),
        HeadlampUpgrade(4, "High-Lumen LED Strobe", 1200L, 180f, 0.75f, "Modern ultra-efficient LEDs illuminating wide rock faces with clear white light."),
        HeadlampUpgrade(5, "Xenon Focused Spotlight", 2800L, 215f, 0.82f, "High-intensity discharge xenon arc that reveals crystal sparkle at long ranges."),
        HeadlampUpgrade(6, "Krypton Cavern Searchlight", 6500L, 250f, 0.88f, "Penetrating krypton gas filament lighting up entire cavern chambers."),
        HeadlampUpgrade(7, "Wide-Angle Floodlight Rig", 15000L, 290f, 0.92f, "Dual curved parabolic reflectors illuminating both walls simultaneously."),
        HeadlampUpgrade(8, "Ultraviolet Mineral Scanner", 32000L, 335f, 0.95f, "UV wavelengths make buried gemstone veins fluoresce with vivid neon luminescence."),
        HeadlampUpgrade(9, "Infrared Heat-Seek Lens", 68000L, 380f, 0.97f, "Thermal imaging highlights molten crystal veins through solid rock."),
        HeadlampUpgrade(10, "Laser Scanning Monocle", 140000L, 430f, 1.00f, "LiDAR pulse laser maps every crack and gemstone protrusion in the dark."),
        HeadlampUpgrade(11, "Bioluminescent Crystal Torch", 280000L, 485f, 1.00f, "Glows with perpetual organic luminescence that never dims."),
        HeadlampUpgrade(12, "Photon Burst Headgear", 550000L, 545f, 1.00f, "Emits high-density photon cascades banishing all cavern gloom."),
        HeadlampUpgrade(13, "Gamma Cavern Illuminator", 1100000L, 610f, 1.00f, "Penetrating gamma radiation lights up subterranean depths like daylight."),
        HeadlampUpgrade(14, "Super-Radiant Sun Crown", 2200000L, 680f, 1.00f, "A microscopic captive sun floating above your brow casting warm celestial rays."),
        HeadlampUpgrade(15, "Cosmic Star-Lantern", 4500000L, 780f, 1.00f, "Illuminates entire subterranean biomes with dazzling starry brilliance!")
    )

    // 9 ESTATE & MINING TECHNOLOGIES (10 LEVELS EACH = 90 UPGRADES)
    val BUILDING_UPGRADES: List<PersistentBuildingUpgrade> = listOf(
        PersistentBuildingUpgrade(
            id = "gem_sifter",
            name = "Mining Sifter Table",
            maxLevel = 10,
            baseCost = 350L,
            costMultiplier = 2.2f,
            iconName = "filter",
            description = "Cleans and polishes mined gems for substantially higher dealer payouts.",
            effectDescription = { level -> "+${level * 6}% increased sell value on all gems" }
        ),
        PersistentBuildingUpgrade(
            id = "tool_grindstone",
            name = "Hardened Grindstone",
            maxLevel = 10,
            baseCost = 500L,
            costMultiplier = 2.3f,
            iconName = "build",
            description = "Hones shovel cutting edges to dramatically increase dig strike damage.",
            effectDescription = { level -> "+${level * 12}% shovel digging power" }
        ),
        PersistentBuildingUpgrade(
            id = "rental_cabin",
            name = "Tourist Rental Cottage",
            maxLevel = 10,
            baseCost = 1200L,
            costMultiplier = 2.5f,
            iconName = "home",
            description = "Attracts visiting geologists and tourists who pay rent while you mine.",
            effectDescription = { level -> "+$${level * 30} passive revenue every 10 seconds" }
        ),
        PersistentBuildingUpgrade(
            id = "radar_beacon",
            name = "Seismic Sonar Pulse",
            maxLevel = 10,
            baseCost = 2000L,
            costMultiplier = 2.6f,
            iconName = "radar",
            description = "Highlights deep crystal veins with radiant sonar ripples through solid rock.",
            effectDescription = { level -> "Reveals hidden gems up to ${level + 1} blocks further away" }
        ),
        PersistentBuildingUpgrade(
            id = "magma_smelter",
            name = "Geothermal Earth Smelter",
            maxLevel = 10,
            baseCost = 3500L,
            costMultiplier = 2.7f,
            iconName = "whatshot",
            description = "Instantly smelts mined rock into pure gold nuggets on every single strike.",
            effectDescription = { level -> "+$${level * 15} instant cash for EVERY block destroyed" }
        ),
        PersistentBuildingUpgrade(
            id = "dynamite_quarry",
            name = "Nitroglycerin Blast Workshop",
            maxLevel = 10,
            baseCost = 6000L,
            costMultiplier = 2.8f,
            iconName = "flare",
            description = "Installs blast charges that trigger secondary chain explosions on dig hits.",
            effectDescription = { level -> "+${level * 5}% chance per dig to trigger an explosive blast" }
        ),
        PersistentBuildingUpgrade(
            id = "prospector_drone",
            name = "Autonomous Prospector Drone",
            maxLevel = 10,
            baseCost = 10000L,
            costMultiplier = 2.9f,
            iconName = "flight",
            description = "Dispatches a robotic drone that gathers loose mineral nuggets automatically.",
            effectDescription = { level -> "Drone gathers +$${level * 45} bonus haul every 15 seconds" }
        ),
        PersistentBuildingUpgrade(
            id = "fossil_museum",
            name = "Cavern Natural History Museum",
            maxLevel = 10,
            baseCost = 15000L,
            costMultiplier = 3.0f,
            iconName = "museum",
            description = "Exhibits excavated fossils for admission fees and doubles fossil payouts.",
            effectDescription = { level -> "+${level * 25}% fossil value & +$${level * 20} tourist tickets" }
        ),
        PersistentBuildingUpgrade(
            id = "luck_shrine",
            name = "Ancient Shrine of Fortune",
            maxLevel = 10,
            baseCost = 25000L,
            costMultiplier = 3.2f,
            iconName = "auto_awesome",
            description = "Sacred shrine imbuing pickaxes with celestial fortune and gem rarity.",
            effectDescription = { level -> "+${level * 15}% spawn rate for Rare, Epic, & Legendary gems" }
        )
    )

    fun getShovelForLevel(level: Int): ShovelUpgrade {
        val index = (level - 1).coerceIn(0, SHOVEL_UPGRADES.size - 1)
        return SHOVEL_UPGRADES[index]
    }

    fun getBackpackForLevel(level: Int): BackpackUpgrade {
        val index = (level - 1).coerceIn(0, BACKPACK_UPGRADES.size - 1)
        return BACKPACK_UPGRADES[index]
    }

    fun getBootsForLevel(level: Int): BootsUpgrade {
        val index = (level - 1).coerceIn(0, BOOTS_UPGRADES.size - 1)
        return BOOTS_UPGRADES[index]
    }

    fun getHeadlampForLevel(level: Int): HeadlampUpgrade {
        val index = (level - 1).coerceIn(0, HEADLAMP_UPGRADES.size - 1)
        return HEADLAMP_UPGRADES[index]
    }

    fun getBuildingUpgrade(id: String): PersistentBuildingUpgrade? {
        return BUILDING_UPGRADES.firstOrNull { it.id == id }
    }
}
