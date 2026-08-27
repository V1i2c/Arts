package com.artspath.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.artspath.app.data.Subject
import com.artspath.app.ui.components.RuledCard
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.subjectColor

/**
 * Subject selector used at first run and later from the Errors book settings.
 * Seeded JAC subjects plus any custom subjects the student adds.
 */
@Composable
fun SubjectPicker(
    subjects: List<Subject>,
    dark: Boolean,
    onToggle: (Subject, Boolean) -> Unit,
    onAddCustom: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    var showAdd by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        subjects.chunked(2).forEach { rowSubjects ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowSubjects.forEach { subject ->
                    SubjectCell(
                        subject = subject,
                        dark = dark,
                        onToggle = { onToggle(subject, !subject.isMine) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSubjects.size == 1) Box(Modifier.weight(1f))
            }
        }

        if (showAdd) {
            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                placeholder = { Text("Subject name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { showAdd = false; customName = "" }) {
                    Text("Cancel", color = p.inkSoft)
                }
                TextButton(
                    onClick = {
                        val name = customName.trim()
                        if (name.isNotEmpty()) {
                            onAddCustom(name)
                            customName = ""
                            showAdd = false
                        }
                    }
                ) { Text("Add", color = p.accentDeep, fontWeight = FontWeight.SemiBold) }
            }
        } else {
            RuledCard(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showAdd = true }
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = p.accentDeep)
                    Text("Add a custom subject", color = p.inkSoft, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SubjectCell(
    subject: Subject,
    dark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val ink = subjectColor(subject.colorKey, dark)
    val shape = RoundedCornerShape(12.dp)
    RuledCard(
        modifier
            .clip(shape)
            .clickable { onToggle() }
            .then(
                if (subject.isMine) Modifier.border(1.5.dp, ink, shape) else Modifier
            )
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (subject.isMine) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(ink)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(Modifier.size(22.dp).clip(CircleShape).border(1.5.dp, ink, CircleShape))
            }
            Text(
                subject.name,
                color = if (subject.isMine) p.ink else p.inkSoft,
                fontSize = 14.sp,
                fontWeight = if (subject.isMine) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}
