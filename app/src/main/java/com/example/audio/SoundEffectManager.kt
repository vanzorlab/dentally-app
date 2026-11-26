package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Ultra-lightweight programmatic SFX synthesizer (0 KB audio assets).
 * Keeps APK well below 50 MB while delivering crisp, playful interactive audio feedback.
 */
object SoundEffectManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isEnabled: Boolean = true

    fun playPop() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 480.0, freqEnd = 740.0, durationMs = 65, volume = 0.28f)
            )
        )
    }

    fun playUncheck() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 420.0, freqEnd = 280.0, durationMs = 60, volume = 0.22f)
            )
        )
    }

    fun playWarningBlip() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 340.0, freqEnd = 300.0, durationMs = 80, volume = 0.24f),
                ToneNote(freqStart = 280.0, freqEnd = 240.0, durationMs = 100, volume = 0.24f)
            )
        )
    }

    fun playSuccessChime() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 523.25, freqEnd = 523.25, durationMs = 80, volume = 0.28f), // C5
                ToneNote(freqStart = 659.25, freqEnd = 659.25, durationMs = 80, volume = 0.28f), // E5
                ToneNote(freqStart = 783.99, freqEnd = 783.99, durationMs = 140, volume = 0.30f) // G5
            )
        )
    }

    fun playQuadrantChime() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 659.25, freqEnd = 659.25, durationMs = 90, volume = 0.28f),
                ToneNote(freqStart = 880.00, freqEnd = 880.00, durationMs = 130, volume = 0.30f)
            )
        )
    }

    fun playFanfare() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 523.25, freqEnd = 523.25, durationMs = 90, volume = 0.30f),  // C5
                ToneNote(freqStart = 659.25, freqEnd = 659.25, durationMs = 90, volume = 0.30f),  // E5
                ToneNote(freqStart = 783.99, freqEnd = 783.99, durationMs = 90, volume = 0.30f),  // G5
                ToneNote(freqStart = 1046.50, freqEnd = 1046.50, durationMs = 240, volume = 0.34f) // C6
            )
        )
    }

    fun playToothGiggle() {
        if (!isEnabled) return
        playSequence(
            listOf(
                ToneNote(freqStart = 587.33, freqEnd = 783.99, durationMs = 75, volume = 0.26f),
                ToneNote(freqStart = 783.99, freqEnd = 987.77, durationMs = 95, volume = 0.26f)
            )
        )
    }

    private data class ToneNote(
        val freqStart: Double,
        val freqEnd: Double,
        val durationMs: Int,
        val volume: Float
    )

    private fun playSequence(notes: List<ToneNote>) {
        scope.launch {
            try {
                val sampleRate = 22050
                val totalSamples = notes.sumOf { (sampleRate * it.durationMs) / 1000 }
                if (totalSamples <= 0) return@launch

                val pcm = ShortArray(totalSamples)
                var offset = 0
                var phase = 0.0

                for (note in notes) {
                    val count = (sampleRate * note.durationMs) / 1000
                    for (i in 0 until count) {
                        val progress = i.toDouble() / count.coerceAtLeast(1)
                        val freq = note.freqStart + (note.freqEnd - note.freqStart) * progress
                        // Smooth attack and decay envelope prevents audible clicks
                        val attack = (i / (count * 0.12)).coerceIn(0.0, 1.0)
                        val release = ((count - i) / (count * 0.25)).coerceIn(0.0, 1.0)
                        val envelope = attack * release
                        phase += (2.0 * PI * freq) / sampleRate
                        val sample = (sin(phase) * envelope * note.volume * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        if (offset < pcm.size) {
                            pcm[offset++] = sample.toShort()
                        }
                    }
                }

                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferBytes = maxOf(minBuffer, pcm.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(bufferBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val totalMs = notes.sumOf { it.durationMs }.toLong() + 80L
                kotlinx.coroutines.delay(totalMs)
                track.stop()
                track.release()
            } catch (_: Throwable) {
                // Safe no-op in headless/Robolectric or devices without audio hardware
            }
        }
    }
}
