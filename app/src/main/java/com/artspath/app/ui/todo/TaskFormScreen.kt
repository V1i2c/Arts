package com.artspath.app.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.artspath.app.AppGraph
import com.artspath.app.core.Stats
import com.artspath.app.data.Actions
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.Subject
import com.artspath.app.data.Task
import com.artspath.app.ui.components.ConfirmDialog
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.components.SubjectDot
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import com.artspath.app.ui.theme.subjectColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

class TaskFormViewModel(private val db: AppDatabase, private val taskId: Long) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = db.subjectDao().observeMine()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var loaded by mutableStateOf<Task?>(null)
        private set

    init {
        if (taskId > 0) viewModelScope.launch(Dispatchers.IO) {
            loaded = db.taskDao().byId(taskId)
        }
    }

    fun save(
        title: String,
        subjectId: Long?,
        dueDay: Long?,
        dueMinute: Int?,
        onDone: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        val existing = loaded
        if (existing == null) {
            Actions.addTask(
                db,
                Task(
                    title = title,
                    subjectId = subjectId,
                    dueDay = dueDay,
                    dueMinute = if (dueDay != null) dueMinute else null
                )
            )
        } else {
            Actions.updateTask(
                db,
                existing.copy(
                    title = title,
                    subjectId = subjectId,
                    dueDay = dueDay,
                    dueMinute = if (dueDay != null) dueMinute else null
                )
            )
        }
        kotlinx.coroutines.withContext(Dispatchers.Main) { onDone() }
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        loaded?.let { Actions.deleteTask(db, it.id) }
        kotlinx.coroutines.withContext(Dispatchers.Main) { onDone() }
    }

    companion object {
        fun factory(taskId: Long) = viewModelFactory {
            initializer { TaskFormViewModel(AppGraph.database, taskId) }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(taskId: Long, onDone: () -> Unit) {
    val vm: TaskFormViewModel = viewModel(
        key = "task_$taskId",
        factory = TaskFormViewModel.factory(taskId)
    )
    val subjects by vm.subjects.collectAsState()
    val existing = vm.loaded
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()

    var title by remember(existing) {
        mutableStateOf(existing?.title ?: "")
    }
    var subjectId by remember(existing) {
        mutableStateOf(existing?.subjectId ?: subjects.firstOrNull()?.id)
    }
    var dueDay by remember(existing) { mutableStateOf(existing?.dueDay) }
    var timeOn by remember(existing) {
        mutableStateOf(existing?.dueMinute != null)
    }
    var dueMinute by remember(existing) {
        mutableStateOf(existing?.dueMinute ?: Stats.DEFAULT_DEADLINE_MINUTE)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = p.ink)
            }
            Text(
                if (existing == null) "New task" else "Edit task",
                color = p.ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("What needs doing?") },
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        SectionLabel("Subject")
        Spacer(Modifier.height(10.dp))
        SubjectChips(
            subjects = subjects,
            selected = subjectId,
            dark = dark,
            onSelect = { subjectId = it }
        )

        Spacer(Modifier.height(20.dp))
        SectionLabel("Deadline (optional)")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateChoice("No date", dueDay == null) { dueDay = null }
            DateChoice("Today", dueDay == Stats.todayEpochDay()) {
                dueDay = Stats.todayEpochDay()
            }
            DateChoice(
                if (dueDay == null || dueDay == Stats.todayEpochDay()) "Pick date…"
                else Stats.formatDayMedium(dueDay!!),
                dueDay != null && dueDay != Stats.todayEpochDay()
            ) { showDatePicker = true }
        }
        if (dueDay != null) {
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Switch(
                    checked = timeOn,
                    onCheckedChange = { timeOn = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = p.accent)
                )
                Text("Set a time", color = p.inkSoft, fontSize = 14.sp)
                if (timeOn) {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(
                            Stats.formatMinuteOfDay(dueMinute),
                            color = p.accentDeep,
                            fontWeight = FontWeight.SemiBold,
                            style = TabularNumbers
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { vm.save(title.trim(), subjectId, dueDay, if (timeOn) dueMinute else null, onDone) },
            enabled = title.isNotBlank() && subjectId != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = p.ink, contentColor = p.paper)
        ) {
            Text(if (existing == null) "Add task" else "Save changes", fontWeight = FontWeight.SemiBold)
        }

        if (existing != null) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = p.danger)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Delete task", modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                "Deleting is permanent, but your history on the dashboard keeps its record.",
                color = p.inkFaint,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = dueDay?.let {
                Instant.ofEpochMilli(it * 86_400_000L + 3_600_000L).toEpochMilli()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        dueDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("OK", color = p.accentDeep) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = p.inkSoft) }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = dueMinute / 60,
            initialMinute = dueMinute % 60,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Deadline time", color = p.ink) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    dueMinute = state.hour * 60 + state.minute
                    showTimePicker = false
                }) { Text("OK", color = p.accentDeep) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = p.inkSoft) }
            }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete this task?",
            text = "\"${existing?.title ?: ""}\" will be removed. This can't be undone.",
            onConfirm = { showDelete = false; vm.delete(onDone) },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun SubjectChips(
    subjects: List<Subject>,
    selected: Long?,
    dark: Boolean,
    onSelect: (Long) -> Unit
) {
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        subjects.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    val ink = subjectColor(s.colorKey, dark)
                    val isSelected = selected == s.id
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ink.copy(alpha = 0.14f) else p.surfaceAlt)
                            .clickable { onSelect(s.id) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SubjectDot(ink, 8)
                            Text(
                                s.name,
                                color = if (isSelected) p.ink else p.inkSoft,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DateChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) p.accentSoft else p.surfaceAlt)
            .border(1.dp, if (selected) p.accent else p.hairline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (selected) p.accentDeep else p.inkSoft, fontSize = 13.sp)
    }
}
