package com.artspath.app.ui.errors

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.artspath.app.data.Actions
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.ATTACHMENT_AUDIO
import com.artspath.app.data.ATTACHMENT_IMAGE
import com.artspath.app.data.Attachment
import com.artspath.app.data.ErrorRecord
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.components.SubjectDot
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import com.artspath.app.ui.theme.subjectColor
import com.artspath.app.util.MediaFiles
import com.artspath.app.util.Perms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PendingAttachment(
    val kind: String,
    val fileName: String,
    val durationMs: Long? = null
)

private data class ShownImage(
    val fileName: String,
    val attachment: Attachment? = null,
    val pending: PendingAttachment? = null
)

class ErrorFormViewModel(private val db: AppDatabase, private val errorId: Long) : ViewModel() {

    val subjects: StateFlow<List<com.artspath.app.data.Subject>> = db.subjectDao().observeMine()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedSubject = kotlinx.coroutines.flow.MutableStateFlow(-1L)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chapters: StateFlow<List<com.artspath.app.data.ChapterWithCount>> =
        selectedSubject.flatMapLatest { sid ->
            if (sid <= 0) kotlinx.coroutines.flow.flow { emit(emptyList<com.artspath.app.data.ChapterWithCount>()) }
            else db.chapterDao().observeWithCounts(sid)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var loadedError by mutableStateOf<ErrorRecord?>(null)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (errorId > 0) {
                val e = db.errorDao().byId(errorId)
                loadedError = e
                if (e != null) selectedSubject.value = e.subjectId
            }
        }
    }

    fun selectSubject(id: Long) {
        selectedSubject.value = id
    }

    fun addChapter(name: String, onCreated: (Long) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            val id = db.chapterDao().insert(
                com.artspath.app.data.Chapter(
                    subjectId = selectedSubject.value,
                    name = name.trim(),
                    sortOrder = 1000,
                    isCustom = true
                )
            )
            withContext(Dispatchers.Main) { onCreated(id) }
        }

    fun save(
        title: String,
        subjectId: Long,
        chapterId: Long,
        note: String,
        pending: List<PendingAttachment>,
        removedAttachmentIds: List<Long>,
        onDone: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        val existing = loadedError
        if (existing == null) {
            val id = Actions.addError(
                db,
                ErrorRecord(
                    subjectId = subjectId,
                    chapterId = chapterId,
                    title = title,
                    note = note
                )
            )
            pending.forEach {
                db.attachmentDao().insert(
                    Attachment(
                        errorId = id,
                        kind = it.kind,
                        fileName = it.fileName,
                        durationMs = it.durationMs
                    )
                )
            }
        } else {
            Actions.updateError(
                db,
                existing.copy(
                    subjectId = subjectId,
                    chapterId = chapterId,
                    title = title,
                    note = note
                )
            )
            pending.forEach {
                db.attachmentDao().insert(
                    Attachment(
                        errorId = existing.id,
                        kind = it.kind,
                        fileName = it.fileName,
                        durationMs = it.durationMs
                    )
                )
            }
            removedAttachmentIds.forEach { attId ->
                db.attachmentDao().byId(attId)?.let { att ->
                    db.attachmentDao().delete(attId)
                    MediaFiles.deleteNow(AppGraph.appContext, att.fileName)
                }
            }
        }
        withContext(Dispatchers.Main) { onDone() }
    }

    companion object {
        fun factory(errorId: Long) = viewModelFactory {
            initializer { ErrorFormViewModel(AppGraph.database, errorId) }
        }
    }
}

