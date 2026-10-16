// ==========================================
// MEDIEVAL ISLAND - DOMAIN TYPES & CATALOGS
// ==========================================

export type CropTypeId =
  | 'WHEAT'
  | 'CORN'
  | 'TOMATO'
  | 'CARROT'
  | 'HERBS'
  | 'SOLAR_SUNFLOWER'
  | 'BIOLUMINESCENT_MUSHROOM'
  | 'TERRACED_WHEAT'
  | 'SOLAR_CORN'
  | 'CYBER_BERRIES'
  | 'SKY_SPIRULINA'
  | 'HYDROPONIC_MELON'
  | 'NITRO_BEANS';

export interface CropDef {
  id: CropTypeId;
  displayName: string;
  description: string;
  growthDurationSec: number;
  waterNeed: number;
  sunNeed: number;
  harvestYield: number;
  baseSellPrice: number;
  seedCost: number;
  energyBonusKwh: number;
  primaryColor: string;
  secondaryColor: string;
}

export const CROP_CATALOG: Record<CropTypeId, CropDef> = {
  WHEAT: {
    id: 'WHEAT',
    displayName: 'Golden Wheat',
    description: 'Deep-rooted heritage grain enriching soil and yielding fresh flour.',
    growthDurationSec: 16,
    waterNeed: 0.3,
    sunNeed: 0.7,
    harvestYield: 3,
    baseSellPrice: 30,
    seedCost: 5,
    energyBonusKwh: 1.0,
    primaryColor: '#FFE082',
    secondaryColor: '#8D6E63',
  },
  CORN: {
    id: 'CORN',
    displayName: 'Sweet Corn',
    description: 'Tall architectural stalks with golden sun-ripened corn cobs.',
    growthDurationSec: 22,
    waterNeed: 0.5,
    sunNeed: 0.85,
    harvestYield: 2,
    baseSellPrice: 50,
    seedCost: 10,
    energyBonusKwh: 2.0,
    primaryColor: '#FFCA28',
    secondaryColor: '#388E3C',
  },
  TOMATO: {
    id: 'TOMATO',
    displayName: 'Ruby Tomato',
    description: 'Juicy sun-ripened red tomatoes on supportive vertical trellis vines.',
    growthDurationSec: 18,
    waterNeed: 0.6,
    sunNeed: 0.75,
    harvestYield: 4,
    baseSellPrice: 40,
    seedCost: 8,
    energyBonusKwh: 1.5,
    primaryColor: '#FF5252',
    secondaryColor: '#2E7D32',
  },
  CARROT: {
    id: 'CARROT',
    displayName: 'Crisp Carrot',
    description: 'Nutritious sweet orange root vegetable thriving in compost-rich soil.',
    growthDurationSec: 14,
    waterNeed: 0.4,
    sunNeed: 0.6,
    harvestYield: 3,
    baseSellPrice: 25,
    seedCost: 4,
    energyBonusKwh: 1.2,
    primaryColor: '#FF9800',
    secondaryColor: '#4CAF50',
  },
  HERBS: {
    id: 'HERBS',
    displayName: 'Aromatic Herbs',
    description: 'Fragrant medicinal and culinary rosemary, basil, and herbal tea leaves.',
    growthDurationSec: 10,
    waterNeed: 0.35,
    sunNeed: 0.5,
    harvestYield: 5,
    baseSellPrice: 60,
    seedCost: 15,
    energyBonusKwh: 0.8,
    primaryColor: '#81C784',
    secondaryColor: '#00E676',
  },
  SOLAR_SUNFLOWER: {
    id: 'SOLAR_SUNFLOWER',
    displayName: 'Mana Sunflower',
    description: 'Radiant flora that gathers arcane energy from the sun while blooming.',
    growthDurationSec: 15,
    waterNeed: 0.4,
    sunNeed: 0.8,
    harvestYield: 2,
    baseSellPrice: 45,
    seedCost: 15,
    energyBonusKwh: 1.5,
    primaryColor: '#FFD54F',
    secondaryColor: '#FFA000',
  },
  BIOLUMINESCENT_MUSHROOM: {
    id: 'BIOLUMINESCENT_MUSHROOM',
    displayName: 'Moon Spores',
    description: 'Glows with a soft silver light at night, thrives in misty weather.',
    growthDurationSec: 22,
    waterNeed: 0.8,
    sunNeed: 0.1,
    harvestYield: 3,
    baseSellPrice: 60,
    seedCost: 25,
    energyBonusKwh: 0.5,
    primaryColor: '#00E5FF',
    secondaryColor: '#7C4DFF',
  },
  TERRACED_WHEAT: {
    id: 'TERRACED_WHEAT',
    displayName: 'Noble Grain',
    description: "Royal wheat variety enriching the island's soil.",
    growthDurationSec: 16,
    waterNeed: 0.3,
    sunNeed: 0.7,
    harvestYield: 3,
    baseSellPrice: 35,
    seedCost: 12,
    energyBonusKwh: 1.0,
    primaryColor: '#FFE082',
    secondaryColor: '#8D6E63',
  },
  SOLAR_CORN: {
    id: 'SOLAR_CORN',
    displayName: 'Royal Corn',
    description: 'Tall architectural stalks with golden sun-blessed kernels.',
    growthDurationSec: 22,
    waterNeed: 0.5,
    sunNeed: 0.85,
    harvestYield: 2,
    baseSellPrice: 55,
    seedCost: 18,
    energyBonusKwh: 2.0,
    primaryColor: '#FFCA28',
    secondaryColor: '#388E3C',
  },
  CYBER_BERRIES: {
    id: 'CYBER_BERRIES',
    displayName: 'Cursed Berries',
    description: 'Dark, potent berries that pulse with a mysterious energy.',
    growthDurationSec: 25,
    waterNeed: 0.5,
    sunNeed: 0.6,
    harvestYield: 5,
    baseSellPrice: 85,
    seedCost: 30,
    energyBonusKwh: 1.2,
    primaryColor: '#E040FB',
    secondaryColor: '#536DFE',
  },
  SKY_SPIRULINA: {
    id: 'SKY_SPIRULINA',
    displayName: 'Medicinal Moss',
    description: 'Ancient moss harvested from the high cliffs and springs.',
    growthDurationSec: 12,
    waterNeed: 0.9,
    sunNeed: 0.5,
    harvestYield: 4,
    baseSellPrice: 35,
    seedCost: 12,
    energyBonusKwh: 0.8,
    primaryColor: '#00E676',
    secondaryColor: '#00B0FF',
  },
  HYDROPONIC_MELON: {
    id: 'HYDROPONIC_MELON',
    displayName: 'Crystalline Melon',
    description: "Sweet melons infused with the purity of the island's crystals.",
    growthDurationSec: 30,
    waterNeed: 0.7,
    sunNeed: 0.6,
    harvestYield: 1,
    baseSellPrice: 120,
    seedCost: 45,
    energyBonusKwh: 2.0,
    primaryColor: '#FF8A80',
    secondaryColor: '#69F0AE',
  },
  NITRO_BEANS: {
    id: 'NITRO_BEANS',
    displayName: 'Earth Beans',
    description: 'Restores the vitality of the soil, aiding neighboring crops.',
    growthDurationSec: 16,
    waterNeed: 0.5,
    sunNeed: 0.5,
    harvestYield: 3,
    baseSellPrice: 40,
    seedCost: 15,
    energyBonusKwh: 0.6,
    primaryColor: '#81C784',
    secondaryColor: '#388E3C',
  },
};

export type CropStage =
  | 'EMPTY'
  | 'SEEDLING'
  | 'SPROUT'
  | 'VEGETATIVE'
  | 'FLOWERING'
  | 'HARVEST_READY'
  | 'WITHERED';

export type PlotType =
  | 'PERMACULTURE_BED'
  | 'HYDROPONIC_TOWER'
  | 'BIO_DOME'
  | 'SOLAR_SOIL_PATCH';

export interface PlotEntity {
  id: number;
  plotType: PlotType;
  cropType: CropTypeId | null;
  stage: CropStage;
  progress: number; // 0.0 to 1.0 (or >1.0 for ready)
  moisture: number; // 0.0 to 1.0
  compostLevel: number;
  posX: number;
  posY: number;
  posZ: number;
  autoIrrigated: boolean;
}

