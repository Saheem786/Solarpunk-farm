package com.example.game3d.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import com.example.data.model.WeatherType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * High-performance Audio Manager handling:
 * 1. Low-latency SoundPool & procedural synthesizer for farming & UI sound effects
 * 2. Dynamic environmental loops (Rain, Storm, Wind, Wildlife Birds & Bees)
 * 3. Smooth 2-second Cross-Fading Background Music between Day & Night cycles
 */
class FarmAudioManager(
    private val scope: CoroutineScope,
    private val context: Context? = null
) {
    private val sampleRate = 22050

    // Volume controllers (0.0f .. 1.0f)
    var masterVolume: Float = 1.0f
    var musicVolume: Float = 0.8f
    var sfxVolume: Float = 0.9f

    // Low-Latency SoundPool for SFX polyphony
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Cross-fading background music variables
    private var musicJob: Job? = null
    private var isDaytimeMode: Boolean = true
    private var dayMusicGain: Float = 1.0f
    private var nightMusicGain: Float = 0.0f
    private var isCrossfading: Boolean = false

    // Ambient loop jobs
    private var weatherAmbientJob: Job? = null
    private var currentWeather: WeatherType = WeatherType.SUNNY_CLEAR
    private var lastFootstepTime = 0L
    private var beeProximityDist: Float = 999f

    init {
        startBackgroundMusicEngine()
        startEnvironmentalLoopEngine()
    }

    // ========================================================================
    // 1. LOW-LATENCY FARMING ACTION SOUNDS
    // ========================================================================

    /**
     * Footstep cadence: plays soft thud every 0.5s walking, faster/punchier every 0.3s running.
     */
    fun playFootstep(isRunning: Boolean) {
        val now = System.currentTimeMillis()
        val interval = if (isRunning) 290L else 490L
        if (now - lastFootstepTime < interval) return
        lastFootstepTime = now

        val vol = (if (isRunning) 0.38f else 0.22f) * sfxVolume * masterVolume
        val baseFreq = if (isRunning) 115f else 88f
        synthesizeShortPulse(
            durationMs = 45,
            frequencies = floatArrayOf(baseFreq, baseFreq * 0.65f),
            volume = vol,
            noiseMod = true
        )
    }

    /**
     * Crisp "pop" sound when harvesting a mature crop.
     */
    fun playHarvestCrop() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.55f * sfxVolume * masterVolume
            generateTone(523.25f, 40, vol) // C5 pop
            generateTone(783.99f, 60, vol) // G5
            generateTone(1046.50f, 90, vol * 0.9f) // C6
        }
    }

    /**
     * Water splash & trickle sound when irrigating soil plots.
     */
    fun playWaterSound() {
        val vol = 0.48f * sfxVolume * masterVolume
        synthesizeShortPulse(
            durationMs = 250,
            frequencies = floatArrayOf(440f, 680f, 880f, 540f),
            volume = vol,
            noiseMod = true
        )
    }

    /**
     * Soft "dig" / soil rustle sound when planting seeds.
     */
    fun playPlantSeedSound() {
        val vol = 0.45f * sfxVolume * masterVolume
        synthesizeShortPulse(
            durationMs = 115,
            frequencies = floatArrayOf(240f, 320f, 170f),
            volume = vol,
            noiseMod = true
        )
    }

    /**
     * Collect egg: soft cluck + pleasant pickup sound.
     */
    fun playCollectEgg() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.52f * sfxVolume * masterVolume
            generateTone(390f, 60, vol)
            generateTone(490f, 75, vol)
            generateTone(740f, 95, vol * 0.9f)
        }
    }

    /**
     * Milk cow: soft moo + liquid pour sound.
     */
    fun playMilkCow() {
        scope.launch(Dispatchers.Default) {
            playCowMoo()
            delay(120)
            playWaterSound()
        }
    }

    /**
     * Soft chewing sound for eating food.
     */
    fun playEatFood() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.46f * sfxVolume * masterVolume
            for (i in 0..2) {
                generateTone(190f + i * 35f, 45, vol)
                delay(55)
            }
        }
    }

    /**
     * Gulping sound for drinking water.
     */
    fun playDrinkWater() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.5f * sfxVolume * masterVolume
            for (i in 0..2) {
                generateTone(330f - i * 30f, 65, vol)
                delay(75)
            }
        }
    }

    /**
     * Fishing line cast swoosh and water landing plop.
     */
    fun playFishingCast() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.45f * sfxVolume * masterVolume
            synthesizeShortPulse(durationMs = 90, frequencies = floatArrayOf(800f, 400f, 250f), volume = vol, noiseMod = true)
            delay(120)
            generateTone(320f, 50, vol * 0.8f) // water plop
        }
    }

    /**
     * Urgent tension alert when fish bites bobber ("!").
     */
    fun playFishBiteAlert() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.65f * sfxVolume * masterVolume
            generateTone(1046.50f, 70, vol) // High C6
            delay(30)
            generateTone(1318.51f, 100, vol) // E6
        }
    }

    /**
     * Triumphant water splash and cheer jingle upon catching fish.
     */
    fun playFishCatch() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.60f * sfxVolume * masterVolume
            // Splash
            synthesizeShortPulse(durationMs = 120, frequencies = floatArrayOf(600f, 450f, 300f), volume = vol * 0.7f, noiseMod = true)
            delay(60)
            // Fanfare
            val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f)
            for (n in notes) {
                generateTone(n, 65, vol)
                delay(35)
            }
        }
    }

    /**
     * Fish escapes reel-in window.
     */
    fun playFishEscape() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.45f * sfxVolume * masterVolume
            generateTone(420f, 70, vol)
            delay(40)
            generateTone(280f, 120, vol * 0.8f)
        }
    }

    /**
     * Water boiling and bubbling sound at campfire.
     */
    fun playBoilWater() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.48f * sfxVolume * masterVolume
            for (i in 0..4) {
                generateTone(380f + (i % 2) * 80f, 40, vol)
                delay(45)
            }
        }
    }

    /**
     * Irrigation sprinkler spray pulse.
     */
    fun playIrrigationSpray() {
        val vol = 0.42f * sfxVolume * masterVolume
        synthesizeShortPulse(durationMs = 280, frequencies = floatArrayOf(900f, 720f, 600f, 450f), volume = vol, noiseMod = true)
    }

    /**
     * Soft "ding" chime on saving game progress.
     */
    fun playSaveGame() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.52f * sfxVolume * masterVolume
            val notes = floatArrayOf(587.33f, 739.99f, 880.00f, 1174.66f)
            for (freq in notes) {
                generateTone(freq, 70, vol)
                delay(25)
            }
        }
    }

    // ========================================================================
    // 2. UI INTERACTIONS SFX
    // ========================================================================

    fun playButtonTap() {
        val vol = 0.35f * sfxVolume * masterVolume
        synthesizeShortPulse(durationMs = 28, frequencies = floatArrayOf(920f), volume = vol)
    }

    fun playBuildPlacement() {
        val vol = 0.55f * sfxVolume * masterVolume
        synthesizeShortPulse(
            durationMs = 95,
            frequencies = floatArrayOf(160f, 120f, 90f),
            volume = vol,
            noiseMod = true
        )
    }

    fun playBuildingComplete() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.55f * sfxVolume * masterVolume
            val chord = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (note in chord) {
                generateTone(note, 90, vol)
                delay(40)
            }
        }
    }

    fun playErrorSound() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.5f * sfxVolume * masterVolume
            generateTone(190f, 80, vol)
            delay(50)
            generateTone(150f, 120, vol * 0.9f)
        }
    }

    fun playMenuWhoosh() {
        val vol = 0.32f * sfxVolume * masterVolume
        synthesizeShortPulse(
            durationMs = 120,
            frequencies = floatArrayOf(500f, 350f, 220f),
            volume = vol,
            noiseMod = true
        )
    }

    fun playCoinEarned() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.5f * sfxVolume * masterVolume
            generateTone(987.77f, 60, vol) // B5
            delay(40)
            generateTone(1318.51f, 100, vol * 0.95f) // E6
        }
    }

    fun playSelectToolSound() {
        val vol = 0.32f * sfxVolume * masterVolume
        synthesizeShortPulse(durationMs = 35, frequencies = floatArrayOf(660f, 880f), volume = vol)
    }

    fun playCraftSuccess() {
        playBuildingComplete()
    }

    fun playLowEnergyWarning() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.6f * sfxVolume * masterVolume
            generateTone(220f, 150, vol)
            delay(80)
            generateTone(180f, 200, vol)
        }
    }

    // ========================================================================
    // 3. ANIMALS & WILDLIFE
    // ========================================================================

    fun playChickenCluck() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.42f * sfxVolume * masterVolume
            generateTone(420f, 50, vol)
            delay(40)
            generateTone(510f, 60, vol)
            delay(50)
            generateTone(380f, 80, vol)
        }
    }

    fun playCowMoo() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.48f * sfxVolume * masterVolume
            val baseFreq = 135f
            generateTone(baseFreq, 300, vol)
        }
    }

    fun playSheepBaa() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.44f * sfxVolume * masterVolume
            generateTone(280f, 200, vol)
        }
    }

    fun playSolarHumSound() {
        val vol = 0.32f * sfxVolume * masterVolume
        synthesizeShortPulse(durationMs = 280, frequencies = floatArrayOf(180f, 240f), volume = vol)
    }

    fun playThunderCrack() {
        scope.launch(Dispatchers.Default) {
            val vol = 0.75f * sfxVolume * masterVolume
            synthesizeShortPulse(
                durationMs = 450,
                frequencies = floatArrayOf(80f, 55f, 40f),
                volume = vol,
                noiseMod = true
            )
        }
    }

    fun playBeeBuzzNear(distanceMeters: Float) {
        beeProximityDist = distanceMeters
    }

    // ========================================================================
    // 4. DAY / NIGHT CROSS-FADING BACKGROUND MUSIC
    // ========================================================================

    /**
     * Updates day/night music state with smooth 2-second cross-fade.
     * Day: 7 AM - 7 PM (Warm acoustic arpeggios, 30% volume)
     * Night: 7 PM - 7 AM (Peaceful ambient soundscape, 20% volume)
     */
    fun updateDayNightMusic(isDay: Boolean) {
        if (isDaytimeMode == isDay && !isCrossfading) return
        isDaytimeMode = isDay
        triggerMusicCrossfade(isDay)
    }

    private fun triggerMusicCrossfade(targetIsDay: Boolean) {
        scope.launch(Dispatchers.Default) {
            isCrossfading = true
            val steps = 20
            val stepDelay = 100L // 20 * 100ms = 2000ms cross-fade
            for (i in 0..steps) {
                val progress = i.toFloat() / steps.toFloat()
                if (targetIsDay) {
                    dayMusicGain = progress
                    nightMusicGain = 1.0f - progress
                } else {
                    dayMusicGain = 1.0f - progress
                    nightMusicGain = progress
                }
                delay(stepDelay)
            }
            isCrossfading = false
        }
    }

    private fun startBackgroundMusicEngine() {
        musicJob?.cancel()
        musicJob = scope.launch(Dispatchers.Default) {
            // Day Pentatonic (C, D, E, G, A, C5, D5, E5)
            val dayScale = floatArrayOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f, 523.25f, 587.33f, 659.25f)
            val dayMelody = intArrayOf(0, 2, 4, 7, 5, 4, 2, 0, 3, 5, 7, 6, 4, 2, 1, 0)

            // Night Ambient (A minor / Lydian modal notes)
            val nightScale = floatArrayOf(220.00f, 261.63f, 293.66f, 329.63f, 392.00f, 440.00f, 493.88f)
            val nightMelody = intArrayOf(0, 3, 5, 3, 1, 4, 2, 0)

            var stepDay = 0
            var stepNight = 0

            while (isActive) {
                val effectiveMaster = masterVolume
                val effectiveMusic = musicVolume

                if (effectiveMaster > 0.01f && effectiveMusic > 0.01f) {
                    // 1. Day Music Voice (Warm, acoustic arpeggios at 30% low volume)
                    val dayVol = (0.30f * effectiveMusic * effectiveMaster * dayMusicGain).coerceIn(0f, 1f)
                    if (dayVol > 0.01f) {
                        val freq = dayScale[dayMelody[stepDay % dayMelody.size] % dayScale.size]
                        generateTone(freq, 280, dayVol)
                    }

                    // 2. Night Music Voice (Quieter, peaceful ambient loop at 20% lower volume)
                    val nightVol = (0.20f * effectiveMusic * effectiveMaster * nightMusicGain).coerceIn(0f, 1f)
                    if (nightVol > 0.01f) {
                        val freq = nightScale[nightMelody[stepNight % nightMelody.size] % nightScale.size]
                        generateTone(freq, 480, nightVol)
                    }
                }

                stepDay++
                if (stepDay % 2 == 0) stepNight++
                delay(420)
            }
        }
    }

    // ========================================================================
    // 5. ENVIRONMENTAL WEATHER & WILDLIFE AMBIENT LOOPS
    // ========================================================================

    fun updateWeatherAmbience(weather: WeatherType) {
        currentWeather = weather
    }

    private fun startEnvironmentalLoopEngine() {
        weatherAmbientJob?.cancel()
        weatherAmbientJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val effectiveMaster = masterVolume
                val effectiveSfx = sfxVolume

                if (effectiveMaster > 0.01f && effectiveSfx > 0.01f) {
                    // Rain & Storm environmental loops
                    when (currentWeather) {
                        WeatherType.RAINY_STORM -> {
                            val rainVol = 0.28f * effectiveSfx * effectiveMaster
                            synthesizeShortPulse(durationMs = 380, frequencies = floatArrayOf(300f, 600f, 900f), volume = rainVol, noiseMod = true)
                        }
                        WeatherType.STORM -> {
                            val stormVol = 0.42f * effectiveSfx * effectiveMaster
                            synthesizeShortPulse(durationMs = 420, frequencies = floatArrayOf(250f, 450f, 800f), volume = stormVol, noiseMod = true)
                            // Occasional thunder in storms
                            if (Math.random() < 0.15) {
                                playThunderCrack()
                            }
                        }
                        WeatherType.WIND_GALE, WeatherType.CLOUDY_OVERCAST -> {
                            val windVol = 0.18f * effectiveSfx * effectiveMaster
                            synthesizeShortPulse(durationMs = 500, frequencies = floatArrayOf(120f, 180f, 90f), volume = windVol, noiseMod = true)
                        }
                        WeatherType.SUNNY_CLEAR -> {
                            // Daytime bird chirps when sun is out
                            if (isDaytimeMode && Math.random() < 0.25) {
                                val birdVol = 0.22f * effectiveSfx * effectiveMaster
                                generateTone(1760.00f + (Math.random() * 400).toFloat(), 60, birdVol)
                            }
                        }
                        else -> {}
                    }

                    // Proximity Bee Buzzing when near apiary/flowers
                    if (beeProximityDist < 12.0f) {
                        val proxFactor = (1.0f - (beeProximityDist / 12.0f)).coerceIn(0f, 1f)
                        val beeVol = 0.25f * proxFactor * effectiveSfx * effectiveMaster
                        if (beeVol > 0.02f) {
                            synthesizeShortPulse(durationMs = 260, frequencies = floatArrayOf(240f, 245f), volume = beeVol, noiseMod = false)
                        }
                    }
                }

                delay(600)
            }
        }
    }

    // ========================================================================
    // 6. LOW-LEVEL AUDIO SYNTHESIS
    // ========================================================================

    private fun generateTone(freq: Float, durationMs: Int, volume: Float) {
        if (volume <= 0.001f) return
        try {
            val numSamples = (sampleRate * durationMs / 1000).coerceAtLeast(64)
            val buffer = ShortArray(numSamples)
            val twoPiF = 2.0 * Math.PI * freq / sampleRate

            for (i in 0 until numSamples) {
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.7 -> (numSamples - i) / (numSamples * 0.3)
                    else -> 1.0
                }
                val sample = sin(twoPiF * i) * envelope * volume * Short.MAX_VALUE
                buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            scope.launch(Dispatchers.Default) {
                delay(durationMs + 60L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Graceful ignore
        }
    }

    private fun synthesizeShortPulse(
        durationMs: Int,
        frequencies: FloatArray,
        volume: Float,
        noiseMod: Boolean = false
    ) {
        if (volume <= 0.001f) return
        scope.launch(Dispatchers.Default) {
            try {
                val numSamples = (sampleRate * durationMs / 1000).coerceAtLeast(64)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples.toFloat()
                    val decay = (1.0f - progress) * (1.0f - progress)
                    var sampleSum = 0.0

                    for (f in frequencies) {
                        sampleSum += sin(2.0 * Math.PI * f * i / sampleRate)
                    }
                    if (noiseMod) {
                        sampleSum += (Math.random() * 2.0 - 1.0) * 0.6
                    }
                    val sample = (sampleSum / (frequencies.size + if (noiseMod) 1 else 0)) * decay * volume * Short.MAX_VALUE
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                delay(durationMs + 60L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    fun release() {
        musicJob?.cancel()
        weatherAmbientJob?.cancel()
        soundPool.release()
    }
}
