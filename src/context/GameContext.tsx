import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';
import {
  GameStateEntity,
  PlotEntity,
  EnergyNodeEntity,
  LivestockEntity,
  InventoryEntity,
  ContractEntity,
  PlacedBuildingEntity,
  NpcEntity,
  ResearchTreeNode,
  StoryMission,
  LoreEntryData,
  TerminalLogData,
  CropTypeId,
  CropStage,
  PlotType,
  LivestockTypeId,
  FishTypeId,
  FISH_CATALOG,
  PlayerTool,
  WeatherType,
  BuildableTypeId,
  PowerPriorityLevel,
  DeviceTypeId,
  NpcRoleId,
  CROP_CATALOG,
  LIVESTOCK_CATALOG,
  BUILDABLE_CATALOG,
  WEATHER_CATALOG,
  DEVICE_CATALOG,
  POINTS_OF_INTEREST,
  RESEARCH_TECHS,
  INITIAL_STORY_MISSIONS,
  BiomeTypeId,
  BIOMES,
} from '../types/game';
import { sounds } from '../audio/soundSystem';

export interface FloatingTextData {
  id: string;
  text: string;
  color: string;
  x: number;
  y: number;
}

export interface NotificationMessage {
  id: string;
  title: string;
  description: string;
  icon?: string;
  type?: 'info' | 'success' | 'warning' | 'achievement';
}

export interface EnergyGridSummary {
  totalGenerationKwhPerDay: number;
  currentGenerationKwhPerHour: number;
  totalStorageCapacityKwh: number;
  currentStoredKwh: number;
  totalHourlyConsumptionKwh: number;
  netHourlyKwh: number;
  isCharging: boolean;
  hoursUntilEmpty: number;
  batteryPercent: number;
  activeDevicesCount: number;
  totalDevicesCount: number;
  unpoweredDevicesCount: number;
  powerSourcesCount: number;
  batteryUnitsCount: number;
  powerPolesCount: number;
}

interface GameContextType {
  gameState: GameStateEntity;
  plots: PlotEntity[];
  energyNodes: EnergyNodeEntity[];
  livestock: LivestockEntity[];
  inventory: InventoryEntity[];
  contracts: ContractEntity[];
  placedBuildings: PlacedBuildingEntity[];
  npcs: NpcEntity[];
  researchTechs: ResearchTreeNode[];
  storyMissions: StoryMission[];
  loreEntries: LoreEntryData[];
  terminalLogs: TerminalLogData[];
  devicePriorities: Record<DeviceTypeId, PowerPriorityLevel>;
  energySummary: EnergyGridSummary;
  ecosystemHealth: number;
  unlockedAchievements: Set<string>;

  // Selection & UI
  selectedTool: PlayerTool;
  setSelectedTool: (tool: PlayerTool) => void;
  selectedSeedForPouch: CropTypeId;
  setSelectedSeedForPouch: (crop: CropTypeId) => void;
  activeModal: string | null;
  openModal: (modal: string | null) => void;
  selectedPlotForModal: PlotEntity | null;
  setSelectedPlotForModal: (plot: PlotEntity | null) => void;
  selectedAnimalForModal: LivestockEntity | null;
  setSelectedAnimalForModal: (animal: LivestockEntity | null) => void;
  selectedNpcForDialogue: NpcEntity | null;
  setSelectedNpcForDialogue: (npc: NpcEntity | null) => void;
  activeTerminal: TerminalLogData | null;
  setActiveTerminal: (term: TerminalLogData | null) => void;

  // Build mode
  isBuildMode: boolean;
  setIsBuildMode: (val: boolean) => void;
  selectedBuildType: BuildableTypeId;
  setSelectedBuildType: (val: BuildableTypeId) => void;
  buildRotationDeg: number;
  setBuildRotationDeg: (val: number | ((prev: number) => number)) => void;
  placeBuilding: (type: BuildableTypeId, x: number, z: number, rotation: number) => boolean;

  // Actions
  waterPlot: (plotId: number) => void;
  waterAllPlots: () => void;
  plantPlot: (plotId: number, cropType: CropTypeId) => void;
  fertilizePlot: (plotId: number) => void;
  harvestPlot: (plotId: number) => void;

  feedAnimal: (animalId: number) => void;
  petAnimal: (animalId: number) => void;
  harvestAnimalProduce: (animalId: number) => void;
  buyLivestock: (type: LivestockTypeId) => boolean;

  craftItem: (recipeId: string) => boolean;
  claimContract: (contractId: string) => boolean;
  buySeed: (cropType: CropTypeId, quantity?: number) => boolean;
  sellInventoryItem: (itemId: string, quantity?: number) => boolean;

  drinkWater: (sourceType: 'CLEAN' | 'RAW') => void;
  boilWater: () => void;
  cookFish: (fishItemId: string) => void;
  brewTea: () => void;

  unlockResearch: (techId: string) => boolean;
  hackTerminal: (terminalId: string, passwordAttempt: string) => boolean;
  interactWithNpc: (npcId: number, optionKey: string) => void;
  assignNpcRole: (npcId: number, newRole: NpcRoleId) => void;

  catchFish: (fishType: FishTypeId, rodTier: number) => void;
  discoverPoiById: (poiId: string) => void;
  setDevicePriority: (device: DeviceTypeId, priority: PowerPriorityLevel) => void;
  restAtCabin: () => void;
  advanceTime: (hours: number) => void;

  // Notifications & Floating texts
  floatingTexts: FloatingTextData[];
  notifications: NotificationMessage[];
  addFloatingText: (text: string, color?: string, x?: number, y?: number) => void;
  showNotification: (title: string, description: string, type?: 'info' | 'success' | 'warning' | 'achievement') => void;

  // Settings
  soundEnabled: boolean;
  setSoundEnabled: (val: boolean) => void;
  masterVolume: number;
  setMasterVolume: (val: number) => void;
  timeSpeedMultiplier: number;
  setTimeSpeedMultiplier: (val: number) => void;
  hudOpacity: number;
  setHudOpacity: (val: number) => void;
  resetGameData: () => void;
}

const STORAGE_KEY = 'medieval_island_save_v1';

const INITIAL_GAME_STATE: GameStateEntity = {
  solCoins: 450,
  ecoPrestige: 100,
  batteryChargeKwh: 65.0,
  batteryMaxCapacityKwh: 120.0,
  gameTimeHour: 8.5,
  gameTimeDay: 1,
  currentWeather: 'SUNNY_CLEAR',
  playerX: 0.0,
  playerY: 0.0,
  playerZ: 0.0,
  playerAngle: 0.0,
  totalHarvests: 0,
  carbonOffsetKg: 150.0,
  health: 100.0,
  maxHealth: 100.0,
  hunger: 90.0,
  maxHunger: 100.0,
  thirst: 85.0,
  maxThirst: 100.0,
  stamina: 100.0,
  maxStamina: 100.0,
  cleanWaterCarried: 4,
  rawWaterCarried: 0,
  maxWaterCarried: 10,
  isSick: false,
  sicknessRemainingHours: 0.0,
  fishingRodTier: 1,
  fishPopStream: 10,
  fishPopRiver: 10,
  fishPopPond: 10,
  researchPoints: 20,
  discoveredPois: ['poi_old_farm'],
  discoveredChunks: ['9_9', '9_10', '10_9', '10_10'],
  discoveredBiomes: ['green_valley'],
  currentBiomeId: 'green_valley',
  storyEndingUnlocked: false,
};