export type BuildableTypeId =
  | 'CABIN'
  | 'GREENHOUSE'
  | 'SOLAR_PANEL'
  | 'WINDMILL'
  | 'STORAGE'
  | 'WELL'
  | 'FENCE'
  | 'COMPOST_BIN'
  | 'RAIN_BARREL'
  | 'WATER_FILTER'
  | 'WATER_PURIFIER'
  | 'IRRIGATION_PIPE'
  | 'IRRIGATION_NODE'
  | 'WATER_STORAGE_SHED'
  | 'HYDRO_GENERATOR'
  | 'ADVANCED_SOLAR'
  | 'BIOGAS_GENERATOR'
  | 'GEOTHERMAL_VENT'
  | 'BASIC_BATTERY'
  | 'ADVANCED_BATTERY'
  | 'BATTERY_BANK'
  | 'POWER_POLE'
  | 'NPC_CABIN'
  | 'BUNKHOUSE'
  | 'KITCHEN'
  | 'MEDIC_STATION'
  | 'WORKSHOP'
  | 'RESEARCH_LAB'
  | 'HYDRO_DOME'
  | 'SOLAR_TREE'
  | 'BIOREACTOR'
  | 'DRONE_DEPOT';

export interface BuildableDef {
  id: BuildableTypeId;
  displayName: string;
  description: string;
  costCoins: number;
  requiredMaterialId: string;
  requiredMaterialQty: number;
  materialName: string;
  gameplayEffect: string;
  previewColor: string;
  size: number;
}

