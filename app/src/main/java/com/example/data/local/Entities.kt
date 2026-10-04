package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.ItemCategory
import com.example.data.model.LivestockType
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
    val droneHarvesterUnlocked: Boolean = false
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
