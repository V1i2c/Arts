package com.artspath.app.ui.errors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.theme.LocalPalette

@Composable
fun ChaptersScreen(
    subjectId: Long,
    onBack: () -> Unit,
    onOpenChapter: (Long) -> Unit,
    onAddError: (Long, Long) -> Unit
) {
    val vm: ChaptersViewModel = viewModel(
        key = "chapters_$subjectId",
        factory = ChaptersViewModel.factory(subjectId)
    )
    val subject by vm.subject.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val p = LocalPalette.current
    var showAddChapter by remember { mutableStateOf(false) }
    var newChapter by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = p.ink)
                    }
                    Column {
                        Text(
                            subject?.name ?: "",
                            color = p.ink,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            "${chapters.size} chapters · pick one to browse its errors",
                            color = p.inkSoft,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showAddChapter = true }) {
                    Icon(Icons.Filled.Add, null, tint = p.accentDeep, modifier = Modifier.size(16.dp))
                    Text(
                        "Add a chapter",
                        color = p.accentDeep,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            if (chapters.isEmpty()) {
                EmptyState(
                    Icons.Filled.MenuBook,
                    "No chapters yet",
                    "Add the chapters of your textbook, then file errors under them."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    var lastPart: String? = null
                    chapters.forEach { chapter ->
                        if (chapter.part != null && chapter.part != lastPart) {
                            lastPart = chapter.part
                            item {
                                SectionLabel(chapter.part!!, Modifier.padding(top = 12.dp, bottom = 4.dp))
                            }
                        }
                        item {
                            ChapterRow(
                                name = chapter.name,
                                errorCount = chapter.errorCount,
                                revisedCount = chapter.revisedCount,
                                onClick = { onOpenChapter(chapter.id) }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onAddError(subjectId, -1L) },
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

    if (showAddChapter) {
        AlertDialog(
            onDismissRequest = { showAddChapter = false },
            title = { Text("Add chapter", color = p.ink) },
            text = {
                OutlinedTextField(
                    value = newChapter,
                    onValueChange = { newChapter = it },
                    placeholder = { Text("Chapter name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newChapter.isNotBlank()) {
                        vm.addChapter(newChapter)
                        newChapter = ""
                    }
                    showAddChapter = false
                }) { Text("Add", color = p.accentDeep, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapter = false }) { Text("Cancel", color = p.inkSoft) }
            }
        )
    }
}

@Composable
private fun ChapterRow(
    name: String,
    errorCount: Int,
    revisedCount: Int,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    RuledCard(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    name,
                    color = p.ink,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Icon(Icons.Filled.ChevronRight, null, tint = p.inkFaint)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    "$errorCount logged · $revisedCount revised",
                    color = p.inkSoft,
                    fontSize = 11.sp
                )
                if (errorCount > 0) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(p.surfaceAlt)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(revisedCount.toFloat() / errorCount)
                                .height(4.dp)
                                .background(p.ok)
                        )
                    }
                }
            }
        }
    }
}