const INITIAL_PLOTS: PlotEntity[] = [
  { id: 0, plotType: 'PERMACULTURE_BED', cropType: 'WHEAT', stage: 'VEGETATIVE', progress: 0.5, moisture: 0.85, compostLevel: 0.7, posX: -4.0 + (-1) * 4.5, posY: 0, posZ: -6.0 + (-0.5) * 4.5, autoIrrigated: false },
  { id: 1, plotType: 'PERMACULTURE_BED', cropType: 'CORN', stage: 'VEGETATIVE', progress: 0.4, moisture: 0.8, compostLevel: 0.6, posX: -4.0 + (0) * 4.5, posY: 0, posZ: -6.0 + (-0.5) * 4.5, autoIrrigated: false },
  { id: 2, plotType: 'PERMACULTURE_BED', cropType: 'TOMATO', stage: 'VEGETATIVE', progress: 0.6, moisture: 0.75, compostLevel: 0.8, posX: -4.0 + (1) * 4.5, posY: 0, posZ: -6.0 + (-0.5) * 4.5, autoIrrigated: false },
  { id: 3, plotType: 'PERMACULTURE_BED', cropType: null, stage: 'EMPTY', progress: 0, moisture: 0.7, compostLevel: 0.5, posX: -4.0 + (-1) * 4.5, posY: 0, posZ: -6.0 + (0.5) * 4.5, autoIrrigated: false },
  { id: 4, plotType: 'PERMACULTURE_BED', cropType: null, stage: 'EMPTY', progress: 0, moisture: 0.7, compostLevel: 0.5, posX: -4.0 + (0) * 4.5, posY: 0, posZ: -6.0 + (0.5) * 4.5, autoIrrigated: false },
  { id: 5, plotType: 'PERMACULTURE_BED', cropType: null, stage: 'EMPTY', progress: 0, moisture: 0.7, compostLevel: 0.5, posX: -4.0 + (1) * 4.5, posY: 0, posZ: -6.0 + (0.5) * 4.5, autoIrrigated: false },
];

const INITIAL_ENERGY_NODES: EnergyNodeEntity[] = [
  { id: 1, nodeType: 'PHOTOVOLTAIC_ARRAY', level: 1, efficiency: 1.0, posX: 14.0, posY: 0, posZ: -10.0, isActive: true },
  { id: 2, nodeType: 'VERTICAL_WIND_TURBINE', level: 1, efficiency: 1.1, posX: 14.0, posY: 0, posZ: -2.0, isActive: true },
  { id: 3, nodeType: 'BATTERY_STORAGE_BANK', level: 1, efficiency: 1.0, posX: 14.0, posY: 0, posZ: 6.0, isActive: true },
  { id: 4, nodeType: 'BIOGAS_DIGESTER', level: 1, efficiency: 0.9, posX: 14.0, posY: 0, posZ: 14.0, isActive: true },
];

const INITIAL_LIVESTOCK: LivestockEntity[] = [
  { id: 1, type: 'SOLAR_SHEEP', name: 'Aura', happiness: 90, hunger: 15, age: 2, produceProgress: 0.7, readyToHarvest: false, posX: -12.0, posY: 0, posZ: 5.0, targetX: -12.0, targetZ: 5.0 },
  { id: 2, type: 'CYBER_BOVINE', name: 'Bessie', happiness: 85, hunger: 25, age: 3, produceProgress: 0.4, readyToHarvest: false, posX: -10.0, posY: 0, posZ: 7.0, targetX: -10.0, targetZ: 7.0 },
  { id: 16, type: 'CYBER_BOVINE', name: 'Daisy', happiness: 80, hunger: 20, age: 2, produceProgress: 0.1, readyToHarvest: false, posX: -14.0, posY: 0, posZ: 10.0, targetX: -14.0, targetZ: 10.0 },
  { id: 3, type: 'ROBO_BEE_POLLINATOR', name: 'Hive Alpha', happiness: 95, hunger: 10, age: 1, produceProgress: 0.85, readyToHarvest: false, posX: -4.0, posY: 1.5, posZ: -14.0, targetX: -2.0, targetZ: -12.0 },
  { id: 4, type: 'MEADOW_ALPACA', name: 'Nimbus', happiness: 88, hunger: 20, age: 2, produceProgress: 0.3, readyToHarvest: false, posX: -16.0, posY: 0, posZ: 0.0, targetX: -14.0, targetZ: 2.0 },
  { id: 10, type: 'CHICKEN', name: 'Clucky', happiness: 90, hunger: 10, age: 1, produceProgress: 0.2, readyToHarvest: false, posX: 4.5, posY: 0, posZ: 5.5, targetX: 4.5, targetZ: 5.5 },
  { id: 11, type: 'CHICKEN', name: 'Henrietta', happiness: 95, hunger: 5, age: 1, produceProgress: 0.4, readyToHarvest: false, posX: 5.5, posY: 0, posZ: 4.0, targetX: 5.5, targetZ: 4.0 },
  { id: 12, type: 'CHICKEN', name: 'Eggatha', happiness: 85, hunger: 15, age: 2, produceProgress: 0.6, readyToHarvest: false, posX: 3.5, posY: 0, posZ: 4.5, targetX: 3.5, targetZ: 4.5 },
  { id: 13, type: 'CHICKEN', name: 'Peep', happiness: 90, hunger: 12, age: 1, produceProgress: 0.3, readyToHarvest: false, posX: 4.0, posY: 0, posZ: 3.0, targetX: 4.0, targetZ: 3.0 },
  { id: 14, type: 'CHICKEN', name: 'Penny', happiness: 80, hunger: 18, age: 1, produceProgress: 0.2, readyToHarvest: false, posX: 5.0, posY: 0, posZ: 4.5, targetX: 5.0, targetZ: 4.5 },
];

const INITIAL_INVENTORY: InventoryEntity[] = [
  { itemId: 'seed_wheat', name: 'Golden Wheat Seeds', category: 'SEEDS', quantity: 8, sellValue: 12 },
  { itemId: 'seed_corn', name: 'Sweet Corn Seeds', category: 'SEEDS', quantity: 6, sellValue: 18 },
  { itemId: 'seed_tomato', name: 'Ruby Tomato Seeds', category: 'SEEDS', quantity: 6, sellValue: 15 },
  { itemId: 'seed_carrot', name: 'Crisp Carrot Seeds', category: 'SEEDS', quantity: 6, sellValue: 14 },
  { itemId: 'seed_herbs', name: 'Aromatic Herb Seeds', category: 'SEEDS', quantity: 8, sellValue: 10 },
  { itemId: 'seed_sunflower', name: 'Solar Sunflower Seeds', category: 'SEEDS', quantity: 5, sellValue: 15 },
  { itemId: 'seed_mushroom', name: 'Biolum Spore Pack', category: 'SEEDS', quantity: 4, sellValue: 25 },
  { itemId: 'material_bio_timber', name: 'Bio-Timber', category: 'CRAFTED', quantity: 25, sellValue: 20 },
  { itemId: 'material_solar_glass', name: 'Solar Glass', category: 'CRAFTED', quantity: 20, sellValue: 25 },
  { itemId: 'material_eco_alloy', name: 'Eco-Alloy', category: 'CRAFTED', quantity: 18, sellValue: 30 },
  { itemId: 'material_bio_polymer', name: 'Bio-Polymer', category: 'CRAFTED', quantity: 15, sellValue: 22 },
  { itemId: 'fertilizer_bio', name: 'Bio-Compost Serum', category: 'TOOL', quantity: 10, sellValue: 10 },
  { itemId: 'solar_wool', name: 'Solar Wool', category: 'PRODUCE', quantity: 2, sellValue: 75 },
  { itemId: 'solar_honey', name: 'Solar Honey', category: 'PRODUCE', quantity: 3, sellValue: 60 },
  { itemId: 'bio_fuel', name: 'Purified Bio-Fuel', category: 'CRAFTED', quantity: 1, sellValue: 120 },
];

const INITIAL_CONTRACTS: ContractEntity[] = [
  {
    id: 'contract_01',
    title: 'Sol City Green Bakery Order',
    buyerName: 'Solaria Metropolis',
    requiredItemId: 'harvest_wheat',
    requiredItemName: 'Golden Grain',
    requiredQty: 6,
    currentQty: 0,
    rewardCoins: 380,
    rewardEco: 60,
    isClaimed: false,
  },
  {
    id: 'contract_02',
    title: 'Bio-Textile Guild Supply',
    buyerName: 'Neo-Eden Weavers',
    requiredItemId: 'solar_wool',
    requiredItemName: 'Solar Wool',
    requiredQty: 4,
    currentQty: 2,
    rewardCoins: 420,
    rewardEco: 80,
    isClaimed: false,
  },
  {
    id: 'contract_03',
    title: 'Clean Energy City Export',
    buyerName: 'Zephyr Sky Port',
    requiredItemId: 'bio_fuel',
    requiredItemName: 'Purified Bio-Fuel',
    requiredQty: 3,
    currentQty: 1,
    rewardCoins: 550,
    rewardEco: 110,
    isClaimed: false,
  },
];

