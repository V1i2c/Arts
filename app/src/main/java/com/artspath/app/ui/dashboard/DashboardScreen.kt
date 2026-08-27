package com.artspath.app.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artspath.app.core.Stats
import com.artspath.app.data.ActivityEvent
import com.artspath.app.ui.components.ProgressRing
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.components.StatCell
import com.artspath.app.ui.components.rememberNow
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CLOCK_FMT = DateTimeFormatter.ofPattern("h:mm:ss a", java.util.Locale.ENGLISH)
private val EVENT_TIME = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)

@Composable
fun DashboardScreen() {
    val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.factory())
    val ui by vm.ui.collectAsState()
    val now by rememberNow()
    val p = LocalPalette.current

    // keep the view model's "today" in sync with the live device clock (date rollover)
    LaunchedEffect(now.toLocalDate()) {
        vm.setDay(now.toLocalDate().toEpochDay())
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header(now) }

        item { StreakCard(ui) }

        item { TodayCard(ui) }

        item { SectionLabel("Last 30 days", Modifier.padding(top = 8.dp)) }
        item { ConsistencyStrip(ui) }

        item {
            RuledCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCell("${ui.totalDone}", "tasks done", Modifier.weight(1f))
                    StatCell("${ui.totalErrors}", "errors logged", Modifier.weight(1f))
                    StatCell("${ui.totalRevisions}", "revisions", Modifier.weight(1f))
                    StatCell("${ui.best}", "best streak", Modifier.weight(1f))
                }
            }
        }

        item { SectionLabel("History", Modifier.padding(top = 8.dp)) }

        val grouped = ui.recent.groupBy { it.day }.entries.sortedByDescending { it.key }
        if (grouped.isEmpty()) {
            item {
                com.artspath.app.ui.components.EmptyState(
                    Icons.Filled.History,
                    "Nothing yet",
                    "Complete a task or revise an error — your history starts today."
                )
            }
        } else {
            grouped.forEach { (day, events) ->
                item {
                    Text(
                        dayLabel(day, ui.today),
                        color = p.inkSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    )
                }
                items(events.size) { idx ->
                    EventRow(events[idx])
                }
            }
        }
    }
}

@Composable
private fun Header(now: LocalDateTime) {
    val p = LocalPalette.current
    val hour = now.hour
    val greeting = when {
        hour < 5 -> "Late night — sleep matters too"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
    Column {
        Text(
            "ARTSPATH · JAC CLASS 12 ARTS",
            color = p.inkFaint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )
        Text(
            now.format(CLOCK_FMT),
            color = p.ink,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            style = TabularNumbers,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "${Stats.formatDayLong(now.toLocalDate().toEpochDay())} · $greeting",
            color = p.inkSoft,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun StreakCard(ui: DashboardUi) {
    val p = LocalPalette.current
    val pulse = rememberInfiniteTransition(label = "flame")
    val flameScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "flameScale"
    )
    RuledCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = if (ui.streak > 0) p.accent else p.inkFaint,
                modifier = Modifier
                    .size(44.dp)
                    .scale(if (ui.streak > 0) flameScale else 1f)
            )
            Column(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = ui.streak,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 2 } + fadeOut())
                    },
                    label = "streakCount",
                    modifier = Modifier
                ) { count ->
                    Text(
                        "$count day${if (count == 1) "" else "s"}",
                        color = p.ink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        style = TabularNumbers
                    )
                }
                Text(
                    ui.level.title,
                    color = p.accentDeep,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                val nextHint = ui.nextLevel
                if (nextHint != null) {
                    val daysTo = nextHint.threshold - ui.streak
                    Text(
                        "$daysTo more day${if (daysTo == 1) "" else "s"} to ${nextHint.title} · best ${ui.best}",
                        color = p.inkSoft,
                        fontSize = 12.sp
                    )
                } else {
                    Text("Top level reached · best ${ui.best}", color = p.inkSoft, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TodayCard(ui: DashboardUi) {
    val p = LocalPalette.current
    val total = ui.doneToday + ui.dueToday + ui.overdue
    val fraction = if (total == 0) 0f else ui.doneToday.toFloat() / total
    RuledCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(
                    fraction = fraction,
                    color = p.accent,
                    track = p.hairline,
                    modifier = Modifier.size(84.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${ui.doneToday}/$total",
                        color = p.ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        style = TabularNumbers
                    )
                    Text("today", color = p.inkSoft, fontSize = 10.sp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TodayRow("Due today", "${ui.dueToday}", p.ink)
                TodayRow("Overdue", "${ui.overdue}", if (ui.overdue > 0) p.danger else p.ink)
                TodayRow("Error revisions today", "${ui.revisedToday}", p.ok)
            }
        }
    }
}

@Composable
private fun TodayRow(label: String, value: String, valueColor: Color) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = p.inkSoft, fontSize = 13.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, style = TabularNumbers)
    }
}

@Composable
private fun ConsistencyStrip(ui: DashboardUi) {
    val p = LocalPalette.current
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(80)
        appeared = true
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ui.strip.forEachIndexed { index, stat ->
            val scale by animateFloatAsState(
                targetValue = if (appeared) 1f else 0f,
                animationSpec = tween(260, delayMillis = index * 18),
                label = "cell$index"
            )
            val count = stat?.total ?: 0
            val color = when {
                count == 0 -> p.surfaceAlt
                count == 1 -> p.accent.copy(alpha = 0.35f)
                count == 2 -> p.accent.copy(alpha = 0.65f)
                else -> p.accent
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(28.dp)
                    .scale(scale)
                    .background(color, RoundedCornerShape(3.dp))
                    .border(0.5.dp, p.hairline, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun EventRow(event: ActivityEvent) {
    val p = LocalPalette.current
    val (icon, tint) = eventVisual(event.type)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            Text(event.label.ifBlank { event.type }, color = p.ink, fontSize = 14.sp, maxLines = 1)
            Text(typeLabel(event.type), color = p.inkFaint, fontSize = 11.sp)
        }
        Text(
            EVENT_TIME.format(Instant.ofEpochMilli(event.ts).atZone(ZoneId.systemDefault()).toLocalTime()),
            color = p.inkSoft,
            fontSize = 11.sp,
            style = TabularNumbers
        )
    }
}

@Composable
private fun eventVisual(type: String): Pair<ImageVector, Color> {
    val p = LocalPalette.current
    return when (type) {
        "TASK_COMPLETED" -> Icons.Filled.CheckCircle to p.accentDeep
        "TASK_ADDED" -> Icons.Filled.Add to p.inkFaint
        "ERROR_ADDED" -> Icons.Filled.MenuBook to p.danger
        "ERROR_REVISED" -> Icons.Filled.Refresh to p.ok
        else -> Icons.Filled.History to p.inkFaint
    }
}

@Composable
private fun typeLabel(type: String): String = when (type) {
    "TASK_COMPLETED" -> "Task completed"
    "TASK_ADDED" -> "Task added"
    "ERROR_ADDED" -> "Error logged"
    "ERROR_REVISED" -> "Error revised"
    else -> type
}

private fun dayLabel(day: Long, today: Long): String = when (day) {
    today -> "Today"
    today - 1 -> "Yesterday"
    else -> Stats.formatDayMedium(day)
}
