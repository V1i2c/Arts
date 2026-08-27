package com.artspath.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Subject::class,
        Chapter::class,
        Task::class,
        PlanEntry::class,
        ErrorRecord::class,
        Attachment::class,
        ActivityEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun taskDao(): TaskDao
    abstract fun planDao(): PlanDao
    abstract fun errorDao(): ErrorDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "artspath.db"
                ).build().also { instance = it }
            }
    }
}
