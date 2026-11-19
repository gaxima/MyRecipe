package com.gaxima.myrecipe.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class BackStackOpsTest {

    @Test
    fun `pop removes the entry that asked for it`() {
        val stack = mutableListOf<NavKey>(RecipeList, RecipeDetail("r-1"))

        stack.popIfTop(RecipeDetail("r-1"))

        assertEquals(listOf<NavKey>(RecipeList), stack)
    }

    @Test
    fun `a second back tap during the pop animation is ignored`() {
        val detail = RecipeDetail("r-1")
        val stack = mutableListOf<NavKey>(RecipeList, detail)

        stack.popIfTop(detail)
        stack.popIfTop(detail)

        assertEquals(listOf<NavKey>(RecipeList), stack)
    }

    @Test
    fun `the root entry is never popped`() {
        val stack = mutableListOf<NavKey>(RecipeList)

        stack.popIfTop(RecipeList)

        assertEquals(listOf<NavKey>(RecipeList), stack)
    }

    @Test
    fun `opening a recipe from the list pushes its detail`() {
        val stack = mutableListOf<NavKey>(RecipeList)

        stack.openRecipe("r-1")

        assertEquals(listOf(RecipeList, RecipeDetail("r-1")), stack)
    }

    @Test
    fun `a double tap on a card opens the detail once`() {
        val stack = mutableListOf<NavKey>(RecipeList)

        stack.openRecipe("r-1")
        stack.openRecipe("r-2")

        assertEquals(listOf(RecipeList, RecipeDetail("r-1")), stack)
    }
}
