package com.artspath.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class StatsTest {

    // ---- 12-hour clock formatting, the "11:59 PM" deadline convention ----

    @Test
    fun `minute formatting covers midnight noon and the default deadline`() {
        assertEquals("12:00 AM", Stats.formatMinuteOfDay(0))
        assertEquals("11:59 AM", Stats.formatMinuteOfDay(11 * 60 + 59))
        assertEquals("12:00 PM", Stats.formatMinuteOfDay(12 * 60))
        assertEquals("5:30 PM", Stats.formatMinuteOfDay(17 * 60 + 30))
        assertEquals("11:59 PM", Stats.formatMinuteOfDay(Stats.DEFAULT_DEADLINE_MINUTE))
    }

    @Test
    fun `negative or overflowing minutes wrap inside the day`() {
        assertEquals("12:00 AM", Stats.formatMinuteOfDay(1440))
        assertEquals("11:59 PM", Stats.formatMinuteOfDay(-1))
    }

    // ---- streaks ----

    @Test
    fun `streak counts consecutive days ending today`() {
        val active = setOf(8L, 9L, 10L)
        assertEquals(3, Stats.computeStreak(active, today = 10L))
    }

    @Test
    fun `yesterday's streak survives until today is saved`() {
        val active = setOf(8L, 9L, 10L)
        assertEquals(3, Stats.computeStreak(active, today = 11L))
    }

    @Test
    fun `a gap breaks the streak`() {
        val active = setOf(5L, 6L, 10L)
        assertEquals(1, Stats.computeStreak(active, today = 10L))
    }

    @Test
    fun `no activity means no streak`() {
        assertEquals(0, Stats.computeStreak(emptySet(), today = 10L))
        assertEquals(0, Stats.computeStreak(setOf(1L, 2L), today = 10L))
    }

    @Test
    fun `best streak finds the longest run`() {
        assertEquals(3, Stats.bestStreak(listOf(1L, 2L, 3L, 7L, 8L)))
        assertEquals(2, Stats.bestStreak(listOf(7L, 8L, 1L, 2L)))
        assertEquals(0, Stats.bestStreak(emptyList()))
        assertEquals(4, Stats.bestStreak(listOf(4L, 3L, 2L, 1L))) // unordered input
    }

    // ---- levels (the "little ramifications") ----

    @Test
    fun `levels map streaks to titles`() {
        assertEquals("Fresh Page", Stats.levelFor(0).title)
        assertEquals("Ink Drop", Stats.levelFor(3).title)
        assertEquals("Quill", Stats.levelFor(7).title)
        assertEquals("Scribe", Stats.levelFor(14).title)
        assertEquals("Scholar", Stats.levelFor(30).title)
        assertEquals("Top of the Class", Stats.levelFor(99).title)
    }

    @Test
    fun `next level is null only at the top`() {
        assertEquals("Ink Drop", Stats.nextLevel(2)?.title)
        assertNull(Stats.nextLevel(60))
    }

    // ---- overdue deadline logic ----

    @Test
    fun `deadline with no time counts as 1159pm`() {
        // due day = today, no explicit time -> the deadline is 11:59 PM
        val dueDay = java.time.LocalDate.of(2026, 8, 27).toEpochDay()
        assertFalse(Stats.isOverdue(dueDay, null, LocalDateTime.of(2026, 8, 27, 23, 59)))
        assertTrue(Stats.isOverdue(dueDay, null, LocalDateTime.of(2026, 8, 27, 23, 59, 1)))
    }

    @Test
    fun `millisUntil is positive before and negative after the target`() {
        val dueDay = java.time.LocalDate.of(2026, 8, 27).toEpochDay()
        val before = LocalDateTime.of(2026, 8, 27, 9, 0)
        val at = LocalDateTime.of(2026, 8, 27, 10, 0)
        assertEquals(3_600_000L, Stats.millisUntil(dueDay, 10 * 60, before))
        assertTrue(Stats.millisUntil(dueDay, 10 * 60, at.plusMinutes(1)) < 0)
    }

    // ---- date labels ----

    @Test
    fun `day labels render in full and short forms`() {
        val day = java.time.LocalDate.of(2026, 8, 27).toEpochDay()
        assertEquals("Thursday, 27 August 2026", Stats.formatDayLong(day))
        assertEquals("Thu, 27 Aug", Stats.formatDayMedium(day))
        assertEquals("Thu 27", Stats.formatDayShort(day))
        assertEquals("27 Aug", Stats.formatMonthDay(day))
    }

    @Test
    fun `time range covers all-day and single-time cases`() {
        assertEquals("All day", Stats.formatTimeRange(null, null))
        assertEquals("5:00 PM", Stats.formatTimeRange(17 * 60, null))
        assertEquals("5:00 PM – 6:30 PM", Stats.formatTimeRange(17 * 60, 18 * 60 + 30))
    }
}
