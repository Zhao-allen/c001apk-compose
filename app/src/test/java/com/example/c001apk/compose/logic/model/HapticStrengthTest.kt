package com.example.c001apk.compose.logic.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticStrengthTest {

    @Test
    fun `strength options keep their display order`() {
        assertEquals(
            listOf(HapticStrength.Light, HapticStrength.Medium, HapticStrength.Strong),
            HapticStrength.options,
        )
    }

    @Test
    fun `compatibility mode swaps light and strong effects`() {
        assertEquals(2, HapticStrength.Light.predefinedEffect(compatibilityMode = false))
        assertEquals(5, HapticStrength.Strong.predefinedEffect(compatibilityMode = false))
        assertEquals(5, HapticStrength.Light.predefinedEffect(compatibilityMode = true))
        assertEquals(2, HapticStrength.Strong.predefinedEffect(compatibilityMode = true))
        assertEquals(0, HapticStrength.Medium.predefinedEffect(compatibilityMode = false))
        assertEquals(0, HapticStrength.Medium.predefinedEffect(compatibilityMode = true))

        assertTrue(HapticStrength.Light.durationMs < HapticStrength.Medium.durationMs)
        assertTrue(HapticStrength.Medium.durationMs < HapticStrength.Strong.durationMs)
    }
}