export const BUILDABLE_CATALOG: Record<BuildableTypeId, BuildableDef> = {
  CABIN: {
    id: 'CABIN',
    displayName: 'Royal Cabin',
    description: 'Sturdy wooden lodge with a thatched roof. Rest here to fully restore thy vitality.',
    costCoins: 200,
    requiredMaterialId: 'material_bio_timber',
    requiredMaterialQty: 5,
    materialName: 'Hewn Timber',
    gameplayEffect: 'Resting spot — restores stamina fully at night',
    previewColor: '#8D6E63',
    size: 4.0,
  },
  GREENHOUSE: {
    id: 'GREENHOUSE',
    displayName: 'Crystal Conservatory',
    description: "Enchanted glass panels focusing the sun's rays. Nearby crops grow 1.3x faster.",
    costCoins: 500,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 4,
    materialName: 'Prismatic Glass',
    gameplayEffect: 'Crops within 5 units grow 1.3x faster',
    previewColor: '#80DEEA',
    size: 5.0,
  },
  SOLAR_PANEL: {
    id: 'SOLAR_PANEL',
    displayName: 'Mana Stone',
    description: 'Inscribed stone slab that gathers 10 Mana/day from direct sunlight.',
    costCoins: 300,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 3,
    materialName: 'Prismatic Glass',
    gameplayEffect: 'Gathers 10 Mana/day during sunlight',
    previewColor: '#FFD54F',
    size: 2.0,
  },
  WINDMILL: {
    id: 'WINDMILL',
    displayName: 'Aeolian Spire',
    description: "Tall wooden spire with canvas sails. Gathers 15 Mana/day from the island's breezes.",
    costCoins: 400,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 3,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Gathers 15 Mana/day 24/7',
    previewColor: '#80D8FF',
    size: 3.0,
  },
  STORAGE: {
    id: 'STORAGE',
    displayName: 'Strongbox Shed',
    description: 'Reinforced shed for storing kingdom goods. Increases inventory by +50 slots.',
    costCoins: 250,
    requiredMaterialId: 'material_bio_polymer',
    requiredMaterialQty: 2,
    materialName: 'Hardened Resin',
    gameplayEffect: '+50 inventory space',
    previewColor: '#00E5FF',
    size: 3.0,
  },
  WELL: {
    id: 'WELL',
    displayName: 'Sacred Well',
    description: "Stone-lined well tapping into the island's pure spirit springs. Drink freely.",
    costCoins: 150,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 2,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Infinite water source — drink anytime',
    previewColor: '#80CBC4',
    size: 2.0,
  },
  FENCE: {
    id: 'FENCE',
    displayName: 'Palisade Section',
    description: 'Sharpened wooden posts to defend and mark thy territory.',
    costCoins: 20,
    requiredMaterialId: 'material_bio_timber',
    requiredMaterialQty: 1,
    materialName: 'Hewn Timber',
    gameplayEffect: 'Decorative boundary section',
    previewColor: '#B0BEC5',
    size: 1.0,
  },
  COMPOST_BIN: {
    id: 'COMPOST_BIN',
    displayName: 'Alchemical Vat',
    description: 'Stone vat for fermenting offerings into sacred seeds.',
    costCoins: 100,
    requiredMaterialId: 'material_bio_polymer',
    requiredMaterialQty: 2,
    materialName: 'Hardened Resin',
    gameplayEffect: 'Converts 3 harvested crops into 1 random seed',
    previewColor: '#A1887F',
    size: 2.0,
  },
  RAIN_BARREL: {
    id: 'RAIN_BARREL',
    displayName: 'Water Basin',
    description: "Catches the heavens' tears (20 units/hr). Pure and safe for the kingdom.",
    costCoins: 80,
    requiredMaterialId: 'material_bio_timber',
    requiredMaterialQty: 2,
    materialName: 'Hewn Timber',
    gameplayEffect: 'Fills 20/hr in rain. Clean water.',
    previewColor: '#5D4037',
    size: 1.4,
  },
  WATER_FILTER: {
    id: 'WATER_FILTER',
    displayName: 'Holy Filter',
    description: 'Purifies raw stream water through blessed sands (1 unit/3 min).',
    costCoins: 150,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 2,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Converts raw water to clean water automatically',
    previewColor: '#00ACC1',
    size: 1.5,
  },
  WATER_PURIFIER: {
    id: 'WATER_PURIFIER',
    displayName: 'Alchemical Purifier',
    description: 'Advanced purifying station using focused sunbeams. Requires 2 Mana/hr.',
    costCoins: 400,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 3,
    materialName: 'Prismatic Glass',
    gameplayEffect: 'Requires 2 Mana/hr. Purifies 5 units/min',
    previewColor: '#00E5FF',
    size: 2.2,
  },
  IRRIGATION_PIPE: {
    id: 'IRRIGATION_PIPE',
    displayName: 'Stone Aqueduct',
    description: 'Carved stone segment for transporting the life-blood of the island.',
    costCoins: 80,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 1,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Transports water to irrigation nodes',
    previewColor: '#B87333',
    size: 1.0,
  },
  IRRIGATION_NODE: {
    id: 'IRRIGATION_NODE',
    displayName: 'Blessing Fountain',
    description: 'Small fountain that auto-waters nearby fields at the break of dawn.',
    costCoins: 40,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 1,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Auto-waters crops at 6 AM',
    previewColor: '#00B0FF',
    size: 1.0,
  },
  WATER_STORAGE_SHED: {
    id: 'WATER_STORAGE_SHED',
    displayName: 'Great Cistern',
    description: 'High-capacity stone basin holding up to 100 units of sacred water.',
    costCoins: 250,
    requiredMaterialId: 'material_bio_polymer',
    requiredMaterialQty: 3,
    materialName: 'Hardened Resin',
    gameplayEffect: 'Stores up to 100 units of water',
    previewColor: '#0288D1',
    size: 3.2,
  },
  HYDRO_GENERATOR: {
    id: 'HYDRO_GENERATOR',
    displayName: 'Great Water Wheel',
    description: "Massive oak wheel harnessing the river's fury. Gathers 20 Mana/day.",
    costCoins: 800,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 5,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Gathers 20 Mana/day (Must be near water)',
    previewColor: '#00E5FF',
    size: 3.0,
  },
  ADVANCED_SOLAR: {
    id: 'ADVANCED_SOLAR',
    displayName: 'Crystal Sun Obelisk',
    description: "Runically aligned obelisk that tracks the sun's path (+20% efficiency, 30 Mana/day).",
    costCoins: 1200,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 6,
    materialName: 'Prismatic Glass',
    gameplayEffect: 'Gathers 30 Mana/day (Sun-tracking)',
    previewColor: '#FFD54F',
    size: 4.0,
  },
  BIOGAS_GENERATOR: {
    id: 'BIOGAS_GENERATOR',
    displayName: 'Earth Spirit Vat',
    description: 'Stone dome converting farm waste into 12 Mana/day 24/7 constant energy.',
    costCoins: 600,
    requiredMaterialId: 'material_bio_polymer',
    requiredMaterialQty: 4,
    materialName: 'Hardened Resin',
    gameplayEffect: 'Gathers 12 Mana/day (Consumes 1 compost/day)',
    previewColor: '#81C784',
    size: 3.0,
  },
  GEOTHERMAL_VENT: {
    id: 'GEOTHERMAL_VENT',
    displayName: 'Volcanic Forge',
    description: "Taps the island's fiery core to provide heavy industrial 40 Mana/day.",
    costCoins: 2000,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 8,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Gathers 40 Mana/day continuous base-load',
    previewColor: '#FF7043',
    size: 4.0,
  },
  BASIC_BATTERY: {
    id: 'BASIC_BATTERY',
    displayName: 'Mana Vessel',
    description: 'Compact stone basin holding 50 Mana essence.',
    costCoins: 200,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 2,
    materialName: 'Enchanted Alloy',
    gameplayEffect: '+50 Mana capacity',
    previewColor: '#29B6F6',
    size: 2.0,
  },
  ADVANCED_BATTERY: {
    id: 'ADVANCED_BATTERY',
    displayName: 'Greater Mana Vessel',
    description: 'Reinforced stone basin holding 150 Mana essence with glowing runes.',
    costCoins: 500,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 4,
    materialName: 'Enchanted Alloy',
    gameplayEffect: '+150 Mana capacity',
    previewColor: '#00B0FF',
    size: 3.0,
  },
  BATTERY_BANK: {
    id: 'BATTERY_BANK',
    displayName: 'Royal Mana Vault',
    description: 'Large-scale crystalline vault holding 500 Mana essence for the kingdom.',
    costCoins: 1200,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 6,
    materialName: 'Prismatic Glass',
    gameplayEffect: '+500 Mana capacity',
    previewColor: '#00E5FF',
    size: 5.0,
  },
  POWER_POLE: {
    id: 'POWER_POLE',
    displayName: 'Ley Line Conduit',
    description: "Iron-bound pole to extend the kingdom's mana grid reach.",
    costCoins: 30,
    requiredMaterialId: 'material_bio_timber',
    requiredMaterialQty: 1,
    materialName: 'Hewn Timber',
    gameplayEffect: 'Extends mana grid reach',
    previewColor: '#8D6E63',
    size: 1.0,
  },
  NPC_CABIN: {
    id: 'NPC_CABIN',
    displayName: "Citizens' Cottage",
    description: 'Cozy wooden cottage for 2 loyal subjects. Provides high loyalty and rest.',
    costCoins: 300,
    requiredMaterialId: 'material_bio_timber',
    requiredMaterialQty: 3,
    materialName: 'Hewn Timber',
    gameplayEffect: 'Housing for 2 citizens (+High Loyalty)',
    previewColor: '#8D6E63',
    size: 3.0,
  },
  BUNKHOUSE: {
    id: 'BUNKHOUSE',
    displayName: 'Royal Barracks',
    description: "Spacious communal lodge for 6 citizens. Expanding the kingdom's reach.",
    costCoins: 800,
    requiredMaterialId: 'material_bio_timber',
    requiredMaterialQty: 6,
    materialName: 'Hewn Timber',
    gameplayEffect: 'Housing for 6 citizens',
    previewColor: '#795548',
    size: 5.0,
  },
  KITCHEN: {
    id: 'KITCHEN',
    displayName: 'Royal Hearth',
    description: 'Large communal kitchen where citizens gather to feast on thy bountiful harvests.',
    costCoins: 500,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 4,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Citizens eat here. Requires 1 Mana/hr.',
    previewColor: '#FFB74D',
    size: 4.0,
  },
  MEDIC_STATION: {
    id: 'MEDIC_STATION',
    displayName: "Healer's Sanctum",
    description: 'Serene clinic where station Medics heal thy wounds and brew elixirs.',
    costCoins: 600,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 4,
    materialName: 'Prismatic Glass',
    gameplayEffect: 'Heals & cures sickness. Requires 1 Mana/hr.',
    previewColor: '#E57373',
    size: 3.0,
  },
  WORKSHOP: {
    id: 'WORKSHOP',
    displayName: "Master's Forge",
    description: 'Heavy-duty forge for crafting legendary items and royal tools.',
    costCoins: 450,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 4,
    materialName: 'Enchanted Alloy',
    gameplayEffect: 'Crafting speed +30% with Engineer. Requires 3 Mana/hr.',
    previewColor: '#90A4AE',
    size: 3.0,
  },
  RESEARCH_LAB: {
    id: 'RESEARCH_LAB',
    displayName: 'Alchemy Chamber',
    description: 'Sacred laboratory where researchers discover forgotten island secrets.',
    costCoins: 800,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 5,
    materialName: 'Prismatic Glass',
    gameplayEffect: 'Generates +2 Prestige/day. Requires 2 Mana/hr.',
    previewColor: '#80DEEA',
    size: 4.0,
  },
  HYDRO_DOME: {
    id: 'HYDRO_DOME',
    displayName: 'Geodesic Hydroponic Dome',
    description: 'Faceted prismatic glass dome with aeroponic misting spirals. Accelerates all internal crop growth by 2.0x.',
    costCoins: 650,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 5,
    materialName: 'Solar Glass',
    gameplayEffect: 'Internal aeroponics accelerates crop growth by 2.0x',
    previewColor: '#00E5FF',
    size: 4.5,
  },
  SOLAR_TREE: {
    id: 'SOLAR_TREE',
    displayName: 'Kinetic Solar-Tree Collector',
    description: 'Sculptural biomimetic tree with photovoltaic leaf arrays tracking the sun. Gathers 25 kWh clean power/day.',
    costCoins: 450,
    requiredMaterialId: 'material_solar_glass',
    requiredMaterialQty: 4,
    materialName: 'Solar Glass',
    gameplayEffect: 'Dynamic sun tracking generates 25 kWh clean energy/day',
    previewColor: '#FFD54F',
    size: 3.0,
  },
  BIOREACTOR: {
    id: 'BIOREACTOR',
    displayName: 'Algae Spirulina Bioreactor',
    description: 'Twin bioluminescent glass tubes cultivating spirulina microalgae. Produces rich bio-fertilizer and superfood biomass.',
    costCoins: 520,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 4,
    materialName: 'Eco Alloy',
    gameplayEffect: 'Produces organic bio-fertilizer (+15% harvest quality)',
    previewColor: '#00E676',
    size: 2.5,
  },
  DRONE_DEPOT: {
    id: 'DRONE_DEPOT',
    displayName: 'Autonomous Eco-Drone Depot',
    description: 'Solar charging pad and launch tower for automated agro-drones that patrol the sanctuary and deliver supplies.',
    costCoins: 700,
    requiredMaterialId: 'material_eco_alloy',
    requiredMaterialQty: 5,
    materialName: 'Eco Alloy',
    gameplayEffect: 'Launches automated harvesting patrol drones across the valley',
    previewColor: '#7C4DFF',
    size: 3.5,
  },
};

export interface PlacedBuildingEntity {
  id: number;
  buildingType: BuildableTypeId;
  posX: number;
  posY: number;
  posZ: number;
  rotationDeg: number;
  level: number;
  waterStored: number;
  customData?: string;
}

export type EnergyNodeTypeId =
  | 'PHOTOVOLTAIC_ARRAY'
  | 'VERTICAL_WIND_TURBINE'
  | 'BIOGAS_DIGESTER'
  | 'BATTERY_STORAGE_BANK';

export interface EnergyNodeEntity {
  id: number;
  nodeType: EnergyNodeTypeId;
  level: number;
  efficiency: number;
  posX: number;
  posY: number;
  posZ: number;
  isActive: boolean;
}

