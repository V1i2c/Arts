package com.artspath.app.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.artspath.app.data.AppDatabase
import com.artspath.app.data.Subject
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.components.SectionLabel
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.util.Perms
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(db: AppDatabase) : ViewModel() {
    private val subjectDao = db.subjectDao()

    val subjects: StateFlow<List<Subject>> = subjectDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(subject: Subject, mine: Boolean) = viewModelScope.launch {
        subjectDao.setMine(subject.id, mine)
    }

    fun addCustom(name: String) = viewModelScope.launch {
        val keys = listOf(
            "terracotta", "indigo", "moss", "plum", "teal", "ochre",
            "wine", "olive", "slate", "berry", "rose", "steel"
        )
        subjectDao.insert(
            Subject(
                name = name,
                colorKey = keys[(name.hashCode().let { if (it < 0) -it else it }) % keys.size],
                isMine = true,
                sortOrder = 500,
                isCustom = true
            )
        )
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val vm: OnboardingViewModel = viewModel(
        factory = viewModelFactory { initializer { OnboardingViewModel(AppGraph.database) } }
    )
    val subjects by vm.subjects.collectAsState()
    val dark = isSystemInDarkColor()
    val context = LocalContext.current
    val p = LocalPalette.current
    var step by remember { mutableIntStateOf(0) }
    var notifGranted by remember { mutableStateOf(Perms.hasNotifications(context)) }
    var micGranted by remember { mutableStateOf(Perms.hasMic(context)) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifGranted = Perms.hasNotifications(context) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { micGranted = Perms.hasMic(context) }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.paper)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        // step indicator
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .size(if (i == step) 22.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (i <= step) p.accent else p.hairline)
                )
            }
        }

        when (step) {
            0 -> {
                Spacer(Modifier.height(28.dp))
                Text(
                    "ArtsPath",
                    style = MaterialTheme.typography.displayLarge,
                    color = p.ink,
                    fontSize = 44.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Your class-12 study companion",
                    color = p.inkSoft,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(24.dp))
                IntroRow("①", "To Do", "Tasks subject-wise with optional deadlines (defaults to 11:59 PM)")
                IntroRow("②", "Plan", "A weekly calendar for the upcoming week, with reminders")
                IntroRow("③", "Errors book", "Every mistake — photo, voice note, written note — filed by chapter")
                IntroRow("④", "Dashboard", "Streaks, consistency and the full history of your work")
                Spacer(Modifier.height(20.dp))
                Text(
                    "Everything stays on your phone. No internet, no account.",
                    color = p.inkFaint,
                    fontSize = 13.sp
                )
            }

            1 -> {
                Spacer(Modifier.height(24.dp))
                SectionLabel("Which subjects are yours?")
                Text(
                    "JAC class 12 (Arts) subjects with their chapters are built in. " +
                        "Pick yours — you can change this later.",
                    color = p.inkSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 16.dp)
                )
                SubjectPicker(
                    subjects = subjects,
                    dark = dark,
                    onToggle = vm::toggle,
                    onAddCustom = vm::addCustom
                )
            }

            2 -> {
                Spacer(Modifier.height(24.dp))
                SectionLabel("Two small permissions")
                Text(
                    "Asked once, here, so nothing interrupts you later.",
                    color = p.inkSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 16.dp)
                )
                PermissionCard(
                    icon = { Icon(Icons.Filled.Notifications, null, tint = p.accentDeep) },
                    title = "Reminders",
                    body = "Notifications for your planned study entries.",
                    granted = notifGranted,
                    onRequest = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                )
                Spacer(Modifier.height(10.dp))
                PermissionCard(
                    icon = { Icon(Icons.Filled.Mic, null, tint = p.accentDeep) },
                    title = "Voice notes",
                    body = "Record explanations of your errors straight into the Errors book.",
                    granted = micGranted,
                    onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "You can skip both and grant later from the app.",
                    color = p.inkFaint,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = { step-- }) { Text("Back", color = p.inkSoft) }
            } else {
                Spacer(Modifier.size(1.dp))
            }
            OutlinedButton(
                onClick = {
                    if (step < 2) step++ else onDone()
                },
                enabled = step != 1 || subjects.any { it.isMine }
            ) {
                Text(
                    if (step < 2) "Next" else "Start studying",
                    color = p.accentDeep,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun IntroRow(num: String, title: String, body: String) {
    val p = LocalPalette.current
    Row(
        Modifier.padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(num, color = p.accentDeep, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Column {
            Text(title, color = p.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(body, color = p.inkSoft, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PermissionCard(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    val p = LocalPalette.current
    RuledCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                icon()
                Text(title, color = p.ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Text(body, color = p.inkSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            if (granted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle, null,
                        tint = p.ok, modifier = Modifier.size(16.dp)
                    )
                    Text("Granted", color = p.ok, fontSize = 13.sp)
                }
            } else {
                TextButton(onClick = onRequest, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Allow", color = p.accentDeep, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
