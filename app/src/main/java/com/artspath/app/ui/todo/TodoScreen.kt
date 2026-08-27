package com.artspath.app.ui.todo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artspath.app.core.Stats
import com.artspath.app.data.TaskRow
import com.artspath.app.ui.components.EmptyState
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.components.SubjectDot
import com.artspath.app.ui.components.rememberNow
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import com.artspath.app.ui.theme.subjectColor
import kotlinx.coroutines.delay

@Composable
fun TodoScreen(onOpenTask: (Long) -> Unit) {
    val vm: TodoViewModel = viewModel(factory = TodoViewModel.factory())
    val tasks by vm.tasks.collectAsState()
    val subjects by vm.subjects.collectAsState()
    val tab by vm.tab.collectAsState()
    val filter by vm.subjectFilter.collectAsState()
    val now by rememberNow()
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()

    LaunchedEffect(now.toLocalDate()) {
        vm.setToday(now.toLocalDate().toEpochDay())
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(
                "To Do",
                color = p.ink,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(
                Stats.formatDayMedium(now.toLocalDate().toEpochDay()),
                color = p.inkSoft,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(14.dp))
        }

        // tab chips
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoTab.entries.forEach { t ->
                val selected = tab == t
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) p.ink else p.surfaceAlt)
                        .border(1.dp, if (selected) p.ink else p.hairline, RoundedCornerShape(20.dp))
                        .clickable { vm.tab.value = t }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        t.label,
                        color = if (selected) p.paper else p.inkSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // subject filter chips
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("All subjects", filter == null) { vm.setFilter(null) }
                subjects.forEach { s ->
                    val ink = subjectColor(s.colorKey, dark)
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (filter == s.id) ink.copy(alpha = 0.15f) else p.surfaceAlt)
                            .border(
                                1.dp,
                                if (filter == s.id) ink else p.hairline,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { vm.setFilter(if (filter == s.id) null else s.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SubjectDot(ink, 8)
                        Text(
                            s.name,
                            color = if (filter == s.id) p.ink else p.inkSoft,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(Modifier.weight(1f)) {
            if (tasks.isEmpty()) {
                EmptyState(
                    Icons.Filled.Inbox,
                    when (tab) {
                        TodoTab.TODAY -> "Nothing due today"
                        TodoTab.UPCOMING -> "Nothing upcoming"
                        TodoTab.ALL -> "No open tasks"
                    },
                    if (tab == TodoTab.TODAY)
                        "Tap the button below to add a task — subject-wise, with an optional deadline."
                    else "Tasks you add with dates will show up here."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val grouped = groupTasks(tasks, tab, now.toLocalDate().toEpochDay())
                    grouped.forEach { (label, rows) ->
                        item {
                            SectionLabel(label, Modifier.padding(top = 10.dp, bottom = 2.dp))
                        }
                        items(rows.size) { idx ->
                            val row = rows[idx]
                            TaskItem(
                                row = row,
                                today = now.toLocalDate().toEpochDay(),
                                nowText = now,
                                onComplete = { vm.complete(row) },
                                onOpen = { onOpenTask(row.id) }
                            )
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { onOpenTask(-1L) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = p.ink,
                contentColor = p.paper
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("New task", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

private fun groupTasks(
    tasks: List<TaskRow>,
    tab: TodoTab,
    today: Long
): List<Pair<String, List<TaskRow>>> = when (tab) {
    TodoTab.TODAY -> {
        val overdue = tasks.filter { (it.dueDay ?: today) < today }
        val todayRows = tasks.filter { it.dueDay == today }
        buildList {
            if (overdue.isNotEmpty()) add("Overdue" to overdue)
            add("Today" to todayRows)
        }
    }
    TodoTab.UPCOMING -> tasks.groupBy { it.dueDay ?: today }
        .toSortedMap()
        .map { (day, rows) -> Stats.formatDayMedium(day) to rows }
    TodoTab.ALL -> tasks.groupBy { it.subjectName ?: "No subject" }
        .map { (name, rows) -> name to rows }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) p.accentSoft else p.surfaceAlt)
            .border(1.dp, if (selected) p.accent else p.hairline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(label, color = if (selected) p.accentDeep else p.inkSoft, fontSize = 12.sp)
    }
}

@Composable
private fun TaskItem(
    row: TaskRow,
    today: Long,
    nowText: java.time.LocalDateTime,
    onComplete: () -> Unit,
    onOpen: () -> Unit
) {
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()
    val ink = row.subjectColor?.let { subjectColor(it, dark) } ?: p.inkFaint

    var checking by remember { mutableStateOf(false) }
    val checkScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (checking) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(220),
        label = "check"
    )
    LaunchedEffect(checking) {
        if (checking) {
            delay(260)
            onComplete()
        }
    }

    val overdue = row.dueDay?.let { Stats.isOverdue(it, row.dueMinute ?: Stats.DEFAULT_DEADLINE_MINUTE, nowText) } ?: false
    val dueText = when {
        row.dueDay == null -> ""
        row.dueDay == today -> "Today" + (row.dueMinute?.let { " · ${Stats.formatMinuteOfDay(it)}" } ?: "")
        else -> Stats.formatDayShort(row.dueDay) + (row.dueMinute?.let { " · ${Stats.formatMinuteOfDay(it)}" } ?: "")
    }

    RuledCard(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpen() }
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // marigold margin line
            Box(
                Modifier
                    .size(width = 3.dp, height = 34.dp)
                    .background(ink)
            )
            Column(Modifier.weight(1f)) {
                Text(row.title, color = p.ink, fontSize = 15.sp, maxLines = 2)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SubjectDot(ink, 7)
                    Text(
                        row.subjectName ?: "No subject",
                        color = p.inkSoft,
                        fontSize = 12.sp
                    )
                    if (dueText.isNotEmpty()) {
                        Text("·", color = p.inkFaint)
                        Text(
                            dueText,
                            color = if (overdue) p.danger else p.inkSoft,
                            fontSize = 12.sp,
                            style = TabularNumbers
                        )
                    }
                }
            }
            // check circle
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, if (checkScale > 0.1f) p.accent else p.inkFaint, CircleShape)
                    .background(if (checking) p.accent else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { checking = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Complete task",
                    tint = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                    modifier = Modifier
                        .size(16.dp)
                        .scale(checkScale)
                )
            }
        }
    }
}