export type LivestockTypeId =
  | 'SOLAR_SHEEP'
  | 'CYBER_BOVINE'
  | 'CHICKEN'
  | 'ROBO_BEE_POLLINATOR'
  | 'MEADOW_ALPACA';

export interface LivestockDef {
  id: LivestockTypeId;
  displayName: string;
  description: string;
  productProduced: string;
  produceItemId: string;
  produceIntervalSec: number;
  productSellPrice: number;
  purchaseCostCoins: number;
  tintColor: string;
}

export const LIVESTOCK_CATALOG: Record<LivestockTypeId, LivestockDef> = {
  SOLAR_SHEEP: {
    id: 'SOLAR_SHEEP',
    displayName: 'Golden Sheep',
    description: 'Gentle creatures with golden fleece that gather sun essence.',
    productProduced: 'Golden Wool',
    produceItemId: 'solar_wool',
    produceIntervalSec: 20,
    productSellPrice: 75,
    purchaseCostCoins: 250,
    tintColor: '#FFF9C4',
  },
  CYBER_BOVINE: {
    id: 'CYBER_BOVINE',
    displayName: 'Royal Cow',
    description: 'Noble beast producing sacred milk and fertile offerings.',
    productProduced: 'Royal Milk',
    produceItemId: 'royal_milk',
    produceIntervalSec: 25,
    productSellPrice: 110,
    purchaseCostCoins: 380,
    tintColor: '#E0E0E0',
  },
  CHICKEN: {
    id: 'CHICKEN',
    displayName: 'Island Hen',
    description: 'A hardy island chicken laying sun-touched eggs.',
    productProduced: 'Egg',
    produceItemId: 'egg',
    produceIntervalSec: 30,
    productSellPrice: 25,
    purchaseCostCoins: 80,
    tintColor: '#FFF8E1',
  },
  ROBO_BEE_POLLINATOR: {
    id: 'ROBO_BEE_POLLINATOR',
    displayName: 'Mystic Bees',
    description: 'Ethereal bees that dramatically enhance thy harvests.',
    productProduced: 'Forest Honey',
    produceItemId: 'solar_honey',
    produceIntervalSec: 15,
    productSellPrice: 60,
    purchaseCostCoins: 180,
    tintColor: '#FFD700',
  },
  MEADOW_ALPACA: {
    id: 'MEADOW_ALPACA',
    displayName: 'Cloud Alpaca',
    description: 'Gentle companion producing ultra-soft divine fiber.',
    productProduced: 'Alpaca Yarn',
    produceItemId: 'alpaca_yarn',
    produceIntervalSec: 30,
    productSellPrice: 140,
    purchaseCostCoins: 450,
    tintColor: '#D7CCC8',
  },
};

export interface LivestockEntity {
  id: number;
  type: LivestockTypeId;
  name: string;
  happiness: number; // 0 to 100
  hunger: number; // 0 (full) to 100 (starving)
  age: number;
  produceProgress: number; // 0.0 to 1.0
  readyToHarvest: boolean;
  posX: number;
  posY: number;
  posZ: number;
  targetX: number;
  targetZ: number;
}

export type WeatherType =
  | 'SUNNY_CLEAR'
  | 'CLOUDY_OVERCAST'
  | 'RAINY_STORM'
  | 'STORM'
  | 'HEATWAVE'
  | 'WIND_GALE'
  | 'MISTY_NEBULA';

export interface WeatherDef {
  id: WeatherType;
  displayName: string;
  description: string;
  solarMultiplier: number;
  windMultiplier: number;
  cropGrowthMultiplier: number;
  windSpeedKmh: number;
  autoWaterRain: boolean;
  colorOverlay: string;
}

export const WEATHER_CATALOG: Record<WeatherType, WeatherDef> = {
  SUNNY_CLEAR: {
    id: 'SUNNY_CLEAR',
    displayName: 'Clear',
    description: '☀️ Optimal solar radiation with crisp blue skies.',
    solarMultiplier: 1.0,
    windMultiplier: 1.0,
    cropGrowthMultiplier: 1.0,
    windSpeedKmh: 12.0,
    autoWaterRain: false,
    colorOverlay: 'rgba(255, 255, 255, 0)',
  },
  CLOUDY_OVERCAST: {
    id: 'CLOUDY_OVERCAST',
    displayName: 'Cloudy',
    description: '☁️ Overcast skies; solar output -30%.',
    solarMultiplier: 0.7,
    windMultiplier: 1.0,
    cropGrowthMultiplier: 1.0,
    windSpeedKmh: 20.0,
    autoWaterRain: false,
    colorOverlay: 'rgba(96, 125, 139, 0.15)',
  },
  RAINY_STORM: {
    id: 'RAINY_STORM',
    displayName: 'Rain',
    description: '🌧️ Permaculture rain; crops grow 2x faster, solar output -60%.',
    solarMultiplier: 0.4,
    windMultiplier: 1.1,
    cropGrowthMultiplier: 2.0,
    windSpeedKmh: 28.0,
    autoWaterRain: true,
    colorOverlay: 'rgba(55, 71, 79, 0.3)',
  },
  STORM: {
    id: 'STORM',
    displayName: 'Storm',
    description: '⛈️ Fierce thunderstorm! Windmill +50%, solar -90%, lightning flashes.',
    solarMultiplier: 0.1,
    windMultiplier: 1.5,
    cropGrowthMultiplier: 1.5,
    windSpeedKmh: 48.0,
    autoWaterRain: true,
    colorOverlay: 'rgba(26, 35, 46, 0.5)',
  },
  HEATWAVE: {
    id: 'HEATWAVE',
    displayName: 'Heatwave',
    description: '🔥 Pale yellow skies. Solar +30%, wind -50%, thirst 2x faster.',
    solarMultiplier: 1.3,
    windMultiplier: 0.5,
    cropGrowthMultiplier: 1.0,
    windSpeedKmh: 6.0,
    autoWaterRain: false,
    colorOverlay: 'rgba(255, 213, 79, 0.2)',
  },
  WIND_GALE: {
    id: 'WIND_GALE',
    displayName: 'Gale',
    description: '💨 Strong gale winds.',
    solarMultiplier: 0.5,
    windMultiplier: 1.6,
    cropGrowthMultiplier: 1.0,
    windSpeedKmh: 55.0,
    autoWaterRain: false,
    colorOverlay: 'rgba(0, 229, 255, 0.15)',
  },
  MISTY_NEBULA: {
    id: 'MISTY_NEBULA',
    displayName: 'Misty',
    description: '🌫️ Low-lying fog over the fields.',
    solarMultiplier: 0.6,
    windMultiplier: 0.8,
    cropGrowthMultiplier: 1.0,
    windSpeedKmh: 8.0,
    autoWaterRain: false,
    colorOverlay: 'rgba(128, 203, 196, 0.15)',
  },
};

export type PlayerTool =
  | 'HAND'
  | 'WATER_CAN'
  | 'FERTILIZER'
  | 'SOLAR_WRENCH'
  | 'SHEARS'
  | 'SEED_POUCH'
  | 'FISHING_ROD';

export interface PlayerToolDef {
  id: PlayerTool;
  displayName: string;
  icon: string;
  description: string;
}

export const PLAYER_TOOLS: PlayerToolDef[] = [
  { id: 'HAND', displayName: 'Free Hands', icon: 'Hand', description: 'Inspect, pet animals, and pick ripe crops.' },
  { id: 'WATER_CAN', displayName: 'Blessing Vessel', icon: 'Droplets', description: 'Hydrate dry fields with sacred water.' },
  { id: 'FERTILIZER', displayName: 'Earth Vitality', icon: 'Sparkles', description: 'Accelerate plant growth with alchemical salts.' },
  { id: 'SOLAR_WRENCH', displayName: 'Mystic Hammer', icon: 'Hammer', description: 'Construct, repair, or upgrade arcane kingdom nodes.' },
  { id: 'SHEARS', displayName: 'Iron Shears', icon: 'Scissors', description: 'Gently shear wool and collect alpaca yarn.' },
  { id: 'SEED_POUCH', displayName: 'Seed Pouch', icon: 'Sprout', description: 'Sow chosen crops into fertile fields.' },
  { id: 'FISHING_ROD', displayName: 'Fishing Rod', icon: 'Fish', description: 'Cast into rivers, streams, and ponds to catch wild fish.' },
];

