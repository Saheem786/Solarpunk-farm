package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Solarpunk Crop catalog with unique growth, energy, and climate properties.
 */
enum class CropType(
    val displayName: String,
    val description: String,
    val growthDurationSec: Float,
    val waterNeed: Float,
    val sunNeed: Float,
    val harvestYield: Int,
    val baseSellPrice: Int,
    val seedCost: Int,
    val energyBonusKwh: Float,
    val primaryColor: Long,
    val secondaryColor: Long
) {
    WHEAT(
        displayName = "Golden Wheat",
        description = "Deep-rooted heritage grain enriching soil and yielding fresh flour.",
        growthDurationSec = 16f,
        waterNeed = 0.3f,
        sunNeed = 0.7f,
        harvestYield = 3,
        baseSellPrice = 30,
        seedCost = 5,
        energyBonusKwh = 1.0f,
        primaryColor = 0xFFFFE082,
        secondaryColor = 0xFF8D6E63
    ),
    CORN(
        displayName = "Sweet Corn",
        description = "Tall architectural stalks with golden sun-ripened corn cobs.",
        growthDurationSec = 22f,
        waterNeed = 0.5f,
        sunNeed = 0.85f,
        harvestYield = 2,
        baseSellPrice = 50,
        seedCost = 10,
        energyBonusKwh = 2.0f,
        primaryColor = 0xFFFFCA28,
        secondaryColor = 0xFF388E3C
    ),
    TOMATO(
        displayName = "Ruby Tomato",
        description = "Juicy sun-ripened red tomatoes on supportive vertical trellis vines.",
        growthDurationSec = 18f,
        waterNeed = 0.6f,
        sunNeed = 0.75f,
        harvestYield = 4,
        baseSellPrice = 40,
        seedCost = 8,
        energyBonusKwh = 1.5f,
        primaryColor = 0xFFFF5252,
        secondaryColor = 0xFF2E7D32
    ),
    CARROT(
        displayName = "Crisp Carrot",
        description = "Nutritious sweet orange root vegetable thriving in compost-rich soil.",
        growthDurationSec = 14f,
        waterNeed = 0.4f,
        sunNeed = 0.6f,
        harvestYield = 3,
        baseSellPrice = 25,
        seedCost = 4,
        energyBonusKwh = 1.2f,
        primaryColor = 0xFFFF9800,
        secondaryColor = 0xFF4CAF50
    ),
    HERBS(
        displayName = "Aromatic Herbs",
        description = "Fragrant medicinal and culinary rosemary, basil, and herbal tea leaves.",
        growthDurationSec = 10f,
        waterNeed = 0.35f,
        sunNeed = 0.5f,
        harvestYield = 5,
        baseSellPrice = 60,
        seedCost = 15,
        energyBonusKwh = 0.8f,
        primaryColor = 0xFF81C784,
        secondaryColor = 0xFF00E676
    ),
    SOLAR_SUNFLOWER(
        displayName = "Solar Sunflower",
        description = "Photovoltaic flora that generates clean energy while blooming.",
        growthDurationSec = 15f,
        waterNeed = 0.4f,
        sunNeed = 0.8f,
        harvestYield = 2,
        baseSellPrice = 45,
        seedCost = 15,
        energyBonusKwh = 1.5f,
        primaryColor = 0xFFFFD54F,
        secondaryColor = 0xFFFFA000
    ),
    BIOLUMINESCENT_MUSHROOM(
        displayName = "Biolum Spores",
        description = "Glows at night, thrives in misty weather without direct sunlight.",
        growthDurationSec = 22f,
        waterNeed = 0.8f,
        sunNeed = 0.1f,
        harvestYield = 3,
        baseSellPrice = 60,
        seedCost = 25,
        energyBonusKwh = 0.5f,
        primaryColor = 0xFF00E5FF,
        secondaryColor = 0xFF7C4DFF
    ),
    TERRACED_WHEAT(
        displayName = "Golden Grain",
        description = "Heritage wheat enriching regenerative soil.",
        growthDurationSec = 16f,
        waterNeed = 0.3f,
        sunNeed = 0.7f,
        harvestYield = 3,
        baseSellPrice = 35,
        seedCost = 12,
        energyBonusKwh = 1.0f,
        primaryColor = 0xFFFFE082,
        secondaryColor = 0xFF8D6E63
    ),
    SOLAR_CORN(
        displayName = "Prismatic Corn",
        description = "Tall architectural stalks with iridescent holographic kernels.",
        growthDurationSec = 22f,
        waterNeed = 0.5f,
        sunNeed = 0.85f,
        harvestYield = 2,
        baseSellPrice = 55,
        seedCost = 18,
        energyBonusKwh = 2.0f,
        primaryColor = 0xFFFFCA28,
        secondaryColor = 0xFF388E3C
    ),
    CYBER_BERRIES(
        displayName = "Cyber Berries",
        description = "Antioxidant-dense berries with bio-electric pulsing stems.",
        growthDurationSec = 25f,
        waterNeed = 0.5f,
        sunNeed = 0.6f,
        harvestYield = 5,
        baseSellPrice = 85,
        seedCost = 30,
        energyBonusKwh = 1.2f,
        primaryColor = 0xFFE040FB,
        secondaryColor = 0xFF536DFE
    ),
    SKY_SPIRULINA(
        displayName = "Sky Spirulina",
        description = "Algae suspension harvested in vertical hydroponic columns.",
        growthDurationSec = 12f,
        waterNeed = 0.9f,
        sunNeed = 0.5f,
        harvestYield = 4,
        baseSellPrice = 35,
        seedCost = 12,
        energyBonusKwh = 0.8f,
        primaryColor = 0xFF00E676,
        secondaryColor = 0xFF00B0FF
    ),
    HYDROPONIC_MELON(
        displayName = "Hydro Melon",
        description = "Sweet crystalline melons grown in mineral nutrient film.",
        growthDurationSec = 30f,
        waterNeed = 0.7f,
        sunNeed = 0.6f,
        harvestYield = 1,
        baseSellPrice = 120,
        seedCost = 45,
        energyBonusKwh = 2.0f,
        primaryColor = 0xFFFF8A80,
        secondaryColor = 0xFF69F0AE
    ),
    NITRO_BEANS(
        displayName = "Bio-Nitro Beans",
        description = "Fixes nitrogen naturally into the soil, boosting neighboring crops.",
        growthDurationSec = 16f,
        waterNeed = 0.5f,
        sunNeed = 0.5f,
        harvestYield = 3,
        baseSellPrice = 40,
        seedCost = 15,
        energyBonusKwh = 0.6f,
        primaryColor = 0xFF81C784,
        secondaryColor = 0xFF388E3C
    );

    val growthDays: Int
        get() = when (this) {
            WHEAT -> 3
            CORN -> 4
            TOMATO -> 3
            CARROT -> 2
            HERBS -> 5
            else -> 3
        }
}

/**
 * Buildings constructible by player in Build Mode.
 */
