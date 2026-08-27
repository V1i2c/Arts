package com.artspath.app.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * AAC/MP4 voice-note recorder. One recording at a time; the UI drives
 * start/stop and reads [currentAmplitude] for a live pulse animation.
 */
class AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt: Long = 0

    val isRecording: Boolean get() = recorder != null

    fun currentAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (_: IllegalStateException) {
        0
    }

    fun elapsedMs(): Long =
        if (recorder != null) System.currentTimeMillis() - startedAt else 0L

    /** Begins recording into [file]; overwrites anything already there. */
    fun start(context: Context, file: File) {
        check(recorder == null) { "Already recording" }
        file.parentFile?.mkdirs()
        if (file.exists()) file.delete()
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(96_000)
        r.setAudioSamplingRate(44_100)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        outputFile = file
        startedAt = System.currentTimeMillis()
    }

    /** Stops and returns (file, durationMs). */
    fun stop(): Pair<File, Long>? {
        val r = recorder ?: return null
        val file = outputFile
        val elapsed = System.currentTimeMillis() - startedAt
        recorder = null
        outputFile = null
        return try {
            r.stop()
            r.release()
            val duration = durationOf(file) ?: elapsed
            file?.let { it to duration }
        } catch (_: RuntimeException) {
            // stop() can throw if no valid audio was captured; discard silently.
            try { r.release() } catch (_: Exception) { }
            file?.delete()
            null
        }
    }

    fun discard() {
        val r = recorder ?: return
        recorder = null
        try {
            r.stop()
        } catch (_: RuntimeException) {
        }
        try {
            r.release()
        } catch (_: Exception) {
        }
        outputFile?.delete()
        outputFile = null
    }

    companion object {
        fun durationOf(file: File?): Long? = try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file?.absolutePath)
            val ms = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull()
            retriever.release()
            ms
        } catch (_: Exception) {
            null
        }
    }
}
