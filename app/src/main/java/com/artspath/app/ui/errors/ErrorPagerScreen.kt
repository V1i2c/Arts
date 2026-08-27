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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artspath.app.AppGraph
import com.artspath.app.core.Stats
import com.artspath.app.data.Attachment
import com.artspath.app.data.ATTACHMENT_AUDIO
import com.artspath.app.data.ATTACHMENT_IMAGE
import com.artspath.app.data.ErrorRecord
import com.artspath.app.ui.components.ConfirmDialog
import com.artspath.app.ui.components.EmptyState
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val REVISED_FMT = DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH)

/**
 * Error pager for one chapter. Bounded: swiping stops at the first/last error of
 * this chapter — it never rolls over into another chapter's errors.
 */
@Composable
fun ErrorPagerScreen(
    subjectId: Long,
    chapterId: Long,
    onBack: () -> Unit,
    onEditError: (Long) -> Unit
) {
    val vm: PagerViewModel = viewModel(
        key = "pager_$chapterId",
        factory = PagerViewModel.factory(chapterId)
    )
    val errors by vm.errors.collectAsState()
    val chapter by vm.chapterState
    val p = LocalPalette.current
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { errors.size })
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // keep the page in range after deletions
    LaunchedEffect(errors.size) {
        if (errors.isNotEmpty() && pagerState.currentPage >= errors.size) {
            pagerState.scrollToPage(errors.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = p.ink)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    chapter?.name ?: "Chapter",
                    color = p.ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1
                )
                Text(
                    if (errors.isEmpty()) "no errors yet" else "swipe within this chapter",
                    color = p.inkFaint,
                    fontSize = 11.sp
                )
            }
            Text(
                if (errors.isEmpty()) "–" else "${pagerState.currentPage + 1} / ${errors.size}",
                color = p.inkSoft,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                style = TabularNumbers,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        if (errors.isEmpty()) {
            EmptyState(
                Icons.Filled.MenuBook,
                "No errors in this chapter",
                "When you make a mistake here, log it with a photo, a voice note or text."
            )
        } else {
            Box(Modifier.weight(1f)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val error = errors.getOrNull(page)
                    if (error == null) {
                        Spacer(Modifier.fillMaxSize())
                    } else {
                        ErrorPage(
                            error = error,
                            onRevise = { vm.revise(error) },
                            onEdit = { onEditError(error.id) },
                            onDelete = { vm.delete(error) }
                        )
                    }
                }
            }

            // bounded prev/next controls
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(16.dp))
                    Text("Prev", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    enabled = pagerState.currentPage < errors.size - 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next", modifier = Modifier.padding(end = 4.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorPage(
    error: ErrorRecord,
    onRevise: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = LocalPalette.current
    var showDelete by remember { mutableStateOf(false) }
    var viewerImage by remember { mutableStateOf<String?>(null) }

    val attachments by remember(error.id) {
        AppGraph.database.attachmentDao().observeForError(error.id)
    }.collectAsState(initial = emptyList())

    val images = attachments.filter { it.kind == ATTACHMENT_IMAGE }
    val audios = attachments.filter { it.kind == ATTACHMENT_AUDIO }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        RuledCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        error.title,
                        color = p.ink,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, "Edit", tint = p.inkSoft, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = p.danger, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    "logged ${REVISED_FMT.format(Instant.ofEpochMilli(error.createdAt).atZone(ZoneId.systemDefault()).toLocalDate())}" +
                        (error.lastRevisedAt?.let {
                            " · revised ${error.revisionCount}×"
                        } ?: ""),
                    color = p.inkFaint,
                    fontSize = 11.sp
                )
                if (error.note.isNotBlank()) {
                    Text(
                        error.note,
                        color = p.ink,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        if (images.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images.size) { idx ->
                    ImageThumb(fileName = images[idx].fileName) { viewerImage = images[idx].fileName }
                }
            }
        }

        if (audios.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                audios.forEachIndexed { idx, audio ->
                    AudioRow(
                        fileName = audio.fileName,
                        durationMs = audio.durationMs,
                        label = "Voice note ${idx + 1}"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(p.accentSoft)
                .clickable { onRevise() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Check, null, tint = p.accentDeep)
            Column(Modifier.weight(1f)) {
                Text(
                    "Mark as revised",
                    color = p.accentDeep,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    error.lastRevisedAt?.let {
                        "last revised ${REVISED_FMT.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())}"
                    } ?: "not revised yet — revise to grow your streak",
                    color = p.inkSoft,
                    fontSize = 11.sp
                )
            }
            Text(
                "${error.revisionCount}×",
                color = p.accentDeep,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                style = TabularNumbers
            )
        }

        Spacer(Modifier.height(28.dp))
    }

    viewerImage?.let { name ->
        ImageViewerDialog(fileName = name, onDismiss = { viewerImage = null })
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete this error?",
            text = "\"${error.title}\" and its ${attachments.size} attachment(s) will be removed.",
            onConfirm = { showDelete = false; onDelete() },
            onDismiss = { showDelete = false }
        )
    }
}