enum class BuildableType(
    val displayName: String,
    val description: String,
    val costCoins: Int,
    val requiredMaterialId: String,
    val requiredMaterialQty: Int,
    val materialName: String,
    val gameplayEffect: String,
    val previewColor: Long
) {
    CABIN(
        displayName = "Wooden Cabin",
        description = "Small wooden house with slanted roof. Sleep here at night to fully restore stamina.",
        costCoins = 200,
        requiredMaterialId = "material_bio_timber",
        requiredMaterialQty = 0,
        materialName = "Bio-Timber",
        gameplayEffect = "Sleeping spot — restores stamina fully at night",
        previewColor = 0xFF8D6E63
    ),
    GREENHOUSE(
        displayName = "Greenhouse",
        description = "Glass panels with wooden frame, green tint. Nearby crops (within 5 units) grow 1.3x faster.",
        costCoins = 500,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "Crops within 5 units grow 1.3x faster",
        previewColor = 0xFF80DEEA
    ),
    SOLAR_PANEL(
        displayName = "Solar Panel",
        description = "Blue-black rectangular panel on small stand. Generates 10 kWh/day during sunlight.",
        costCoins = 300,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "Generates 10 kWh/day during sunlight",
        previewColor = 0xFFFFD54F
    ),
    WINDMILL(
        displayName = "Windmill",
        description = "Tall wooden tower with rotating blades. Generates 15 kWh/day 24/7 (30% less at night).",
        costCoins = 400,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Generates 15 kWh/day 24/7 (30% less at night)",
        previewColor = 0xFF80D8FF
    ),
    STORAGE(
        displayName = "Storage Shed",
        description = "Small wooden shed with door. Increases inventory capacity by +50 slots.",
        costCoins = 250,
        requiredMaterialId = "material_bio_polymer",
        requiredMaterialQty = 0,
        materialName = "Bio-Polymer",
        gameplayEffect = "+50 inventory space",
        previewColor = 0xFF00E5FF
    ),
    WELL(
        displayName = "Water Well",
        description = "Stone circle with wooden roof and bucket. Infinite water source — player can drink here anytime.",
        costCoins = 150,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Infinite water source — drink anytime",
        previewColor = 0xFF80CBC4
    ),
    FENCE(
        displayName = "Fence Section",
        description = "Wooden fence post and rails. Decorative boundary marker to block paths.",
        costCoins = 20,
        requiredMaterialId = "material_bio_timber",
        requiredMaterialQty = 0,
        materialName = "Bio-Timber",
        gameplayEffect = "Decorative boundary section",
        previewColor = 0xFFB0BEC5
    ),
    COMPOST_BIN(
        displayName = "Compost Bin",
        description = "Wooden box with dark organic material. Converts 3 harvested crops into 1 seed of any type.",
        costCoins = 100,
        requiredMaterialId = "material_bio_polymer",
        requiredMaterialQty = 0,
        materialName = "Bio-Polymer",
        gameplayEffect = "Converts 3 harvested crops into 1 random seed",
        previewColor = 0xFFA1887F
    ),
    RAIN_BARREL(
        displayName = "Rain Barrel",
        description = "Catches rainwater during showers (20 units/hr, holds 100). Safe to drink and connects to irrigation.",
        costCoins = 80,
        requiredMaterialId = "material_bio_timber",
        requiredMaterialQty = 0,
        materialName = "Bio-Timber",
        gameplayEffect = "Fills 20/hr in rain (holds 100). Clean water.",
        previewColor = 0xFF5D4037
    ),
    WATER_FILTER(
        displayName = "Water Filter",
        description = "Gravity sand & activated carbon filter. Automatically converts raw water into clean water (1 unit/3 min).",
        costCoins = 150,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Converts raw water to clean water automatically",
        previewColor = 0xFF00ACC1
    ),
    WATER_PURIFIER(
        displayName = "Water Purifier",
        description = "High-tech solar-powered UV purifier. Consumes 2 kWh/hr to purify 5 units of raw water per minute.",
        costCoins = 400,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "Requires 2 kWh/hr. Purifies 5 units/min",
        previewColor = 0xFF00E5FF
    ),
    IRRIGATION_PIPE(
        displayName = "Irrigation Pipe",
        description = "Copper pipe segment (1x1 unit). Transports water from wells, barrels, or purifiers to sprinkler nodes.",
        costCoins = 80,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Transports water to irrigation nodes",
        previewColor = 0xFFB87333
    ),
    IRRIGATION_NODE(
        displayName = "Irrigation Node",
        description = "Sprinkler head attached to pipe network. Automatically waters all crops within 3 units at 6:00 AM.",
        costCoins = 40,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Auto-waters crops within 3 units at 6 AM (5 water/day)",
        previewColor = 0xFF00B0FF
    ),
    WATER_STORAGE_SHED(
        displayName = "Water Storage Shed",
        description = "High-capacity cistern holding up to 100 units of clean water. Fills from connected filters or purifiers.",
        costCoins = 250,
        requiredMaterialId = "material_bio_polymer",
        requiredMaterialQty = 0,
        materialName = "Bio-Polymer",
        gameplayEffect = "Stores up to 100 units of water",
        previewColor = 0xFF0288D1
    ),
    HYDRO_GENERATOR(
        displayName = "Small Hydro Generator",
        description = "Water wheel harnessing flowing river or stream currents. Generates 20 kWh/day 24/7 continuous clean power.",
        costCoins = 800,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Generates 20 kWh/day 24/7 (Must be near river/stream)",
        previewColor = 0xFF00E5FF
    ),
    ADVANCED_SOLAR(
        displayName = "Advanced Solar Array",
        description = "4x4 motorized solar array with sun tracking. Rotates to face the sun throughout the day (+20% efficiency, 30 kWh/day).",
        costCoins = 1200,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "Generates 30 kWh/day (Daylight only, Sun-tracking)",
        previewColor = 0xFFFFD54F
    ),
    BIOGAS_GENERATOR(
        displayName = "Biogas Generator",
        description = "Dome tank converting farm compost and livestock manure into 12 kWh/day 24/7 base-load power.",
        costCoins = 600,
        requiredMaterialId = "material_bio_polymer",
        requiredMaterialQty = 0,
        materialName = "Bio-Polymer",
        gameplayEffect = "Generates 12 kWh/day 24/7 (Consumes 1 compost/day)",
        previewColor = 0xFF81C784
    ),
    GEOTHERMAL_VENT(
        displayName = "Geothermal Vent",
        description = "Deep thermal steam turbine providing heavy industrial 40 kWh/day continuous 24/7 power.",
        costCoins = 2000,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Generates 40 kWh/day 24/7 continuous base-load",
        previewColor = 0xFFFF7043
    ),
    BASIC_BATTERY(
        displayName = "Basic Battery",
        description = "2x2 compact solid-state battery unit. Holds 50 kWh capacity with 5 kWh/hour charge rate.",
        costCoins = 200,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "+50 kWh storage capacity (Charge rate: 5 kWh/hr)",
        previewColor = 0xFF29B6F6
    ),
    ADVANCED_BATTERY(
        displayName = "Advanced Battery",
        description = "3x3 multi-cell battery bank with status LEDs. Holds 150 kWh capacity with 15 kWh/hour charge rate.",
        costCoins = 500,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "+150 kWh storage capacity (Charge rate: 15 kWh/hr)",
        previewColor = 0xFF00B0FF
    ),
    BATTERY_BANK(
        displayName = "Industrial Battery Bank",
        description = "5x5 high-density grid storage bank with cooling fans. Holds 500 kWh capacity with 50 kWh/hour charge rate.",
        costCoins = 1200,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "+500 kWh storage capacity (Charge rate: 50 kWh/hr)",
        previewColor = 0xFF00E5FF
    ),
    POWER_POLE(
        displayName = "Power Pole",
        description = "Wooden transmission pole with ceramic high-voltage insulators. Extends electrical grid reach by 15 units.",
        costCoins = 30,
        requiredMaterialId = "material_bio_timber",
        requiredMaterialQty = 0,
        materialName = "Bio-Timber",
        gameplayEffect = "Extends power grid by 15 units",
        previewColor = 0xFF8D6E63
    ),
    NPC_CABIN(
        displayName = "NPC Cabin",
        description = "Cozy wooden cottage with 2 warm beds. Provides safe shelter and high morale for 2 settlement survivors.",
        costCoins = 300,
        requiredMaterialId = "material_bio_timber",
        requiredMaterialQty = 0,
        materialName = "Bio-Timber",
        gameplayEffect = "Housing for 2 NPCs (+High Morale & Recovery)",
        previewColor = 0xFF8D6E63
    ),
    BUNKHOUSE(
        displayName = "Settlement Bunkhouse",
        description = "Spacious communal lodge fitted with 6 comfortable bunks. Large-scale housing for expanding communities.",
        costCoins = 800,
        requiredMaterialId = "material_bio_timber",
        requiredMaterialQty = 0,
        materialName = "Bio-Timber",
        gameplayEffect = "Housing for 6 NPCs (Communal living shelter)",
        previewColor = 0xFF795548
    ),
    KITCHEN(
        displayName = "Community Kitchen",
        description = "Solar-powered communal galley where survivors gather for meals and player can cook nutritious feasts.",
        costCoins = 500,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Survivors eat here. Requires 1 kWh/hr.",
        previewColor = 0xFFFFB74D
    ),
    MEDIC_STATION(
        displayName = "Sanctuary Clinic",
        description = "Sterile dispensary and triage station. Stationed Medic heals player and produces herbal remedies.",
        costCoins = 600,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "Medic heals & cures sickness. Requires 1 kWh/hr.",
        previewColor = 0xFFE57373
    ),
    WORKSHOP(
        displayName = "Tech Workshop",
        description = "Heavy-duty engineering bench with CNC fabricator. Engineers stationed here boost crafting speed by 30%.",
        costCoins = 450,
        requiredMaterialId = "material_eco_alloy",
        requiredMaterialQty = 0,
        materialName = "Eco-Alloy",
        gameplayEffect = "Crafting speed +30% with Engineer. Requires 3 kWh/hr.",
        previewColor = 0xFF90A4AE
    ),
    RESEARCH_LAB(
        displayName = "Bio-Tech Research Lab",
        description = "Advanced solarpunk laboratory with cleanroom hood. Researchers stationed here produce +2 RP/day.",
        costCoins = 800,
        requiredMaterialId = "material_solar_glass",
        requiredMaterialQty = 0,
        materialName = "Solar Glass",
        gameplayEffect = "Generates +2 RP/day. Requires 2 kWh/hr.",
        previewColor = 0xFF80DEEA
    );

    val size: Float
        get() = when (this) {
            CABIN -> 4.0f
            GREENHOUSE -> 5.0f
            SOLAR_PANEL -> 2.0f
            WINDMILL -> 3.0f
            STORAGE -> 3.0f
            WELL -> 2.0f
            FENCE -> 1.0f
            COMPOST_BIN -> 2.0f
            RAIN_BARREL -> 1.4f
            WATER_FILTER -> 1.5f
            WATER_PURIFIER -> 2.2f
            IRRIGATION_PIPE -> 1.0f
            IRRIGATION_NODE -> 1.0f
            WATER_STORAGE_SHED -> 3.2f
            HYDRO_GENERATOR -> 3.0f
            ADVANCED_SOLAR -> 4.0f
            BIOGAS_GENERATOR -> 3.0f
            GEOTHERMAL_VENT -> 4.0f
            BASIC_BATTERY -> 2.0f
            ADVANCED_BATTERY -> 3.0f
            BATTERY_BANK -> 5.0f
            POWER_POLE -> 1.0f
            NPC_CABIN -> 3.0f
            BUNKHOUSE -> 5.0f
            KITCHEN -> 4.0f
            MEDIC_STATION -> 3.0f
            WORKSHOP -> 3.0f
            RESEARCH_LAB -> 4.0f
        }
}

