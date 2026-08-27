package com.artspath.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.room.withTransaction
import com.artspath.app.audio.AudioPlayer
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.Chapter
import com.artspath.app.data.SyllabusCatalog
import com.artspath.app.data.Subject
import com.artspath.app.work.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ArtsApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
        Notifier.ensureChannel(this)
        appScope.launch(Dispatchers.IO) {
            seedSyllabusIfNeeded()
        }
    }

    private suspend fun seedSyllabusIfNeeded() {
        val db = AppGraph.database
        val subjectDao = db.subjectDao()
        if (subjectDao.count() > 0) return
        db.withTransaction {
            SyllabusCatalog.subjects.forEachIndexed { sIndex, seed ->
                val subjectId = subjectDao.insert(
                    Subject(
                        name = seed.name,
                        colorKey = seed.colorKey,
                        isMine = false,
                        sortOrder = seed.sortOrder,
                        isCustom = false
                    )
                )
                val chapters = seed.chapters.mapIndexed { cIndex, c ->
                    Chapter(
                        subjectId = subjectId,
                        name = c.name,
                        part = c.part,
                        sortOrder = cIndex + 1,
                        isCustom = false
                    )
                }
                if (chapters.isNotEmpty()) db.chapterDao().insertAll(chapters)
            }
        }
    }
}

/** Tiny service locator — no DI framework needed for an app this size. */
object AppGraph {
    lateinit var appContext: Context
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var prefs: android.content.SharedPreferences
        private set
    val audioPlayer by lazy { AudioPlayer() }

    fun init(app: ArtsApp) {
        appContext = app
        database = AppDatabase.get(app)
        prefs = app.getSharedPreferences("artspath_prefs", Context.MODE_PRIVATE)
    }

    var onboarded: Boolean
        get() = prefs.getBoolean("onboarded", false)
        set(value) = prefs.edit().putBoolean("onboarded", value).apply()
}
