package com.dumpphone.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyguardGeometryTest {
    @Test fun `native Pixel display aligns with the CAD opening centers and sizes`() {
        val geometry = keyguardGeometry(1344f, 2992f)
        // Independent reference coordinates from the CAD's centered display projection.
        val expectedX = listOf(193.37f, 672f, 1150.63f)
        val expectedDiameter = listOf(344.62f, 459.49f, 344.62f)
        geometry.buttons.forEachIndexed { i, button ->
            assertEquals(expectedX[i], button.centerX, 0.02f)
            assertEquals(2671.36f, button.centerY, 0.02f)
            assertEquals(expectedDiameter[i], button.diameter, 0.02f)
        }
        assertEquals(684.36f, geometry.coveredHeight, 0.02f)
    }

    @Test fun `lower resolution and Android density changes preserve physical alignment`() {
        val native = keyguardGeometry(1344f, 2992f)
        // 1008 × 2244 is the lower-resolution Pixel mode; other scales represent dp
        // bounds at different Display size settings. None may add a fixed dp inset.
        for (scale in listOf(0.75f, 1f / 3f, 1f / 3.5f)) {
            val scaled = keyguardGeometry(1344f * scale, 2992f * scale)
            assertEquals(native.coveredHeight * scale, scaled.coveredHeight, 0.001f)
            native.buttons.zip(scaled.buttons).forEach { (a, b) ->
                assertEquals(a.centerX * scale, b.centerX, 0.001f)
                assertEquals(a.centerY * scale, b.centerY, 0.001f)
                assertEquals(a.diameter * scale, b.diameter, 0.001f)
            }
        }
    }

    @Test fun `controls fit within the display and leave the upper content uncovered`() {
        val geometry = keyguardGeometry(1344f, 2992f)
        val guardTop = 2992f - geometry.coveredHeight
        geometry.buttons.forEach { button ->
            val radius = button.diameter / 2
            assertTrue(button.centerX - radius >= 0f)
            assertTrue(button.centerX + radius <= 1344f)
            assertTrue(button.centerY - radius > guardTop)
            assertTrue(button.centerY + radius < 2992f)
        }
        geometry.buttons.zipWithNext().forEach { (left, right) ->
            assertTrue(left.centerX + left.diameter / 2 < right.centerX - right.diameter / 2)
        }
    }
}