/**
 * Growth cycle stage of a crop.
 */
enum class CropStage {
    EMPTY,
    SEEDLING,
    SPROUT,
    VEGETATIVE,
    FLOWERING,
    HARVEST_READY,
    WITHERED
}

/**
 * Types of farming plots.
 */
enum class PlotType(val displayName: String, val maxMoistureRetention: Float) {
    PERMACULTURE_BED("Permaculture Bed", 1.0f),
    HYDROPONIC_TOWER("Hydroponic Tower", 1.5f),
    BIO_DOME("Glass Bio-Dome", 2.0f),
    SOLAR_SOIL_PATCH("Solar Ground Patch", 0.8f)
}

/**
 * Renewable Clean Energy generator/storage nodes.
 */
enum class EnergyNodeType(
    val displayName: String,
    val description: String,
    val baseOutputKwhPerSec: Float,
    val baseStorageCapacityKwh: Float,
    val buildCostCoins: Int,
    val iconColor: Long
) {
    PHOTOVOLTAIC_ARRAY(
        displayName = "Solar Glass Array",
        description = "Converts sunlight directly into electrical charge.",
        baseOutputKwhPerSec = 3.5f,
        baseStorageCapacityKwh = 0f,
        buildCostCoins = 150,
        iconColor = 0xFFFFD54F
    ),
    VERTICAL_WIND_TURBINE(
        displayName = "Aero-Spire Turbine",
        description = "Silent helical turbine harvesting wind and breeze.",
        baseOutputKwhPerSec = 2.8f,
        baseStorageCapacityKwh = 0f,
        buildCostCoins = 200,
        iconColor = 0xFF80D8FF
    ),
    BIOGAS_DIGESTER(
        displayName = "Biomass Digester",
        description = "Converts organic farm compost into steady base-load power.",
        baseOutputKwhPerSec = 2.0f,
        baseStorageCapacityKwh = 0f,
        buildCostCoins = 180,
        iconColor = 0xFF81C784
    ),
    BATTERY_STORAGE_BANK(
        displayName = "Solid-State Battery",
        description = "Stores surplus energy generated during peak sunshine.",
        baseOutputKwhPerSec = 0f,
        baseStorageCapacityKwh = 50.0f,
        buildCostCoins = 220,
        iconColor = 0xFF00E5FF
    )
}

