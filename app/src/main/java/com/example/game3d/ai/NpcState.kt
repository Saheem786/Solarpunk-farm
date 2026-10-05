package com.example.game3d.ai

import com.example.data.local.NpcEntity
import com.example.data.model.NpcActivity
import com.example.data.model.NpcRole

/**
 * Finite State Machine (FSM) States for NPC Survivor AI Controller.
 */
sealed class NpcState {
    abstract val activity: NpcActivity

    /** State entered when NPC initializes or has no pending targets */
    object Idle : NpcState() {
        override val activity = NpcActivity.FREE_TIME
    }

    /** Morning breakfast or dinner mealtime state */
    data class Eating(val isDinner: Boolean = false) : NpcState() {
        override val activity = if (isDinner) NpcActivity.DINNER else NpcActivity.WAKING
    }

    /** Active pathfinding along waypoints toward a target destination */
    data class Navigating(
        val destinationX: Float,
        val destinationZ: Float,
        val nextStateAfterArrival: NpcState
    ) : NpcState() {
        override val activity = NpcActivity.WORKING
    }

    /** Farmer job state: watering crops, harvesting, planting seeds */
    data class Farming(val targetPlotId: Int?) : NpcState() {
        override val activity = NpcActivity.WORKING
    }

    /** Engineer/Builder job state: inspecting and repairing power grid & structures */
    data class Repairing(val targetBuildingId: Int?) : NpcState() {
        override val activity = NpcActivity.WORKING
    }

    /** Builder job state: accelerating construction & tier upgrades */
    data class Building(val targetBuildingId: Int?) : NpcState() {
        override val activity = NpcActivity.WORKING
    }

    /** Researcher job state: generating research points at lab */
    data class Researching(val labBuildingId: Int?) : NpcState() {
        override val activity = NpcActivity.WORKING
    }

    /** Medic job state: healing player and nearby survivors at infirmary */
    data class Healing(val stationBuildingId: Int?) : NpcState() {
        override val activity = NpcActivity.WORKING
    }

    /** Socializing and wandering state during free time */
    object Socializing : NpcState() {
        override val activity = NpcActivity.SOCIALIZING
    }

    /** Night time sleeping state in assigned bed or cabin */
    data class Sleeping(val bedX: Float, val bedZ: Float) : NpcState() {
        override val activity = NpcActivity.SLEEPING
    }
}