export type ItemCategory =
  | 'SEEDS'
  | 'HARVEST'
  | 'PRODUCE'
  | 'CRAFTED'
  | 'TECH_UPGRADE'
  | 'TOOL';

export interface InventoryEntity {
  itemId: string;
  name: string;
  category: ItemCategory;
  quantity: number;
  sellValue: number;
}

export interface ContractEntity {
  id: string;
  title: string;
  buyerName: string;
  requiredItemId: string;
  requiredItemName: string;
  requiredQty: number;
  currentQty: number;
  rewardCoins: number;
  rewardEco: number;
  isClaimed: boolean;
}

export type FishTypeId =
  | 'TROUT'
  | 'BASS'
  | 'CATFISH'
  | 'CARP'
  | 'GOLDEN_FISH';

export interface FishDef {
  id: FishTypeId;
  displayName: string;
  rarity: 'Common' | 'Uncommon' | 'Rare';
  sellPrice: number;
  hungerRestoreRaw: number;
  hungerRestoreCooked: number;
  healthBonus: number;
  baseCatchWeight: number;
  colorHex: string;
}

export const FISH_CATALOG: Record<FishTypeId, FishDef> = {
  TROUT: {
    id: 'TROUT',
    displayName: 'Rainbow Trout',
    rarity: 'Common',
    sellPrice: 15,
    hungerRestoreRaw: 20,
    hungerRestoreCooked: 30,
    healthBonus: 0,
    baseCatchWeight: 35,
    colorHex: '#FF8A80',
  },
  BASS: {
    id: 'BASS',
    displayName: 'Largemouth Bass',
    rarity: 'Common',
    sellPrice: 25,
    hungerRestoreRaw: 25,
    hungerRestoreCooked: 35,
    healthBonus: 0,
    baseCatchWeight: 30,
    colorHex: '#81C784',
  },
  CATFISH: {
    id: 'CATFISH',
    displayName: 'Whisker Catfish',
    rarity: 'Uncommon',
    sellPrice: 40,
    hungerRestoreRaw: 30,
    hungerRestoreCooked: 40,
    healthBonus: 0,
    baseCatchWeight: 20,
    colorHex: '#90A4AE',
  },
  CARP: {
    id: 'CARP',
    displayName: 'River Carp',
    rarity: 'Uncommon',
    sellPrice: 50,
    hungerRestoreRaw: 30,
    hungerRestoreCooked: 45,
    healthBonus: 0,
    baseCatchWeight: 10,
    colorHex: '#FFB74D',
  },
  GOLDEN_FISH: {
    id: 'GOLDEN_FISH',
    displayName: 'Golden Sun Fish',
    rarity: 'Rare',
    sellPrice: 150,
    hungerRestoreRaw: 50,
    hungerRestoreCooked: 60,
    healthBonus: 5,
    baseCatchWeight: 5,
    colorHex: '#FFD700',
  },
};

export type FishingSpotTypeId =
  | 'GREEN_VALLEY_STREAM'
  | 'WETLAND_RIVER'
  | 'WETLAND_POND';

export interface FishingSpotDef {
  id: FishingSpotTypeId;
  displayName: string;
  worldX: number;
  worldZ: number;
  maxPopulation: number;
}

export const FISHING_SPOTS: Record<FishingSpotTypeId, FishingSpotDef> = {
  GREEN_VALLEY_STREAM: {
    id: 'GREEN_VALLEY_STREAM',
    displayName: 'Valley Mountain Stream',
    worldX: 16.0,
    worldZ: -4.0,
    maxPopulation: 10,
  },
  WETLAND_RIVER: {
    id: 'WETLAND_RIVER',
    displayName: 'Wetland River Delta',
    worldX: -24.0,
    worldZ: -98.0,
    maxPopulation: 10,
  },
  WETLAND_POND: {
    id: 'WETLAND_POND',
    displayName: 'Wetland Lily Pond',
    worldX: 20.0,
    worldZ: -120.0,
    maxPopulation: 10,
  },
};

export type BiomeTypeId = 'green_valley' | 'deep_forest' | 'wetland';

export interface BiomeDef {
  id: BiomeTypeId;
  displayName: string;
  description: string;
  centerZ: number;
  mapColor: string;
}

export const BIOMES: Record<BiomeTypeId, BiomeDef> = {
  green_valley: {
    id: 'green_valley',
    displayName: 'Green Valley',
    description: 'Sun-drenched meadows, sustainable permaculture plots, and heritage farmlands.',
    centerZ: 0.0,
    mapColor: '#4CAF50',
  },
  deep_forest: {
    id: 'deep_forest',
    displayName: 'Deep Forest',
    description: 'Ancient towering canopies, bioluminescent flora, and wildlife habitats.',
    centerZ: 100.0,
    mapColor: '#1B5E20',
  },
  wetland: {
    id: 'wetland',
    displayName: 'Wetland & River',
    description: 'Scenic winding river delta, reed beds, aquatic fauna, and rich river clay.',
    centerZ: -100.0,
    mapColor: '#00897B',
  },
};

export interface PointOfInterestDef {
  id: string;
  displayName: string;
  biome: BiomeTypeId;
  worldX: number;
  worldZ: number;
  discoveryRewardTitle: string;
  discoveryRewardDesc: string;
  rewardCoins: number;
  rewardEcoScore: number;
  rewardItemName: string;
}

