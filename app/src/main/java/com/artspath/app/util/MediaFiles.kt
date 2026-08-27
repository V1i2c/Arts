package com.artspath.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/** All media lives in app-private storage (filesDir/media) — no storage permission needed. */
object MediaFiles {

    fun mediaDir(context: Context): File =
        File(context.filesDir, "media").apply { mkdirs() }

    fun fileFor(context: Context, fileName: String): File =
        File(mediaDir(context), fileName)

    /** Copies a picked image into private storage; returns the stored file. */
    fun copyImage(context: Context, uri: Uri): File? = try {
        val name = "img_${UUID.randomUUID()}.jpg"
        val out = File(mediaDir(context), name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        out
    } catch (_: Exception) {
        null
    }

    fun newAudioFile(context: Context): File =
        File(mediaDir(context), "aud_${UUID.randomUUID()}.m4a")

    fun delete(fileName: String) {
        // Caller has context; kept simple by resolving lazily through a stored root.
        pendingDeletes.add(fileName)
    }

    private val pendingDeletes = mutableListOf<String>()

    /** Actually deletes files registered via [delete]. Called with a context available. */
    fun flushDeletes(context: Context) {
        val dir = mediaDir(context)
        synchronized(pendingDeletes) {
            for (name in pendingDeletes) File(dir, name).delete()
            pendingDeletes.clear()
        }
    }

    fun deleteNow(context: Context, fileName: String) {
        File(mediaDir(context), fileName).delete()
    }
}