const INITIAL_NPCS: NpcEntity[] = [
  {
    id: 1,
    name: 'Rowan Greenhand',
    role: 'role_farmer',
    age: 28,
    skillLevel: 2,
    trait: 'Hardworking',
    morale: 85,
    hunger: 20,
    thirst: 15,
    assignedBedId: 1,
    assignedBuildingId: null,
    currentActivity: 'Tending Fields',
    posX: 6.0,
    posY: 0,
    posZ: 2.0,
    speechBubble: null,
    speechBubbleTimer: 0,
    relationshipToPlayer: 45,
  },
  {
    id: 2,
    name: 'Astrid Vane',
    role: 'role_engineer',
    age: 32,
    skillLevel: 3,
    trait: 'Curious',
    morale: 90,
    hunger: 15,
    thirst: 20,
    assignedBedId: 2,
    assignedBuildingId: null,
    currentActivity: 'Checking Mana Grid',
    posX: 12.0,
    posY: 0,
    posZ: -3.0,
    speechBubble: null,
    speechBubbleTimer: 0,
    relationshipToPlayer: 50,
  },
];

const INITIAL_LORE: LoreEntryData[] = [
  { id: 'lore_1', title: 'Log 01: Arriving at Sector 4', author: 'Previous Settlers', category: 'SETTLER_DIARY', textContent: 'We thought the island project with clean mana and permaculture beds would be fully self-sustaining. When the old grid tripped, many fled to the mainland. But this fertile valley holds the spark of rebirth.', isUnlocked: true },
  { id: 'lore_2', title: 'Old Farm Diary', author: 'Matron Elspeth', category: 'SETTLER_DIARY', textContent: 'The soil in Green Valley is blessed if watered with purified spring water. Crops like wheat and corn respond best to alchemical compost salve at dawn.', isUnlocked: true },
  { id: 'lore_3', title: 'SDZ Research Log: Closed Loop Grid', author: 'Dr. Ananya', category: 'SDZ_SCIENTIFIC_LOG', textContent: 'Testing solar prismatic glass and micro-hydro wheels. Storage efficiency is the single key to surviving winter storms on the island.', isUnlocked: false },
  { id: 'lore_4', title: 'Facility Core Log', author: 'Chief Engineer', category: 'SDZ_SCIENTIFIC_LOG', textContent: 'Emergency protocol ALPHA-9 initiated. Hydro-electric sluices and volcanic vents locked down. Master override password: SOLARIS.', isUnlocked: false },
  { id: 'lore_5', title: 'Radio Signal Intercept', author: 'Outpost 3', category: 'RADIO_BROADCAST', textContent: 'This is Outpost 3 broadcasting on emergency frequency 104.5 MHz... If anyone receives this near Deep Forest, clean energy can rebuild our valley.', isUnlocked: false },
  { id: 'lore_6', title: 'Ancient Banyan Inscription', author: 'Eco-Guardians', category: 'SETTLER_DIARY', textContent: 'The elders say: Every tree holds a guardian spirit. Clean soil and water bring back wild birds, mountain deer, and the golden sun fish.', isUnlocked: false },
  { id: 'lore_7', title: 'Stone Circle Notes', author: 'Surveyor Marcus', category: 'SDZ_SCIENTIFIC_LOG', textContent: 'Subterranean magnetic resonance detected. Energy grid efficiency increased by harmonic alignment with the ancient stone circle.', isUnlocked: false },
  { id: 'lore_8', title: "Fisherman's Field Manual", author: 'Old Angler', category: 'SETTLER_DIARY', textContent: 'Best fishing spots are near the Wetland river bend during evening dusk. Golden lures attract rare bio-luminescent fish.', isUnlocked: false },
  { id: 'lore_9', title: 'Ranger Survey 2038', author: 'Ranger Dave', category: 'SDZ_SCIENTIFIC_LOG', textContent: 'Wildlife returning to Northern Green Valley as carbon levels drop below 100 ppm and mana flows freely.', isUnlocked: false },
  { id: 'lore_10', title: 'Foundational Creed: New Dawn', author: 'Rebuilders Guild', category: 'SETTLER_DIARY', textContent: 'We did not just rebuild stone structures. We restored eternal harmony between human ingenuity and nature.', isUnlocked: false },
];

const INITIAL_TERMINALS: TerminalLogData[] = [
  { id: 'term_1', terminalName: 'Security Terminal A1', locationName: 'Research Facility Entrance', logText: 'SECURITY OVERRIDE LOG: Peripheral defenses offline. Main power diverted to Cryo Vault. Password hint: SOLARIS.', isHacked: true, requiresPassword: true, passwordAnswer: 'SOLARIS' },
  { id: 'term_2', terminalName: 'Agronomy Lab Terminal', locationName: 'Facility Bio-Lab', logText: 'GENETIC ARCHIVE: Seed strains WHEAT, CORN, and TOMATO proven resilient to high UV radiation when hydrated daily.', isHacked: true, requiresPassword: true, passwordAnswer: 'SOLARIS' },
  { id: 'term_3', terminalName: 'Geothermal Core Control', locationName: 'Deep Facility Vault', logText: 'CRITICAL SHUTDOWN: Magma taps sealed at 40 bar pressure. Awaiting manual cipher validation: SOLARIS.', isHacked: false, requiresPassword: true, passwordAnswer: 'SOLARIS' },
  { id: 'term_4', terminalName: 'Drone Network Hub', locationName: 'Communications Deck', logText: 'COMMUNICATIONS RELAY: Long-range broadcast towers pinging frequency 104.5 MHz. Signal detected from Green Valley.', isHacked: true, requiresPassword: true, passwordAnswer: 'SOLARIS' },
  { id: 'term_5', terminalName: 'Civilization Archive', locationName: 'SDZ Command Room', logText: 'ARCHIVE RESTORED: Project New Dawn blueprint uploaded to planetary database. The medieval island has achieved sustainability!', isHacked: false, requiresPassword: true, passwordAnswer: 'SOLARIS' },
];

const GameContext = createContext<GameContextType | null>(null);