export const POINTS_OF_INTEREST: PointOfInterestDef[] = [
  {
    id: 'poi_old_farm',
    displayName: 'Forgotten Citadel',
    biome: 'green_valley',
    worldX: 6.0,
    worldZ: 0.0,
    discoveryRewardTitle: 'Citadel Territory',
    discoveryRewardDesc: "Thy kingdom's heart and mana nexus",
    rewardCoins: 50,
    rewardEcoScore: 5,
    rewardItemName: 'Heritage Seeds',
  },
  {
    id: 'poi_old_well',
    displayName: 'Abandoned Old Well',
    biome: 'green_valley',
    worldX: -18.0,
    worldZ: -15.0,
    discoveryRewardTitle: 'Historical Well Found!',
    discoveryRewardDesc: 'Gives lore about previous eco-settlers and water filtration notes',
    rewardCoins: 100,
    rewardEcoScore: 10,
    rewardItemName: 'Pure Mineral Water',
  },
  {
    id: 'poi_resource_cave',
    displayName: 'Crystal Resource Cave',
    biome: 'green_valley',
    worldX: -28.0,
    worldZ: 20.0,
    discoveryRewardTitle: 'Mineral Deposit Found!',
    discoveryRewardDesc: 'Rich vein of iron ore, quartz, and construction minerals',
    rewardCoins: 140,
    rewardEcoScore: 10,
    rewardItemName: 'Iron Ore Chunk',
  },
  {
    id: 'poi_broken_windmill',
    displayName: 'Derelict Windmill',
    biome: 'green_valley',
    worldX: 25.0,
    worldZ: -30.0,
    discoveryRewardTitle: 'Derelict Windmill Discovered!',
    discoveryRewardDesc: 'Repairable turbine structure. +1 Free Vertical Wind Turbine',
    rewardCoins: 160,
    rewardEcoScore: 15,
    rewardItemName: 'Wind Turbine Blade',
  },
  {
    id: 'poi_radio_tower',
    displayName: 'Deep Forest Radio Tower',
    biome: 'deep_forest',
    worldX: 45.0,
    worldZ: 120.0,
    discoveryRewardTitle: 'Radio Tower Located!',
    discoveryRewardDesc: 'Broadcast antenna repaired! Researcher NPC signal detected',
    rewardCoins: 200,
    rewardEcoScore: 20,
    rewardItemName: 'Radio Transceiver',
  },
  {
    id: 'poi_research_facility',
    displayName: 'Ancient Mage Academy',
    biome: 'deep_forest',
    worldX: -45.0,
    worldZ: 160.0,
    discoveryRewardTitle: 'Academy Discovered!',
    discoveryRewardDesc: 'Abandoned magical complex with encrypted scrolls & volcanic forge blueprints',
    rewardCoins: 300,
    rewardEcoScore: 30,
    rewardItemName: 'Volcanic Blueprint',
  },
  {
    id: 'poi_stone_circle',
    displayName: 'Mysterious Stone Circle',
    biome: 'deep_forest',
    worldX: -12.0,
    worldZ: 148.0,
    discoveryRewardTitle: 'Ancient Leyline Resonance!',
    discoveryRewardDesc: 'Harmonic resonance elevates farm ecosystem vitality',
    rewardCoins: 250,
    rewardEcoScore: 20,
    rewardItemName: 'Bioluminescent Spore',
  },
  {
    id: 'poi_forest_campsite',
    displayName: 'Old Forest Campsite',
    biome: 'deep_forest',
    worldX: -32.0,
    worldZ: 85.0,
    discoveryRewardTitle: 'Survival Supplies Found!',
    discoveryRewardDesc: 'Recovered emergency rations and medicinal herbs',
    rewardCoins: 100,
    rewardEcoScore: 5,
    rewardItemName: 'Medicinal Herb',
  },
  {
    id: 'poi_ranger_tower',
    displayName: 'Abandoned Ranger Tower',
    biome: 'deep_forest',
    worldX: 14.0,
    worldZ: 110.0,
    discoveryRewardTitle: 'Tower Lookout Unlocked!',
    discoveryRewardDesc: 'Discovered surveyor notes (+Hardwood & Maps)',
    rewardCoins: 150,
    rewardEcoScore: 10,
    rewardItemName: 'Aged Hardwood',
  },
  {
    id: 'poi_hidden_waterfall',
    displayName: 'Hidden Forest Waterfall',
    biome: 'deep_forest',
    worldX: -50.0,
    worldZ: 90.0,
    discoveryRewardTitle: 'Hidden Springs Found!',
    discoveryRewardDesc: 'Pristine mountain cascade heals player & boosts max health (+10 HP)',
    rewardCoins: 180,
    rewardEcoScore: 25,
    rewardItemName: 'Cascade Spring Essence',
  },
  {
    id: 'poi_broken_bridge',
    displayName: 'Broken Timber Bridge',
    biome: 'wetland',
    worldX: 16.0,
    worldZ: -75.0,
    discoveryRewardTitle: 'Historic River Span!',
    discoveryRewardDesc: 'Discovered aquatic engineering blueprint',
    rewardCoins: 120,
    rewardEcoScore: 8,
    rewardItemName: 'River Reeds',
  },
  {
    id: 'poi_fishing_hut',
    displayName: 'Old Fishing Hut',
    biome: 'wetland',
    worldX: -24.0,
    worldZ: -95.0,
    discoveryRewardTitle: 'Master Angler Blueprint!',
    discoveryRewardDesc: 'Unlocked fishing tackle and aquaculture techniques',
    rewardCoins: 180,
    rewardEcoScore: 12,
    rewardItemName: 'Golden Fish Lure',
  },
  {
    id: 'poi_sunken_boat',
    displayName: 'Sunken Supply Boat',
    biome: 'wetland',
    worldX: 35.0,
    worldZ: -135.0,
    discoveryRewardTitle: 'Sunken Cargo Salvaged!',
    discoveryRewardDesc: 'Recovered clean river clay and rare alloy fasteners',
    rewardCoins: 200,
    rewardEcoScore: 15,
    rewardItemName: 'Pure River Clay',
  },
  {
    id: 'poi_ancient_tree',
    displayName: 'Ancient Banyan Tree',
    biome: 'wetland',
    worldX: -15.0,
    worldZ: -140.0,
    discoveryRewardTitle: 'Ancient Banyan Discovered!',
    discoveryRewardDesc: 'Historic sentinel tree of the valley. +50 Research Points & Ancient Lore',
    rewardCoins: 300,
    rewardEcoScore: 40,
    rewardItemName: 'Ancient Seed Pod',
  },
  {
    id: 'poi_hot_spring',
    displayName: 'Geothermal Hot Spring',
    biome: 'wetland',
    worldX: 40.0,
    worldZ: -110.0,
    discoveryRewardTitle: 'Natural Geothermal Spring!',
    discoveryRewardDesc: 'Soothing mineral waters heal health & completely restore stamina',
    rewardCoins: 220,
    rewardEcoScore: 20,
    rewardItemName: 'Mineral Salt',
  },
];

export type PowerPriorityLevel =
  | 'CRITICAL'
  | 'IMPORTANT'
  | 'NORMAL'
  | 'COMFORT'
  | 'DECORATIVE';

export interface PowerPriorityDef {
  level: number;
  displayName: string;
  thresholdPercent: number;
  color: string;
  description: string;
}

export const POWER_PRIORITIES: Record<PowerPriorityLevel, PowerPriorityDef> = {
  CRITICAL: {
    level: 1,
    displayName: 'Critical',
    thresholdPercent: 0.0,
    color: '#FF5252',
    description: 'Always powered (if battery > 0%)',
  },
  IMPORTANT: {
    level: 2,
    displayName: 'Important',
    thresholdPercent: 0.3,
    color: '#FF9800',
    description: 'Powered if battery > 30%',
  },
  NORMAL: {
    level: 3,
    displayName: 'Normal',
    thresholdPercent: 0.5,
    color: '#FFD54F',
    description: 'Powered if battery > 50%',
  },
  COMFORT: {
    level: 4,
    displayName: 'Comfort',
    thresholdPercent: 0.7,
    color: '#81C784',
    description: 'Powered if battery > 70%',
  },
  DECORATIVE: {
    level: 5,
    displayName: 'Decorative',
    thresholdPercent: 0.9,
    color: '#80D8FF',
    description: 'Powered if battery > 90%',
  },
};

export type DeviceTypeId =
  | 'REFRIGERATOR'
  | 'WATER_PURIFIER'
  | 'MEDICAL_STATION'
  | 'GREENHOUSE_HEATER'
  | 'WATER_PUMP'
  | 'WORKSHOP'
  | 'LIGHTS'
  | 'COOKING_STATION'
  | 'ELECTRIC_FENCE'
  | 'TV_ENTERTAINMENT'
  | 'VENTILATION_FAN'
  | 'PATIO_FOUNTAIN'
  | 'AMBIENT_LIGHTING'
  | 'DECORATIVE_FOUNTAIN';

export interface DeviceDef {
  id: DeviceTypeId;
  displayName: string;
  tierLabel: string;
  consumptionKwhPerHour: number;
  defaultPriority: PowerPriorityLevel;
  category: string;
}

