package com.gaxima.myrecipe.presentation.common

import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ErrorMessagesTest {

    @Test
    fun `not found maps to recipe not found message`() {
        assertEquals(R.string.error_recipe_not_found, RecipeNotFoundException("x").toUserMessage())
    }

    @Test
    fun `io failure maps to network message`() {
        assertEquals(R.string.error_network, IOException().toUserMessage())
    }

    @Test
    fun `anything else maps to generic message`() {
        assertEquals(R.string.error_generic, IllegalStateException().toUserMessage())
    }
}
