package com.artspath.app.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewModelScope
import com.artspath.app.AppGraph
import com.artspath.app.core.Stats
import com.artspath.app.data.Actions
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.Subject
import com.artspath.app.data.TaskRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoTab(val label: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    ALL("All")
}

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModel(private val db: AppDatabase) : ViewModel() {

    val tab = MutableStateFlow(TodoTab.TODAY)
    val subjectFilter = MutableStateFlow<Long?>(null)
    private val todayFlow = MutableStateFlow(Stats.todayEpochDay())

    val subjects: StateFlow<List<Subject>> = db.subjectDao().observeMine()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<TaskRow>> =
        combine(tab, subjectFilter, todayFlow) { t, f, today -> Triple(t, f, today) }
            .flatMapLatest { (chosenTab, filter, today) ->
                val base = when (chosenTab) {
                    TodoTab.TODAY -> db.taskDao().observeDueAndOverdue(today)
                    TodoTab.UPCOMING -> db.taskDao().observeUpcoming(today)
                    TodoTab.ALL -> db.taskDao().observeAllPending()
                }
                base.map { list -> list.filter { filter == null || it.subjectId == filter } }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setToday(day: Long) {
        todayFlow.value = day
    }

    fun setFilter(subjectId: Long?) {
        subjectFilter.value = subjectId
    }

    fun complete(task: TaskRow) = viewModelScope.launch(Dispatchers.IO) {
        Actions.completeTask(db, task)
    }

    fun uncomplete(taskId: Long) = viewModelScope.launch(Dispatchers.IO) {
        Actions.uncompleteTask(db, taskId)
    }

    companion object {
        fun factory() = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { TodoViewModel(AppGraph.database) }
        }
    }
}
