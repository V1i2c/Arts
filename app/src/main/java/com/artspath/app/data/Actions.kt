package com.artspath.app.data

import android.content.Context
import com.artspath.app.core.Stats
import com.artspath.app.work.ReminderWorker

/**
 * Cross-screen write actions. Every meaningful action also writes an append-only
 * ActivityEvent so the dashboard history/streaks survive edits and deletions.
 */
object Actions {

    private const val TASK_COMPLETED = "TASK_COMPLETED"
    private const val TASK_ADDED = "TASK_ADDED"
    private const val ERROR_ADDED = "ERROR_ADDED"
    private const val ERROR_REVISED = "ERROR_REVISED"

    suspend fun addTask(db: AppDatabase, task: Task): Long {
        val id = db.taskDao().insert(task)
        db.activityDao().insert(
            ActivityEvent(
                day = Stats.todayEpochDay(), type = TASK_ADDED, refId = id,
                label = task.title, subjectId = task.subjectId
            )
        )
        return id
    }

    suspend fun updateTask(db: AppDatabase, task: Task) = db.taskDao().update(task)

    suspend fun completeTask(db: AppDatabase, task: TaskRow) {
        val now = System.currentTimeMillis()
        val today = Stats.todayEpochDay()
        val full = db.taskDao().byId(task.id) ?: return
        if (full.status == TASK_DONE) return
        db.taskDao().markDone(task.id, today, now)
        db.activityDao().insert(
            ActivityEvent(
                day = today, type = TASK_COMPLETED, refId = task.id,
                label = task.title, subjectId = task.subjectId
            )
        )
    }

    /** Undo — removes the history event too, so consistency counts stay truthful. */
    suspend fun uncompleteTask(db: AppDatabase, taskId: Long) {
        db.taskDao().markPending(taskId)
        db.activityDao().removeFor(TASK_COMPLETED, taskId)
    }

    suspend fun deleteTask(db: AppDatabase, taskId: Long) = db.taskDao().delete(taskId)

    suspend fun savePlan(context: Context, db: AppDatabase, entry: PlanEntry): Long {
        val id = if (entry.id == 0L) db.planDao().insert(entry) else {
            db.planDao().update(entry); entry.id
        }
        val saved = db.planDao().byId(id) ?: entry.copy(id = id)
        if (saved.reminderMinutesBefore != null && saved.triggerAtMillis != null) {
            ReminderWorker.schedule(context, saved)
        } else {
            ReminderWorker.cancel(context, id)
        }
        return id
    }

    suspend fun deletePlan(context: Context, db: AppDatabase, planId: Long) {
        db.planDao().delete(planId)
        ReminderWorker.cancel(context, planId)
    }

    suspend fun addError(db: AppDatabase, error: ErrorRecord): Long {
        val id = db.errorDao().insert(error)
        db.activityDao().insert(
            ActivityEvent(
                day = Stats.todayEpochDay(), type = ERROR_ADDED, refId = id,
                label = error.title, subjectId = error.subjectId
            )
        )
        return id
    }

    suspend fun updateError(db: AppDatabase, error: ErrorRecord) = db.errorDao().update(error)

    suspend fun reviseError(db: AppDatabase, error: ErrorRecord) {
        val now = System.currentTimeMillis()
        db.errorDao().bumpRevision(error.id, now)
        db.activityDao().insert(
            ActivityEvent(
                day = Stats.todayEpochDay(), type = ERROR_REVISED, refId = error.id,
                label = error.title, subjectId = error.subjectId
            )
        )
    }

    suspend fun deleteError(context: Context, db: AppDatabase, error: ErrorRecord) {
        db.errorDao().delete(error.id)
        db.attachmentDao().forError(error.id).forEach {
            com.artspath.app.util.MediaFiles.deleteNow(context, it.fileName)
        }
    }
}
