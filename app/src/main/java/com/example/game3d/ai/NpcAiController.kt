package com.example.game3d.ai

import com.example.data.local.LivestockEntity
import com.example.data.local.NpcEntity
import com.example.data.local.PlacedBuildingEntity
import com.example.data.local.PlotEntity
import com.example.data.model.BiomeType
import com.example.data.model.CropStage
import com.example.data.model.NpcActivity
import com.example.data.model.NpcRole
import kotlin.math.min
import kotlin.random.Random

/**
 * Master NPC AI Controller that manages finite state machines, pathfinding, daily schedules,
 * and job behaviors (farming, engineering repairs, research, medical care) for all settlement survivors.
 */
class NpcAiController {

    val stateManager = NpcStateManager()
    private val pathfinder = BiomePathfinder()
    private val pendingSpeechQuotes = mutableMapOf<Int, String>()

    init {
        stateManager.setTransitionListener { npcId, _, newState, _ ->
            val quote = stateManager.getTransitionSpeechQuote(NpcRole.FARMER, newState.activity)
            pendingSpeechQuotes[npcId] = quote
        }
    }

    /**
     * Ticks the AI controller for all active NPCs in the game loop.
     * Evaluates schedules, drives pathfinding along waypoints, executes job routines,
     * and returns the updated list of NPC entities.
     */
    fun tickAll(
        npcs: List<NpcEntity>,
        deltaSec: Float,
        gameHour: Float,
        plots: List<PlotEntity>,
        placedBuildings: List<PlacedBuildingEntity>,
        livestock: List<LivestockEntity> = emptyList(),
        currentBiome: BiomeType = BiomeType.GREEN_VALLEY
    ): ControllerUpdateResult {

        val updatedNpcs = mutableListOf<NpcEntity>()
        var totalFoodConsumed = 0
        var totalWaterConsumed = 0
        var researchPointsGenerated = 0
        var healedPlayerHp = 0.0f

        for (npc in npcs) {
            val fsm = stateManager.getOrCreateStateMachine(npc.id)

            // 1. Evaluate schedule and task transitions via NpcStateManager
            stateManager.updateNpcSchedule(npc, gameHour, plots, placedBuildings, livestock, pathfinder)

            // 2. Perform FSM state tick
            val result = fsm.tick(deltaSec, npc, pathfinder, placedBuildings)

            // 3. Enforce Zone & Biome Boundaries via NpcStateManager
            val bedX = placedBuildings.find { it.id == (npc.assignedBedId ?: -1) }?.posX
            val bedZ = placedBuildings.find { it.id == (npc.assignedBedId ?: -1) }?.posZ
            val clamped = stateManager.enforceZoneBoundaries(result.posX, result.posZ, bedX, bedZ, currentBiome)
            val clampedX = clamped.x
            val clampedZ = clamped.z

            // 4. Update Needs (Food, Water, Morale)
            var hunger = npc.hunger + 0.15f * deltaSec
            var thirst = npc.thirst + 0.20f * deltaSec
            var morale = npc.morale

            // Consume pantry food/water during mealtimes
            val currentActivity = fsm.currentState.activity
            if (currentActivity == NpcActivity.WAKING || currentActivity == NpcActivity.LUNCH || currentActivity == NpcActivity.DINNER) {
                if (hunger > 30.0f && Random.nextFloat() < 0.10f * deltaSec) {
                    hunger = (hunger - 40.0f).coerceAtLeast(0.0f)
                    totalFoodConsumed++
                    morale = (morale + 2.0f).coerceAtMost(100.0f)
                }
                if (thirst > 30.0f && Random.nextFloat() < 0.10f * deltaSec) {
                    thirst = (thirst - 40.0f).coerceAtLeast(0.0f)
                    totalWaterConsumed++
                    morale = (morale + 2.0f).coerceAtMost(100.0f)
                }
            }

            // Job-specific bonuses
            if (currentActivity == NpcActivity.WORKING) {
                when (npc.role) {
                    NpcRole.RESEARCHER -> {
                        if (Random.nextFloat() < 0.05f * deltaSec) {
                            researchPointsGenerated += 1
                        }
                    }
                    NpcRole.MEDIC -> {
                        if (Random.nextFloat() < 0.10f * deltaSec) {
                            healedPlayerHp += 1.0f
                        }
                    }
                    else -> {}
                }
            }

            // Check transition quote override
            val transitionQuote = pendingSpeechQuotes.remove(npc.id)

            // Speech bubble timer decay
            var speech = transitionQuote ?: result.speechBubble ?: npc.speechBubble
            var speechTimer = if (transitionQuote != null || result.speechBubble != null) 3.5f else (npc.speechBubbleTimer - deltaSec).coerceAtLeast(0.0f)
            if (speechTimer <= 0f) speech = null

            val updatedNpc = npc.copy(
                morale = morale,
                hunger = hunger,
                thirst = thirst,
                currentActivity = currentActivity,
                posX = clampedX,
                posZ = clampedZ,
                rotationDeg = result.rotDeg,
                isWalking = result.isWalking,
                speechBubble = speech,
                speechBubbleTimer = speechTimer,
                tasksCompleted = npc.tasksCompleted + result.taskCompletedDelta
            )

            updatedNpcs.add(updatedNpc)
        }

        return ControllerUpdateResult(
            updatedNpcs = updatedNpcs,
            foodConsumed = totalFoodConsumed,
            waterConsumed = totalWaterConsumed,
            researchPointsEarned = researchPointsGenerated,
            healedPlayerHp = healedPlayerHp
        )
    }

    /**
     * Resets or clears state machine for an NPC if removed/dismissed.
     */
    fun removeNpc(npcId: Int) {
        stateManager.removeNpc(npcId)
    }

    data class ControllerUpdateResult(
        val updatedNpcs: List<NpcEntity>,
        val foodConsumed: Int,
        val waterConsumed: Int,
        val researchPointsEarned: Int,
        val healedPlayerHp: Float
    )
}
