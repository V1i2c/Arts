package com.artspath.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artspath.app.ui.theme.LocalPalette

/** Small-caps section label with the marigold margin-line motif under it. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Column(modifier = modifier) {
        Text(
            text = text.uppercase(),
            color = p.inkSoft,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Box(
            Modifier
                .padding(top = 4.dp)
                .size(width = 28.dp, height = 3.dp)
                .background(p.accent, RoundedCornerShape(2.dp))
        )
    }
}

/** Card on paper: hairline border, barely-raised surface — no drop shadows. */
@Composable
fun RuledCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val p = LocalPalette.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(p.surfaceRaised)
            .border(1.dp, p.hairline, RoundedCornerShape(14.dp))
            .animateContentSize()
    ) { content() }
}

@Composable
fun SubjectDot(color: Color, size: Int = 10) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = p.inkFaint, modifier = Modifier.size(40.dp))
        Text(title, color = p.ink, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            subtitle,
            color = p.inkSoft,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Delete confirmation — every destructive action in the app goes through this. */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val p = LocalPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = p.ink) },
        text = { Text(text, color = p.inkSoft) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = p.danger, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = p.inkSoft) } },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/** Animated progress ring used on the dashboard. */
@Composable
fun ProgressRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color,
    track: Color,
    strokeWidth: Float = 10f
) {
    val animated = androidx.compose.animation.core.animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(700),
        label = "ring"
    )
    Canvas(modifier) {
        val inset = strokeWidth / 2 + 2f
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(this.size.width - inset * 2, this.size.height - inset * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animated.value,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(this.size.width - inset * 2, this.size.height - inset * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/** Row of tiny stats: value over label. */
@Composable
fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = p.ink,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            style = com.artspath.app.ui.theme.TabularNumbers
        )
        Text(label, color = p.inkSoft, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}