export const GameProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Load saved state or default
  const [gameState, setGameState] = useState<GameStateEntity>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.gameState) return parsed.gameState;
      }
    } catch {}
    return INITIAL_GAME_STATE;
  });

  const [plots, setPlots] = useState<PlotEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.plots) return parsed.plots;
      }
    } catch {}
    return INITIAL_PLOTS;
  });

  const [energyNodes, setEnergyNodes] = useState<EnergyNodeEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.energyNodes) return parsed.energyNodes;
      }
    } catch {}
    return INITIAL_ENERGY_NODES;
  });

  const [livestock, setLivestock] = useState<LivestockEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.livestock) return parsed.livestock;
      }
    } catch {}
    return INITIAL_LIVESTOCK;
  });

  const [inventory, setInventory] = useState<InventoryEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.inventory) return parsed.inventory;
      }
    } catch {}
    return INITIAL_INVENTORY;
  });

  const [contracts, setContracts] = useState<ContractEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.contracts) return parsed.contracts;
      }
    } catch {}
    return INITIAL_CONTRACTS;
  });

  const [placedBuildings, setPlacedBuildings] = useState<PlacedBuildingEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.placedBuildings) return parsed.placedBuildings;
      }
    } catch {}
    return [];
  });

  const [npcs, setNpcs] = useState<NpcEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.npcs) return parsed.npcs;
      }
    } catch {}
    return INITIAL_NPCS;
  });

  const [researchTechs, setResearchTechs] = useState<ResearchTreeNode[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.researchTechs) return parsed.researchTechs;
      }
    } catch {}
    return RESEARCH_TECHS;
  });

  const [storyMissions, setStoryMissions] = useState<StoryMission[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.storyMissions) return parsed.storyMissions;
      }
    } catch {}
    return INITIAL_STORY_MISSIONS;
  });

  const [loreEntries, setLoreEntries] = useState<LoreEntryData[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.loreEntries) return parsed.loreEntries;
      }
    } catch {}
    return INITIAL_LORE;
  });

  const [terminalLogs, setTerminalLogs] = useState<TerminalLogData[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.terminalLogs) return parsed.terminalLogs;
      }
    } catch {}
    return INITIAL_TERMINALS;
  });

  const [devicePriorities, setDevicePriorities] = useState<Record<DeviceTypeId, PowerPriorityLevel>>(() => {
    const map: Record<string, PowerPriorityLevel> = {};
    Object.values(DEVICE_CATALOG).forEach((dev) => {
      map[dev.id] = dev.defaultPriority;
    });
    return map as Record<DeviceTypeId, PowerPriorityLevel>;
  });

  const [unlockedAchievements, setUnlockedAchievements] = useState<Set<string>>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.unlockedAchievements) return new Set(parsed.unlockedAchievements);
      }
    } catch {}
    return new Set(['ach_first_steps']);
  });

  // UI state
  const [selectedTool, setSelectedTool] = useState<PlayerTool>('HAND');
  const [selectedSeedForPouch, setSelectedSeedForPouch] = useState<CropTypeId>('WHEAT');
  const [activeModal, setActiveModal] = useState<string | null>(null);
  const [selectedPlotForModal, setSelectedPlotForModal] = useState<PlotEntity | null>(null);
  const [selectedAnimalForModal, setSelectedAnimalForModal] = useState<LivestockEntity | null>(null);
  const [selectedNpcForDialogue, setSelectedNpcForDialogue] = useState<NpcEntity | null>(null);
  const [activeTerminal, setActiveTerminal] = useState<TerminalLogData | null>(null);

  // Build mode
  const [isBuildMode, setIsBuildMode] = useState<boolean>(false);
  const [selectedBuildType, setSelectedBuildType] = useState<BuildableTypeId>('CABIN');
  const [buildRotationDeg, setBuildRotationDeg] = useState<number>(0);

  // Feedback
  const [floatingTexts, setFloatingTexts] = useState<FloatingTextData[]>([]);
  const [notifications, setNotifications] = useState<NotificationMessage[]>([]);

  // Settings
  const [soundEnabled, setSoundEnabled] = useState<boolean>(true);
  const [masterVolume, setMasterVolumeState] = useState<number>(0.8);
  const [timeSpeedMultiplier, setTimeSpeedMultiplier] = useState<number>(1.0);
  const [hudOpacity, setHudOpacity] = useState<number>(0.9);

  const addFloatingText = useCallback((text: string, color = '#D4AF37', x = 0, y = 0) => {
    const id = Math.random().toString(36).substring(7);
    setFloatingTexts((prev) => [...prev, { id, text, color, x, y }]);
    setTimeout(() => {
      setFloatingTexts((prev) => prev.filter((item) => item.id !== id));
    }, 2000);
  }, []);

  const showNotification = useCallback((title: string, description: string, type: 'info' | 'success' | 'warning' | 'achievement' = 'info') => {
    const id = Math.random().toString(36).substring(7);
    setNotifications((prev) => [...prev, { id, title, description, type }]);
    setTimeout(() => {
      setNotifications((prev) => prev.filter((item) => item.id !== id));
    }, 4500);
  }, []);

  const unlockAchievement = useCallback((achId: string) => {
    setUnlockedAchievements((prev) => {
      if (prev.has(achId)) return prev;
      const next = new Set(prev);
      next.add(achId);
      sounds.playDiscoverySparkle();
      showNotification('ACHIEVEMENT UNLOCKED!', achId.replace('ach_', '').replace(/_/g, ' ').toUpperCase(), 'achievement');
      return next;
    });
  }, [showNotification]);

  const setMasterVolume = (val: number) => {
    setMasterVolumeState(val);
    sounds.masterVolume = val;
  };

  // Helper: Add or remove inventory item
  const updateInventoryItem = useCallback((itemId: string, name: string, category: InventoryEntity['category'], delta: number, sellValue = 10) => {
    setInventory((prev) => {
      const idx = prev.findIndex((i) => i.itemId === itemId);
      if (idx >= 0) {
        const item = prev[idx];
        const newQty = Math.max(0, item.quantity + delta);
        const updated = [...prev];
        updated[idx] = { ...item, quantity: newQty };
        return updated;
      } else if (delta > 0) {
        return [...prev, { itemId, name, category, quantity: delta, sellValue }];
      }
      return prev;
    });
  }, []);

  // Calculate Ecosystem Health
  const ecosystemHealth = Math.min(100, Math.max(0,
    50 +
    new Set(livestock.map((l) => l.type)).size * 5 +
    (plots.some((p) => p.cropType !== null && p.moisture > 0.4 && p.stage !== 'HARVEST_READY' && p.stage !== 'WITHERED') ? 10 : 0) +
    (gameState.currentWeather !== 'STORM' ? 10 : 0) +
    placedBuildings.length * 2
  ));

  // Calculate Energy Grid Summary
  const weatherDef = WEATHER_CATALOG[gameState.currentWeather];
  const hour = gameState.gameTimeHour;
  const isDay = hour >= 6.0 && hour <= 19.0;
  const sunIntensity = isDay ? Math.sin(((hour - 6.0) / 13.0) * Math.PI) : 0;

  let totalGenKwhPerHour = 0;
  let totalCapacity = 100;

  // Initial nodes & placed buildings
  energyNodes.forEach((node) => {
    if (!node.isActive) return;
    if (node.nodeType === 'PHOTOVOLTAIC_ARRAY') {
      totalGenKwhPerHour += 3.5 * sunIntensity * weatherDef.solarMultiplier * node.efficiency;
    } else if (node.nodeType === 'VERTICAL_WIND_TURBINE') {
      totalGenKwhPerHour += 2.8 * weatherDef.windMultiplier * node.efficiency;
    } else if (node.nodeType === 'BIOGAS_DIGESTER') {
      totalGenKwhPerHour += 2.0 * node.efficiency;
    } else if (node.nodeType === 'BATTERY_STORAGE_BANK') {
      totalCapacity += 50.0 * node.level;
    }
  });

  placedBuildings.forEach((b) => {
    if (b.buildingType === 'SOLAR_PANEL') {
      totalGenKwhPerHour += (10 / 12) * sunIntensity * weatherDef.solarMultiplier;
    } else if (b.buildingType === 'ADVANCED_SOLAR') {
      totalGenKwhPerHour += (30 / 12) * sunIntensity * weatherDef.solarMultiplier * 1.2;
    } else if (b.buildingType === 'WINDMILL') {
      totalGenKwhPerHour += (15 / 24) * weatherDef.windMultiplier;
    } else if (b.buildingType === 'HYDRO_GENERATOR') {
      totalGenKwhPerHour += 20 / 24;
    } else if (b.buildingType === 'BIOGAS_GENERATOR') {
      totalGenKwhPerHour += 12 / 24;
    } else if (b.buildingType === 'GEOTHERMAL_VENT') {
      totalGenKwhPerHour += 40 / 24;
    } else if (b.buildingType === 'BASIC_BATTERY') {
      totalCapacity += 50;
    } else if (b.buildingType === 'ADVANCED_BATTERY') {
      totalCapacity += 150;
    } else if (b.buildingType === 'BATTERY_BANK') {
      totalCapacity += 500;
    }
  });

  const batteryPercent = totalCapacity > 0 ? gameState.batteryChargeKwh / totalCapacity : 0;

  // Hourly consumption of devices
  let totalHourlyConsumption = 0;
  let activeDevices = 0;
  let unpoweredDevices = 0;

  Object.values(DEVICE_CATALOG).forEach((dev) => {
    const priority = devicePriorities[dev.id] || dev.defaultPriority;
    const threshold = priority === 'CRITICAL' ? 0.0 : priority === 'IMPORTANT' ? 0.3 : priority === 'NORMAL' ? 0.5 : priority === 'COMFORT' ? 0.7 : 0.9;
    const isPowered = batteryPercent >= threshold && gameState.batteryChargeKwh > 0.1;
    if (isPowered) {
      totalHourlyConsumption += dev.consumptionKwhPerHour;
      activeDevices++;
    } else {
      unpoweredDevices++;
    }
  });

  const netHourlyKwh = totalGenKwhPerHour - totalHourlyConsumption;
  const isCharging = netHourlyKwh > 0;
  const hoursUntilEmpty = netHourlyKwh < 0 ? gameState.batteryChargeKwh / Math.abs(netHourlyKwh) : 999;

  const energySummary: EnergyGridSummary = {
    totalGenerationKwhPerDay: totalGenKwhPerHour * 24,
    currentGenerationKwhPerHour: totalGenKwhPerHour,
    totalStorageCapacityKwh: totalCapacity,
    currentStoredKwh: gameState.batteryChargeKwh,
    totalHourlyConsumptionKwh: totalHourlyConsumption,
    netHourlyKwh,
    isCharging,
    hoursUntilEmpty,
    batteryPercent,
    activeDevicesCount: activeDevices,
    totalDevicesCount: Object.keys(DEVICE_CATALOG).length,
    unpoweredDevicesCount: unpoweredDevices,
    powerSourcesCount: energyNodes.filter((n) => n.nodeType !== 'BATTERY_STORAGE_BANK').length,
    batteryUnitsCount: energyNodes.filter((n) => n.nodeType === 'BATTERY_STORAGE_BANK').length,
    powerPolesCount: placedBuildings.filter((b) => b.buildingType === 'POWER_POLE').length,
  };

  // Persistent Save
  useEffect(() => {
    const saveObj = {
      gameState,
      plots,
      energyNodes,
      livestock,
      inventory,
      contracts,
      placedBuildings,
      npcs,
      researchTechs,
      storyMissions,
      loreEntries,
      terminalLogs,
      unlockedAchievements: Array.from(unlockedAchievements),
    };
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(saveObj));
    } catch {}
  }, [gameState, plots, energyNodes, livestock, inventory, contracts, placedBuildings, npcs, researchTechs, storyMissions, loreEntries, terminalLogs, unlockedAchievements]);

  // Main Simulation Loop
  useEffect(() => {
    const interval = setInterval(() => {
      const dtHours = (1 / 60) * timeSpeedMultiplier; // 1 second real-time = 1 min game-time

      setGameState((prev) => {
        let newHour = prev.gameTimeHour + dtHours;
        let newDay = prev.gameTimeDay;
        if (newHour >= 24.0) {
          newHour -= 24.0;
          newDay += 1;
        }

        // Energy battery update
        const netFlow = netHourlyKwh * dtHours;
        const newBattery = Math.max(0, Math.min(totalCapacity, prev.batteryChargeKwh + netFlow));

        // Vitals decay
        const thirstDecay = (weatherDef.id === 'HEATWAVE' ? 3.0 : 1.5) * dtHours;
        const hungerDecay = 1.0 * dtHours;
        const newThirst = Math.max(0, prev.thirst - thirstDecay);
        const newHunger = Math.max(0, prev.hunger - hungerDecay);

        let newHealth = prev.health;
        let isSick = prev.isSick;
        let sicknessHours = prev.sicknessRemainingHours;
        if (isSick) {
          sicknessHours = Math.max(0, sicknessHours - dtHours);
          if (sicknessHours === 0) isSick = false;
        }

        if (newThirst <= 0 || newHunger <= 0) {
          newHealth = Math.max(0, newHealth - 2.0 * dtHours);
        } else if (newThirst > 70 && newHunger > 70 && !isSick) {
          newHealth = Math.min(prev.maxHealth, newHealth + 1.0 * dtHours);
        }

        return {
          ...prev,
          gameTimeHour: newHour,
          gameTimeDay: newDay,
          batteryChargeKwh: newBattery,
          batteryMaxCapacityKwh: totalCapacity,
          thirst: newThirst,
          hunger: newHunger,
          health: newHealth,
          isSick,
          sicknessRemainingHours: sicknessHours,
        };
      });

      // Update Crops
      setPlots((prevPlots) =>
        prevPlots.map((plot) => {
          if (!plot.cropType || plot.stage === 'HARVEST_READY' || plot.stage === 'WITHERED') {
            return plot;
          }
          const cropDef = CROP_CATALOG[plot.cropType];
          const weatherMultiplier = weatherDef.cropGrowthMultiplier;
          const isRaining = weatherDef.autoWaterRain;

          let newMoisture = plot.moisture;
          if (isRaining) {
            newMoisture = Math.min(1.0, newMoisture + 0.1 * dtHours);
          } else {
            newMoisture = Math.max(0.0, newMoisture - 0.05 * dtHours);
          }

          // Growth calculation
          if (newMoisture <= 0.05) {
            return { ...plot, moisture: newMoisture, stage: 'WITHERED' };
          }

          const growthPerSec = (1.0 / cropDef.growthDurationSec) * (0.8 + plot.compostLevel * 0.4) * weatherMultiplier;
          const newProgress = plot.progress + growthPerSec * (dtHours * 10);

          let newStage: CropStage = plot.stage;
          if (newProgress >= 1.0) {
            newStage = 'HARVEST_READY';
          } else if (newProgress >= 0.75) {
            newStage = 'FLOWERING';
          } else if (newProgress >= 0.5) {
            newStage = 'VEGETATIVE';
          } else if (newProgress >= 0.25) {
            newStage = 'SPROUT';
          } else {
            newStage = 'SEEDLING';
          }

          return {
            ...plot,
            moisture: newMoisture,
            progress: newProgress,
            stage: newStage,
          };
        })
      );

      // Update Livestock produce
      setLivestock((prev) =>
        prev.map((animal) => {
          if (animal.readyToHarvest) return animal;
          const def = LIVESTOCK_CATALOG[animal.type];
          const rate = 1.0 / def.produceIntervalSec;
          const newProg = animal.produceProgress + rate * (dtHours * 8);
          if (newProg >= 1.0) {
            return { ...animal, produceProgress: 1.0, readyToHarvest: true };
          }
          return { ...animal, produceProgress: newProg };
        })
      );
    }, 1000);

    return () => clearInterval(interval);
  }, [timeSpeedMultiplier, netHourlyKwh, totalCapacity, weatherDef]);

  // Story Missions Progress Checker
  useEffect(() => {
    setStoryMissions((prevMissions) => {
      let changed = false;
      const updated = prevMissions.map((m) => {
        if (m.isCompleted) return m;

        let cur = m.currentProgress;
        if (m.id === 'mission_1') {
          // Check if any plot was watered or has crop
          if (plots.some((p) => p.cropType !== null)) cur = 1;
        } else if (m.id === 'mission_2') {
          cur = Math.min(m.targetProgress, gameState.totalHarvests);
        } else if (m.id === 'mission_4') {
          cur = gameState.discoveredPois.length;
        } else if (m.id === 'mission_5') {
          cur = Math.floor(gameState.batteryChargeKwh);
        } else if (m.id === 'mission_6') {
          cur = gameState.discoveredPois.includes('poi_radio_tower') ? 1 : 0;
        } else if (m.id === 'mission_7') {
          cur = npcs.some((n) => n.role === 'role_researcher') ? 1 : 0;
        } else if (m.id === 'mission_8') {
          cur = ecosystemHealth;
        } else if (m.id === 'mission_9') {
          cur = terminalLogs.some((t) => t.isHacked) ? 1 : 0;
        } else if (m.id === 'mission_10') {
          cur = energySummary.activeDevicesCount;
        } else if (m.id === 'mission_11') {
          cur = npcs.length;
        } else if (m.id === 'mission_12') {
          cur = researchTechs.some((t) => t.id === 'tech_new_dawn' && t.isUnlocked) ? 1 : 0;
        }

        const completed = cur >= m.targetProgress;
        if (completed && !m.isCompleted) {
          changed = true;
          sounds.playCraftSuccess();
          showNotification('MISSION COMPLETED!', m.title + ' — ' + m.rewardSummary, 'success');
          // Grant reward
          setGameState((s) => ({
            ...s,
            researchPoints: s.researchPoints + m.rpReward,
            solCoins: s.solCoins + (m.rpReward * 5),
          }));
          return { ...m, currentProgress: cur, isCompleted: true };
        }
        if (cur !== m.currentProgress) {
          changed = true;
          return { ...m, currentProgress: cur };
        }
        return m;
      });

      // Unlock next mission if previous completed
      for (let i = 0; i < updated.length; i++) {
        if (updated[i].isCompleted && i + 1 < updated.length && !updated[i + 1].isUnlocked) {
          updated[i + 1].isUnlocked = true;
          changed = true;
        }
      }

      return changed ? updated : prevMissions;
    });
  }, [plots, gameState.totalHarvests, gameState.discoveredPois, gameState.batteryChargeKwh, npcs, ecosystemHealth, terminalLogs, energySummary.activeDevicesCount, researchTechs, showNotification]);

  // Actions
  const waterPlot = useCallback((plotId: number) => {
    setPlots((prev) =>
      prev.map((p) => {
        if (p.id === plotId) {
          sounds.playWatering();
          addFloatingText('+40% Water', '#00E5FF');
          return { ...p, moisture: Math.min(1.0, p.moisture + 0.4) };
        }
        return p;
      })
    );
  }, [addFloatingText]);

  const waterAllPlots = useCallback(() => {
    sounds.playWatering();
    setPlots((prev) =>
      prev.map((p) => (p.cropType ? { ...p, moisture: Math.min(1.0, p.moisture + 0.4) } : p))
    );
    addFloatingText('All Grooves Blessed!', '#00E5FF');
  }, [addFloatingText]);

  const plantPlot = useCallback((plotId: number, cropType: CropTypeId) => {
    const seedItemId = `seed_${cropType.toLowerCase()}`;
    const invItem = inventory.find((i) => i.itemId === seedItemId || i.itemId.includes(cropType.toLowerCase()));

    if (!invItem || invItem.quantity <= 0) {
      sounds.playErrorSound();
      showNotification('No Seeds!', `Thou hast no ${CROP_CATALOG[cropType].displayName} seeds.`, 'warning');
      return;
    }

    updateInventoryItem(invItem.itemId, invItem.name, 'SEEDS', -1);
    sounds.playHarvest();
    setPlots((prev) =>
      prev.map((p) => {
        if (p.id === plotId) {
          return {
            ...p,
            cropType,
            stage: 'SEEDLING',
            progress: 0.05,
            moisture: Math.max(0.6, p.moisture),
          };
        }
        return p;
      })
    );
    addFloatingText(`Planted ${CROP_CATALOG[cropType].displayName}`, '#81C784');
    setActiveModal(null);
  }, [inventory, updateInventoryItem, showNotification, addFloatingText]);

  const fertilizePlot = useCallback((plotId: number) => {
    const fertItem = inventory.find((i) => i.itemId === 'fertilizer_bio' && i.quantity > 0);
    if (!fertItem) {
      sounds.playErrorSound();
      showNotification('No Fertilizer', 'Craft Bio-Compost Serum at the Forge.', 'warning');
      return;
    }
    updateInventoryItem('fertilizer_bio', 'Bio-Compost Serum', 'TOOL', -1);
    sounds.playCraftSuccess();
    setPlots((prev) =>
      prev.map((p) => (p.id === plotId ? { ...p, compostLevel: Math.min(1.0, p.compostLevel + 0.4) } : p))
    );
    addFloatingText('Vitality Infused! +Compost', '#FFD54F');
  }, [inventory, updateInventoryItem, showNotification, addFloatingText]);

  const harvestPlot = useCallback((plotId: number) => {
    const plot = plots.find((p) => p.id === plotId);
    if (!plot || !plot.cropType || plot.stage !== 'HARVEST_READY') return;

    const def = CROP_CATALOG[plot.cropType];
    sounds.playHarvest();
    updateInventoryItem(`harvest_${plot.cropType.toLowerCase()}`, def.displayName, 'HARVEST', def.harvestYield, def.baseSellPrice);

    setPlots((prev) =>
      prev.map((p) => (p.id === plotId ? { ...p, cropType: null, stage: 'EMPTY', progress: 0 } : p))
    );

    setGameState((s) => ({
      ...s,
      solCoins: s.solCoins + 10,
      ecoPrestige: s.ecoPrestige + 5,
      totalHarvests: s.totalHarvests + 1,
      carbonOffsetKg: s.carbonOffsetKg + 2.5,
    }));

    addFloatingText(`+${def.harvestYield} ${def.displayName}!`, '#FFD700');
    unlockAchievement('ach_first_harvest');
  }, [plots, updateInventoryItem, addFloatingText, unlockAchievement]);

  const feedAnimal = useCallback((animalId: number) => {
    const hayItem = inventory.find((i) => i.itemId === 'harvest_wheat' && i.quantity > 0);
    if (!hayItem) {
      sounds.playErrorSound();
      showNotification('No Feed', 'Requires Golden Grain to feed animals.', 'warning');
      return;
    }
    updateInventoryItem('harvest_wheat', 'Golden Grain', 'HARVEST', -1);
    sounds.playAnimalSound('CHICKEN');
    setLivestock((prev) =>
      prev.map((a) => (a.id === animalId ? { ...a, hunger: Math.max(0, a.hunger - 40), happiness: Math.min(100, a.happiness + 15) } : a))
    );
    addFloatingText('Fed Animal! +Joy', '#81C784');
  }, [inventory, updateInventoryItem, showNotification, addFloatingText]);

  const petAnimal = useCallback((animalId: number) => {
    const animal = livestock.find((a) => a.id === animalId);
    if (!animal) return;
    sounds.playAnimalSound(animal.type);
    setLivestock((prev) =>
      prev.map((a) => (a.id === animalId ? { ...a, happiness: Math.min(100, a.happiness + 10) } : a))
    );
    addFloatingText('❤️ Loved!', '#FF8A80');
    unlockAchievement('ach_animal_friend');
  }, [livestock, addFloatingText, unlockAchievement]);

  const harvestAnimalProduce = useCallback((animalId: number) => {
    const animal = livestock.find((a) => a.id === animalId);
    if (!animal || !animal.readyToHarvest) return;
    const def = LIVESTOCK_CATALOG[animal.type];
    sounds.playHarvest();
    updateInventoryItem(def.produceItemId, def.productProduced, 'PRODUCE', 1, def.productSellPrice);

    setLivestock((prev) =>
      prev.map((a) => (a.id === animalId ? { ...a, produceProgress: 0, readyToHarvest: false } : a))
    );
    addFloatingText(`Collected ${def.productProduced}!`, '#FFD700');
  }, [livestock, updateInventoryItem, addFloatingText]);

  const buyLivestock = useCallback((type: LivestockTypeId) => {
    const def = LIVESTOCK_CATALOG[type];
    if (gameState.solCoins < def.purchaseCostCoins) {
      sounds.playErrorSound();
      showNotification('Coins Needed', `Requires ${def.purchaseCostCoins} Sol Coins.`, 'warning');
      return false;
    }
    sounds.playCraftSuccess();
    setGameState((s) => ({ ...s, solCoins: s.solCoins - def.purchaseCostCoins, ecoPrestige: s.ecoPrestige + 10 }));
    const newAnimal: LivestockEntity = {
      id: Date.now(),
      type,
      name: `${def.displayName} #${livestock.length + 1}`,
      happiness: 90,
      hunger: 10,
      age: 1,
      produceProgress: 0,
      readyToHarvest: false,
      posX: -10 + (Math.random() - 0.5) * 8,
      posY: 0,
      posZ: 4 + (Math.random() - 0.5) * 8,
      targetX: -10,
      targetZ: 4,
    };
    setLivestock((prev) => [...prev, newAnimal]);
    addFloatingText(`Adopted ${def.displayName}!`, '#00E676');
    return true;
  }, [gameState.solCoins, livestock.length, showNotification, addFloatingText]);

  const craftItem = useCallback((recipeId: string) => {
    // Check recipes
    sounds.playCraftSuccess();
    return true;
  }, []);

  const claimContract = useCallback((contractId: string) => {
    const contract = contracts.find((c) => c.id === contractId);
    if (!contract || contract.isClaimed) return false;

    const inv = inventory.find((i) => i.itemId === contract.requiredItemId);
    const available = inv ? inv.quantity : 0;
    if (available < contract.requiredQty) {
      sounds.playErrorSound();
      showNotification('Incomplete Order', `Need ${contract.requiredQty}x ${contract.requiredItemName}.`, 'warning');
      return false;
    }

    updateInventoryItem(contract.requiredItemId, contract.requiredItemName, 'HARVEST', -contract.requiredQty);
    sounds.playCraftSuccess();
    setContracts((prev) => prev.map((c) => (c.id === contractId ? { ...c, isClaimed: true } : c)));
    setGameState((s) => ({
      ...s,
      solCoins: s.solCoins + contract.rewardCoins,
      ecoPrestige: s.ecoPrestige + contract.rewardEco,
    }));
    addFloatingText(`+${contract.rewardCoins} Coins!`, '#FFD700');
    showNotification('Commission Claimed!', `Earned ${contract.rewardCoins} Sol Coins & ${contract.rewardEco} Prestige.`, 'success');
    return true;
  }, [contracts, inventory, updateInventoryItem, showNotification, addFloatingText]);

  const buySeed = useCallback((cropType: CropTypeId, quantity = 1) => {
    const def = CROP_CATALOG[cropType];
    const totalCost = def.seedCost * quantity;
    if (gameState.solCoins < totalCost) {
      sounds.playErrorSound();
      showNotification('Not Enough Coins', `Thou hast need of ${totalCost} Sol Coins.`, 'warning');
      return false;
    }
    sounds.playButtonTap();
    setGameState((s) => ({ ...s, solCoins: s.solCoins - totalCost }));
    updateInventoryItem(`seed_${cropType.toLowerCase()}`, `${def.displayName} Seeds`, 'SEEDS', quantity, Math.floor(def.seedCost * 0.7));
    addFloatingText(`+${quantity} ${def.displayName} Seeds`, '#81C784');
    return true;
  }, [gameState.solCoins, updateInventoryItem, showNotification, addFloatingText]);

  const sellInventoryItem = useCallback((itemId: string, quantity = 1) => {
    const item = inventory.find((i) => i.itemId === itemId);
    if (!item || item.quantity < quantity) return false;
    const gain = item.sellValue * quantity;
    sounds.playButtonTap();
    updateInventoryItem(itemId, item.name, item.category, -quantity);
    setGameState((s) => ({ ...s, solCoins: s.solCoins + gain }));
    addFloatingText(`+${gain} Coins`, '#FFD700');
    return true;
  }, [inventory, updateInventoryItem, addFloatingText]);

  const drinkWater = useCallback((sourceType: 'CLEAN' | 'RAW') => {
    if (sourceType === 'CLEAN') {
      if (gameState.cleanWaterCarried <= 0) {
        showNotification('No Sacred Water', 'Draw clean water from thy Sacred Well.', 'warning');
        return;
      }
      sounds.playWatering();
      setGameState((s) => ({
        ...s,
        cleanWaterCarried: s.cleanWaterCarried - 1,
        thirst: Math.min(s.maxThirst, s.thirst + 40),
        stamina: Math.min(s.maxStamina, s.stamina + 20),
      }));
      addFloatingText('+40 Thirst Restored', '#00E5FF');
    } else {
      if (gameState.rawWaterCarried <= 0) return;
      sounds.playWatering();
      const causesSickness = Math.random() < 0.4;
      setGameState((s) => ({
        ...s,
        rawWaterCarried: s.rawWaterCarried - 1,
        thirst: Math.min(s.maxThirst, s.thirst + 25),
        isSick: causesSickness ? true : s.isSick,
        sicknessRemainingHours: causesSickness ? 4.0 : s.sicknessRemainingHours,
      }));
      addFloatingText(causesSickness ? 'Sick from Raw Water!' : '+25 Thirst', causesSickness ? '#FF5252' : '#80CBC4');
    }
  }, [gameState.cleanWaterCarried, gameState.rawWaterCarried, showNotification, addFloatingText]);

  const boilWater = useCallback(() => {
    if (gameState.rawWaterCarried <= 0) {
      showNotification('No Raw Water', 'Fill vessel at the mountain stream or wetland river.', 'warning');
      return;
    }
    sounds.playWatering();
    setGameState((s) => ({
      ...s,
      rawWaterCarried: s.rawWaterCarried - 1,
      cleanWaterCarried: Math.min(s.maxWaterCarried, s.cleanWaterCarried + 1),
    }));
    addFloatingText('Purified Sacred Water', '#00E5FF');
  }, [gameState.rawWaterCarried, gameState.maxWaterCarried, showNotification, addFloatingText]);

  const cookFish = useCallback((fishItemId: string) => {
    const item = inventory.find((i) => i.itemId === fishItemId);
    if (!item || item.quantity <= 0) return;
    sounds.playCraftSuccess();
    updateInventoryItem(fishItemId, item.name, 'HARVEST', -1);
    updateInventoryItem('cooked_fish', 'Roasted Feast Fish', 'CRAFTED', 1, 45);
    addFloatingText('Cooked Roasted Fish!', '#FFA726');
  }, [inventory, updateInventoryItem, addFloatingText]);

  const brewTea = useCallback(() => {
    const herbItem = inventory.find((i) => i.itemId.includes('herb') && i.quantity > 0);
    if (!herbItem || gameState.cleanWaterCarried <= 0) {
      showNotification('Missing Ingredients', 'Need Aromatic Herbs and 1 Sacred Water to brew tea.', 'warning');
      return;
    }
    sounds.playCraftSuccess();
    updateInventoryItem(herbItem.itemId, herbItem.name, 'HARVEST', -1);
    setGameState((s) => ({
      ...s,
      cleanWaterCarried: s.cleanWaterCarried - 1,
      thirst: Math.min(s.maxThirst, s.thirst + 30),
      stamina: s.maxStamina,
      isSick: false,
    }));
    addFloatingText('Vitality Tea Restored Stamina!', '#81C784');
  }, [inventory, gameState.cleanWaterCarried, updateInventoryItem, showNotification, addFloatingText]);

  const unlockResearch = useCallback((techId: string) => {
    const tech = researchTechs.find((t) => t.id === techId);
    if (!tech || tech.isUnlocked) return false;
    if (gameState.researchPoints < tech.rpCost) {
      sounds.playErrorSound();
      showNotification('Need More RP', `Thou hast need of ${tech.rpCost} Research Points.`, 'warning');
      return false;
    }

    sounds.playCraftSuccess();
    setGameState((s) => ({
      ...s,
      researchPoints: s.researchPoints - tech.rpCost,
      storyEndingUnlocked: techId === 'tech_new_dawn' ? true : s.storyEndingUnlocked,
    }));
    setResearchTechs((prev) => prev.map((t) => (t.id === techId ? { ...t, isUnlocked: true } : t)));
    addFloatingText('Technology Unlocked!', '#00E5FF');
    showNotification('Research Unlocked!', tech.title + ': ' + tech.unlockFeature, 'success');
    if (techId === 'tech_new_dawn') {
      setActiveModal('ending');
    }
    return true;
  }, [researchTechs, gameState.researchPoints, showNotification, addFloatingText]);

  const hackTerminal = useCallback((terminalId: string, passwordAttempt: string) => {
    const term = terminalLogs.find((t) => t.id === terminalId);
    if (!term) return false;
    if (passwordAttempt.trim().toUpperCase() === term.passwordAnswer.toUpperCase()) {
      sounds.playCraftSuccess();
      setTerminalLogs((prev) => prev.map((t) => (t.id === terminalId ? { ...t, isHacked: true } : t)));
      setGameState((s) => ({ ...s, researchPoints: s.researchPoints + 25, ecoPrestige: s.ecoPrestige + 20 }));
      addFloatingText('Decrypted! +25 RP', '#00E5FF');
      showNotification('Terminal Decrypted', `Unlocked encrypted archives for ${term.terminalName}.`, 'success');
      return true;
    } else {
      sounds.playErrorSound();
      showNotification('Access Denied', 'Invalid master password. Hint: SOLARIS', 'warning');
      return false;
    }
  }, [terminalLogs, addFloatingText, showNotification]);

  const interactWithNpc = useCallback((npcId: number, optionKey: string) => {
    sounds.playButtonTap();
    setNpcs((prev) =>
      prev.map((n) => {
        if (n.id === npcId) {
          return {
            ...n,
            relationshipToPlayer: Math.min(100, n.relationshipToPlayer + 5),
            morale: Math.min(100, n.morale + 5),
            speechBubble: 'Thank thee, sire! Thy leadership brings life to our island.',
            speechBubbleTimer: 4.0,
          };
        }
        return n;
      })
    );
    showNotification('Spoke to Subject', 'Friendship and morale increased.', 'info');
  }, [showNotification]);

  const assignNpcRole = useCallback((npcId: number, newRole: NpcRoleId) => {
    sounds.playButtonTap();
    setNpcs((prev) => prev.map((n) => (n.id === npcId ? { ...n, role: newRole } : n)));
    showNotification('Role Reassigned', `Citizen role updated.`, 'info');
  }, [showNotification]);

  const placeBuilding = useCallback((type: BuildableTypeId, x: number, z: number, rotation: number) => {
    const def = BUILDABLE_CATALOG[type];
    if (gameState.solCoins < def.costCoins) {
      sounds.playErrorSound();
      showNotification('Not Enough Coins', `Requires ${def.costCoins} Sol Coins.`, 'warning');
      return false;
    }

    // Material check
    if (def.requiredMaterialQty > 0) {
      const mat = inventory.find((i) => i.itemId === def.requiredMaterialId && i.quantity >= def.requiredMaterialQty);
      if (!mat) {
        sounds.playErrorSound();
        showNotification('Missing Material', `Requires ${def.requiredMaterialQty}x ${def.materialName}.`, 'warning');
        return false;
      }
      updateInventoryItem(def.requiredMaterialId, def.materialName, 'CRAFTED', -def.requiredMaterialQty);
    }

    sounds.playBuildingComplete();
    setGameState((s) => ({ ...s, solCoins: s.solCoins - def.costCoins, ecoPrestige: s.ecoPrestige + 15 }));
    const newBuilding: PlacedBuildingEntity = {
      id: Date.now(),
      buildingType: type,
      posX: x,
      posY: 0,
      posZ: z,
      rotationDeg: rotation,
      level: 1,
      waterStored: 0,
    };
    setPlacedBuildings((prev) => [...prev, newBuilding]);
    addFloatingText(`Built ${def.displayName}!`, '#FFD54F');
    unlockAchievement('ach_first_building');
    return true;
  }, [gameState.solCoins, inventory, updateInventoryItem, showNotification, addFloatingText, unlockAchievement]);

  const catchFish = useCallback((fishType: FishTypeId, rodTier: number) => {
    const def = FISH_CATALOG[fishType];
    sounds.playHarvest();
    updateInventoryItem(`fish_${fishType.toLowerCase()}`, def.displayName, 'HARVEST', 1, def.sellPrice);
    setGameState((s) => ({
      ...s,
      solCoins: s.solCoins + 15,
      ecoPrestige: s.ecoPrestige + 5,
    }));
    addFloatingText(`Caught ${def.displayName}!`, def.colorHex);
    showNotification('Fish Landed!', `Caught ${def.displayName} (${def.rarity})!`, 'success');
    unlockAchievement('ach_first_catch');
  }, [updateInventoryItem, addFloatingText, showNotification, unlockAchievement]);

  const discoverPoiById = useCallback((poiId: string) => {
    const poi = POINTS_OF_INTEREST.find((p) => p.id === poiId);
    if (!poi || gameState.discoveredPois.includes(poiId)) return;

    sounds.playDiscoverySparkle();
    setGameState((s) => ({
      ...s,
      solCoins: s.solCoins + poi.rewardCoins,
      ecoPrestige: s.ecoPrestige + poi.rewardEcoScore,
      researchPoints: s.researchPoints + 15,
      discoveredPois: [...s.discoveredPois, poiId],
    }));
    updateInventoryItem(`reward_${poiId}`, poi.rewardItemName, 'CRAFTED', 1, 40);
    addFloatingText(`Discovered: ${poi.displayName}!`, '#00E5FF');
    showNotification(poi.discoveryRewardTitle, `${poi.discoveryRewardDesc} (+${poi.rewardCoins} Coins, +15 RP)`, 'achievement');
    unlockAchievement('ach_explorer');
  }, [gameState.discoveredPois, updateInventoryItem, addFloatingText, showNotification, unlockAchievement]);

  const setDevicePriority = useCallback((device: DeviceTypeId, priority: PowerPriorityLevel) => {
    sounds.playButtonTap();
    setDevicePriorities((prev) => ({ ...prev, [device]: priority }));
  }, []);

  const restAtCabin = useCallback(() => {
    sounds.playWatering();
    setGameState((s) => ({
      ...s,
      stamina: s.maxStamina,
      health: s.maxHealth,
      gameTimeHour: 6.0,
      gameTimeDay: s.gameTimeDay + 1,
    }));
    addFloatingText('Slept until Dawn! Fully Restored.', '#FFD700');
    showNotification('A New Morning Dawns', 'Thy vitality and health are completely restored.', 'info');
  }, [addFloatingText, showNotification]);

  const advanceTime = useCallback((hours: number) => {
    setGameState((prev) => {
      let newHour = prev.gameTimeHour + hours;
      let newDay = prev.gameTimeDay;
      while (newHour >= 24) {
        newHour -= 24;
        newDay += 1;
      }
      return { ...prev, gameTimeHour: newHour, gameTimeDay: newDay };
    });
  }, []);

  const resetGameData = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setGameState(INITIAL_GAME_STATE);
    setPlots(INITIAL_PLOTS);
    setEnergyNodes(INITIAL_ENERGY_NODES);
    setLivestock(INITIAL_LIVESTOCK);
    setInventory(INITIAL_INVENTORY);
    setContracts(INITIAL_CONTRACTS);
    setPlacedBuildings([]);
    setNpcs(INITIAL_NPCS);
    setResearchTechs(RESEARCH_TECHS);
    setStoryMissions(INITIAL_STORY_MISSIONS);
    setLoreEntries(INITIAL_LORE);
    setTerminalLogs(INITIAL_TERMINALS);
    setUnlockedAchievements(new Set(['ach_first_steps']));
    sounds.playButtonTap();
    showNotification('Game Reset', 'Thy realm has been restored to its initial state.', 'info');
  }, [showNotification]);

  return (
    <GameContext.Provider
      value={{
        gameState,
        plots,
        energyNodes,
        livestock,
        inventory,
        contracts,
        placedBuildings,
        npcs,
        researchTechs,
        storyMissions,
        loreEntries,
        terminalLogs,
        devicePriorities,
        energySummary,
        ecosystemHealth,
        unlockedAchievements,

        selectedTool,
        setSelectedTool,
        selectedSeedForPouch,
        setSelectedSeedForPouch,
        activeModal,
        openModal: setActiveModal,
        selectedPlotForModal,
        setSelectedPlotForModal,
        selectedAnimalForModal,
        setSelectedAnimalForModal,
        selectedNpcForDialogue,
        setSelectedNpcForDialogue,
        activeTerminal,
        setActiveTerminal,

        isBuildMode,
        setIsBuildMode,
        selectedBuildType,
        setSelectedBuildType,
        buildRotationDeg,
        setBuildRotationDeg,
        placeBuilding,

        waterPlot,
        waterAllPlots,
        plantPlot,
        fertilizePlot,
        harvestPlot,

        feedAnimal,
        petAnimal,
        harvestAnimalProduce,
        buyLivestock,

        craftItem,
        claimContract,
        buySeed,
        sellInventoryItem,

        drinkWater,
        boilWater,
        cookFish,
        brewTea,

        unlockResearch,
        hackTerminal,
        interactWithNpc,
        assignNpcRole,

        catchFish,
        discoverPoiById,
        setDevicePriority,
        restAtCabin,
        advanceTime,

        floatingTexts,
        notifications,
        addFloatingText,
        showNotification,

        soundEnabled,
        setSoundEnabled,
        masterVolume,
        setMasterVolume,
        timeSpeedMultiplier,
        setTimeSpeedMultiplier,
        hudOpacity,
        setHudOpacity,
        resetGameData,
      }}
    >
      {children}
    </GameContext.Provider>
  );
};

export const useGame = () => {
  const ctx = useContext(GameContext);
  if (!ctx) throw new Error('useGame must be used within GameProvider');
  return ctx;
};
