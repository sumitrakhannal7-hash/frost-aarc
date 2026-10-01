package com.example.ui.screens.workout

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

data class WorkoutTrack(
    val title: String,
    val genre: String,
    val bpm: Int
)

class WorkoutMusicManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val random = Random()

    val tracks = listOf(
        WorkoutTrack(
            title = "FrostArc Synthwave Pulse",
            genre = "Electro Gym Beat",
            bpm = 135
        ),
        WorkoutTrack(
            title = "Cyber Iron Tempo",
            genre = "Calisthenics Power",
            bpm = 128
        ),
        WorkoutTrack(
            title = "Cardio Sprint Surge",
            genre = "High-Intensity Drive",
            bpm = 145
        ),
        WorkoutTrack(
            title = "Deep Focus Rhythm Flow",
            genre = "Electronic Tempo",
            bpm = 118
        )
    )

    private val _isMusicOn = MutableStateFlow(false)
    val isMusicOn: StateFlow<Boolean> = _isMusicOn

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

    private val _visualizerAmplitudes = MutableStateFlow(listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f))
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private var visualizerJob: Job? = null

    init {
        startVisualizerLoop()
    }

    fun toggleMusic() {
        if (_isMusicOn.value) {
            turnOffMusic()
        } else {
            turnOnMusic()
        }
    }

    fun turnOnMusic() {
        _isMusicOn.value = true
        _isPlaying.value = true
        startAudioPlayback()
    }

    fun turnOffMusic() {
        _isMusicOn.value = false
        _isPlaying.value = false
        stopAudioPlayback()
    }

    fun togglePlayPause() {
        if (!_isMusicOn.value) {
            turnOnMusic()
            return
        }
        if (_isPlaying.value) {
            pauseAudio()
        } else {
            resumeAudio()
        }
    }

    fun nextTrack() {
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % tracks.size
        if (_isMusicOn.value && _isPlaying.value) {
            startAudioPlayback()
        }
    }

    fun prevTrack() {
        _currentTrackIndex.value = if (_currentTrackIndex.value - 1 < 0) tracks.size - 1 else _currentTrackIndex.value - 1
        if (_isMusicOn.value && _isPlaying.value) {
            startAudioPlayback()
        }
    }

    private fun pauseAudio() {
        _isPlaying.value = false
        stopAudioPlayback()
    }

    private fun resumeAudio() {
        _isPlaying.value = true
        startAudioPlayback()
    }

    private fun startAudioPlayback() {
        stopAudioPlayback()
        val currentTrack = tracks[_currentTrackIndex.value]

        synthJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(8192)

            val track: AudioTrack
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                audioTrack = track
            } catch (_: Exception) {
                return@launch
            }

            val bpm = currentTrack.bpm
            val stepDurationSec = (60.0 / bpm) / 4.0 // 16th-note step
            val stepSamples = (sampleRate * stepDurationSec).toInt()
            val buffer = ShortArray(stepSamples)

            // Bass note frequencies for progression (A1, C2, D2, E2)
            val bassFreqs = when (_currentTrackIndex.value) {
                0 -> doubleArrayOf(55.0, 55.0, 65.41, 73.42) // A minor
                1 -> doubleArrayOf(73.42, 73.42, 82.41, 65.41) // D minor punch
                2 -> doubleArrayOf(82.41, 87.31, 98.0, 110.0) // High drive E minor
                else -> doubleArrayOf(65.41, 73.42, 82.41, 98.0) // Chill C major flow
            }

            var step = 0
            while (isActive && _isPlaying.value) {
                val stepInBar = step % 16
                val barIndex = (step / 16) % 4
                val isKick = (stepInBar % 4 == 0) // Beats 1, 2, 3, 4
                val isSnare = (stepInBar == 4 || stepInBar == 12) // Beats 2 and 4
                val isHiHat = (stepInBar % 2 == 1) // 8th note upbeats
                val isBass = (stepInBar % 2 == 0)

                val bassFreq = bassFreqs[barIndex]

                for (i in 0 until stepSamples) {
                    val progress = i.toDouble() / stepSamples
                    val timeSec = progress * stepDurationSec
                    var sampleVal = 0.0

                    // 1. Kick Drum (Deep punch dropping from 150Hz to 45Hz)
                    if (isKick) {
                        val kickProg = (i.toDouble() / (stepSamples * 1.5)).coerceAtMost(1.0)
                        val kickFreq = 145.0 - (100.0 * kickProg)
                        val kickAmp = (1.0 - kickProg).coerceAtLeast(0.0)
                        val angle = 2.0 * PI * kickFreq * timeSec
                        sampleVal += sin(angle) * kickAmp * 15000.0
                    }

                    // 2. Snare / Clap (Snap noise on beats 2 and 4)
                    if (isSnare) {
                        val snareProg = (i.toDouble() / stepSamples).coerceAtMost(1.0)
                        val snareAmp = (1.0 - snareProg) * (1.0 - snareProg)
                        val toneAngle = 2.0 * PI * 220.0 * timeSec
                        val noise = (random.nextDouble() * 2.0 - 1.0) * 0.7
                        sampleVal += (sin(toneAngle) * 0.3 + noise) * snareAmp * 12000.0
                    }

                    // 3. Hi-Hat (Crisp high-frequency tick)
                    if (isHiHat) {
                        val hatProg = (i.toDouble() / (stepSamples * 0.5)).coerceAtMost(1.0)
                        val hatAmp = (1.0 - hatProg).coerceAtLeast(0.0)
                        val noise = (random.nextDouble() * 2.0 - 1.0)
                        sampleVal += noise * hatAmp * 5000.0
                    }

                    // 4. Synthwave Bass Arp
                    if (isBass) {
                        val bassAmp = (1.0 - progress * 0.7)
                        val angle = 2.0 * PI * bassFreq * timeSec
                        val harmonic = sin(2.0 * angle) * 0.35
                        sampleVal += (sin(angle) + harmonic) * bassAmp * 7000.0
                    }

                    // Soft clip limiter to prevent audio distortion
                    val clamped = sampleVal.coerceIn(-32000.0, 32000.0)
                    buffer[i] = clamped.toInt().toShort()
                }

                track.write(buffer, 0, stepSamples)
                step++
            }
        }
    }

    private fun stopAudioPlayback() {
        synthJob?.cancel()
        synthJob = null

        val trackToRelease = audioTrack
        audioTrack = null

        if (trackToRelease != null) {
            try {
                if (trackToRelease.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    trackToRelease.stop()
                }
                trackToRelease.release()
            } catch (_: Exception) {}
        }
    }

    private fun startVisualizerLoop() {
        visualizerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                if (_isMusicOn.value && _isPlaying.value) {
                    val newAmps = List(6) { 0.25f + random.nextFloat() * 0.75f }
                    _visualizerAmplitudes.value = newAmps
                } else {
                    _visualizerAmplitudes.value = listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
                }
                delay(100)
            }
        }
    }

    fun release() {
        _isMusicOn.value = false
        _isPlaying.value = false
        stopAudioPlayback()
        visualizerJob?.cancel()
        scope.cancel()
    }
}