/**
 * Livestock species in the regenerative sanctuary.
 */
enum class LivestockType(
    val displayName: String,
    val description: String,
    val productProduced: String,
    val produceIntervalSec: Float,
    val productSellPrice: Int,
    val purchaseCostCoins: Int,
    val tintColor: Long
) {
    SOLAR_SHEEP(
        displayName = "Solar Sheep",
        description = "Silky fleece woven with micro-photovoltaic fibers.",
        productProduced = "Solar Wool",
        produceIntervalSec = 20f,
        productSellPrice = 75,
        purchaseCostCoins = 250,
        tintColor = 0xFFFFF9C4
    ),
    CYBER_BOVINE(
        displayName = "Meadow Cow",
        description = "Regenerative pasture grazer producing organic elixir & compost.",
        productProduced = "Organic Bio-Milk",
        produceIntervalSec = 25f,
        productSellPrice = 110,
        purchaseCostCoins = 380,
        tintColor = 0xFFE0E0E0
    ),
    CHICKEN(
        displayName = "Chicken",
        description = "A friendly low-poly farm chicken laying organic eggs.",
        productProduced = "Egg",
        produceIntervalSec = 30f,
        productSellPrice = 25,
        purchaseCostCoins = 80,
        tintColor = 0xFFFFF8E1
    ),
    ROBO_BEE_POLLINATOR(
        displayName = "Bio-Bees Colony",
        description = "Dramatically enhances surrounding crop growth and yields.",
        productProduced = "Solar Honey",
        produceIntervalSec = 15f,
        productSellPrice = 60,
        purchaseCostCoins = 180,
        tintColor = 0xFFFFD700
    ),
    MEADOW_ALPACA(
        displayName = "Cloud Alpaca",
        description = "Gentle companion producing ultra-soft thermal fiber.",
        productProduced = "Alpaca Yarn",
        produceIntervalSec = 30f,
        productSellPrice = 140,
        purchaseCostCoins = 450,
        tintColor = 0xFFD7CCC8
    )
}

/**
 * 24-Hour Day/Night Celestial Phases.
 */
enum class TimeOfDayPhase(
    val displayName: String,
    val startHour: Float,
    val endHour: Float,
    val solarIntensity: Float,
    val ambientBrightness: Float,
    val skyHexTop: Long,
    val skyHexHorizon: Long
) {
    MIDNIGHT("Midnight", 0.0f, 4.5f, 0.0f, 0.25f, 0xFF080D1A, 0xFF101B30),
    DAWN("Astronomical Dawn", 4.5f, 6.0f, 0.2f, 0.45f, 0xFF1C1A3A, 0xFF6D4C7D),
    SUNRISE("Golden Sunrise", 6.0f, 7.5f, 0.6f, 0.75f, 0xFF2A5298, 0xFFFFB347),
    MORNING("Bright Morning", 7.5f, 11.0f, 0.9f, 0.95f, 0xFF1E88E5, 0xFF81D4FA),
    ZENITH("Midday Zenith", 11.0f, 14.0f, 1.0f, 1.0f, 0xFF0D47A1, 0xFFE1F5FE),
    AFTERNOON("Warm Afternoon", 14.0f, 17.0f, 0.85f, 0.95f, 0xFF1565C0, 0xFFFFF176),
    GOLDEN_HOUR("Golden Sunset", 17.0f, 19.0f, 0.5f, 0.8f, 0xFF4A148C, 0xFFFF7043),
    DUSK("Civil Twilight", 19.0f, 20.5f, 0.1f, 0.5f, 0xFF28104E, 0xFFB388FF),
    NIGHT("Starlit Night", 20.5f, 24.0f, 0.0f, 0.3f, 0xFF090E1F, 0xFF1A237E)
}

/**
 * Dynamic Weather state.
 */
enum class WeatherType(
    val displayName: String,
    val description: String,
    val solarMultiplier: Float,
    val windMultiplier: Float,
    val cropGrowthMultiplier: Float,
    val windSpeedKmh: Float,
    val autoWaterRain: Boolean,
    val colorOverlay: Long
) {
    SUNNY_CLEAR("Clear", "☀️ Optimal solar radiation with crisp blue skies.", 1.0f, 1.0f, 1.0f, 12.0f, false, 0x00FFFFFF),
    CLOUDY_OVERCAST("Cloudy", "☁️ Overcast skies; solar output -30%.", 0.70f, 1.0f, 1.0f, 20.0f, false, 0x1A607D8B),
    RAINY_STORM("Rain", "🌧️ Permaculture rain; crops grow 2x faster, solar output -60%.", 0.40f, 1.10f, 2.0f, 28.0f, true, 0x3337474F),
    STORM("Storm", "⛈️ Fierce thunderstorm! Windmill +50%, solar -90%, lightning flashes.", 0.10f, 1.50f, 1.5f, 48.0f, true, 0x551A232E),
    HEATWAVE("Heatwave", "🔥 Pale yellow skies. Solar +30%, wind -50%, thirst 2x faster.", 1.30f, 0.50f, 1.0f, 6.0f, false, 0x26FFD54F),
    WIND_GALE("Gale", "💨 Strong gale winds.", 0.50f, 1.60f, 1.0f, 55.0f, false, 0x1A00E5FF),
    MISTY_NEBULA("Misty", "🌫️ Low-lying fog over the fields.", 0.60f, 0.80f, 1.0f, 8.0f, false, 0x1A80CBC4)
}

/**
 * Inventory categories.
 */
enum class ItemCategory {
    SEEDS,
    HARVEST,
    PRODUCE,
    CRAFTED,
    TECH_UPGRADE,
    TOOL
}

/**
 * Player tools for interacting with the farm.
 */
enum class PlayerTool(val displayName: String, val iconRes: String, val description: String) {
    HAND("Free Hands", "hand", "Inspect, pet animals, and pick ripe crops."),
    WATER_CAN("Smart Sprinkler", "water_drop", "Hydrate dry permaculture soil plots."),
    FERTILIZER("Bio-Nutrient Serum", "compost", "Accelerate plant growth and increase yield."),
    SOLAR_WRENCH("Plasma Wrench", "build", "Construct, repair, or upgrade clean energy nodes."),
    SHEARS("Laser Clippers", "content_cut", "Gently shear solar wool and collect alpaca yarn."),
    SEED_POUCH("Seed Pouch", "eco", "Sow chosen crops into fertile plots."),
    FISHING_ROD("Fishing Rod", "phishing", "Cast into rivers, streams, and ponds to catch wild fish.")
}

/**
 * 3 Water Quality Tiers.
 */