@Composable
fun ErrorFormScreen(
    errorId: Long,
    presetSubjectId: Long,
    presetChapterId: Long,
    onDone: () -> Unit
) {
    val vm: ErrorFormViewModel = viewModel(
        key = "error_$errorId",
        factory = ErrorFormViewModel.factory(errorId)
    )
    val subjects by vm.subjects.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val selectedSubjectId by vm.selectedSubject.collectAsState()
    val existing = vm.loadedError
    val p = LocalPalette.current
    val dark = isSystemInDarkColor()
    val context = LocalContext.current

    remember {
        if (presetSubjectId > 0 && errorId <= 0) vm.selectSubject(presetSubjectId)
        true
    }

    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var chapterId by remember(existing) {
        mutableLongStateOf(existing?.chapterId ?: if (presetChapterId > 0) presetChapterId else -1L)
    }
    var chapterQuery by remember { mutableStateOf("") }
    var showNewChapter by remember { mutableStateOf(false) }
    var newChapterName by remember { mutableStateOf("") }

    val pending = remember { mutableStateListOf<PendingAttachment>() }
    val removedIds = remember { mutableStateListOf<Long>() }
    var existingAttachments by remember(errorId) {
        mutableStateOf<List<Attachment>>(emptyList())
    }
    LaunchedEffect(errorId) {
        if (errorId > 0) {
            existingAttachments = AppGraph.database.attachmentDao().forError(errorId)
        }
    }

    // if the selected chapter doesn't belong to the current subject, clear it
    LaunchedEffect(chapters) {
        if (chapterId > 0 && chapters.none { it.id == chapterId }) chapterId = -1L
    }

    // image picker — Android Photo Picker, no storage permission needed
    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri ->
            MediaFiles.copyImage(context, uri)?.let { file ->
                pending.add(PendingAttachment(ATTACHMENT_IMAGE, file.name))
            }
        }
    }

    // microphone
    val recorder = remember { com.artspath.app.audio.AudioRecorder() }
    var recording by remember { mutableStateOf(false) }
    var amplitude by remember { mutableFloatStateOf(0f) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    DisposableEffect(Unit) {
        onDispose {
            if (recorder.isRecording) recorder.discard()
        }
    }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startRecording(context, recorder) { recording = true } }

    LaunchedEffect(recording) {
        while (recording) {
            amplitude = recorder.currentAmplitude() / 32767f
            elapsedMs = recorder.elapsedMs()
            delay(100)
        }
    }

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
                if (existing == null) "Log an error" else "Edit error",
                color = p.ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
        }

        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("What was the mistake?") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Short note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))
        SectionLabel("Subject")
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            subjects.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { s ->
                        val ink = subjectColor(s.colorKey, dark)
                        val isSelected = selectedSubjectId == s.id
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ink.copy(alpha = 0.14f) else p.surfaceAlt)
                                .border(
                                    1.dp,
                                    if (isSelected) ink else p.hairline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { vm.selectSubject(s.id) }
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SubjectDot(ink, 8)
                                Text(s.name, color = if (isSelected) p.ink else p.inkSoft, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel("Chapter")
        Spacer(Modifier.height(8.dp))
        if (selectedSubjectId <= 0) {
            Text("Pick a subject first — its chapters will appear here.", color = p.inkFaint, fontSize = 12.sp)
        } else {
            OutlinedTextField(
                value = chapterQuery,
                onValueChange = { chapterQuery = it },
                placeholder = { Text("Search chapters") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            val filtered = if (chapterQuery.isBlank()) chapters
            else chapters.filter { it.name.contains(chapterQuery.trim(), ignoreCase = true) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                filtered.take(8).forEach { chapter ->
                    val selected = chapterId == chapter.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) p.accentSoft else androidx.compose.ui.graphics.Color.Transparent)
                            .border(
                                1.dp,
                                if (selected) p.accent else p.hairline,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { chapterId = chapter.id }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check, null, tint = p.accentDeep,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(chapter.name, color = p.ink, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    }
                }
                if (filtered.size > 8) {
                    Text("…and ${filtered.size - 8} more — keep typing to narrow down", color = p.inkFaint, fontSize = 11.sp)
                }
                TextButton(onClick = { showNewChapter = true }) {
                    Icon(Icons.Filled.Add, null, tint = p.accentDeep, modifier = Modifier.size(15.dp))
                    Text("New chapter", color = p.accentDeep, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel("Screenshots")
        Spacer(Modifier.height(8.dp))
        val allImages =
            existingAttachments.filter { it.kind == ATTACHMENT_IMAGE && it.id !in removedIds }
                .map { ShownImage(it.fileName, attachment = it) } +
                pending.filter { it.kind == ATTACHMENT_IMAGE }
                    .map { ShownImage(it.fileName, pending = it) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allImages.size) { idx ->
                val item = allImages[idx]
                Box {
                    ImageThumb(
                        fileName = item.fileName,
                        onClick = {
                            val att = item.attachment
                            if (att != null) {
                                removedIds.add(att.id)
                            } else {
                                item.pending?.let { pen ->
                                    pending.removeAll { it.fileName == pen.fileName }
                                    MediaFiles.deleteNow(context, pen.fileName)
                                }
                            }
                        }
                    )
                    Icon(
                        Icons.Filled.Close, "remove",
                        tint = p.danger,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                    )
                }
            }
            item {
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, p.hairline, RoundedCornerShape(10.dp))
                        .clickable {
                            pickImages.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Add, null, tint = p.accentDeep)
                        Text("add photo", color = p.inkFaint, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel("Voice note")
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .scale(1f + amplitude * 0.35f)
                    .clip(CircleShape)
                    .background(if (recording) p.danger else p.ink)
                    .clickable {
                        if (recording) {
                            recorder.stop()?.let { (file, duration) ->
                                pending.add(
                                    PendingAttachment(ATTACHMENT_AUDIO, file.name, duration)
                                )
                            }
                            recording = false
                            amplitude = 0f
                        } else if (Perms.hasMic(context)) {
                            startRecording(context, recorder) { recording = true }
                        } else {
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (recording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (recording) "Stop recording" else "Record voice note",
                    tint = p.paper,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    if (recording) "Recording… ${formatDuration(elapsedMs)}" else "Tap to record an explanation",
                    color = if (recording) p.danger else p.inkSoft,
                    fontSize = 13.sp,
                    style = TabularNumbers
                )
                if (recording) {
                    Text("Tap again to stop and keep", color = p.inkFaint, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            existingAttachments.filter { it.kind == ATTACHMENT_AUDIO && it.id !in removedIds }
                .forEach { audio ->
                    AudioRow(
                        fileName = audio.fileName,
                        durationMs = audio.durationMs,
                        label = "Voice note",
                        onDelete = { removedIds.add(audio.id) }
                    )
                }
            pending.filter { it.kind == ATTACHMENT_AUDIO }.forEach { audio ->
                AudioRow(
                    fileName = audio.fileName,
                    durationMs = audio.durationMs,
                    label = "New voice note",
                    onDelete = {
                        pending.removeAll { it.fileName == audio.fileName }
                        MediaFiles.deleteNow(context, audio.fileName)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                vm.save(
                    title = title.trim(),
                    subjectId = vm.selectedSubject.value,
                    chapterId = chapterId,
                    note = note.trim(),
                    pending = pending.toList(),
                    removedAttachmentIds = removedIds.toList(),
                    onDone = onDone
                )
            },
            enabled = title.isNotBlank() && selectedSubjectId > 0 && chapterId > 0,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = p.ink, contentColor = p.paper)
        ) {
            Text(if (existing == null) "Save to errors book" else "Save changes", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(32.dp))
    }

    if (showNewChapter) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNewChapter = false },
            title = { Text("New chapter", color = p.ink) },
            text = {
                OutlinedTextField(
                    value = newChapterName,
                    onValueChange = { newChapterName = it },
                    placeholder = { Text("Chapter name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newChapterName.trim()
                    if (name.isNotEmpty()) {
                        vm.addChapter(name) { id -> chapterId = id }
                        newChapterName = ""
                    }
                    showNewChapter = false
                }) { Text("Add", color = p.accentDeep, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showNewChapter = false }) { Text("Cancel", color = p.inkSoft) }
            }
        )
    }
}

private fun startRecording(
    context: android.content.Context,
    recorder: com.artspath.app.audio.AudioRecorder,
    onStarted: () -> Unit
) {
    try {
        val file = MediaFiles.newAudioFile(context)
        recorder.start(context, file)
        onStarted()
    } catch (_: Exception) {
        // mic busy or unavailable — ignore; user can retry
    }
}
