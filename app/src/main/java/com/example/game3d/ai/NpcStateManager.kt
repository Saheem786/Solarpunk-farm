package com.example.game3d.ai

import com.example.data.local.LivestockEntity
import com.example.data.local.NpcEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BiomeType
import com.example.data.model.NpcActivity
import com.example.data.model.NpcRole
import kotlin.math.sqrt

/**
 * Settlement Zone Boundary Definitions for NPC territorial navigation.
 */
enum class ZoneType(
    val displayName: String,
    val minX: Float,
    val maxX: Float,
    val minZ: Float,
    val maxZ: Float,
    val maxRadiusFromCenter: Float = 30.0f
) {
    SETTLEMENT_CORE("Sanctuary Core", -30.0f, 30.0f, -30.0f, 30.0f, 30.0f),
    GREEN_VALLEY_FARMLAND("Green Valley Farm", -45.0f, 35.0f, -45.0f, 35.0f, 40.0f),
    DEEP_FOREST_OUTPOST("Deep Forest Outpost", -100.0f, -45.0f, -50.0f, 50.0f, 50.0f),
    WETLAND_DELTA_ZONE("Wetland River Delta", 35.0f, 100.0f, -40.0f, 40.0f, 50.0f);

    fun contains(x: Float, z: Float): Boolean {
        return x in minX..maxX && z in minZ..maxZ
    }

    fun clamp(x: Float, z: Float): Vector2D {
        val clampedX = x.coerceIn(minX + 1.0f, maxX - 1.0f)
        val clampedZ = z.coerceIn(minZ + 1.0f, maxZ - 1.0f)
        return Vector2D(clampedX, clampedZ)
    }

    companion object {
        fun getZoneForBiome(biome: BiomeType): ZoneType = when (biome) {
            BiomeType.GREEN_VALLEY -> GREEN_VALLEY_FARMLAND
            BiomeType.DEEP_FOREST -> DEEP_FOREST_OUTPOST
            BiomeType.WETLAND -> WETLAND_DELTA_ZONE
        }
    }
}

/**
 * Listener interface for NPC task state change transition events.
 */
fun interface NpcStateChangeListener {
    fun onStateTransition(npcId: Int, oldState: NpcState, newState: NpcState, transitionReason: String)
}

/**
 * Manager handling NPC state transitions, daily schedule shifts (farming, repairing, sleeping),
 * task priorities, and zone/biome boundary enforcement.
 */
class NpcStateManager {

    private val stateMachines = mutableMapOf<Int, NpcStateMachine>()
    private var transitionListener: NpcStateChangeListener? = null

    fun setTransitionListener(listener: NpcStateChangeListener) {
        this.transitionListener = listener
    }

    /**
     * Retrieves or initializes the FSM for a given NPC.
     */
    fun getOrCreateStateMachine(npcId: Int): NpcStateMachine {
        return stateMachines.getOrPut(npcId) { NpcStateMachine(NpcState.Idle) }
    }

    /**
     * Executes a state transition for an NPC, firing lifecycle transition events.
     */
    fun transitionState(npcId: Int, newState: NpcState, reason: String = "Schedule Shift") {
        val fsm = getOrCreateStateMachine(npcId)
        val oldState = fsm.currentState
        if (oldState != newState) {
            fsm.transitionTo(newState)
            transitionListener?.onStateTransition(npcId, oldState, newState, reason)
        }
    }

    /**
     * Evaluates daily schedule shifts and updates task state transitions for an NPC survivor.
     */
    fun updateNpcSchedule(
        npc: NpcEntity,
        gameHour: Float,
        plots: List<PlotEntity>,
        placedBuildings: List<PlacedBuildingEntity>,
        dynamicObjects: List<LivestockEntity>,
        pathfinder: BiomePathfinder
    ) {
        val fsm = getOrCreateStateMachine(npc.id)
        val previousState = fsm.currentState

        // Evaluate schedule & pathfinding targets
        fsm.evaluateSchedule(npc, gameHour, plots, placedBuildings, dynamicObjects, pathfinder)

        val newState = fsm.currentState
        if (previousState != newState) {
            val reason = "Schedule transition to ${newState.activity.displayName}"
            transitionListener?.onStateTransition(npc.id, previousState, newState, reason)
        }
    }

    /**
     * Enforces biome and zone territory boundaries on NPC positions.
     * Keeps NPCs within 30 units of their assigned cabin/bed or within their designated biome zone.
     */
    fun enforceZoneBoundaries(
        posX: Float,
        posZ: Float,
        assignedBedX: Float?,
        assignedBedZ: Float?,
        currentBiome: BiomeType
    ): Vector2D {
        val zone = ZoneType.getZoneForBiome(currentBiome)
        var clamped = zone.clamp(posX, posZ)

        // Bed / Housing Anchor Restriction (Stay within 30 units of bed)
        if (assignedBedX != null && assignedBedZ != null) {
            val dx = clamped.x - assignedBedX
            val dz = clamped.z - assignedBedZ
            val distFromBed = sqrt(dx * dx + dz * dz)
            val maxDist = 30.0f

            if (distFromBed > maxDist) {
                val ratio = maxDist / distFromBed
                clamped = Vector2D(assignedBedX + dx * ratio, assignedBedZ + dz * ratio)
            }
        }

        return clamped
    }

    /**
     * Returns a contextual speech bubble quote when an NPC transitions to a new state.
     */
    fun getTransitionSpeechQuote(role: NpcRole, activity: NpcActivity): String {
        return when (activity) {
            NpcActivity.WAKING -> "Good morning! Time for a fresh breakfast."
            NpcActivity.WORKING -> when (role) {
                NpcRole.FARMER -> "Heading to the crops for morning watering!"
                NpcRole.ENGINEER -> "Inspecting power grid micro-inverters."
                NpcRole.BUILDER -> "Checking structural integrity of our cabins."
                NpcRole.RESEARCHER -> "Heading to the Research Lab to analyze data."
                NpcRole.MEDIC -> "Opening the infirmary for health checkups."
            }
            NpcActivity.LUNCH -> "Taking a quick lunch break at the kitchen."
            NpcActivity.FREE_TIME, NpcActivity.SOCIALIZING -> "Free time! Time to stroll around our sanctuary."
            NpcActivity.DINNER -> "Joining everyone at the community kitchen for dinner."
            NpcActivity.SLEEPING -> "Resting up for another productive day. Goodnight!"
        }
    }

    fun removeNpc(npcId: Int) {
        stateMachines.remove(npcId)
    }

    fun clearAll() {
        stateMachines.clear()
    }
}
