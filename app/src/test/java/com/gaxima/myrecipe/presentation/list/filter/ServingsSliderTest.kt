package com.gaxima.myrecipe.presentation.list.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServingsSliderTest {

    @Test
    fun `full slider range means any servings`() {
        assertNull(servingsRangeFromSlider(1, 8))
    }

    @Test
    fun `inner range is kept as is`() {
        assertEquals(2..4, servingsRangeFromSlider(2, 4))
    }

    @Test
    fun `top of the slider is open-ended`() {
        assertEquals(6..Int.MAX_VALUE, servingsRangeFromSlider(6, 8))
    }

    @Test
    fun `any servings maps to the full slider`() {
        assertEquals(1f..8f, (null as IntRange?).toSliderPosition())
    }

    @Test
    fun `open-ended range maps to the top of the slider`() {
        assertEquals(6f..8f, (6..Int.MAX_VALUE).toSliderPosition())
    }

    @Test
    fun `out of bounds ranges are clamped to the slider`() {
        assertEquals(1f..8f, (0..20).toSliderPosition())
    }
}
