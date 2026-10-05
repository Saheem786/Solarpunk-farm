package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BuildableType
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
import com.example.data.model.NpcActivity
import com.example.data.model.NpcPersonalityTrait
import com.example.data.model.NpcRole
import com.example.data.model.PlotType
import com.example.data.model.WeatherType

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val solCoins: Int = 300,
    val ecoPrestige: Int = 50,
    val batteryChargeKwh: Float = 40.0f,
    val batteryMaxCapacityKwh: Float = 100.0f,
    val gameTimeHour: Float = 8.0f, // 8:00 AM Morning start
    val gameTimeDay: Int = 1,
    val currentWeather: WeatherType = WeatherType.SUNNY_CLEAR,
    val playerX: Float = 0.0f,
    val playerY: Float = 0.0f,
    val playerZ: Float = 0.0f,
    val playerAngle: Float = 0.0f,
    val totalHarvests: Int = 0,
    val carbonOffsetKg: Float = 120.0f,
    val autoIrrigationUnlocked: Boolean = false,
    val droneHarvesterUnlocked: Boolean = false,
    val health: Float = 100.0f,
    val maxHealth: Float = 100.0f,
    val hunger: Float = 100.0f,
    val maxHunger: Float = 100.0f,
    val thirst: Float = 100.0f,
    val maxThirst: Float = 100.0f,
    val stamina: Float = 100.0f,
    val maxStamina: Float = 100.0f,
    val weatherChangeCountdownHours: Float = 6.0f,
    val discoveredPois: String = "poi_old_farm",
    val discoveredChunks: String = "9_9,9_10,10_9,10_10",
    val discoveredBiomes: String = "green_valley",
    val currentBiomeId: String = "green_valley",
    val cleanWaterCarried: Int = 4,
    val rawWaterCarried: Int = 0,
    val maxWaterCarried: Int = 10,
    val isSick: Boolean = false,
    val sicknessRemainingHours: Float = 0.0f,
    val fishingRodTier: Int = 1,
    val fishPopStream: Int = 10,
    val fishPopRiver: Int = 10,
    val fishPopPond: Int = 10,
    val researchPoints: Int = 20,
    val activeMissionId: String = "mission_1",
    val storyEndingUnlocked: Boolean = false
)

@Entity(tableName = "farm_plots")
data class PlotEntity(
    @PrimaryKey val id: Int,
    val plotType: PlotType = PlotType.PERMACULTURE_BED,
    val cropType: CropType? = null,
    val stage: CropStage = CropStage.EMPTY,
    val progress: Float = 0.0f, // 0.0 to 1.0
    val moisture: Float = 0.8f, // 0.0 to 1.0
    val compostLevel: Float = 0.5f,
    val posX: Float = 0.0f,
    val posY: Float = 0.0f,
    val posZ: Float = 0.0f,
    val autoIrrigated: Boolean = false
)

@Entity(tableName = "energy_nodes")
data class EnergyNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nodeType: EnergyNodeType,
    val level: Int = 1,
    val efficiency: Float = 1.0f,
    val posX: Float = 0.0f,
    val posY: Float = 0.0f,
    val posZ: Float = 0.0f,
    val isActive: Boolean = true
)

@Entity(tableName = "livestock")
data class LivestockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: LivestockType,
    val name: String,
    val happiness: Float = 80.0f, // 0 to 100
    val hunger: Float = 20.0f, // 0 to 100 (0 = full, 100 = starving)
    val age: Int = 1,
    val produceProgress: Float = 0.0f, // 0.0 to 1.0
    val readyToHarvest: Boolean = false,
    val posX: Float = 0.0f,
    val posY: Float = 0.0f,
    val posZ: Float = 0.0f,
    val targetX: Float = 0.0f,
    val targetZ: Float = 0.0f
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val name: String,
    val category: ItemCategory,
    val quantity: Int = 0,
    val sellValue: Int = 10
)

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey val id: String,
    val title: String,
    val buyerName: String,
    val requiredItemId: String,
    val requiredItemName: String,
    val requiredQty: Int,
    val currentQty: Int = 0,
    val rewardCoins: Int,
    val rewardEco: Int,
    val isClaimed: Boolean = false
)

@Entity(tableName = "placed_buildings")
data class PlacedBuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buildingType: BuildableType,
    val posX: Float,
    val posY: Float = 0.0f,
    val posZ: Float,
    val rotationDeg: Float = 0.0f,
    val level: Int = 1,
    val waterStored: Float = 0.0f,
    val customData: String = ""
)

@Entity(tableName = "npcs")
data class NpcEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: NpcRole,
    val age: Int = 26,
    val skillLevel: Int = 1,
    val trait: NpcPersonalityTrait = NpcPersonalityTrait.CHEERFUL,
    val appearanceColor: Long = 0xFF81C784,
    val hairColor: Long = 0xFF4E342E,
    val morale: Float = 75.0f, // 0 to 100
    val hunger: Float = 20.0f, // 0 to 100 (0 = full, 100 = starving)
    val thirst: Float = 20.0f, // 0 to 100 (0 = hydrated, 100 = dehydrated)
    val assignedBedId: Int? = null,
    val assignedBuildingId: Int? = null,
    val currentActivity: NpcActivity = NpcActivity.WORKING,
    val posX: Float = 6.0f,
    val posY: Float = 0.0f,
    val posZ: Float = 2.0f,
    val targetX: Float = 6.0f,
    val targetZ: Float = 2.0f,
    val rotationDeg: Float = 0.0f,
    val isWalking: Boolean = false,
    val speechBubble: String? = null,
    val speechBubbleTimer: Float = 0.0f,
    val relationshipToPlayer: Int = 25, // -100 to +100
    val partnerNpcId: Int? = null,
    val tasksCompleted: Int = 0
)

@Entity(tableName = "npc_arrivals")
data class NpcArrivalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: NpcRole,
    val name: String,
    val dayTrigger: Int,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED_COOLDOWN
    val cooldownDaysRemaining: Int = 0
)

@Entity(tableName = "research_techs")
data class ResearchTechEntity(
    @PrimaryKey val id: String,
    val tier: Int,
    val title: String,
    val rpCost: Int,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "story_missions")
data class StoryMissionEntity(
    @PrimaryKey val id: String,
    val missionNumber: Int,
    val title: String,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "points_of_interest")
data class PoiEntity(
    @PrimaryKey val id: String,
    val name: String,
    val biomeId: String,
    val posX: Float,
    val posZ: Float,
    val isDiscovered: Boolean = false
)

@Entity(tableName = "lore_entries")
data class LoreEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val category: String,
    val textContent: String,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "terminal_logs")
data class TerminalLogEntity(
    @PrimaryKey val id: String,
    val terminalName: String,
    val locationName: String,
    val isHacked: Boolean = false
)