enum class WaterQuality(
    val displayName: String,
    val thirstRestored: Float,
    val causesSickness: Boolean,
    val description: String
) {
    CLEAN(
        displayName = "Clean Water",
        thirstRestored = 40.0f,
        causesSickness = false,
        description = "Pure, fresh ground or rainwater. Safe to drink."
    ),
    RAW(
        displayName = "Raw Water",
        thirstRestored = 30.0f,
        causesSickness = true,
        description = "Unfiltered river, stream, or pond water. May cause sickness unless boiled or filtered."
    ),
    PURIFIED(
        displayName = "Purified Water",
        thirstRestored = 45.0f,
        causesSickness = false,
        description = "High-tech UV sterilized mineral water. Safe and deeply hydrating."
    )
}

/**
 * 5 Fish Species caught across wetlands, streams, and ponds.
 */
enum class FishType(
    val displayName: String,
    val rarity: String,
    val sellPrice: Int,
    val hungerRestoreRaw: Float,
    val hungerRestoreCooked: Float,
    val healthBonus: Float,
    val baseCatchWeight: Int,
    val colorHex: Long
) {
    TROUT(
        displayName = "Rainbow Trout",
        rarity = "Common",
        sellPrice = 15,
        hungerRestoreRaw = 20.0f,
        hungerRestoreCooked = 30.0f,
        healthBonus = 0.0f,
        baseCatchWeight = 35,
        colorHex = 0xFFFF8A80
    ),
    BASS(
        displayName = "Largemouth Bass",
        rarity = "Common",
        sellPrice = 25,
        hungerRestoreRaw = 25.0f,
        hungerRestoreCooked = 35.0f,
        healthBonus = 0.0f,
        baseCatchWeight = 30,
        colorHex = 0xFF81C784
    ),
    CATFISH(
        displayName = "Whisker Catfish",
        rarity = "Uncommon",
        sellPrice = 40,
        hungerRestoreRaw = 30.0f,
        hungerRestoreCooked = 40.0f,
        healthBonus = 0.0f,
        baseCatchWeight = 20,
        colorHex = 0xFF90A4AE
    ),
    CARP(
        displayName = "River Carp",
        rarity = "Uncommon",
        sellPrice = 50,
        hungerRestoreRaw = 30.0f,
        hungerRestoreCooked = 45.0f,
        healthBonus = 0.0f,
        baseCatchWeight = 10,
        colorHex = 0xFFFFB74D
    ),
    GOLDEN_FISH(
        displayName = "Solar Golden Fish",
        rarity = "Rare",
        sellPrice = 150,
        hungerRestoreRaw = 50.0f,
        hungerRestoreCooked = 60.0f,
        healthBonus = 5.0f,
        baseCatchWeight = 5,
        colorHex = 0xFFFFD700
    )
}

/**
 * Fishing Rod Equipment Tiers.
 */
enum class FishingRodTier(
    val tier: Int,
    val displayName: String,
    val minWaitSec: Float,
    val maxWaitSec: Float,
    val rareBonusPct: Int,
    val doubleCatchChance: Float,
    val upgradeCost: Int,
    val description: String
) {
    NONE(
        tier = 0,
        displayName = "No Rod",
        minWaitSec = 0f,
        maxWaitSec = 0f,
        rareBonusPct = 0,
        doubleCatchChance = 0f,
        upgradeCost = 0,
        description = "Requires a fishing rod to cast into water."
    ),
    BASIC(
        tier = 1,
        displayName = "Basic Rod",
        minWaitSec = 3.0f,
        maxWaitSec = 9.0f,
        rareBonusPct = 0,
        doubleCatchChance = 0.0f,
        upgradeCost = 50,
        description = "Handmade bamboo & twine rod. Wait time: 3-9s."
    ),
    IMPROVED(
        tier = 2,
        displayName = "Improved Rod",
        minWaitSec = 2.0f,
        maxWaitSec = 6.0f,
        rareBonusPct = 10,
        doubleCatchChance = 0.0f,
        upgradeCost = 150,
        description = "Flexible carbon-fiber rod. Faster bites & +10% rare fish."
    ),
    MASTER(
        tier = 3,
        displayName = "Master Rod",
        minWaitSec = 1.0f,
        maxWaitSec = 4.0f,
        rareBonusPct = 25,
        doubleCatchChance = 0.20f,
        upgradeCost = 500,
        description = "Solar-luminescent master tackle. Lightning bites, +25% rare fish, and 20% double catches!"
    );

    companion object {
        fun fromTier(tier: Int): FishingRodTier = when (tier) {
            1 -> BASIC
            2 -> IMPROVED
            3 -> MASTER
            else -> NONE
        }
    }
}

/**
 * World Water Fishing Spots with dynamic fish populations (10 max).
 */
enum class FishingSpotType(
    val id: String,
    val displayName: String,
    val biome: BiomeType,
    val worldX: Float,
    val worldZ: Float,
    val maxPopulation: Int = 10
) {
    GREEN_VALLEY_STREAM(
        id = "spot_stream",
        displayName = "Valley Mountain Stream",
        biome = BiomeType.GREEN_VALLEY,
        worldX = 16.0f,
        worldZ = -4.0f
    ),
    WETLAND_RIVER(
        id = "spot_river",
        displayName = "Wetland River Delta",
        biome = BiomeType.WETLAND,
        worldX = -24.0f,
        worldZ = -98.0f
    ),
    WETLAND_POND(
        id = "spot_pond",
        displayName = "Wetland Lily Pond",
        biome = BiomeType.WETLAND,
        worldX = 20.0f,
        worldZ = -120.0f
    )
}

/**
 * 3 Biomes spanning the 400m x 400m world map.
 */
enum class BiomeType(
    val id: String,
    val displayName: String,
    val description: String,
    val centerZ: Float,
    val mapColor: Long,
    val ambientMusicHint: String
) {
    GREEN_VALLEY(
        id = "green_valley",
        displayName = "Green Valley",
        description = "Sun-drenched meadows, sustainable permaculture plots, and heritage farmlands.",
        centerZ = 0.0f,
        mapColor = 0xFF4CAF50,
        ambientMusicHint = "peaceful_morning"
    ),
    DEEP_FOREST(
        id = "deep_forest",
        displayName = "Deep Forest",
        description = "Ancient towering canopies, bioluminescent flora, and wildlife habitats.",
        centerZ = 100.0f,
        mapColor = 0xFF1B5E20,
        ambientMusicHint = "forest_mystic"
    ),
    WETLAND(
        id = "wetland",
        displayName = "Wetland & River",
        description = "Scenic winding river delta, reed beds, aquatic fauna, and rich river clay.",
        centerZ = -100.0f,
        mapColor = 0xFF00897B,
        ambientMusicHint = "wetland_river"
    );

    companion object {
        fun fromPosition(z: Float): BiomeType {
            return when {
                z > 40.0f -> DEEP_FOREST
                z < -40.0f -> WETLAND
                else -> GREEN_VALLEY
            }
        }
    }
}

