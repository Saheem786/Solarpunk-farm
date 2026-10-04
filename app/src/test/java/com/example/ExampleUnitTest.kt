package com.example

import com.example.data.local.EnergyNodeEntity
import com.example.data.local.LivestockEntity
import com.example.data.local.PlotEntity
import com.example.data.model.CropStage
import com.example.data.model.CropType
import com.example.data.model.EnergyNodeType
import com.example.data.model.LivestockType
import com.example.data.model.PlotType
import com.example.data.model.WeatherType
import com.example.game3d.interaction.InteractionSystem
import com.example.game3d.interaction.InteractionTargetType
import com.example.game3d.player.PlayerInputState
import com.example.game3d.player.ThirdPersonPlayer
import com.example.game3d.renderer.DayNightLightingSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun test_DayNightLightingSystem_24HourCycle() {
        val midnight = DayNightLightingSystem.calculateLighting(0.0f, WeatherType.SUNNY_CLEAR)
        assertTrue("Midnight should be marked as night", midnight.isNight)
        assertTrue("Midnight ambient intensity should be low", midnight.ambientIntensity < 0.4f)

        val noon = DayNightLightingSystem.calculateLighting(12.0f, WeatherType.SUNNY_CLEAR)
        assertFalse("Noon should not be night", noon.isNight)
        assertTrue("Noon direct light intensity should be high", noon.directLightIntensity >= 0.9f)

        val sunset = DayNightLightingSystem.calculateLighting(18.0f, WeatherType.SUNNY_CLEAR)
        assertFalse("Sunset at 18:00 is daytime", sunset.isNight)
    }

    @Test
    fun test_DayNightLightingSystem_WeatherModulation() {
        val clear = DayNightLightingSystem.calculateLighting(12.0f, WeatherType.SUNNY_CLEAR)
        val rainy = DayNightLightingSystem.calculateLighting(12.0f, WeatherType.RAINY_STORM)

        assertTrue(
            "Rainy weather should reduce direct sunlight intensity",
            rainy.directLightIntensity < clear.directLightIntensity
        )
    }

    @Test
    fun test_InteractionSystem_ProximityDetection() {
        val plots = listOf(
            PlotEntity(
                id = 0,
                plotType = PlotType.PERMACULTURE_BED,
                cropType = CropType.SOLAR_SUNFLOWER,
                stage = CropStage.HARVEST_READY,
                progress = 1.0f,
                moisture = 0.8f,
                posX = 2.0f,
                posZ = 2.0f
            )
        )
        val energyNodes = emptyList<EnergyNodeEntity>()
        val livestock = emptyList<LivestockEntity>()

        val promptNear = InteractionSystem.findNearestInteraction(
            playerX = 2.5f,
            playerZ = 2.5f,
            plots = plots,
            energyNodes = energyNodes,
            animals = livestock
        )

        assertNotNull("Prompt should be detected within proximity", promptNear)
        assertEquals(InteractionTargetType.PLOT, promptNear?.targetType)
        assertTrue(promptNear?.title?.contains("Harvest") == true)
    }

    @Test
    fun test_ThirdPersonPlayer_MovementAndClamping() {
        val player = ThirdPersonPlayer(0f, 0f, 0f)
        val input = PlayerInputState(moveX = 1.0f, moveZ = 0.0f, isSprinting = true)

        player.update(input, cameraYawDeg = 0f, deltaSec = 0.1f)
        assertTrue("Player should be moving", player.isMoving)
        assertTrue("Player speed should be non-zero", player.currentSpeed > 0f)
        assertTrue("Player X coordinate should have moved", player.posX > 0f)
    }
}
