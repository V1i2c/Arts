package com.artspath.app.ui.errors

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewModelScope
import com.artspath.app.AppGraph
import com.artspath.app.data.Actions
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.ChapterWithCount
import com.artspath.app.data.ErrorRecord
import com.artspath.app.data.Subject
import com.artspath.app.data.SubjectErrorCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ErrorsHomeViewModel(db: AppDatabase) : ViewModel() {
    val subjects: StateFlow<List<Subject>> = db.subjectDao().observeMine()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val counts: StateFlow<List<SubjectErrorCount>> = db.errorDao().observeSubjectCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory() = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { ErrorsHomeViewModel(AppGraph.database) }
        }
    }
}

class ChaptersViewModel(private val db: AppDatabase, private val subjectId: Long) : ViewModel() {
    val subject: StateFlow<Subject?> = db.subjectDao().observeAll()
        .flatMapLatest { list -> flow { emit(list.firstOrNull { it.id == subjectId }) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val chapters: StateFlow<List<ChapterWithCount>> =
        db.chapterDao().observeWithCounts(subjectId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addChapter(name: String) =
        viewModelScope.launch(Dispatchers.IO) {
            db.chapterDao().insert(
                com.artspath.app.data.Chapter(
                    subjectId = subjectId,
                    name = name.trim(),
                    sortOrder = 1000,
                    isCustom = true
                )
            )
        }

    companion object {
        fun factory(subjectId: Long) = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { ChaptersViewModel(AppGraph.database, subjectId) }
        }
    }
}

data class PagerUi(
    val errors: List<ErrorRecord> = emptyList()
)

class PagerViewModel(private val db: AppDatabase, private val chapterId: Long) : ViewModel() {

    val errors: StateFlow<List<ErrorRecord>> = db.errorDao().observeByChapter(chapterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val chapterState = mutableStateOf<com.artspath.app.data.Chapter?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chapterState.value = db.chapterDao().byId(chapterId)
        }
    }

    fun revise(error: ErrorRecord) = viewModelScope.launch(Dispatchers.IO) {
        Actions.reviseError(db, error)
    }

    fun delete(error: ErrorRecord) = viewModelScope.launch(Dispatchers.IO) {
        Actions.deleteError(AppGraph.appContext, db, error)
    }

    companion object {
        fun factory(chapterId: Long) = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { PagerViewModel(AppGraph.database, chapterId) }
        }
    }
}

