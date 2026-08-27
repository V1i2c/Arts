package com.artspath.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Live device clock — recomposes on a tick so the dashboard clock, "today"
 * boundaries and overdue states stay in sync with the system time (Android 16
 * uses the same SystemClock the LocalDateTime here reads from).
 */
@Composable
fun rememberNow(tickMs: Long = 1_000): State<LocalDateTime> {
    val now = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now.value = LocalDateTime.now()
            delay(tickMs)
        }
    }
    return now
}
