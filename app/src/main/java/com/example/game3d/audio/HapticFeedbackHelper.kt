package com.example.game3d.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Supported tactile feedback interaction types.
 */
enum class HapticFeedbackType {
    BUTTON_TAP,
    CROP_HARVEST,
    BUILDING_PLACEMENT,
    PLANT_SEED,
    WATER_CROP,
    SUCCESS_CONFIRMATION,
    ERROR_ALERT,
    FAINT_WARNING
}

/**
 * Unified Haptic Feedback Service providing crisp, distinct tactile vibrations
 * for core gameplay interactions (crop harvesting, building placement, UI clicks, etc.).
 */
interface IHapticFeedbackService {
    var vibrationEnabled: Boolean
    fun trigger(type: HapticFeedbackType)
    fun vibrateButtonTap()
    fun vibrateHarvest()
    fun vibrateBuildPlacement()
    fun vibratePlantSeed()
    fun vibrateWaterCrop()
    fun vibrateSuccess()
    fun vibrateError()
    fun vibrateFaint()
}

/**
 * Robust implementation of IHapticFeedbackService leveraging modern VibrationEffect
 * predefined effects (API 29+ / API 31+ VibratorManager) with custom amplitude waveforms
 * and legacy fallbacks.
 */
class HapticFeedbackHelper(private val context: Context) : IHapticFeedbackService {

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        Log.w("HapticService", "Unable to acquire system vibrator service", e)
        null
    }

    override var vibrationEnabled: Boolean = true

    override fun trigger(type: HapticFeedbackType) {
        if (!vibrationEnabled) return
        when (type) {
            HapticFeedbackType.BUTTON_TAP -> vibrateButtonTap()
            HapticFeedbackType.CROP_HARVEST -> vibrateHarvest()
            HapticFeedbackType.BUILDING_PLACEMENT -> vibrateBuildPlacement()
            HapticFeedbackType.PLANT_SEED -> vibratePlantSeed()
            HapticFeedbackType.WATER_CROP -> vibrateWaterCrop()
            HapticFeedbackType.SUCCESS_CONFIRMATION -> vibrateSuccess()
            HapticFeedbackType.ERROR_ALERT -> vibrateError()
            HapticFeedbackType.FAINT_WARNING -> vibrateFaint()
        }
    }

    /**
     * Short, crisp tactile click for UI button taps (10ms).
     */
    override fun vibrateButtonTap() {
        if (!isVibrationAvailable()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                return
            } catch (_: Exception) {}
        }
        playWaveform(longArrayOf(0, 10), intArrayOf(0, 130))
    }

    /**
     * Satisfying pop-pulse on harvesting crops (16ms, medium-high amplitude).
     */
    override fun vibrateHarvest() {
        if (!isVibrationAvailable()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                return
            } catch (_: Exception) {}
        }
        playWaveform(longArrayOf(0, 16), intArrayOf(0, 220))
    }

    /**
     * Solid, grounded thud when a building/structure is placed (24ms, high amplitude).
     */
    override fun vibrateBuildPlacement() {
        if (!isVibrationAvailable()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                return
            } catch (_: Exception) {}
        }
        playWaveform(longArrayOf(0, 24), intArrayOf(0, 255))
    }

    /**
     * Soft earthy pulse when sowing seeds (12ms).
     */
    override fun vibratePlantSeed() {
        if (!isVibrationAvailable()) return
        playWaveform(longArrayOf(0, 12), intArrayOf(0, 150))
    }

    /**
     * Smooth dual micro-pulse for watering crops.
     */
    override fun vibrateWaterCrop() {
        if (!isVibrationAvailable()) return
        playWaveform(longArrayOf(0, 8, 20, 10), intArrayOf(0, 140, 0, 180))
    }

    /**
     * Uplifting two-tier confirmation pulse for quests, saves, and contracts.
     */
    override fun vibrateSuccess() {
        if (!isVibrationAvailable()) return
        playWaveform(longArrayOf(0, 12, 22, 18), intArrayOf(0, 150, 0, 240))
    }

    /**
     * Distinct double-pulse buzz for errors and invalid placements.
     */
    override fun vibrateError() {
        if (!isVibrationAvailable()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                return
            } catch (_: Exception) {}
        }
        playWaveform(longArrayOf(0, 25, 35, 25), intArrayOf(0, 220, 0, 220))
    }

    /**
     * Heavy emergency shudder on player fainting / stamina depletion.
     */
    override fun vibrateFaint() {
        if (!isVibrationAvailable()) return
        playWaveform(longArrayOf(0, 45, 35, 65), intArrayOf(0, 255, 0, 255))
    }

    private fun isVibrationAvailable(): Boolean {
        return vibrationEnabled && vibrator != null && vibrator.hasVibrator()
    }

    private fun playWaveform(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {
            // Graceful fallback for restricted devices/emulators
        }
    }
}
