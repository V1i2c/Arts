package com.artspath.app.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.artspath.app.AppGraph
import com.artspath.app.core.Stats
import com.artspath.app.data.PlanEntry
import java.time.Duration

/** Fires the notification for one planned study entry. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val body = inputData.getString(KEY_BODY) ?: ""
        Notifier.showReminder(applicationContext, title, body)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"

        fun uniqueName(planId: Long) = "plan-reminder-$planId"

        /**
         * Schedule (or re-schedule) the reminder for a plan entry.
         * Cancels any previous work for the entry first; no-ops for past times.
         */
        fun schedule(context: Context, entry: PlanEntry) {
            val wm = WorkManager.getInstance(context)
            if (entry.triggerAtMillis == null) {
                wm.cancelUniqueWork(uniqueName(entry.id))
                return
            }
            val delay = entry.triggerAtMillis - System.currentTimeMillis()
            if (delay < 5_000) {
                wm.cancelUniqueWork(uniqueName(entry.id))
                return
            }
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(Duration.ofMillis(delay))
                .setInputData(
                    workDataOf(
                        KEY_TITLE to entry.title,
                        KEY_BODY to reminderBody(entry)
                    )
                )
                .addTag("plan_reminder")
                .build()
            wm.enqueueUniqueWork(uniqueName(entry.id), ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context, planId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueName(planId))
        }

        private fun reminderBody(entry: PlanEntry): String {
            val whenText = Stats.formatTimeRange(entry.startMinute, entry.endMinute)
            return "Planned $whenText — open ArtsPath"
        }
    }
}
