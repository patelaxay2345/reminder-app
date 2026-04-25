package com.example.reminderapp.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class QuietHoursTest {

    private fun millisAt(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Long {
        val c = Calendar.getInstance()
        c.set(year, month, day, hour, minute, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    @Test
    fun `midnight to 10am is inside 23-10 window`() {
        val t = millisAt(2025, 0, 15, 2)
        assertTrue(QuietHours.isInQuietHours(t, 23, 10))
    }

    @Test
    fun `9am is inside 23-10 window`() {
        val t = millisAt(2025, 0, 15, 9)
        assertTrue(QuietHours.isInQuietHours(t, 23, 10))
    }

    @Test
    fun `10am is OUTSIDE 23-10 window`() {
        val t = millisAt(2025, 0, 15, 10)
        assertFalse(QuietHours.isInQuietHours(t, 23, 10))
    }

    @Test
    fun `11pm is inside 23-10 window`() {
        val t = millisAt(2025, 0, 15, 23)
        assertTrue(QuietHours.isInQuietHours(t, 23, 10))
    }

    @Test
    fun `3pm is OUTSIDE 23-10 window`() {
        val t = millisAt(2025, 0, 15, 15)
        assertFalse(QuietHours.isInQuietHours(t, 23, 10))
    }

    @Test
    fun `shift from 2am to 10am same day`() {
        val input = millisAt(2025, 0, 15, 2)
        val expected = millisAt(2025, 0, 15, 10)
        assertEquals(expected, QuietHours.shiftOutOfQuietHours(input, 23, 10))
    }

    @Test
    fun `shift from 11pm to 10am next day`() {
        val input = millisAt(2025, 0, 15, 23)
        val expected = millisAt(2025, 0, 16, 10)
        assertEquals(expected, QuietHours.shiftOutOfQuietHours(input, 23, 10))
    }

    @Test
    fun `non-wrapping window 13-17`() {
        assertTrue(QuietHours.isInQuietHours(millisAt(2025, 0, 15, 14), 13, 17))
        assertFalse(QuietHours.isInQuietHours(millisAt(2025, 0, 15, 17), 13, 17))
        assertFalse(QuietHours.isInQuietHours(millisAt(2025, 0, 15, 12), 13, 17))
    }

    @Test
    fun `time outside window is not shifted`() {
        val input = millisAt(2025, 0, 15, 14)
        assertEquals(input, QuietHours.shiftOutOfQuietHours(input, 23, 10))
    }
}