export const DEVICE_CATALOG: Record<DeviceTypeId, DeviceDef> = {
  REFRIGERATOR: { id: 'REFRIGERATOR', displayName: 'Cold Storage Cellar', tierLabel: 'Tier 1 (Critical)', consumptionKwhPerHour: 1.5, defaultPriority: 'CRITICAL', category: 'Food Preservation' },
  WATER_PURIFIER: { id: 'WATER_PURIFIER', displayName: 'Alchemical Purifier', tierLabel: 'Tier 1 (Critical)', consumptionKwhPerHour: 2.0, defaultPriority: 'CRITICAL', category: 'Hydration' },
  MEDICAL_STATION: { id: 'MEDICAL_STATION', displayName: "Healer's Sanctum", tierLabel: 'Tier 1 (Critical)', consumptionKwhPerHour: 1.0, defaultPriority: 'CRITICAL', category: 'Healthcare' },
  GREENHOUSE_HEATER: { id: 'GREENHOUSE_HEATER', displayName: 'Conservatory Hearth', tierLabel: 'Tier 2 (Important)', consumptionKwhPerHour: 4.0, defaultPriority: 'IMPORTANT', category: 'Crop Climate' },
  WATER_PUMP: { id: 'WATER_PUMP', displayName: 'Aqueduct Sluice', tierLabel: 'Tier 2 (Important)', consumptionKwhPerHour: 1.0, defaultPriority: 'IMPORTANT', category: 'Irrigation' },
  WORKSHOP: { id: 'WORKSHOP', displayName: "Artisan's Forge", tierLabel: 'Tier 2 (Important)', consumptionKwhPerHour: 3.0, defaultPriority: 'IMPORTANT', category: 'Crafting' },
  LIGHTS: { id: 'LIGHTS', displayName: 'Citadel Lanterns', tierLabel: 'Tier 3 (Normal)', consumptionKwhPerHour: 0.5, defaultPriority: 'NORMAL', category: 'Lighting' },
  COOKING_STATION: { id: 'COOKING_STATION', displayName: 'Arcane Hearth', tierLabel: 'Tier 3 (Normal)', consumptionKwhPerHour: 1.0, defaultPriority: 'NORMAL', category: 'Cooking' },
  ELECTRIC_FENCE: { id: 'ELECTRIC_FENCE', displayName: 'Perimeter Shock Fence', tierLabel: 'Tier 3 (Normal)', consumptionKwhPerHour: 0.5, defaultPriority: 'NORMAL', category: 'Sanctuary Security' },
  TV_ENTERTAINMENT: { id: 'TV_ENTERTAINMENT', displayName: 'Entertainment Terminal', tierLabel: 'Tier 4 (Comfort)', consumptionKwhPerHour: 0.8, defaultPriority: 'COMFORT', category: 'Recreation' },
  VENTILATION_FAN: { id: 'VENTILATION_FAN', displayName: 'Living Quarters Fan', tierLabel: 'Tier 4 (Comfort)', consumptionKwhPerHour: 0.3, defaultPriority: 'COMFORT', category: 'Air Circulation' },
  PATIO_FOUNTAIN: { id: 'PATIO_FOUNTAIN', displayName: 'Patio Water Fountain', tierLabel: 'Tier 4 (Comfort)', consumptionKwhPerHour: 0.4, defaultPriority: 'COMFORT', category: 'Aesthetics' },
  AMBIENT_LIGHTING: { id: 'AMBIENT_LIGHTING', displayName: 'Pathway Ambient Glow', tierLabel: 'Tier 5 (Decorative)', consumptionKwhPerHour: 0.2, defaultPriority: 'DECORATIVE', category: 'Landscape Decor' },
  DECORATIVE_FOUNTAIN: { id: 'DECORATIVE_FOUNTAIN', displayName: 'Garden Fountain', tierLabel: 'Tier 5 (Decorative)', consumptionKwhPerHour: 0.3, defaultPriority: 'DECORATIVE', category: 'Landscape Decor' },
};

export type NpcRoleId =
  | 'role_farmer'
  | 'role_engineer'
  | 'role_builder'
  | 'role_researcher'
  | 'role_medic';

export interface NpcRoleDef {
  id: NpcRoleId;
  displayName: string;
  title: string;
  description: string;
  color: string;
}

export const NPC_ROLES: Record<NpcRoleId, NpcRoleDef> = {
  role_farmer: { id: 'role_farmer', displayName: 'Farmer', title: 'Agronomist & Cultivator', description: 'Waters & harvests crops every morning, deposits harvest in storage, boosts crop growth +10%', color: '#81C784' },
  role_engineer: { id: 'role_engineer', displayName: 'Engineer', title: 'Grid & Systems Technician', description: 'Optimizes energy grid (+5% efficiency), repairs devices, warns before failures', color: '#00E5FF' },
  role_builder: { id: 'role_builder', displayName: 'Builder', title: 'Master Architect & Fabricator', description: 'Speeds up construction by 50%, upgrades buildings, maintains structural integrity', color: '#FFD54F' },
  role_researcher: { id: 'role_researcher', displayName: 'Researcher', title: 'Clean-Tech Scientist', description: 'Generates +2 Research Points/day, accelerates blueprint discoveries and unlocks tech 20% faster', color: '#BA68C8' },
  role_medic: { id: 'role_medic', displayName: 'Medic', title: 'Physician & Herbalist', description: 'Passively heals player (+1 HP/hr in settlement), cures sickness instantly, brews herbal medicine', color: '#FF5252' },
};

export interface NpcEntity {
  id: number;
  name: string;
  role: NpcRoleId;
  age: number;
  skillLevel: number;
  trait: string;
  morale: number; // 0 to 100
  hunger: number; // 0 to 100
  thirst: number; // 0 to 100
  assignedBedId: number | null;
  assignedBuildingId: number | null;
  currentActivity: string;
  posX: number;
  posY: number;
  posZ: number;
  speechBubble: string | null;
  speechBubbleTimer: number;
  relationshipToPlayer: number;
}

export interface ResearchTreeNode {
  id: string;
  tier: number;
  title: string;
  rpCost: number;
  description: string;
  unlockFeature: string;
  prerequisiteTechIds: string[];
  isUnlocked: boolean;
}

export const RESEARCH_TECHS: ResearchTreeNode[] = [
  {
    id: 'tech_permaculture_mastery',
    tier: 1,
    title: 'Heritage Soil Chemistry',
    rpCost: 15,
    description: 'Ancient bio-compost principles allowing crops to retain moisture 30% longer.',
    unlockFeature: 'Crops need 30% less water',
    prerequisiteTechIds: [],
    isUnlocked: false,
  },
  {
    id: 'tech_aqueduct_flow',
    tier: 1,
    title: 'Blessed Sluice Engineering',
    rpCost: 20,
    description: 'Enables construction of automatic stone aqueducts and blessing fountains.',
    unlockFeature: 'Unlocks Stone Aqueducts & Blessing Fountains',
    prerequisiteTechIds: [],
    isUnlocked: false,
  },
  {
    id: 'tech_solar_focus',
    tier: 2,
    title: 'Prismatic Lens Grinding',
    rpCost: 35,
    description: 'Cut crystals that focus solar warmth, boosting solar panel generation by +25%.',
    unlockFeature: 'Mana stones +25% generation',
    prerequisiteTechIds: ['tech_permaculture_mastery'],
    isUnlocked: false,
  },
  {
    id: 'tech_aeolian_resonance',
    tier: 2,
    title: 'Aeolian Sail Tuning',
    rpCost: 40,
    description: 'Enchanted canvas sails that spin smoothly in gentle breeze (+30% wind output).',
    unlockFeature: 'Aeolian Spire +30% power',
    prerequisiteTechIds: ['tech_aqueduct_flow'],
    isUnlocked: false,
  },
  {
    id: 'tech_deep_vault',
    tier: 3,
    title: 'Runic Mana Storage',
    rpCost: 60,
    description: 'Inscribes containment runes inside stone vessels, unlocking Greater Mana Vaults.',
    unlockFeature: 'Unlocks Royal Mana Vault (+500 kWh)',
    prerequisiteTechIds: ['tech_solar_focus', 'tech_aeolian_resonance'],
    isUnlocked: false,
  },
  {
    id: 'tech_new_dawn',
    tier: 4,
    title: 'Project New Dawn - Harmonic Island Nexus',
    rpCost: 100,
    description: 'Restores the grand ancient nexus of the island, uniting nature and clean arcane power!',
    unlockFeature: 'Triggers the Story Ending Epilogue!',
    prerequisiteTechIds: ['tech_deep_vault'],
    isUnlocked: false,
  },
];

export interface StoryMission {
  id: string;
  missionNumber: number;
  title: string;
  description: string;
  objectiveDescription: string;
  currentProgress: number;
  targetProgress: number;
  rpReward: number;
  rewardSummary: string;
  isCompleted: boolean;
  isUnlocked: boolean;
  isEndingMission?: boolean;
}

