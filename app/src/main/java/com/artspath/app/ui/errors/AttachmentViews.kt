package com.artspath.app.ui.errors

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artspath.app.AppGraph
import com.artspath.app.ui.theme.LocalPalette
import com.artspath.app.ui.theme.TabularNumbers
import com.artspath.app.util.MediaFiles
import java.io.File
import java.util.concurrent.TimeUnit

/** Decodes a bitmap bounded to [maxDim] on the longer side. */
private fun decodeBounded(path: String, maxDim: Int): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (
            (bounds.outWidth / sample > maxDim) || (bounds.outHeight / sample > maxDim)
        ) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(path, opts)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun rememberBitmap(path: String, maxDim: Int = 1280): androidx.compose.ui.graphics.ImageBitmap? =
    remember(path) { decodeBounded(path, maxDim)?.asImageBitmap() }

@Composable
fun ImageThumb(fileName: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val file = remember(fileName) { MediaFiles.fileFor(context, fileName) }
    val bitmap = rememberBitmap(file.absolutePath, 480)
    val p = LocalPalette.current
    Box(
        Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(p.surfaceAlt)
            .clickable { onClick() }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Error screenshot",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                "image",
                color = p.inkFaint,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/** Fullscreen pinch-zoom image viewer. */
@Composable
fun ImageViewerDialog(fileName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val path = remember(fileName) { MediaFiles.fileFor(context, fileName).absolutePath }
    val bitmap = rememberBitmap(path, 2048)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xE6100C08))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Error screenshot, pinch to zoom",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = offset.x
                            translationY = offset.y
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            ) {
                Icon(Icons.Filled.Close, "Close", tint = Color(0xFFF2E9DA))
            }
            Text(
                "Pinch to zoom · tap back to close",
                color = Color(0x99F2E9DA),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            )
        }
    }
}

/** One voice note row with play/pause and progress against the shared player. */
@Composable
fun AudioRow(
    fileName: String,
    durationMs: Long?,
    label: String = "Voice note",
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val file = remember(fileName) { MediaFiles.fileFor(context, fileName) }
    val player = AppGraph.audioPlayer
    val state by player.state.collectAsState()
    val p = LocalPalette.current

    val isThis = state.path == file.absolutePath
    val playing = isThis && state.playing
    val fraction = if (isThis && state.durationMs > 0) {
        state.progressMs.toFloat() / state.durationMs
    } else 0f

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(p.surfaceAlt)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.GraphicEq, null, tint = if (playing) p.accentDeep else p.inkFaint, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = p.ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = p.accent,
                trackColor = p.hairline
            )
        }
        Text(
            formatDuration(durationMs ?: state.durationMs.toLong()),
            color = p.inkSoft,
            fontSize = 11.sp,
            style = TabularNumbers
        )
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(p.ink)
                .clickable { player.toggle(file) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                tint = p.paper,
                modifier = Modifier.size(18.dp)
            )
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.Close, "Remove", tint = p.danger, modifier = Modifier.size(16.dp))
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
