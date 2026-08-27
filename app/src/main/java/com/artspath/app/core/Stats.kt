package com.artspath.app.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure date/streak/format logic. No Android imports so it is unit-testable on the JVM.
 * All "days" are LocalDate.toEpochDay() values in the device's time zone.
 */
object Stats {

    private val TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val DAY_LONG = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
    private val DAY_MED = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
    private val DAY_SHORT = DateTimeFormatter.ofPattern("EEE d", Locale.ENGLISH)
    private val MONTH_DAY = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

    fun todayEpochDay(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zone).toEpochDay()

    fun dateOf(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun formatDayLong(epochDay: Long): String = DAY_LONG.format(dateOf(epochDay))
    fun formatDayMedium(epochDay: Long): String = DAY_MED.format(dateOf(epochDay))
    fun formatDayShort(epochDay: Long): String = DAY_SHORT.format(dateOf(epochDay))
    fun formatMonthDay(epochDay: Long): String = MONTH_DAY.format(dateOf(epochDay))

    /** 23 * 60 + 59 -> "11:59 PM" */
    fun formatMinuteOfDay(minute: Int): String {
        val m = ((minute % 1440) + 1440) % 1440
        return TIME_FMT.format(LocalDateTime.of(2000, 1, 1, m / 60, m % 60))
    }

    /** The default deadline shown when a date is set but no time: 11:59 PM. */
    const val DEFAULT_DEADLINE_MINUTE: Int = 23 * 60 + 59

    fun formatTimeRange(start: Int?, end: Int?): String = when {
        start == null -> "All day"
        end == null -> formatMinuteOfDay(start)
        else -> formatMinuteOfDay(start) + " – " + formatMinuteOfDay(end)
    }

    /** True if the task's deadline has already passed (only meaningful for pending tasks). */
    fun isOverdue(dueDay: Long, dueMinute: Int?, now: LocalDateTime): Boolean {
        val minute = dueMinute ?: DEFAULT_DEADLINE_MINUTE
        val deadline = LocalDateTime.of(LocalDate.ofEpochDay(dueDay), java.time.LocalTime.of(minute / 60, minute % 60))
        return now.isAfter(deadline)
    }

    /**
     * Current streak: consecutive active days ending today.
     * If today isn't active yet, the streak from yesterday still counts (it can still be saved today).
     */
    fun computeStreak(activeDays: Set<Long>, today: Long): Int {
        var day = today
        if (day !in activeDays) day--
        var streak = 0
        while (day in activeDays) {
            streak++
            day--
        }
        return streak
    }

    /** Longest run of consecutive active days ever. */
    fun bestStreak(activeDays: Collection<Long>): Int {
        val sorted = activeDays.distinct().sorted()
        var best = 0
        var run = 0
        var previous: Long? = null
        for (d in sorted) {
            run = if (previous != null && d == previous + 1) run + 1 else 1
            if (run > best) best = run
            previous = d
        }
        return best
    }

    data class Level(val title: String, val threshold: Int)

    val levels: List<Level> = listOf(
        Level("Fresh Page", 0),
        Level("Ink Drop", 3),
        Level("Quill", 7),
        Level("Scribe", 14),
        Level("Scholar", 30),
        Level("Top of the Class", 60)
    )

    fun levelFor(streakDays: Int): Level =
        levels.last { streakDays >= it.threshold }

    /** Next level after the given streak, if any. */
    fun nextLevel(streakDays: Int): Level? = levels.firstOrNull { it.threshold > streakDays }

    /** Millis until the given day+minute (device zone). Negative if in the past. */
    fun millisUntil(epochDay: Long, minute: Int, now: LocalDateTime = LocalDateTime.now()): Long {
        val target = LocalDateTime.of(
            LocalDate.ofEpochDay(epochDay),
            java.time.LocalTime.of(minute / 60, minute % 60)
        )
        return java.time.Duration.between(now, target).toMillis()
    }
}
