package com.gaxima.myrecipe.presentation.detail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeDetailContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<RecipeDetailAction>()
    private var backClicks = 0
    private val recipe = PreviewData.vegetarianRecipe

    private fun setContent(state: RecipeDetailUiState) {
        composeRule.setContent {
            MyRecipeTheme(dynamicColor = false) {
                RecipeDetailContent(state = state, onAction = { actions += it }, onBack = { backClicks++ })
            }
        }
    }

    @Test
    fun successShowsEveryPartOfTheRecipe() {
        setContent(RecipeDetailUiState.Success(recipe))

        composeRule.onNodeWithText(recipe.description).assertExists()
        recipe.ingredients.forEach { ingredient ->
            composeRule.onNodeWithText(ingredient.name).performScrollTo().assertExists()
            ingredient.quantity?.let { composeRule.onNodeWithText(it).assertExists() }
        }
        composeRule.onNodeWithText("1").performScrollTo().assertExists()
        composeRule.onNodeWithText(recipe.instructions.first()).assertExists()
    }

    @Test
    fun emptyIngredientsAndInstructionsHideTheirHeadings() {
        setContent(RecipeDetailUiState.Success(recipe.copy(ingredients = emptyList(), instructions = emptyList())))

        composeRule.onNodeWithText("Ingredients").assertDoesNotExist()
        composeRule.onNodeWithText("Instructions").assertDoesNotExist()
    }

    @Test
    fun stepNumberIsReadTogetherWithItsText() {
        setContent(RecipeDetailUiState.Success(recipe))

        composeRule.onNode(hasText("1") and hasText(recipe.instructions.first())).performScrollTo().assertExists()
    }

    @Test
    fun ingredientNameIsReadTogetherWithItsQuantity() {
        val ingredient = recipe.ingredients.first { it.quantity != null }
        setContent(RecipeDetailUiState.Success(recipe))

        composeRule.onNode(hasText(ingredient.name) and hasText(ingredient.quantity!!)).performScrollTo().assertExists()
    }

    @Test
    fun screenIsTitledWithTheRecipeNameForAccessibility() {
        setContent(RecipeDetailUiState.Success(recipe))

        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, recipe.title)).assertExists()
    }

    @Test
    fun vegetarianRecipeShowsVegetarianChip() {
        setContent(RecipeDetailUiState.Success(recipe))

        composeRule.onNodeWithText("Vegetarian").assertExists()
    }

    @Test
    fun errorShowsMessageAndRetrySendsAction() {
        setContent(RecipeDetailUiState.Error(R.string.error_recipe_not_found))

        composeRule.onNodeWithText("This recipe could not be found.").assertExists()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(listOf<RecipeDetailAction>(RecipeDetailAction.Retry), actions)
    }

    @Test
    fun backButtonCallsOnBack() {
        setContent(RecipeDetailUiState.Loading)

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backClicks)
    }
}