export const INITIAL_STORY_MISSIONS: StoryMission[] = [
  {
    id: 'mission_1',
    missionNumber: 1,
    title: 'First Light',
    description: 'Welcome to thy medieval sanctuary! Inspect thy land and prepare the soil for renewal.',
    objectiveDescription: 'Inspect or cultivate thy first crop field.',
    currentProgress: 0,
    targetProgress: 1,
    rpReward: 15,
    rewardSummary: '15 RP + 100 Sol Coins',
    isCompleted: false,
    isUnlocked: true,
  },
  {
    id: 'mission_2',
    missionNumber: 2,
    title: 'Seeds of Hope',
    description: 'Sow the seeds of golden heritage grains to nourish the settlers.',
    objectiveDescription: 'Plant or harvest 5 crops across thy plots.',
    currentProgress: 0,
    targetProgress: 5,
    rpReward: 25,
    rewardSummary: '25 RP + 150 Sol Coins',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_3',
    missionNumber: 3,
    title: 'Water of Life',
    description: 'Quench the thirst of thy fields with sacred water from the well or streams.',
    objectiveDescription: 'Hydrate crops 5 times with thy Blessing Vessel.',
    currentProgress: 0,
    targetProgress: 5,
    rpReward: 25,
    rewardSummary: '25 RP + Water Purification Notes',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_4',
    missionNumber: 4,
    title: 'The Old Farm',
    description: 'Survey the ancient citadel and rediscover forgotten techniques.',
    objectiveDescription: 'Discover 3 Points of Interest across the island.',
    currentProgress: 1,
    targetProgress: 3,
    rpReward: 30,
    rewardSummary: '30 RP + 200 Sol Coins',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_5',
    missionNumber: 5,
    title: 'Power of the Sun',
    description: 'Harness the clean arcane essence with mana stones and wind spires.',
    objectiveDescription: 'Produce at least 50 kWh of clean mana storage.',
    currentProgress: 0,
    targetProgress: 50,
    rpReward: 35,
    rewardSummary: '35 RP + Battery Blueprint',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_6',
    missionNumber: 6,
    title: 'Signals & Survivors',
    description: 'Travel into Deep Forest and find the old radio antenna outpost.',
    objectiveDescription: 'Discover the Deep Forest Radio Tower POI.',
    currentProgress: 0,
    targetProgress: 1,
    rpReward: 40,
    rewardSummary: '40 RP + Survivor Signal',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_7',
    missionNumber: 7,
    title: 'The Researcher',
    description: 'Welcome a clean-tech researcher or build an Alchemy Chamber.',
    objectiveDescription: 'Assign an NPC to the Researcher role.',
    currentProgress: 0,
    targetProgress: 1,
    rpReward: 40,
    rewardSummary: '40 RP + 300 Sol Coins',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_8',
    missionNumber: 8,
    title: 'Wildlife Returns',
    description: 'Bring harmony back to the island menagerie with sheep, cows, and bees.',
    objectiveDescription: 'Reach an Ecosystem Health score of 75 or higher.',
    currentProgress: 50,
    targetProgress: 75,
    rpReward: 45,
    rewardSummary: '45 RP + Golden Wool Shears',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_9',
    missionNumber: 9,
    title: 'The Facility',
    description: 'Decrypt the ancient academy terminal in Deep Forest.',
    objectiveDescription: 'Hack and decrypt 1 Ancient Terminal.',
    currentProgress: 0,
    targetProgress: 1,
    rpReward: 50,
    rewardSummary: '50 RP + Secret Blueprints',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_10',
    missionNumber: 10,
    title: 'Power Grid Harmony',
    description: 'Construct a self-sustaining grid with generators and storage.',
    objectiveDescription: 'Maintain 10 active powered devices simultaneously.',
    currentProgress: 5,
    targetProgress: 10,
    rpReward: 55,
    rewardSummary: '55 RP + Grid Efficiency +10%',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_11',
    missionNumber: 11,
    title: 'Flourishing Community',
    description: 'Expand the sanctuary so all loyal subjects are housed and fed.',
    objectiveDescription: 'Reach a sanctuary population of 4 or more with morale > 70%.',
    currentProgress: 2,
    targetProgress: 4,
    rpReward: 60,
    rewardSummary: '60 RP + 500 Sol Coins',
    isCompleted: false,
    isUnlocked: false,
  },
  {
    id: 'mission_12',
    missionNumber: 12,
    title: 'New Dawn',
    description: 'Unlock the Project New Dawn research and activate the eternal harmony nexus!',
    objectiveDescription: 'Unlock Project New Dawn in the Research tree.',
    currentProgress: 0,
    targetProgress: 1,
    rpReward: 100,
    rewardSummary: 'Story Ending Epilogue!',
    isCompleted: false,
    isUnlocked: false,
    isEndingMission: true,
  },
];

export interface LoreEntryData {
  id: string;
  title: string;
  author: string;
  category: 'SETTLER_DIARY' | 'SDZ_SCIENTIFIC_LOG' | 'RADIO_BROADCAST' | 'TERMINAL';
  textContent: string;
  isUnlocked: boolean;
}

export interface TerminalLogData {
  id: string;
  terminalName: string;
  locationName: string;
  logText: string;
  isHacked: boolean;
  requiresPassword: boolean;
  passwordAnswer: string;
}

export interface GameStateEntity {
  solCoins: number;
  ecoPrestige: number;
  batteryChargeKwh: number;
  batteryMaxCapacityKwh: number;
  gameTimeHour: number; // 0.0 - 24.0
  gameTimeDay: number;
  currentWeather: WeatherType;
  playerX: number;
  playerY: number;
  playerZ: number;
  playerAngle: number;
  totalHarvests: number;
  carbonOffsetKg: number;
  health: number;
  maxHealth: number;
  hunger: number;
  maxHunger: number;
  thirst: number;
  maxThirst: number;
  stamina: number;
  maxStamina: number;
  cleanWaterCarried: number;
  rawWaterCarried: number;
  maxWaterCarried: number;
  isSick: boolean;
  sicknessRemainingHours: number;
  fishingRodTier: number;
  fishPopStream: number;
  fishPopRiver: number;
  fishPopPond: number;
  researchPoints: number;
  discoveredPois: string[];
  discoveredChunks: string[];
  discoveredBiomes: string[];
  currentBiomeId: BiomeTypeId;
  storyEndingUnlocked: boolean;
}

export interface CraftingRecipeItem {
  id: string;
  name: string;
  description: string;
  category: ItemCategory;
  sellValue: number;
  energyCostKwh: number;
  ingredients: Record<string, { itemName: string; requiredQty: number }>;
}

export const CRAFTING_RECIPES: CraftingRecipeItem[] = [
  {
    id: 'bio_fuel',
    name: 'Essence of Sun',
    description: 'High-energy sacred distillate from golden grains and solar sunflowers.',
    category: 'CRAFTED',
    sellValue: 160,
    energyCostKwh: 5.0,
    ingredients: {
      harvest_wheat: { itemName: 'Golden Grain', requiredQty: 2 },
      harvest_sunflower: { itemName: 'Solar Sunflower', requiredQty: 1 },
    },
  },
  {
    id: 'solar_fabric',
    name: 'Silk of the Heavens',
    description: 'Luminous photovoltaic textile woven with sheared solar wool.',
    category: 'CRAFTED',
    sellValue: 220,
    energyCostKwh: 4.0,
    ingredients: {
      solar_wool: { itemName: 'Solar Wool', requiredQty: 2 },
    },
  },
  {
    id: 'fertilizer_bio',
    name: 'Alchemical Growth Salve',
    description: 'Concentrated elixir for accelerating the growth of sacred crops.',
    category: 'TOOL',
    sellValue: 45,
    energyCostKwh: 3.0,
    ingredients: {
      harvest_nitro_beans: { itemName: 'Earth Beans', requiredQty: 2 },
      harvest_bioluminescent_mushroom: { itemName: 'Moon Spores', requiredQty: 1 },
    },
  },
  {
    id: 'kombucha_elixir',
    name: 'Elixir of Rejuvenation',
    description: 'Probiotic brew crafted with medicinal moss and wild forest honey.',
    category: 'CRAFTED',
    sellValue: 180,
    energyCostKwh: 3.5,
    ingredients: {
      harvest_sky_spirulina: { itemName: 'Medicinal Moss', requiredQty: 2 },
      solar_honey: { itemName: 'Forest Honey', requiredQty: 1 },
    },
  },
  {
    id: 'power_cell',
    name: 'Mystic Power Stone',
    description: 'Arcane storage stone infused with cursed berries and pure solar essence.',
    category: 'TECH_UPGRADE',
    sellValue: 350,
    energyCostKwh: 10.0,
    ingredients: {
      harvest_cyber_berries: { itemName: 'Cursed Berries', requiredQty: 2 },
      bio_fuel: { itemName: 'Essence of Sun', requiredQty: 1 },
    },
  },
];