/**
 * Discoverable Points of Interest across the 3 Biomes.
 */
enum class PointOfInterestType(
    val id: String,
    val displayName: String,
    val biome: BiomeType,
    val worldX: Float,
    val worldZ: Float,
    val iconName: String,
    val discoveryRewardTitle: String,
    val discoveryRewardDesc: String,
    val rewardCoins: Int,
    val rewardEcoScore: Int,
    val rewardItemName: String
) {
    // Green Valley POIs
    OLD_FARM(
        id = "poi_old_farm",
        displayName = "Solarpunk Homestead",
        biome = BiomeType.GREEN_VALLEY,
        worldX = 6.0f,
        worldZ = 0.0f,
        iconName = "home",
        discoveryRewardTitle = "Homestead Territory",
        discoveryRewardDesc = "Your regenerative farm and energy nexus",
        rewardCoins = 50,
        rewardEcoScore = 5,
        rewardItemName = "Heritage Seeds"
    ),
    OLD_WELL(
        id = "poi_old_well",
        displayName = "Abandoned Old Well",
        biome = BiomeType.GREEN_VALLEY,
        worldX = -18.0f,
        worldZ = -15.0f,
        iconName = "water_drop",
        discoveryRewardTitle = "Historical Well Found!",
        discoveryRewardDesc = "Gives lore about previous eco-settlers and water filtration notes",
        rewardCoins = 100,
        rewardEcoScore = 10,
        rewardItemName = "Pure Mineral Water"
    ),
    RESOURCE_CAVE(
        id = "poi_resource_cave",
        displayName = "Crystal Resource Cave",
        biome = BiomeType.GREEN_VALLEY,
        worldX = -28.0f,
        worldZ = 20.0f,
        iconName = "cave",
        discoveryRewardTitle = "Mineral Deposit Found!",
        discoveryRewardDesc = "Rich vein of iron ore, quartz, and construction minerals",
        rewardCoins = 140,
        rewardEcoScore = 10,
        rewardItemName = "Iron Ore Chunk"
    ),
    BROKEN_WINDMILL(
        id = "poi_broken_windmill",
        displayName = "Derelict Windmill",
        biome = BiomeType.GREEN_VALLEY,
        worldX = 25.0f,
        worldZ = -30.0f,
        iconName = "air",
        discoveryRewardTitle = "Derelict Windmill Discovered!",
        discoveryRewardDesc = "Repairable turbine structure. +1 Free Vertical Wind Turbine",
        rewardCoins = 160,
        rewardEcoScore = 15,
        rewardItemName = "Wind Turbine Blade"
    ),

    // Deep Forest POIs
    RADIO_TOWER(
        id = "poi_radio_tower",
        displayName = "Deep Forest Radio Tower",
        biome = BiomeType.DEEP_FOREST,
        worldX = 45.0f,
        worldZ = 120.0f,
        iconName = "cell_tower",
        discoveryRewardTitle = "Radio Tower Located!",
        discoveryRewardDesc = "Broadcast antenna repaired! Researcher NPC signal detected",
        rewardCoins = 200,
        rewardEcoScore = 20,
        rewardItemName = "Radio Transceiver"
    ),
    RESEARCH_FACILITY(
        id = "poi_research_facility",
        displayName = "SDZ Research Facility",
        biome = BiomeType.DEEP_FOREST,
        worldX = -45.0f,
        worldZ = 160.0f,
        iconName = "science",
        discoveryRewardTitle = "SDZ Research Facility Discovered!",
        discoveryRewardDesc = "Abandoned eco-tech complex with encrypted terminal logs & geothermal blueprints",
        rewardCoins = 300,
        rewardEcoScore = 30,
        rewardItemName = "Geothermal Blueprint"
    ),
    STONE_CIRCLE(
        id = "poi_stone_circle",
        displayName = "Mysterious Stone Circle",
        biome = BiomeType.DEEP_FOREST,
        worldX = -12.0f,
        worldZ = 148.0f,
        iconName = "stones",
        discoveryRewardTitle = "Ancient Leyline Resonance!",
        discoveryRewardDesc = "Harmonic resonance elevates farm ecosystem vitality",
        rewardCoins = 250,
        rewardEcoScore = 20,
        rewardItemName = "Bioluminescent Spore"
    ),
    FOREST_CAMPSITE(
        id = "poi_forest_campsite",
        displayName = "Old Forest Campsite",
        biome = BiomeType.DEEP_FOREST,
        worldX = -32.0f,
        worldZ = 85.0f,
        iconName = "camp",
        discoveryRewardTitle = "Survival Supplies Found!",
        discoveryRewardDesc = "Recovered emergency rations and medicinal herbs",
        rewardCoins = 100,
        rewardEcoScore = 5,
        rewardItemName = "Medicinal Herb"
    ),
    RANGER_TOWER(
        id = "poi_ranger_tower",
        displayName = "Abandoned Ranger Tower",
        biome = BiomeType.DEEP_FOREST,
        worldX = 14.0f,
        worldZ = 110.0f,
        iconName = "tower",
        discoveryRewardTitle = "Tower Lookout Unlocked!",
        discoveryRewardDesc = "Discovered surveyor notes (+Hardwood & Maps)",
        rewardCoins = 150,
        rewardEcoScore = 10,
        rewardItemName = "Aged Hardwood"
    ),
    HIDDEN_WATERFALL(
        id = "poi_hidden_waterfall",
        displayName = "Hidden Forest Waterfall",
        biome = BiomeType.DEEP_FOREST,
        worldX = -50.0f,
        worldZ = 90.0f,
        iconName = "water",
        discoveryRewardTitle = "Hidden Springs Found!",
        discoveryRewardDesc = "Pristine mountain cascade heals player & boosts max health (+10 HP)",
        rewardCoins = 180,
        rewardEcoScore = 25,
        rewardItemName = "Cascade Spring Essence"
    ),

    // Wetland POIs
    BROKEN_BRIDGE(
        id = "poi_broken_bridge",
        displayName = "Broken Timber Bridge",
        biome = BiomeType.WETLAND,
        worldX = 16.0f,
        worldZ = -75.0f,
        iconName = "bridge",
        discoveryRewardTitle = "Historic River Span!",
        discoveryRewardDesc = "Discovered aquatic engineering blueprint",
        rewardCoins = 120,
        rewardEcoScore = 8,
        rewardItemName = "River Reeds"
    ),
    FISHING_HUT(
        id = "poi_fishing_hut",
        displayName = "Old Fishing Hut",
        biome = BiomeType.WETLAND,
        worldX = -24.0f,
        worldZ = -95.0f,
        iconName = "hut",
        discoveryRewardTitle = "Master Angler Blueprint!",
        discoveryRewardDesc = "Unlocked fishing tackle and aquaculture techniques",
        rewardCoins = 180,
        rewardEcoScore = 12,
        rewardItemName = "Golden Fish Lure"
    ),
    SUNKEN_BOAT(
        id = "poi_sunken_boat",
        displayName = "Sunken Supply Boat",
        biome = BiomeType.WETLAND,
        worldX = 35.0f,
        worldZ = -135.0f,
        iconName = "boat",
        discoveryRewardTitle = "Sunken Cargo Salvaged!",
        discoveryRewardDesc = "Recovered clean river clay and rare alloy fasteners",
        rewardCoins = 200,
        rewardEcoScore = 15,
        rewardItemName = "Pure River Clay"
    ),
    ANCIENT_TREE(
        id = "poi_ancient_tree",
        displayName = "Ancient Banyan Tree",
        biome = BiomeType.WETLAND,
        worldX = -15.0f,
        worldZ = -140.0f,
        iconName = "park",
        discoveryRewardTitle = "Ancient Banyan Discovered!",
        discoveryRewardDesc = "Historic sentinel tree of the valley. +50 Research Points & Ancient Lore",
        rewardCoins = 300,
        rewardEcoScore = 40,
        rewardItemName = "Ancient Seed Pod"
    ),
    HOT_SPRING(
        id = "poi_hot_spring",
        displayName = "Geothermal Hot Spring",
        biome = BiomeType.WETLAND,
        worldX = 40.0f,
        worldZ = -110.0f,
        iconName = "hot_tub",
        discoveryRewardTitle = "Natural Geothermal Spring!",
        discoveryRewardDesc = "Soothing mineral waters heal health & completely restore stamina",
        rewardCoins = 220,
        rewardEcoScore = 20,
        rewardItemName = "Mineral Salt"
    );
}

