package com.artspath.app.ui.errors

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artspath.app.ui.components.EmptyState
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.onboarding.OnboardingViewModel
import com.artspath.app.ui.onboarding.SubjectPicker
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.subjectColor

@Composable
fun ErrorsHomeScreen(
    onOpenSubject: (Long) -> Unit,
    onAddError: (Long, Long) -> Unit
) {
    val vm: ErrorsHomeViewModel = viewModel(factory = ErrorsHomeViewModel.factory())
    val subjects by vm.subjects.collectAsState()
    val counts by vm.counts.collectAsState()
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()
    var showManage by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Errors book",
                            color = p.ink,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            "Every mistake, filed by chapter",
                            color = p.inkSoft,
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = { showManage = true }) {
                        Icon(Icons.Filled.Settings, "Manage subjects", tint = p.inkSoft)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (subjects.isEmpty()) {
                EmptyState(
                    Icons.Filled.MenuBook,
                    "No subjects selected",
                    "Tap the gear icon above to pick your subjects."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects.size) { idx ->
                        val subject = subjects[idx]
                        val count = counts.firstOrNull { it.subjectId == subject.id }?.errorCount ?: 0
                        val ink = subjectColor(subject.colorKey, dark)
                        RuledCard(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenSubject(subject.id) }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(width = 4.dp, height = 36.dp)
                                        .background(ink)
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(subject.name, color = p.ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "$count error${if (count == 1) "" else "s"} logged",
                                        color = p.inkSoft,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, null, tint = p.inkFaint)
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onAddError(subjects.firstOrNull()?.id ?: -1L, -1L) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = p.ink,
            contentColor = p.paper
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Log error", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
        }
    }

    if (showManage) {
        ManageSubjectsDialog(onDismiss = { showManage = false })
    }
}

@Composable
private fun ManageSubjectsDialog(onDismiss: () -> Unit) {
    val vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory())
    val subjects by vm.subjects.collectAsState()
    val dark = isSystemInDarkColor()
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your subjects", color = LocalPalette.current.ink) },
        text = {
            Column {
                Text(
                    "Tick the subjects you study. This list is shared across the whole app.",
                    color = LocalPalette.current.inkSoft,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                SubjectPicker(
                    subjects = subjects,
                    dark = dark,
                    onToggle = vm::toggle,
                    onAddCustom = vm::addCustom,
                    modifier = Modifier.height(320.dp)
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Done", color = LocalPalette.current.accentDeep, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    )
}
