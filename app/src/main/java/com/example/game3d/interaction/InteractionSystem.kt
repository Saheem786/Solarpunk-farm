package com.example.game3d.interaction

import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlotEntity
import com.example.data.model.CropStage
import com.example.data.model.PlayerTool
import kotlin.math.sqrt

enum class InteractionTargetType {
    NONE,
    PLOT,
    ENERGY_NODE,
    LIVESTOCK,
    WORKSHOP_BUILDING,
    MARKET_STALL,
    WATER_SOURCE,
    FARMHOUSE
}

data class InteractionPrompt(
    val targetType: InteractionTargetType,
    val targetId: Int,
    val title: String,
    val subtitle: String,
    val recommendedTool: PlayerTool,
    val distance: Float
)

object InteractionSystem {

    private const val MAX_INTERACT_DISTANCE = 4.2f

    fun findNearestInteraction(
        playerX: Float,
        playerZ: Float,
        plots: List<PlotEntity>,
        energyNodes: List<EnergyNodeEntity>,
        animals: List<LivestockEntity>
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
                            subtitle = "Ready to collect (+${plot.cropType.harvestYield} items)",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist
                        )
                    }
                    plot.cropType == null -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.PLOT,
                            targetId = plot.id,
                            title = "Plant ${plot.plotType.displayName}",
                            subtitle = "Select crop seeds to sow",
                            recommendedTool = PlayerTool.SEED_POUCH,
                            distance = dist
                        )
                    }
                    plot.moisture < 0.35f -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.PLOT,
                            targetId = plot.id,
                            title = "Water ${plot.cropType.displayName}",
                            subtitle = "Moisture low: ${(plot.moisture * 100).toInt()}%",
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
                        InteractionPrompt(
                            targetType = InteractionTargetType.LIVESTOCK,
                            targetId = animal.id,
                            title = "Collect ${animal.type.productProduced}",
                            subtitle = "Ready from ${animal.name}",
                            recommendedTool = PlayerTool.SHEARS,
                            distance = dist
                        )
                    }
                    animal.hunger > 50.0f -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.LIVESTOCK,
                            targetId = animal.id,
                            title = "Feed ${animal.name}",
                            subtitle = "Hungry! Provide organic grain",
                            recommendedTool = PlayerTool.HAND,
                            distance = dist
                        )
                    }
                    else -> {
                        InteractionPrompt(
                            targetType = InteractionTargetType.LIVESTOCK,
                            targetId = animal.id,
                            title = "Pet ${animal.name}",
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

        // Check Artisan Workshop (Located at X=0, Z=14)
        val workshopDist = distance(playerX, playerZ, 0.0f, 14.0f)
        if (workshopDist < closestDist) {
            closestDist = workshopDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WORKSHOP_BUILDING,
                targetId = 999,
                title = "Artisan Eco-Workshop",
                subtitle = "Craft bio-fuel, textiles, and smart tools",
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
                title = "Sol City Trading Post",
                subtitle = "Sell produce & claim wholesale contracts",
                recommendedTool = PlayerTool.HAND,
                distance = marketDist
            )
        }

        // Check Drinking Water Well (Near House at X=8, Z=-3.5)
        val wellDist = distance(playerX, playerZ, 8.0f, -3.5f)
        if (wellDist < closestDist) {
            closestDist = wellDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 997,
                title = "Drink from Well",
                subtitle = "Pure Ground Water (+40 Thirst)",
                recommendedTool = PlayerTool.HAND,
                distance = wellDist
            )
        }

        // Check Fresh Water Pond (Located at X=10, Z=-10)
        val pondDist = distance(playerX, playerZ, 10.0f, -10.0f)
        if (pondDist < closestDist) {
            closestDist = pondDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 997,
                title = "Drink Spring Water",
                subtitle = "Replenish Thirst (+40 Thirst)",
                recommendedTool = PlayerTool.HAND,
                distance = pondDist
            )
        }

        // Check Scenic River (Located at X=16, Z=0)
        val riverDist = distance(playerX, playerZ, 16.0f, 0.0f)
        if (riverDist < closestDist) {
            closestDist = riverDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.WATER_SOURCE,
                targetId = 997,
                title = "Drink River Water",
                subtitle = "Fresh Mountain Stream (+40 Thirst)",
                recommendedTool = PlayerTool.HAND,
                distance = riverDist
            )
        }

        // Check Farmhouse (Located at X=6, Z=0)
        val houseDist = distance(playerX, playerZ, 6.0f, 0.0f)
        if (houseDist < closestDist) {
            closestDist = houseDist
            closestPrompt = InteractionPrompt(
                targetType = InteractionTargetType.FARMHOUSE,
                targetId = 996,
                title = "Rest in Farmhouse",
                subtitle = "Restore full stamina & recover health (+1 hr)",
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
