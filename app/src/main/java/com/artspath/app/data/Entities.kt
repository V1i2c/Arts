package com.artspath.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["name"], unique = true)]
)
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Key into the fixed palette in ui/theme/SubjectColors.kt */
    val colorKey: String,
    val isMine: Boolean = false,
    val sortOrder: Int = 0,
    val isCustom: Boolean = false
)

@Entity(
    tableName = "chapters",
    indices = [Index("subjectId"), Index(value = ["subjectId", "name"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    /** Book / part label, e.g. "Flamingo — Poetry" */
    val part: String? = null,
    val sortOrder: Int = 0,
    val isCustom: Boolean = false
)

const val TASK_PENDING = "PENDING"
const val TASK_DONE = "DONE"

@Entity(
    tableName = "tasks",
    indices = [Index("subjectId"), Index("status"), Index("dueDay")],
    foreignKeys = [ForeignKey(
        entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectId: Long?,
    /** LocalDate.toEpochDay() — null = no date */
    val dueDay: Long? = null,
    /** Minutes from midnight, e.g. 23 * 60 + 59 for 11:59 PM. Null = no time. */
    val dueMinute: Int? = null,
    val status: String = TASK_PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedDay: Long? = null,
    val completedAt: Long? = null
)

@Entity(
    tableName = "plan_entries",
    indices = [Index("day"), Index("subjectId")],
    foreignKeys = [ForeignKey(
        entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class PlanEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    /** LocalDate.toEpochDay() */
    val day: Long,
    /** Minutes from midnight; null = all-day entry */
    val startMinute: Int? = null,
    val endMinute: Int? = null,
    val subjectId: Long? = null,
    /** Reminder this many minutes before start; null = no reminder */
    val reminderMinutesBefore: Int? = null,
    /** Absolute wall-clock millis at which the reminder should fire */
    val triggerAtMillis: Long? = null,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "errors",
    indices = [Index("chapterId"), Index("subjectId")],
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class, parentColumns = ["id"], childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class, parentColumns = ["id"], childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ErrorRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val chapterId: Long,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastRevisedAt: Long? = null,
    val revisionCount: Int = 0
)

const val ATTACHMENT_IMAGE = "IMAGE"
const val ATTACHMENT_AUDIO = "AUDIO"

@Entity(
    tableName = "attachments",
    indices = [Index("errorId")],
    foreignKeys = [ForeignKey(
        entity = ErrorRecord::class, parentColumns = ["id"], childColumns = ["errorId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val errorId: Long,
    val kind: String,
    /** File name inside filesDir/media/ */
    val fileName: String,
    val durationMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** Append-only history log. Powers streaks, consistency and per-day totals. */
@Entity(tableName = "activity_events", indices = [Index("day"), Index("type", "refId")])
data class ActivityEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** LocalDate.toEpochDay() of the day this happened on (device zone) */
    val day: Long,
    val ts: Long = System.currentTimeMillis(),
    val type: String,
    val refId: Long = 0,
    val label: String = "",
    val subjectId: Long? = null
)
