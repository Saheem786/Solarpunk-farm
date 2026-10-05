package com.example.game3d.ai

import com.example.data.local.NpcEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BuildableType
import com.example.data.model.CropStage
import com.example.data.model.NpcActivity
import com.example.data.model.NpcRole
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * State Machine Manager for an NPC survivor.
 * Handles state transitions based on schedule, needs, job role priorities, and pathfinding.
 */
class NpcStateMachine(
    var currentState: NpcState = NpcState.Idle
) {

    private var activeWaypoints = listOf<Vector2D>()
    private var stateTimeSec = 0.0f

    fun transitionTo(newState: NpcState) {
        if (currentState == newState) return
        currentState = newState
        stateTimeSec = 0.0f
        activeWaypoints = emptyList()
    }

    /**
     * Evaluates schedule and environmental state to update FSM state and target destination.
     */
    fun evaluateSchedule(
        npc: NpcEntity,
        gameHour: Float,
        plots: List<PlotEntity>,
        placedBuildings: List<PlacedBuildingEntity>,
        dynamicObjects: List<com.example.data.local.LivestockEntity> = emptyList(),
        pathfinder: BiomePathfinder
    ) {
        val scheduledActivity = NpcActivity.getActivityForHour(gameHour)

        when (scheduledActivity) {
            NpcActivity.SLEEPING -> {
                if (currentState !is NpcState.Sleeping && currentState !is NpcState.Navigating) {
                    val bedPos = findAssignedBedPos(npc, placedBuildings)
                    val waypoints = pathfinder.findPathAStar(npc.posX, npc.posZ, bedPos.x, bedPos.z, placedBuildings = placedBuildings, dynamicObjects = dynamicObjects)
                    if (waypoints.isNotEmpty()) {
                        activeWaypoints = waypoints
                        transitionTo(NpcState.Navigating(bedPos.x, bedPos.z, NpcState.Sleeping(bedPos.x, bedPos.z)))
                    } else {
                        transitionTo(NpcState.Sleeping(bedPos.x, bedPos.z))
                    }
                }
            }

            NpcActivity.WAKING, NpcActivity.LUNCH, NpcActivity.DINNER -> {
                if (currentState !is NpcState.Eating && currentState !is NpcState.Navigating) {
                    val kitchen = placedBuildings.find { it.buildingType == BuildableType.KITCHEN }
                    val eatX = kitchen?.posX ?: 4.0f
                    val eatZ = kitchen?.posZ ?: 4.0f
                    val waypoints = pathfinder.findPathAStar(npc.posX, npc.posZ, eatX, eatZ, placedBuildings = placedBuildings, dynamicObjects = dynamicObjects)
                    if (waypoints.isNotEmpty()) {
                        activeWaypoints = waypoints
                        transitionTo(NpcState.Navigating(eatX, eatZ, NpcState.Eating(scheduledActivity == NpcActivity.DINNER)))
                    } else {
                        transitionTo(NpcState.Eating(scheduledActivity == NpcActivity.DINNER))
                    }
                }
            }

            NpcActivity.WORKING -> {
                if (currentState is NpcState.Idle || currentState is NpcState.Eating || currentState is NpcState.Sleeping || currentState is NpcState.Socializing) {
                    val jobTarget = findJobTargetLocation(npc, plots, placedBuildings)
                    if (jobTarget != null) {
                        val waypoints = pathfinder.findPathAStar(npc.posX, npc.posZ, jobTarget.x, jobTarget.z, placedBuildings = placedBuildings, dynamicObjects = dynamicObjects)
                        val jobState = createJobStateForRole(npc.role, jobTarget)
                        if (waypoints.isNotEmpty()) {
                            activeWaypoints = waypoints
                            transitionTo(NpcState.Navigating(jobTarget.x, jobTarget.z, jobState))
                        } else {
                            transitionTo(jobState)
                        }
                    } else {
                        transitionTo(createJobStateForRole(npc.role, Vector2D(npc.posX, npc.posZ)))
                    }
                }
            }

            NpcActivity.FREE_TIME, NpcActivity.SOCIALIZING -> {
                if (currentState !is NpcState.Socializing && currentState !is NpcState.Navigating) {
                    transitionTo(NpcState.Socializing)
                }
            }
        }
    }

    /**
     * Executes the active state behavior for the NPC per delta tick.
     */
    fun tick(
        deltaSec: Float,
        npc: NpcEntity,
        pathfinder: BiomePathfinder,
        placedBuildings: List<PlacedBuildingEntity>
    ): StateTickResult {
        stateTimeSec += deltaSec

        return when (val state = currentState) {
            is NpcState.Navigating -> {
                val step = pathfinder.stepAlongPath(npc.posX, npc.posZ, activeWaypoints, speed = 2.2f, deltaSec = deltaSec)
                activeWaypoints = step.remainingPath

                if (step.isArrived) {
                    transitionTo(state.nextStateAfterArrival)
                }

                StateTickResult(
                    posX = step.x,
                    posZ = step.z,
                    rotDeg = step.rotationDeg,
                    isWalking = step.isWalking,
                    speechBubble = null,
                    taskCompletedDelta = 0
                )
            }

            is NpcState.Farming -> {
                val speech = if (Random.nextFloat() < 0.04f * deltaSec) "Watering and harvesting organic crops..." else null
                StateTickResult(
                    posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg,
                    isWalking = false, speechBubble = speech, taskCompletedDelta = if (stateTimeSec > 5.0f) 1 else 0
                )
            }

            is NpcState.Repairing -> {
                val speech = if (Random.nextFloat() < 0.04f * deltaSec) "Inspecting power nodes & structural integrity..." else null
                StateTickResult(
                    posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg,
                    isWalking = false, speechBubble = speech, taskCompletedDelta = if (stateTimeSec > 6.0f) 1 else 0
                )
            }

            is NpcState.Building -> {
                val speech = if (Random.nextFloat() < 0.04f * deltaSec) "Reinforcing frames! Construction speed +50%" else null
                StateTickResult(
                    posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg,
                    isWalking = false, speechBubble = speech, taskCompletedDelta = if (stateTimeSec > 4.0f) 1 else 0
                )
            }

            is NpcState.Researching -> {
                val speech = if (Random.nextFloat() < 0.04f * deltaSec) "Analyzing closed-loop grid telemetry (+2 RP/day)..." else null
                StateTickResult(
                    posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg,
                    isWalking = false, speechBubble = speech, taskCompletedDelta = if (stateTimeSec > 8.0f) 1 else 0
                )
            }

            is NpcState.Healing -> {
                val speech = if (Random.nextFloat() < 0.04f * deltaSec) "Infirmary open — restoring settlement stamina & health" else null
                StateTickResult(
                    posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg,
                    isWalking = false, speechBubble = speech, taskCompletedDelta = 0
                )
            }

            is NpcState.Socializing -> {
                // Wandering around in free time
                var newX = npc.posX
                var newZ = npc.posZ
                var rot = npc.rotationDeg
                var walking = false

                if (stateTimeSec > 4.0f && Random.nextFloat() < 0.3f) {
                    val angle = Random.nextFloat() * 6.283f
                    val dist = Random.nextFloat() * 2.0f
                    val targetX = npc.posX + cos(angle) * dist
                    val targetZ = npc.posZ + sin(angle) * dist

                    val path = pathfinder.findPathAStar(npc.posX, npc.posZ, targetX, targetZ, placedBuildings = placedBuildings)
                    if (path.isNotEmpty()) {
                        activeWaypoints = path
                        transitionTo(NpcState.Navigating(targetX, targetZ, NpcState.Socializing))
                    }
                }

                val quotes = listOf(
                    "Clean energy makes the valley so vibrant!",
                    "Grateful for our sanctuary community.",
                    "Water reserves are looking good.",
                    "The crops look amazing today!"
                )
                val speech = if (Random.nextFloat() < 0.03f * deltaSec) quotes[Random.nextInt(quotes.size)] else null

                StateTickResult(posX = newX, posZ = newZ, rotDeg = rot, isWalking = walking, speechBubble = speech, taskCompletedDelta = 0)
            }

            is NpcState.Eating -> {
                val speech = if (Random.nextFloat() < 0.05f * deltaSec) "Enjoying communal pantry meal..." else null
                StateTickResult(posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg, isWalking = false, speechBubble = speech, taskCompletedDelta = 0)
            }

            is NpcState.Sleeping -> {
                StateTickResult(posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg, isWalking = false, speechBubble = null, taskCompletedDelta = 0)
            }

            else -> {
                StateTickResult(posX = npc.posX, posZ = npc.posZ, rotDeg = npc.rotationDeg, isWalking = false, speechBubble = null, taskCompletedDelta = 0)
            }
        }
    }

    private fun findAssignedBedPos(npc: NpcEntity, buildings: List<PlacedBuildingEntity>): Vector2D {
        val cabins = buildings.filter { it.buildingType == BuildableType.NPC_CABIN || it.buildingType == BuildableType.BUNKHOUSE }
        if (cabins.isNotEmpty()) {
            val home = cabins[npc.id % cabins.size]
            return Vector2D(home.posX, home.posZ)
        }
        return Vector2D(6.0f + (npc.id % 3) * 1.5f, -2.0f)
    }

    private fun findJobTargetLocation(
        npc: NpcEntity,
        plots: List<PlotEntity>,
        buildings: List<PlacedBuildingEntity>
    ): Vector2D? {
        return when (npc.role) {
            NpcRole.FARMER -> {
                val targetPlot = plots.find { it.cropType != null && (it.moisture < 0.5f || it.stage == CropStage.HARVEST_READY) }
                if (targetPlot != null) Vector2D(targetPlot.posX, targetPlot.posZ) else Vector2D(2.0f, 6.0f)
            }
            NpcRole.ENGINEER -> {
                val powerNode = buildings.find { it.buildingType == BuildableType.ADVANCED_SOLAR || it.buildingType == BuildableType.BASIC_BATTERY || it.buildingType == BuildableType.WINDMILL }
                if (powerNode != null) Vector2D(powerNode.posX + 1.2f, powerNode.posZ + 1.2f) else Vector2D(0.0f, 14.0f)
            }
            NpcRole.BUILDER -> Vector2D(-2.0f, 10.0f)
            NpcRole.RESEARCHER -> {
                val lab = buildings.find { it.buildingType == BuildableType.RESEARCH_LAB }
                Vector2D(lab?.posX ?: 12.0f, lab?.posZ ?: -8.0f)
            }
            NpcRole.MEDIC -> {
                val station = buildings.find { it.buildingType == BuildableType.MEDIC_STATION }
                Vector2D(station?.posX ?: 8.0f, station?.posZ ?: 4.0f)
            }
        }
    }

    private fun createJobStateForRole(role: NpcRole, target: Vector2D): NpcState {
        return when (role) {
            NpcRole.FARMER -> NpcState.Farming(targetPlotId = null)
            NpcRole.ENGINEER -> NpcState.Repairing(targetBuildingId = null)
            NpcRole.BUILDER -> NpcState.Building(targetBuildingId = null)
            NpcRole.RESEARCHER -> NpcState.Researching(labBuildingId = null)
            NpcRole.MEDIC -> NpcState.Healing(stationBuildingId = null)
        }
    }

    data class StateTickResult(
        val posX: Float,
        val posZ: Float,
        val rotDeg: Float,
        val isWalking: Boolean,
        val speechBubble: String?,
        val taskCompletedDelta: Int
    )
}
