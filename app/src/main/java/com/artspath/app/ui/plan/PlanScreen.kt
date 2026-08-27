package com.artspath.app.ui.plan

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artspath.app.core.Stats
import com.artspath.app.data.PlanRow
import com.artspath.app.ui.components.EmptyState
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.components.SubjectDot
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import com.artspath.app.ui.theme.subjectColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val RANGE_FMT = DateTimeFormatter.ofPattern("d MMM", java.util.Locale.ENGLISH)

@Composable
fun PlanScreen(onOpenPlan: (Long, Long) -> Unit) {
    val vm: PlanViewModel = viewModel(factory = PlanViewModel.factory())
    val anchor by vm.anchor.collectAsState()
    val selectedDay by vm.selectedDay.collectAsState()
    val entries by vm.entries.collectAsState()
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()

    val today = Stats.todayEpochDay()
    val monday = anchor
    val days = (0..6).map { monday.plusDays(it.toLong()) }
    val dayEntries = entries.filter { it.day == selectedDay }
    val isThisWeek = monday == LocalDate.now().with(java.time.DayOfWeek.MONDAY)

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Plan",
                    color = p.ink,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.weight(1f)
                )
                if (!isThisWeek) {
                    TextButton(onClick = { vm.thisWeek() }) {
                        Text("This week", color = p.accentDeep, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { vm.shiftWeeks(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous week", tint = p.inkSoft)
                }
                Text(
                    "${RANGE_FMT.format(monday)} – ${RANGE_FMT.format(monday.plusDays(6))}",
                    color = p.inkSoft,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TabularNumbers
                )
                IconButton(onClick = { vm.shiftWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next week", tint = p.inkSoft)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // day selector — Google-Calendar style strip
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.forEach { date ->
                val day = date.toEpochDay()
                val selected = day == selectedDay
                val isToday = day == today
                val hasEntries = entries.any { it.day == day }
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) p.ink else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { vm.selectDay(day) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                        color = if (selected) p.paper else p.inkFaint,
                        fontSize = 10.sp
                    )
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .size(30.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isToday && !selected) 1.5.dp else 0.dp,
                                color = if (isToday && !selected) p.accent else androidx.compose.ui.graphics.Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${date.dayOfMonth}",
                            color = when {
                                selected -> p.paper
                                isToday -> p.accentDeep
                                else -> p.ink
                            },
                            fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            style = TabularNumbers
                        )
                    }
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (hasEntries) p.accent else androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(Modifier.weight(1f)) {
            if (dayEntries.isEmpty()) {
                EmptyState(
                    Icons.Filled.DateRange,
                    "Nothing planned",
                    "Add a study entry for this day — optionally with a time and a reminder."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SectionLabel(Stats.formatDayLong(selectedDay), Modifier.padding(top = 8.dp))
                    }
                    items(dayEntries.size) { idx ->
                        PlanEntryCard(
                            entry = dayEntries[idx],
                            dark = dark,
                            onToggleDone = { vm.toggleDone(dayEntries[idx]) },
                            onOpen = { onOpenPlan(dayEntries[idx].id, selectedDay) }
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { onOpenPlan(-1L, selectedDay) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = p.ink,
                contentColor = p.paper
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add entry", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun PlanEntryCard(
    entry: PlanRow,
    dark: Boolean,
    onToggleDone: () -> Unit,
    onOpen: () -> Unit
) {
    val p = LocalPalette.current
    val ink = entry.subjectColor?.let { subjectColor(it, dark) } ?: p.inkFaint
    val strikeAlpha by animateFloatAsState(
        targetValue = if (entry.done) 0.5f else 1f,
        animationSpec = tween(250),
        label = "done"
    )

    RuledCard(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpen() }
    ) {
        Row(
            Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .alpha(strikeAlpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 40.dp)
                    .background(if (entry.startMinute == null) p.inkFaint else ink)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    entry.title,
                    color = p.ink,
                    fontSize = 15.sp,
                    textDecoration = if (entry.done) TextDecoration.LineThrough else null,
                    maxLines = 2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        Stats.formatTimeRange(entry.startMinute, entry.endMinute),
                        color = p.inkSoft,
                        fontSize = 12.sp,
                        style = TabularNumbers
                    )
                    entry.subjectName?.let {
                        SubjectDot(ink, 7)
                        Text(it, color = p.inkSoft, fontSize = 12.sp)
                    }
                    if (entry.reminderMinutesBefore != null) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Reminder set",
                            tint = p.accentDeep,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, if (entry.done) p.ok else p.inkFaint, CircleShape)
                    .background(if (entry.done) p.ok else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onToggleDone() },
                contentAlignment = Alignment.Center
            ) {
                if (entry.done) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
