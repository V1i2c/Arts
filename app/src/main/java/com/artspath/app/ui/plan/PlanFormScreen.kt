package com.artspath.app.ui.plan

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import com.artspath.app.data.PlanEntry
import com.artspath.app.data.Subject
import com.artspath.app.ui.components.ConfirmDialog
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.components.SubjectDot
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import com.artspath.app.ui.theme.subjectColor
import com.artspath.app.util.Perms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class PlanFormViewModel(
    private val db: AppDatabase,
    private val planId: Long
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = db.subjectDao().observeMine()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var loaded by mutableStateOf<PlanEntry?>(null)
        private set

    init {
        if (planId > 0) viewModelScope.launch(Dispatchers.IO) {
            loaded = db.planDao().byId(planId)
        }
    }

    fun save(entry: PlanEntry, onDone: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val context = AppGraph.appContext
        Actions.savePlan(context, db, entry)
        withContext(Dispatchers.Main) { onDone() }
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val context = AppGraph.appContext
        Actions.deletePlan(context, db, planId)
        withContext(Dispatchers.Main) { onDone() }
    }

    companion object {
        fun factory(planId: Long) = viewModelFactory {
            initializer { PlanFormViewModel(AppGraph.database, planId) }
        }
    }
}

private val REMINDER_CHOICES = listOf(
    null to "No reminder",
    0 to "At start time",
    10 to "10 min before",
    30 to "30 min before",
    60 to "1 hour before"
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlanFormScreen(planId: Long, presetDay: Long, onDone: () -> Unit) {
    val vm: PlanFormViewModel = viewModel(
        key = "plan_$planId",
        factory = PlanFormViewModel.factory(planId)
    )
    val subjects by vm.subjects.collectAsState()
    val existing = vm.loaded
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()
    val context = LocalContext.current

    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var day by remember(existing) {
        mutableStateOf(existing?.day ?: (if (presetDay > 0) presetDay else Stats.todayEpochDay()))
    }
    var timed by remember(existing) { mutableStateOf(existing?.startMinute != null) }
    var startMinute by remember(existing) { mutableIntStateOf(existing?.startMinute ?: 18 * 60) }
    var hasEnd by remember(existing) { mutableStateOf(existing?.endMinute != null) }
    var endMinute by remember(existing) { mutableIntStateOf(existing?.endMinute ?: 19 * 60) }
    var subjectId by remember(existing) { mutableStateOf(existing?.subjectId) }
    var reminderIdx by remember(existing) {
        mutableIntStateOf(
            REMINDER_CHOICES.indexOfFirst { it.first == existing?.reminderMinutesBefore }
                .coerceAtLeast(0)
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var notifGranted by remember { mutableStateOf(Perms.hasNotifications(context)) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifGranted = Perms.hasNotifications(context) }

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
                if (existing == null) "New plan entry" else "Edit entry",
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
            label = { Text("What will you study?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        SectionLabel("Date")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(Stats.formatDayShort(day), true) { showDatePicker = true }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("Time")
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Switch(
                checked = timed,
                onCheckedChange = { timed = it },
                colors = SwitchDefaults.colors(checkedTrackColor = p.accent)
            )
            Text("Timed entry", color = p.inkSoft, fontSize = 14.sp)
        }
        if (timed) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip("Starts ${Stats.formatMinuteOfDay(startMinute)}", true) { showStartPicker = true }
                if (hasEnd) {
                    ChoiceChip("Ends ${Stats.formatMinuteOfDay(endMinute)}", true) { showEndPicker = true }
                } else {
                    ChoiceChip("+ End time", false) { hasEnd = true }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("Subject (optional)")
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceChip("None", subjectId == null) { subjectId = null }
            subjects.forEach { s ->
                val ink = subjectColor(s.colorKey, dark)
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (subjectId == s.id) ink.copy(alpha = 0.15f) else p.surfaceAlt)
                        .border(1.dp, if (subjectId == s.id) ink else p.hairline, RoundedCornerShape(20.dp))
                        .clickable { subjectId = s.id }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SubjectDot(ink, 8)
                        Text(s.name, color = p.inkSoft, fontSize = 12.sp)
                    }
                }
            }
        }

        if (timed) {
            Spacer(Modifier.height(20.dp))
            SectionLabel("Reminder")
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                REMINDER_CHOICES.chunked(3).forEach { rowChoices ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowChoices.forEachIndexed { i, choice ->
                            val globalIdx = REMINDER_CHOICES.indexOf(choice)
                            ChoiceChip(
                                choice.second,
                                reminderIdx == globalIdx,
                                Modifier.weight(1f)
                            ) { reminderIdx = globalIdx }
                        }
                        repeat(3 - rowChoices.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            if (reminderIdx > 0 && !notifGranted) {
                Spacer(Modifier.height(10.dp))
                RuledCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Notifications, null, tint = p.accentDeep)
                        Text(
                            "Allow notifications so the reminder can reach you.",
                            color = p.inkSoft,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }) { Text("Allow", color = p.accentDeep, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                val reminder = REMINDER_CHOICES[reminderIdx].first
                val trigger = if (timed && reminder != null) {
                    val start = ZonedDateTime.of(
                        java.time.LocalDate.ofEpochDay(day),
                        LocalTime.of(startMinute / 60, startMinute % 60),
                        ZoneId.systemDefault()
                    ).toInstant().toEpochMilli() - reminder * 60_000L
                    start
                } else null
                vm.save(
                    PlanEntry(
                        id = existing?.id ?: 0L,
                        title = title.trim(),
                        note = note.trim(),
                        day = day,
                        startMinute = if (timed) startMinute else null,
                        endMinute = if (timed && hasEnd) endMinute else null,
                        subjectId = subjectId,
                        reminderMinutesBefore = if (timed) reminder else null,
                        triggerAtMillis = trigger,
                        done = existing?.done ?: false,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    ),
                    onDone
                )
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = p.ink, contentColor = p.paper)
        ) {
            Text(if (existing == null) "Add to plan" else "Save changes", fontWeight = FontWeight.SemiBold)
        }

        if (existing != null) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = p.danger)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Delete entry", modifier = Modifier.padding(start = 8.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = day * 86_400_000L + 3_600_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        day = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("OK", color = p.accentDeep) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = p.inkSoft) }
            }
        ) { DatePicker(state = state) }
    }

    if (showStartPicker) {
        TimeDialog(
            initial = startMinute,
            title = "Start time",
            onPick = { startMinute = it; if (hasEnd && endMinute <= it) endMinute = it + 60 },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        TimeDialog(
            initial = endMinute,
            title = "End time",
            onPick = { endMinute = it.coerceAtLeast(startMinute) },
            onDismiss = { showEndPicker = false }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete this entry?",
            text = "\"${existing?.title ?: ""}\" will be removed from your plan.",
            onConfirm = { showDelete = false; vm.delete(onDone) },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun TimeDialog(initial: Int, title: String, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val state = rememberTimePickerState(
        initialHour = initial / 60,
        initialMinute = initial % 60,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = p.ink) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onPick(state.hour * 60 + state.minute) }) {
                Text("OK", color = p.accentDeep)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = p.inkSoft) }
        }
    )
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) p.accentSoft else p.surfaceAlt)
            .border(1.dp, if (selected) p.accent else p.hairline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) p.accentDeep else p.inkSoft,
            fontSize = 13.sp,
            style = TabularNumbers
        )
    }
}
