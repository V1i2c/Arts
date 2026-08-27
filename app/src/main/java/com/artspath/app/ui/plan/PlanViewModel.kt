package com.artspath.app.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewModelScope
import com.artspath.app.AppGraph
import com.artspath.app.core.Stats
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.PlanRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModel(private val db: AppDatabase) : ViewModel() {

    /** Monday of the week currently displayed. */
    val anchor = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))

    val selectedDay = MutableStateFlow(Stats.todayEpochDay())

    val entries: StateFlow<List<PlanRow>> = anchor
        .flatMapLatest { monday ->
            db.planDao().observeBetween(monday.toEpochDay(), monday.plusDays(6).toEpochDay())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun shiftWeeks(weeks: Int) {
        anchor.value = anchor.value.plusWeeks(weeks.toLong())
    }

    fun thisWeek() {
        anchor.value = LocalDate.now().with(DayOfWeek.MONDAY)
    }

    fun selectDay(day: Long) {
        selectedDay.value = day
    }

    fun toggleDone(entry: PlanRow) = viewModelScope.launch(Dispatchers.IO) {
        val full = db.planDao().byId(entry.id) ?: return@launch
        db.planDao().update(full.copy(done = !full.done))
    }

    companion object {
        fun factory() = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { PlanViewModel(AppGraph.database) }
        }
    }
}
