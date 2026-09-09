package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class GameAudioEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var sfxVolume: Float = 1.0f
    var musicVolume: Float = 0.8f
    var vibrationEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sampleRate = 22050

    fun playFootstep() {
        if (sfxVolume <= 0.05f) return
        scope.launch {
            playTones(listOf(Tone(110.0, 0.035, 0.25f * sfxVolume, WaveType.SINE)))
        }
    }

    fun playJump() {
        if (sfxVolume <= 0.05f) return
        vibrate(25)
        scope.launch {
            playPitchBend(180.0, 360.0, 0.12, 0.35f * sfxVolume)
        }
    }

    fun playClimb() {
        if (sfxVolume <= 0.05f) return
        vibrate(15)
        scope.launch {
            playTones(listOf(Tone(160.0, 0.04, 0.25f * sfxVolume, WaveType.TRIANGLE)))
        }
    }

    fun playDigHit() {
        if (sfxVolume <= 0.05f) return
        vibrate(30)
        scope.launch {
            playPitchBend(140.0, 70.0, 0.08, 0.45f * sfxVolume)
        }
    }

    fun playBlockBreak() {
        if (sfxVolume <= 0.05f) return
        vibrate(50)
        scope.launch {
            playTones(listOf(
                Tone(120.0, 0.05, 0.4f * sfxVolume, WaveType.SQUARE),
                Tone(80.0, 0.08, 0.5f * sfxVolume, WaveType.NOISE)
            ))
        }
    }

    fun playGemCollect(isRare: Boolean = false) {
        if (sfxVolume <= 0.05f) return
        vibrate(if (isRare) 100 else 40)
        scope.launch {
            if (isRare) {
                // Triumphant 4-note chime
                playTones(listOf(
                    Tone(523.25, 0.08, 0.4f * sfxVolume, WaveType.SINE), // C5
                    Tone(659.25, 0.08, 0.45f * sfxVolume, WaveType.SINE), // E5
                    Tone(783.99, 0.08, 0.5f * sfxVolume, WaveType.SINE), // G5
                    Tone(1046.50, 0.22, 0.6f * sfxVolume, WaveType.SINE) // C6
                ))
            } else {
                playTones(listOf(
                    Tone(880.0, 0.07, 0.4f * sfxVolume, WaveType.SINE),
                    Tone(1320.0, 0.14, 0.5f * sfxVolume, WaveType.SINE)
                ))
            }
        }
    }

    fun playBackpackFull() {
        if (sfxVolume <= 0.05f) return
        vibratePattern(longArrayOf(0, 80, 80, 80))
        scope.launch {
            playTones(listOf(
                Tone(440.0, 0.1, 0.5f * sfxVolume, WaveType.SQUARE),
                Tone(330.0, 0.15, 0.5f * sfxVolume, WaveType.SQUARE)
            ))
        }
    }

    fun playSellReward() {
        if (sfxVolume <= 0.05f) return
        vibrate(70)
        scope.launch {
            val tones = listOf(
                Tone(659.25, 0.06, 0.4f * sfxVolume, WaveType.SINE),
                Tone(880.00, 0.06, 0.45f * sfxVolume, WaveType.SINE),
                Tone(1174.66, 0.06, 0.5f * sfxVolume, WaveType.SINE),
                Tone(1567.98, 0.18, 0.55f * sfxVolume, WaveType.SINE)
            )
            playTones(tones)
        }
    }

    fun playUpgradeFanfare() {
        if (sfxVolume <= 0.05f) return
        vibrate(90)
        scope.launch {
            playTones(listOf(
                Tone(440.0, 0.08, 0.45f * sfxVolume, WaveType.TRIANGLE),
                Tone(554.37, 0.08, 0.5f * sfxVolume, WaveType.TRIANGLE),
                Tone(659.25, 0.08, 0.55f * sfxVolume, WaveType.TRIANGLE),
                Tone(880.0, 0.25, 0.6f * sfxVolume, WaveType.TRIANGLE)
            ))
        }
    }

    fun playRepairHouse() {
        if (sfxVolume <= 0.05f) return
        vibrate(60)
        scope.launch {
            playTones(listOf(
                Tone(260.0, 0.06, 0.5f * sfxVolume, WaveType.SQUARE),
                Tone(320.0, 0.06, 0.45f * sfxVolume, WaveType.SQUARE),
                Tone(520.0, 0.14, 0.5f * sfxVolume, WaveType.TRIANGLE)
            ))
        }
    }

    fun playButtonClick() {
        if (sfxVolume <= 0.05f) return
        vibrate(10)
        scope.launch {
            playTones(listOf(Tone(600.0, 0.025, 0.3f * sfxVolume, WaveType.SINE)))
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(timings: LongArray) {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    private enum class WaveType { SINE, SQUARE, TRIANGLE, NOISE }

    private data class Tone(
        val frequency: Double,
        val durationSeconds: Double,
        val volume: Float,
        val waveType: WaveType
    )

    private fun playTones(tones: List<Tone>) {
        val totalSamples = tones.sumOf { (it.durationSeconds * sampleRate).toInt() }
        if (totalSamples <= 0) return
        val buffer = ShortArray(totalSamples)
        var offset = 0

        for (tone in tones) {
            val count = (tone.durationSeconds * sampleRate).toInt()
            for (i in 0 until count) {
                val t = i.toDouble() / sampleRate
                val decay = 1.0 - (i.toDouble() / count.toDouble()) // Linear fade out
                val rawVal: Double = when (tone.waveType) {
                    WaveType.SINE -> sin(2.0 * PI * tone.frequency * t)
                    WaveType.SQUARE -> if (sin(2.0 * PI * tone.frequency * t) >= 0) 0.8 else -0.8
                    WaveType.TRIANGLE -> {
                        val p = (t * tone.frequency) % 1.0
                        if (p < 0.5) (4.0 * p - 1.0) else (3.0 - 4.0 * p)
                    }
                    WaveType.NOISE -> (Math.random() * 2.0 - 1.0)
                }
                val sample = (rawVal * decay * tone.volume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[offset + i] = sample.toShort()
            }
            offset += count
        }
        writeToAudioTrack(buffer)
    }

    private fun playPitchBend(startFreq: Double, endFreq: Double, durationSeconds: Double, volume: Float) {
        val count = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count.toDouble()
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / sampleRate
            val decay = 1.0 - progress
            val raw = sin(phase)
            val sample = (raw * decay * volume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        writeToAudioTrack(buffer)
    }

    private fun writeToAudioTrack(buffer: ShortArray) {
        try {
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
            // Track clean-up is handled after playback duration
            scope.launch {
                val playDurationMs = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 50L
                kotlinx.coroutines.delay(playDurationMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
