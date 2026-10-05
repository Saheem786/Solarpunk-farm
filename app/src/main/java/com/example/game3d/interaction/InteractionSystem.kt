package com.example.game3d.interaction

import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.CropStage
import com.example.data.model.PlayerTool
import kotlin.math.abs
import kotlin.math.sqrt

enum class InteractionTargetType {
    NONE,
    PLOT,
    ENERGY_NODE,
    LIVESTOCK,
    WORKSHOP_BUILDING,
    MARKET_STALL,
    WATER_SOURCE,
    WATER_BUILDING,
    CAMPFIRE,
    FARMHOUSE,
    NPC
}

data class InteractionPrompt(
    val targetType: InteractionTargetType,
    val targetId: Int,
    val title: String,
    val subtitle: String,
    val recommendedTool: PlayerTool,
    val distance: Float,
    val secondaryActionTitle: String? = null,
    val waterTypeKey: String = "clean"
)

object InteractionSystem {

    private const val MAX_INTERACT_DISTANCE = 4.2f

    fun findNearestInteraction(
        playerX: Float,
        playerZ: Float,
        plots: List<PlotEntity>,
        energyNodes: List<EnergyNodeEntity>,
        animals: List<LivestockEntity>,
        placedBuildings: List<PlacedBuildingEntity> = emptyList()
    ): InteractionPrompt? {
        var closestPrompt: InteractionPrompt? = null
        var closestDist = MAX_INTERACT_DISTANCE

        // Check Farm Plots
        for (plot in plots) {
            val dist = distance(playerX, playerZ, plot.posX, plot.posZ)
            if (dist < closestDist) {
                closestDist = dist
                val prompt = when {
                    plot.stage == CropStage.HARVEST_READY && plot.cropType != null -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.PLOT,
                            targetId = plot.id,
                            title = "Harvest ${plot.cropType.displayName}",
                            subtitle = "Ready (+${plot.cropType.harvestYield} yield)",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist
                        )
                    }
                    plot.cropType == null -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.PLOT,
                            targetId = plot.id,
                            title = "Plant Seed",
                            subtitle = "Select crop to sow",
                            recommendedTool = PlayerTool.SEED_POUCH,
                            distance = dist
                        )
                    }
                    plot.moisture < 0.35f -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.PLOT,
                            targetId = plot.id,
                            title = "Water",
                            subtitle = "Moisture: ${(plot.moisture * 100).toInt()}%",
                            recommendedTool = PlayerTool.WATER_CAN,
                            distance = dist
                        )
                    }
                    else -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.PLOT,
                            targetId = plot.id,
                            title = "Inspect ${plot.cropType.displayName}",
                            subtitle = "Stage: ${plot.stage.name} (${(plot.progress * 100).toInt()}%)",
                            recommendedTool = PlayerTool.FERTILIZER,
                            distance = dist
                        )
                    }
                }
                closestPrompt = prompt
            }
        }

        // Check Livestock
        for (animal in animals) {
            val dist = distance(playerX, playerZ, animal.posX, animal.posZ)
            if (dist < closestDist) {
                closestDist = dist
                closestPrompt = when {
                    animal.readyToHarvest -> {
                        val actionTitle = when (animal.type) {
                            com.example.data.model.LivestockType.CHICKEN -> "Collect Egg"
                            com.example.data.model.LivestockType.CYBER_BOVINE -> "Milk Cow"
                            else -> "Collect ${animal.type.productProduced}"
                        }
                        val actionSubtitle = when (animal.type) {
                            com.example.data.model.LivestockType.CHICKEN -> "Layed by ${animal.name}"
                            com.example.data.model.LivestockType.CYBER_BOVINE -> "Fresh milk from ${animal.name}"
                            else -> "Ready from ${animal.name}"
                        }
                        InteractionPrompt(
                            targetType = InteractionTargetType.LIVESTOCK,
                            targetId = animal.id,
                            title = actionTitle,
                            subtitle = actionSubtitle,
                            recommendedTool = PlayerTool.SHEARS,
                            distance = dist
                        )
                    }
                    animal.hunger > 50.0f -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.LIVESTOCK,
                            targetId = animal.id,
                            title = "Feed ${animal.name} (${animal.type.displayName})",
                            subtitle = "Hungry! Provide organic grain",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist
                        )
                    }
                    else -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.LIVESTOCK,
                            targetId = animal.id,
                            title = "Pet ${animal.name} (${animal.type.displayName})",
                            subtitle = "Happiness: ${animal.happiness.toInt()}%",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist
                        )
                    }
                }
            }
        }

        // Check Clean Energy Nodes
        for (node in energyNodes) {
            val dist = distance(playerX, playerZ, node.posX, node.posZ)
            if (dist < closestDist) {
                closestDist = dist
                closestPrompt = InteractionPrompt(
                    targetType = InteractionTargetType.ENERGY_NODE,
                    targetId = node.id,
                    title = "Manage ${node.nodeType.displayName}",
                    subtitle = "Level ${node.level} • Output ${(node.nodeType.baseOutputKwhPerSec * node.efficiency).toInt()} kWh/s",
                    recommendedTool = PlayerTool.SOLAR_WRENCH,
                    distance = dist
                )
            }
        }

        // Check Placed Water Buildings (Rain Barrels, Water Filters, Purifiers, Cisterns, Irrigation)
        for (b in placedBuildings) {
            val dist = distance(playerX, playerZ, b.posX, b.posZ)
            if (dist < closestDist) {
                when (b.buildingType) {
                    BuildableType.RAIN_BARREL -> {
                        closestDist = dist
                        closestPrompt = InteractionPrompt(
                            targetType = InteractionTargetType.WATER_BUILDING,
                            targetId = b.id,
                            title = "Drink Clean Water",
                            subtitle = "Rain Barrel (${b.waterStored.toInt()}/100 units)",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist,
                            secondaryActionTitle = "Collect Water",
                            waterTypeKey = "barrel"
                        )
                    }
                    BuildableType.WATER_FILTER -> {
                        closestDist = dist
                        closestPrompt = InteractionPrompt(
                            targetType = InteractionTargetType.WATER_BUILDING,
                            targetId = b.id,
                            title = "Collect Clean Water",
                            subtitle = "Filter (${b.waterStored.toInt()} units) • Tap to fill/collect",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist,
                            secondaryActionTitle = "Fill Filter",
                            waterTypeKey = "filter"
                        )
                    }
                    BuildableType.WATER_PURIFIER -> {
                        closestDist = dist
                        closestPrompt = InteractionPrompt(
                            targetType = InteractionTargetType.WATER_BUILDING,
                            targetId = b.id,
                            title = "Collect Clean Water",
                            subtitle = "UV Purifier • Tap to collect pure water",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist,
                            secondaryActionTitle = "Collect Pure",
                            waterTypeKey = "purifier"
                        )
                    }
                    BuildableType.WATER_STORAGE_SHED -> {
                        closestDist = dist
                        closestPrompt = InteractionPrompt(
                            targetType = InteractionTargetType.WATER_BUILDING,
                            targetId = b.id,
                            title = "Collect Clean Water",
                            subtitle = "Cistern Tank (${b.waterStored.toInt()}/100 units)",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist,
                            secondaryActionTitle = "Collect Cistern",
                            waterTypeKey = "storage"
                        )
                    }
                    BuildableType.IRRIGATION_NODE -> {
                        closestDist = dist
                        closestPrompt = InteractionPrompt(
                            targetType = InteractionTargetType.WATER_BUILDING,
                            targetId = b.id,
                            title = "Irrigation Sprinkler",
                            subtitle = "Waters crops within 3m at 6:00 AM",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist
                        )
                    }
                    else -> {}
                }
            }
        }

        // Check Artisan Workshop (Located at X=0, Z=14)
        val workshopDist = distance(playerX, playerZ, 0.0f, 14.0f)
        if (workshopDist < closestDist) {
            closestDist = workshopDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WORKSHOP_BUILDING,
                targetId = 999,
                title = "Artisan Eco-Workshop",
                subtitle = "Craft bio-fuel, fishing rods, textiles & tools",
                recommendedTool = PlayerTool.HAND,
                distance = workshopDist
            )
        }

        // Check Solarpunk Market Stall (Located at X=-14, Z=-14)
        val marketDist = distance(playerX, playerZ, -14.0f, -14.0f)
        if (marketDist < closestDist) {
            closestDist = marketDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.MARKET_STALL,
                targetId = 998,
                title = "Trade",
                subtitle = "Sol City Trading Post • Rods, Seeds & Fish",
                recommendedTool = PlayerTool.HAND,
                distance = marketDist
            )
        }

        // Check Campfire (Old Campsite at X=-32, Z=85)
        val campDist = distance(playerX, playerZ, -32.0f, 85.0f)
        if (campDist < closestDist) {
            closestDist = campDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.CAMPFIRE,
                targetId = 995,
                title = "Boil Water / Cook Fish",
                subtitle = "Campfire • Purify raw water & grill fresh catch",
                recommendedTool = PlayerTool.HAND,
                distance = campDist,
                secondaryActionTitle = "Cook Fish"
            )
        }

        // Check Drinking Water Well (Near House at X=8, Z=-3.5)
        val wellDist = distance(playerX, playerZ, 8.0f, -3.5f)
        if (wellDist < closestDist) {
            closestDist = wellDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 997,
                title = "Drink Clean Water",
                subtitle = "Infinite Pure Well (+40 Thirst)",
                recommendedTool = PlayerTool.HAND,
                distance = wellDist,
                secondaryActionTitle = "Collect Clean Water",
                waterTypeKey = "well"
            )
        }

        // Check Green Valley Stream (Located along X=16 to 18, Z=-16 to 14)
        val streamDist = abs(playerX - 16.5f)
        if (streamDist < 3.2f && playerZ in -18.0f..16.0f && streamDist < closestDist) {
            closestDist = streamDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 994,
                title = "Drink Raw Water",
                subtitle = "Stream (Contaminated!) • Restores +30 Thirst",
                recommendedTool = PlayerTool.FISHING_ROD,
                distance = streamDist,
                secondaryActionTitle = "Fish Stream",
                waterTypeKey = "stream"
            )
        }

        // Check Wetland River (Located across Z=-100, 10m wide)
        val riverDist = abs(playerZ - (-100.0f))
        if (riverDist < 5.0f && riverDist < closestDist) {
            closestDist = riverDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 993,
                title = "Drink Raw Water",
                subtitle = "River (Contaminated!) • Restores +30 Thirst",
                recommendedTool = PlayerTool.FISHING_ROD,
                distance = riverDist,
                secondaryActionTitle = "Fish River",
                waterTypeKey = "river"
            )
        }

        // Check Wetland Pond (Located at X=20, Z=-120)
        val wetlandPondDist = distance(playerX, playerZ, 20.0f, -120.0f)
        if (wetlandPondDist < 6.5f && wetlandPondDist < closestDist) {
            closestDist = wetlandPondDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 992,
                title = "Drink Raw Water",
                subtitle = "Wetland Pond (Contaminated!) • Restores +30 Thirst",
                recommendedTool = PlayerTool.FISHING_ROD,
                distance = wetlandPondDist,
                secondaryActionTitle = "Fish Pond",
                waterTypeKey = "pond"
            )
        }

        // Check Farmstead Pond (Located at X=10, Z=-10)
        val pondDist = distance(playerX, playerZ, 10.0f, -10.0f)
        if (pondDist < 5.5f && pondDist < closestDist) {
            closestDist = pondDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 991,
                title = "Drink Raw Water",
                subtitle = "Farm Pond (Contaminated!) • Restores +30 Thirst",
                recommendedTool = PlayerTool.FISHING_ROD,
                distance = pondDist,
                secondaryActionTitle = "Fish Pond",
                waterTypeKey = "stream"
            )
        }

        // Check Farmhouse (Located at X=6, Z=0)
        val houseDist = distance(playerX, playerZ, 6.0f, 0.0f)
        if (houseDist < closestDist) {
            closestDist = houseDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.FARMHOUSE,
                targetId = 996,
                title = "Rest / Sleep",
                subtitle = "Cures sickness & restores full stamina",
                recommendedTool = PlayerTool.HAND,
                distance = houseDist
            )
        }

        return closestPrompt
    }

    private fun distance(x1: Float, z1: Float, x2: Float, z2: Float): Float {
        val dx = x1 - x2
        val dz = z1 - z2
        return sqrt(dx * dx + dz * dz)
    }
}
