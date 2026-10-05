package com.example.game3d.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural Audio Synthesizer for Solarpunk Farm effects without external asset files.
 */
class SpatialLivestockAudioSystem(private val scope: CoroutineScope) {

    private val sampleRate = 22050

    fun playSelectToolSound() {
        playTone(durationMs = 100, frequencies = floatArrayOf(587.33f, 783.99f), volume = 0.4f)
    }

    fun playWaterSound() {
        playTone(
            durationMs = 250,
            frequencies = floatArrayOf(440f, 660f, 880f, 520f),
            volume = 0.4f,
            noiseMod = true
        )
    }

    fun playHarvestSparkleSound() {
        scope.launch(Dispatchers.Default) {
            val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f)
            for (freq in notes) {
                generateBeep(freq, 60, 0.5f)
            }
        }
    }

    fun playPlantSeedSound() {
        playTone(durationMs = 120, frequencies = floatArrayOf(300f, 400f, 250f), volume = 0.45f)
    }

    fun playSolarHumSound() {
        playTone(durationMs = 300, frequencies = floatArrayOf(220f, 440f, 880f), volume = 0.35f)
    }

    fun playPetPurrSound() {
        playTone(durationMs = 350, frequencies = floatArrayOf(150f, 180f, 160f), volume = 0.4f, noiseMod = true)
    }

    fun playSheepBaa() {
        playTone(durationMs = 400, frequencies = floatArrayOf(280f, 260f, 290f, 270f), volume = 0.5f)
    }

    fun playCowMoo() {
        playTone(durationMs = 600, frequencies = floatArrayOf(140f, 130f, 125f, 120f), volume = 0.6f)
    }

    fun playCraftSuccess() {
        scope.launch(Dispatchers.Default) {
            val notes = floatArrayOf(440f, 554.37f, 659.25f, 880f)
            for (freq in notes) {
                generateBeep(freq, 80, 0.45f)
            }
        }
    }

    fun playLowEnergyWarning() {
        scope.launch(Dispatchers.Default) {
            generateBeep(330f, 150, 0.5f)
            generateBeep(220f, 250, 0.5f)
        }
    }

    fun playItemCollect() {
        scope.launch(Dispatchers.Default) {
            generateBeep(587.33f, 60, 0.4f)
            generateBeep(880.00f, 80, 0.45f)
        }
    }

    fun playWatering() {
        playWaterSound()
    }

    fun playCoinEarned() {
        scope.launch(Dispatchers.Default) {
            generateBeep(987.77f, 70, 0.4f)
            generateBeep(1318.51f, 120, 0.4f)
        }
    }

    private fun playTone(
        durationMs: Int,
        frequencies: FloatArray,
        volume: Float,
        noiseMod: Boolean = false
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                if (numSamples <= 0) return@launch
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    var sampleVal = 0.0
                    for (freq in frequencies) {
                        sampleVal += sin(2.0 * Math.PI * freq * time)
                    }
                    sampleVal /= frequencies.size
                    if (noiseMod) {
                        sampleVal = sampleVal * 0.7 + (Math.random() * 2.0 - 1.0) * 0.3
                    }
                    val envelope = sin(Math.PI * i / numSamples)
                    buffer[i] = (sampleVal * envelope * volume * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
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

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
            } catch (_: Exception) {
                // Ignore audio failures on headless environments
            }
        }
    }

    private fun generateBeep(freq: Float, durationMs: Int, volume: Float) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val sampleVal = sin(2.0 * Math.PI * freq * time)
                val envelope = sin(Math.PI * i / numSamples)
                buffer[i] = (sampleVal * envelope * volume * Short.MAX_VALUE).toInt().toShort()
            }
            val audioTrack = AudioTrack.Builder()
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

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong())
        } catch (_: Exception) {
        }
    }
}
