package com.artspath.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artspath.app.AppGraph
import com.artspath.app.core.Stats
import com.artspath.app.data.ActivityEvent
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.DayStat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class DashboardUi(
    val today: Long = Stats.todayEpochDay(),
    val streak: Int = 0,
    val best: Int = 0,
    val level: Stats.Level = Stats.levels.first(),
    val nextLevel: Stats.Level? = null,
    /** 30 entries, oldest first; null = no activity that day. */
    val strip: List<DayStat?> = emptyList(),
    val dueToday: Int = 0,
    val overdue: Int = 0,
    val doneToday: Int = 0,
    val revisedToday: Int = 0,
    val totalDone: Int = 0,
    val totalErrors: Int = 0,
    val totalRevisions: Int = 0,
    val recent: List<ActivityEvent> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(db: AppDatabase) : ViewModel() {

    private val activityDao = db.activityDao()
    private val taskDao = db.taskDao()
    private val errorDao = db.errorDao()

    /** The UI pushes the live date here so everything refreshes at midnight. */
    private val todayFlow = MutableStateFlow(Stats.todayEpochDay())

    fun setDay(day: Long) {
        todayFlow.value = day
    }

    private val window = todayFlow.flatMapLatest { today ->
        activityDao.observeDayStats(today - 29, today)
    }

    private val todayCounts = todayFlow.flatMapLatest { today ->
        combine(
            taskDao.observeDueAndOverdue(today),
            taskDao.observeDoneOn(today),
            activityDao.observeDayStats(today, today)
        ) { due, done, stats -> Triple(due, done, stats.firstOrNull()) }
    }

    val ui: StateFlow<DashboardUi> = combine(
        combine(activityDao.observeActiveDays(), todayFlow) { days, today -> days.toSet() to today },
        combine(window, todayCounts) { w, t -> w to t },
        combine(taskDao.observeTotalDone(), errorDao.observeTotal()) { d, e -> d to e },
        combine(errorDao.observeTotalRevisions(), activityDao.observeRecent(300)) { r, rec -> r to rec }
    ) { daysAndToday, windowAndCounts, totals, revisionsAndRecent ->
        val (activeDays, today) = daysAndToday
        val (windowStats, counts) = windowAndCounts
        val (totalDone, totalErrors) = totals
        val (totalRevisions, recent) = revisionsAndRecent

        val byDay = windowStats.associateBy { it.day }
        val strip = (today - 29..today).map { byDay[it] }
        val streak = Stats.computeStreak(activeDays, today)
        val due = counts.first

        DashboardUi(
            today = today,
            streak = streak,
            best = Stats.bestStreak(activeDays),
            level = Stats.levelFor(streak),
            nextLevel = Stats.nextLevel(streak),
            strip = strip,
            dueToday = due.count { it.dueDay == today },
            overdue = due.count { (it.dueDay ?: today) < today },
            doneToday = counts.second.size,
            revisedToday = counts.third?.errorsRevised ?: 0,
            totalDone = totalDone,
            totalErrors = totalErrors,
            totalRevisions = totalRevisions,
            recent = recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUi())

    companion object {
        fun factory() = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { DashboardViewModel(AppGraph.database) }
        }
    }
}