/**
 * Priority levels for power allocation during low battery conditions.
 */
enum class PowerPriority(
    val level: Int,
    val displayName: String,
    val thresholdPercent: Float,
    val colorHex: Long,
    val description: String
) {
    CRITICAL(1, "Critical", 0.0f, 0xFFFF5252, "Always powered (if battery > 0%)"),
    IMPORTANT(2, "Important", 0.30f, 0xFFFF9800, "Powered if battery > 30%"),
    NORMAL(3, "Normal", 0.50f, 0xFFFFD54F, "Powered if battery > 50%"),
    COMFORT(4, "Comfort", 0.70f, 0xFF81C784, "Powered if battery > 70%"),
    DECORATIVE(5, "Decorative", 0.90f, 0xFF80D8FF, "Powered if battery > 90%")
}

/**
 * Devices on the farm that consume electrical energy.
 */
enum class DeviceType(
    val id: String,
    val displayName: String,
    val tierLabel: String,
    val consumptionKwhPerHour: Float,
    val defaultPriority: PowerPriority,
    val category: String,
    val iconName: String = "bolt"
) {
    REFRIGERATOR("device_fridge", "Farmhouse Refrigerator", "Tier 1 (Critical)", 1.5f, PowerPriority.CRITICAL, "Food Preservation", "kitchen"),
    WATER_PURIFIER("device_purifier", "UV Water Purifier", "Tier 1 (Critical)", 2.0f, PowerPriority.CRITICAL, "Hydration", "water_drop"),
    MEDICAL_STATION("device_medical", "Medical Dispensary", "Tier 1 (Critical)", 1.0f, PowerPriority.CRITICAL, "Healthcare", "medical_services"),

    GREENHOUSE_HEATER("device_gh_heater", "Greenhouse Climate Heater", "Tier 2 (Important)", 4.0f, PowerPriority.IMPORTANT, "Crop Climate", "thermostat"),
    WATER_PUMP("device_water_pump", "Irrigation Water Pump", "Tier 2 (Important)", 1.0f, PowerPriority.IMPORTANT, "Irrigation", "water"),
    WORKSHOP("device_workshop", "Artisan Tech Workshop", "Tier 2 (Important)", 3.0f, PowerPriority.IMPORTANT, "Crafting", "build"),

    LIGHTS("device_lights", "Farmhouse & Barn Lights", "Tier 3 (Normal)", 0.5f, PowerPriority.NORMAL, "Lighting", "lightbulb"),
    COOKING_STATION("device_cooking", "Electric Cooktop", "Tier 3 (Normal)", 1.0f, PowerPriority.NORMAL, "Cooking", "restaurant"),
    ELECTRIC_FENCE("device_elec_fence", "Perimeter Shock Fence", "Tier 3 (Normal)", 0.5f, PowerPriority.NORMAL, "Sanctuary Security", "fence"),

    TV_ENTERTAINMENT("device_tv", "Entertainment Terminal", "Tier 4 (Comfort)", 0.8f, PowerPriority.COMFORT, "Recreation", "tv"),
    VENTILATION_FAN("device_fan", "Living Quarters Fan", "Tier 4 (Comfort)", 0.3f, PowerPriority.COMFORT, "Air Circulation", "mode_fan"),
    PATIO_FOUNTAIN("device_fountain", "Patio Water Fountain", "Tier 4 (Comfort)", 0.4f, PowerPriority.COMFORT, "Aesthetics", "waves"),

    AMBIENT_LIGHTING("device_ambient_light", "Pathway Ambient Glow", "Tier 5 (Decorative)", 0.2f, PowerPriority.DECORATIVE, "Landscape Decor", "flare"),
    DECORATIVE_FOUNTAIN("device_garden_fountain", "Garden Fountain", "Tier 5 (Decorative)", 0.3f, PowerPriority.DECORATIVE, "Landscape Decor", "yard")
}

data class DevicePowerState(
    val device: DeviceType,
    val currentPriority: PowerPriority,
    val isPowered: Boolean,
    val hourlyConsumptionKwh: Float
)

data class GridConnectionLine(
    val startX: Float,
    val startY: Float,
    val startZ: Float,
    val endX: Float,
    val endY: Float,
    val endZ: Float,
    val isPowered: Boolean,
    val powerFlowRatio: Float = 1.0f
)

data class EnergyGridSummary(
    val totalGenerationKwhPerDay: Float,
    val currentGenerationKwhPerHour: Float,
    val totalStorageCapacityKwh: Float,
    val currentStoredKwh: Float,
    val totalHourlyConsumptionKwh: Float,
    val netHourlyKwh: Float,
    val isCharging: Boolean,
    val hoursUntilEmpty: Float,
    val batteryPercent: Float,
    val activeDevicesCount: Int,
    val totalDevicesCount: Int,
    val unpoweredDevicesCount: Int,
    val powerSourcesCount: Int,
    val batteryUnitsCount: Int,
    val powerPolesCount: Int
)

data class PowerSourceInfo(
    val name: String,
    val typeName: String,
    val generationKwhPerDay: Float,
    val currentGenerationKw: Float,
    val status: String,
    val isOperating: Boolean,
    val location: String
)

data class BatteryUnitInfo(
    val name: String,
    val capacityKwh: Float,
    val currentChargeKwh: Float,
    val maxChargeRateKw: Float,
    val status: String
)

