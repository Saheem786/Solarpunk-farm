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
    SEED_POUCH("Seed Pouch", "eco", "Sow chosen crops into fertile plots.")
}
