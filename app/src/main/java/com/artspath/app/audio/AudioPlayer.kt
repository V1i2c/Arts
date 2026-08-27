package com.artspath.app.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * One shared voice-note player for the whole app so only one clip can play
 * at a time and playback stops when the caller goes away.
 */
class AudioPlayer {

    data class State(
        val path: String? = null,
        val playing: Boolean = false,
        val progressMs: Int = 0,
        val durationMs: Int = 0
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private var player: MediaPlayer? = null
    private var ticker: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    /** Plays [file] from the start, or stops it if it is the current clip. */
    fun toggle(file: File) {
        val path = file.absolutePath
        if (_state.value.path == path && _state.value.playing) {
            stop()
            return
        }
        stop()
        val mp = MediaPlayer()
        mp.setDataSource(path)
        mp.prepare()
        mp.setOnCompletionListener {
            _state.value = State(path = path, playing = false, progressMs = it.duration, durationMs = it.duration)
            releasePlayer()
        }
        mp.start()
        player = mp
        _state.value = State(path = path, playing = true, progressMs = 0, durationMs = mp.duration)
        startTicker(path)
    }

    fun stop() {
        releasePlayer()
        _state.value = State()
    }

    private fun startTicker(path: String) {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val mp = player ?: break
                if (!mp.isPlaying) break
                _state.value = _state.value.copy(progressMs = mp.currentPosition)
                delay(200)
            }
        }
    }

    private fun releasePlayer() {
        ticker?.cancel()
        ticker = null
        try {
            player?.let { if (it.isPlaying) it.stop() }
        } catch (_: IllegalStateException) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
    }
}