data class GridMapNode(
    val id: String,
    val name: String,
    val x: Float,
    val z: Float,
    val isSource: Boolean,
    val isBattery: Boolean,
    val isPole: Boolean,
    val isPowered: Boolean
)

data class EnergyResearchTech(
    val id: String,
    val title: String,
    val requiredRp: Int,
    val description: String,
    val unlockEffect: String,
    val isUnlocked: Boolean
)

/**
 * Role of an NPC survivor in the settlement.
 */
enum class NpcRole(
    val id: String,
    val displayName: String,
    val title: String,
    val description: String,
    val iconName: String,
    val primaryColorHex: Long
) {
    FARMER(
        id = "role_farmer",
        displayName = "Farmer",
        title = "Agronomist & Cultivator",
        description = "Waters & harvests crops every morning, deposits harvest in storage, boosts crop growth +10%",
        iconName = "eco",
        primaryColorHex = 0xFF81C784
    ),
    ENGINEER(
        id = "role_engineer",
        displayName = "Engineer",
        title = "Grid & Systems Technician",
        description = "Optimizes energy grid (+5% efficiency), repairs devices, warns before failures",
        iconName = "bolt",
        primaryColorHex = 0xFF00E5FF
    ),
    BUILDER(
        id = "role_builder",
        displayName = "Builder",
        title = "Master Architect & Fabricator",
        description = "Speeds up construction by 50%, upgrades buildings, maintains structural integrity",
        iconName = "construction",
        primaryColorHex = 0xFFFFD54F
    ),
    RESEARCHER(
        id = "role_researcher",
        displayName = "Researcher",
        title = "Clean-Tech Scientist",
        description = "Generates +2 Research Points/day, accelerates blueprint discoveries and unlocks tech 20% faster",
        iconName = "science",
        primaryColorHex = 0xFFBA68C8
    ),
    MEDIC(
        id = "role_medic",
        displayName = "Medic",
        title = "Physician & Herbalist",
        description = "Passively heals player (+1 HP/hr in settlement), cures sickness instantly, brews herbal medicine",
        iconName = "medical_services",
        primaryColorHex = 0xFFFF5252
    );

    companion object {
        fun fromId(id: String): NpcRole = values().find { it.id == id } ?: FARMER
    }
}

/**
 * Personality traits of an NPC survivor.
 */
enum class NpcPersonalityTrait(
    val displayName: String,
    val description: String,
    val moraleModifier: Float
) {
    CHEERFUL("Cheerful", "Spreads good spirits to nearby survivors (+1 Morale to others)", 1.15f),
    SERIOUS("Serious", "Highly focused on tasks, work efficiency +15%", 1.0f),
    CURIOUS("Curious", "Loves exploration, finds bonus items and research notes", 1.05f),
    KIND("Kind", "Comforts companions, boosts settlement harmony", 1.10f),
    HARDWORKING("Hardworking", "Completes duties 20% faster without complaining", 1.05f)
}

/**
 * Schedule activities throughout the 24-hour cycle.
 */
enum class NpcActivity(
    val displayName: String,
    val animationState: String,
    val iconName: String
) {
    WAKING("Waking & Breakfast", "eating", "restaurant"),
    WORKING("Working at Station", "working", "build"),
    LUNCH("Lunch Break", "eating", "restaurant"),
    FREE_TIME("Free Time / Wandering", "idle", "explore"),
    DINNER("Communal Dinner", "eating", "restaurant"),
    SOCIALIZING("Socializing & Relaxing", "social", "forum"),
    SLEEPING("Sleeping", "sleeping", "bed");

    companion object {
        fun getActivityForHour(hour: Float): NpcActivity {
            return when {
                hour in 6.0f..6.99f -> WAKING
                hour in 7.0f..11.99f -> WORKING
                hour in 12.0f..12.99f -> LUNCH
                hour in 13.0f..16.99f -> WORKING
                hour in 17.0f..18.99f -> FREE_TIME
                hour in 19.0f..20.99f -> DINNER
                hour in 21.0f..21.99f -> SOCIALIZING
                else -> SLEEPING // 22:00 to 06:00
            }
        }
    }
}

/**
 * Settlement summary data for the Sanctuary Tab.
 */
data class SettlementStats(
    val totalPopulation: Int,
    val occupiedBeds: Int,
    val totalBeds: Int,
    val overallMorale: Float,
    val foodStockUnits: Int,
    val cleanWaterStockUnits: Int,
    val daysOfFoodRemaining: Float,
    val daysOfWaterRemaining: Float,
    val dailyFoodConsumption: Int,
    val dailyWaterConsumption: Int,
    val roleBreakdown: Map<NpcRole, Int>,
    val averageSkillLevel: Float,
    val isHousingDeficit: Boolean,
    val isStarving: Boolean,
    val isDehydrated: Boolean
)

/**
 * Arrival candidate ready to visit or approach the settlement.
 */
data class NpcArrivalCandidate(
    val role: NpcRole,
    val name: String,
    val age: Int,
    val trait: NpcPersonalityTrait,
    val greetingQuote: String,
    val arrivalDay: Int,
    val backgroundStory: String
)

/**
 * Phase 6: Research Tree Tech Node
 */
data class ResearchTreeNode(
    val id: String,
    val tier: Int,
    val title: String,
    val rpCost: Int,
    val description: String,
    val unlockFeature: String,
    val prerequisiteTechIds: List<String> = emptyList(),
    val iconName: String = "science",
    val isUnlocked: Boolean = false,
    val isAvailable: Boolean = false
)

/**
 * Phase 6: Story Mission Objective
 */
data class StoryMission(
    val id: String,
    val missionNumber: Int,
    val title: String,
    val description: String,
    val objectiveDescription: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val rpReward: Int = 20,
    val rewardSummary: String = "",
    val autoStartDay: Int = 1,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false,
    val isEndingMission: Boolean = false
)

/**
 * Phase 6: Point of Interest (Discovery Location)
 */
data class PointOfInterestData(
    val id: String,
    val name: String,
    val biome: BiomeType,
    val posX: Float,
    val posZ: Float,
    val description: String,
    val rpReward: Int = 15,
    val isDiscovered: Boolean = false,
    val interactiveType: String = "EXAMINE", // EXAMINE, CLIMB, REPAIR, HEAL, TERMINAL
    val rewardSummary: String = "15 RP + Lore Entry"
)

/**
 * Phase 6: Journal Lore Entry
 */
data class LoreEntryData(
    val id: String,
    val title: String,
    val author: String,
    val category: String, // SETTLER_DIARY, SDZ_SCIENTIFIC_LOG, RADIO_BROADCAST, TERMINAL
    val textContent: String,
    val isUnlocked: Boolean = false
)

/**
 * Phase 6: Interactive Terminal Log
 */
data class TerminalLogData(
    val id: String,
    val terminalName: String,
    val locationName: String,
    val logText: String,
    val isHacked: Boolean = false,
    val requiresPassword: Boolean = true,
    val passwordAnswer: String = "SOLARIS"
)
