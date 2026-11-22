package com.gaxima.myrecipe.presentation.list.filter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val applied = mutableListOf<RecipeSearchParams>()

    private fun setContent(initial: RecipeSearchParams = RecipeSearchParams()) {
        composeRule.setContent {
            var draft by remember { mutableStateOf(initial) }
            MyRecipeTheme(dynamicColor = false) {
                FilterSheetContent(
                    draft = draft,
                    onDraftChange = { draft = it },
                    onApply = { applied += it },
                    onReset = { draft = RecipeSearchParams(query = draft.query) },
                )
            }
        }
    }

    private fun addIngredient(fieldLabel: String, name: String) {
        val field = composeRule.onNode(hasText(fieldLabel) and hasSetTextAction())
        field.performScrollTo()
        field.performTextInput(name)
        field.performImeAction()
    }

    @Test
    fun applyReturnsEditedParams() {
        setContent()

        composeRule.onNodeWithText("Vegetarian only").performClick()
        composeRule.onNodeWithText("Apply").performScrollTo().performClick()

        assertEquals(true, applied.single().vegetarianOnly)
    }

    @Test
    fun vegetarianRowIsAtLeastTheMinimumTouchTargetHeight() {
        setContent()

        composeRule.onNodeWithText("Vegetarian only").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun typingAnIngredientAddsANormalisedChip() {
        setContent()

        addIngredient("Must include", "Rice ")
        composeRule.onNodeWithText("Apply").performScrollTo().performClick()

        composeRule.onNodeWithText("rice").assertExists()
        assertEquals(setOf("rice"), applied.single().includeIngredients)
    }

    @Test
    fun ingredientTypedWithoutPressingDoneIsAppliedToo() {
        setContent()

        val field = composeRule.onNode(hasText("Must include") and hasSetTextAction())
        field.performScrollTo()
        field.performTextInput("Garlic")
        composeRule.onNodeWithText("Apply").performScrollTo().performClick()

        assertEquals(setOf("garlic"), applied.single().includeIngredients)
    }

    @Test
    fun excludingAnIncludedIngredientMovesIt() {
        setContent(RecipeSearchParams(includeIngredients = setOf("rice")))

        addIngredient("Must not include", "rice")
        composeRule.onNodeWithText("Apply").performScrollTo().performClick()

        assertEquals(emptySet<String>(), applied.single().includeIngredients)
        assertEquals(setOf("rice"), applied.single().excludeIngredients)
    }

    @Test
    fun resetClearsFiltersButKeepsQuery() {
        setContent(RecipeSearchParams(query = "pasta", vegetarianOnly = true, includeIngredients = setOf("rice")))

        composeRule.onNodeWithText("Reset").performScrollTo().performClick()
        composeRule.onNodeWithText("Apply").performScrollTo().performClick()

        assertEquals(RecipeSearchParams(query = "pasta"), applied.single())
    }
}
