package com.example.game3d.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin
import kotlin.random.Random

/**
 * Standalone Ambient Audio Manager that proceduraly synthesizes spatialized background sounds.
 * Focuses on time-of-day specific ambiance (Birds at dawn, Crickets at night).
 * Spatialization is handled via stereo panning and distance-based volume modulation.
 */
class AmbientAudioManager(private val scope: CoroutineScope) {
    private val sampleRate = 22050
    private var masterVolume = 1.0f
    private var ambientVolume = 0.6f

    private var ambianceJob: Job? = null
    private var currentHour = 12.0f
    
    // Stereo AudioTrack for continuous rustle/wind
    private var windTrack: AudioTrack? = null
    private var isRunning = false

    init {
        startAmbianceEngine()
    }

    fun setVolumes(master: Float, ambient: Float) {
        masterVolume = master
        ambientVolume = ambient
    }

    fun updateTime(hour: Float) {
        currentHour = hour
    }

    private fun startAmbianceEngine() {
        ambianceJob?.cancel()
        ambianceJob = scope.launch(Dispatchers.Default) {
            isRunning = true
            
            // Start the continuous low-level wind/rustle track in background
            launch { runWindRustleTrack() }

            while (isActive && isRunning) {
                val hour = currentHour
                
                // Logic for bird/cricket triggers based on time
                // Dawn: 5-7, Day: 7-18, Dusk: 18-20, Night: 20-5
                when {
                    hour in 5.0f..7.0f -> { // Dawn - Peak Birds
                        if (Random.nextFloat() < 0.4f) {
                            playProceduralBird(panning = Random.nextFloat() * 2f - 1f)
                        }
                    }
                    hour in 7.0f..18.0f -> { // Day - Active Birds
                        if (Random.nextFloat() < 0.15f) {
                            playProceduralBird(panning = Random.nextFloat() * 2f - 1f)
                        }
                    }
                    hour in 18.0f..20.0f -> { // Dusk - Mixed
                        if (Random.nextFloat() < 0.1f) {
                            playProceduralBird(panning = Random.nextFloat() * 2f - 1f)
                        }
                        if (Random.nextFloat() < 0.2f) {
                            playProceduralCricket(panning = Random.nextFloat() * 2f - 1f)
                        }
                    }
                    else -> { // Night - Peak Crickets
                        if (Random.nextFloat() < 0.5f) {
                            playProceduralCricket(panning = Random.nextFloat() * 2f - 1f)
                        }
                    }
                }

                delay(800 + Random.nextLong(1200))
            }
        }
    }

    /**
     * Procedural bird chirp: 2-3 quick frequency sweeps.
     */
    private fun playProceduralBird(panning: Float) {
        val baseFreq = 1800f + Random.nextFloat() * 800f
        val durationMs = 150
        val vol = 0.2f * ambientVolume * masterVolume
        
        scope.launch(Dispatchers.Default) {
            generateStereoTone(
                freqStart = baseFreq,
                freqEnd = baseFreq + 400f,
                durationMs = durationMs,
                volume = vol,
                panning = panning
            )
            if (Random.nextBoolean()) {
                delay(100)
                generateStereoTone(
                    freqStart = baseFreq + 200f,
                    freqEnd = baseFreq + 600f,
                    durationMs = 120,
                    volume = vol * 0.8f,
                    panning = panning
                )
            }
        }
    }

    /**
     * Procedural cricket chirp: rhythmic pulses of high frequency.
     */
    private fun playProceduralCricket(panning: Float) {
        val freq = 4500f + Random.nextFloat() * 500f
        val vol = 0.15f * ambientVolume * masterVolume
        
        scope.launch(Dispatchers.Default) {
            repeat(3) {
                generateStereoTone(freq, freq, 40, vol, panning)
                delay(60)
            }
        }
    }

    /**
     * Continuous background "air" or "wind" texture using filtered noise logic.
     */
    private suspend fun runWindRustleTrack() {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        windTrack = track
        track.play()

        val buffer = ShortArray(bufferSize)
        var lastSample = 0f
        
        while (isRunning && currentCoroutineContext().isActive) {
            val vol = 0.05f * ambientVolume * masterVolume
            for (i in 0 until buffer.size step 2) {
                // Simple low-pass filtered noise for "wind"
                val white = (Random.nextFloat() * 2f - 1f)
                lastSample = lastSample * 0.95f + white * 0.05f
                
                val sample = (lastSample * vol * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                
                // Slow panning oscillation for the wind
                val pan = sin(System.currentTimeMillis() / 5000.0).toFloat()
                val left = (sample * (1f - pan).coerceIn(0f, 1f)).toInt().toShort()
                val right = (sample * (1f + pan).coerceIn(0f, 1f)).toInt().toShort()
                
                buffer[i] = left
                buffer[i + 1] = right
            }
            track.write(buffer, 0, buffer.size)
            yield()
        }
        track.stop()
        track.release()
    }

    /**
     * Low-level helper to generate a short stereo tone with optional frequency sweep.
     */
    private fun generateStereoTone(
        freqStart: Float,
        freqEnd: Float,
        durationMs: Int,
        volume: Float,
        panning: Float // -1.0 (Left) to 1.0 (Right)
    ) {
        if (volume <= 0.001f) return
        try {
            val numSamples = (sampleRate * durationMs / 1000) * 2 // Stereo
            val buffer = ShortArray(numSamples)
            
            val leftVol = (1.0f - panning).coerceIn(0f, 1f)
            val rightVol = (1.0f + panning).coerceIn(0f, 1f)

            var phase = 0.0
            for (i in 0 until numSamples step 2) {
                val progress = (i / 2).toFloat() / (numSamples / 2).toFloat()
                val currentFreq = freqStart + (freqEnd - freqStart) * progress
                val twoPiF = 2.0 * Math.PI * currentFreq / sampleRate
                
                val envelope = if (progress < 0.1f) progress / 0.1f else (1.0f - progress).coerceIn(0f, 1f)
                val sampleValue = sin(phase) * envelope * volume * Short.MAX_VALUE
                phase += twoPiF

                buffer[i] = (sampleValue * leftVol).toInt().toShort()
                buffer[i + 1] = (sampleValue * rightVol).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build())
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            scope.launch {
                delay(durationMs + 100L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    fun release() {
        isRunning = false
        ambianceJob?.cancel()
        try {
            windTrack?.stop()
            windTrack?.release()
        } catch (_: Exception) {}
    }
}
