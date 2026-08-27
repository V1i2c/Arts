package com.artspath.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the JAC syllabus catalog against accidental edits or data loss.
 * Chapter counts verified against published JAC/NCERT 2026-27 syllabus sources.
 */
class SyllabusCatalogTest {

    @Test
    fun `subject names are unique`() {
        val names = SyllabusCatalog.subjects.map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun `all JAC class 12 arts subjects are present`() {
        val expected = listOf(
            "History", "Political Science", "Geography", "Economics", "Sociology",
            "Psychology", "Hindi (Core)", "English (Core)", "Sanskrit", "Home Science",
            "Philosophy", "Anthropology", "Music", "Urdu", "Mathematics (optional)"
        )
        expected.forEach { name ->
            assertTrue("missing subject: $name", SyllabusCatalog.subjects.any { it.name == name })
        }
    }

    @Test
    fun `verified chapter counts match the published syllabus`() {
        val counts = mapOf(
            "History" to 12,
            "Political Science" to 15,
            "Geography" to 22,
            "Economics" to 16,
            "Sociology" to 14,
            "Psychology" to 9,
            "Hindi (Core)" to 21,
            "English (Core)" to 19,
            "Sanskrit" to 14,
            "Home Science" to 10,
            "Mathematics (optional)" to 13
        )
        counts.forEach { (name, expected) ->
            val subject = SyllabusCatalog.subjects.first { it.name == name }
            assertEquals("$name chapter count", expected, subject.chapters.size)
        }
    }

    @Test
    fun `chapter names are unique within each subject`() {
        SyllabusCatalog.subjects.forEach { subject ->
            val names = subject.chapters.map { it.name }
            assertEquals(
                "duplicate chapters in ${subject.name}",
                names.size,
                names.distinct().size
            )
        }
    }

    @Test
    fun `every subject has a color key and an order`() {
        SyllabusCatalog.subjects.forEach { subject ->
            assertTrue(subject.colorKey.isNotBlank())
            assertTrue(subject.sortOrder > 0)
        }
    }

    @Test
    fun `chapter part labels stay grouped in book order`() {
        val polSci = SyllabusCatalog.subjects.first { it.name == "Political Science" }
        val parts = SyllabusCatalog.partsOf(polSci)
        assertEquals(listOf("Contemporary World Politics", "Politics in India since Independence"), parts)
    }
}
