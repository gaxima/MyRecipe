package com.gaxima.myrecipe.presentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.Ingredient
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.presentation.components.DietaryBadge
import com.gaxima.myrecipe.presentation.components.ErrorState
import com.gaxima.myrecipe.presentation.components.LoadingState
import com.gaxima.myrecipe.presentation.components.RecipeImage
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme

/** Stateless recipe detail screen. The back button stays available in every state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailContent(
    state: RecipeDetailUiState,
    onAction: (RecipeDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (state) {
            RecipeDetailUiState.Loading -> LoadingState(contentModifier)
            is RecipeDetailUiState.Error -> ErrorState(
                messageRes = state.messageRes,
                onRetry = { onAction(RecipeDetailAction.Retry) },
                modifier = contentModifier,
            )
            is RecipeDetailUiState.Success -> RecipeDetailBody(state.recipe, contentModifier)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeDetailBody(recipe: Recipe, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = null, // decorative: the title follows
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.large),
        )
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        if (recipe.description.isNotEmpty()) {
            Text(text = recipe.description, style = MaterialTheme.typography.bodyLarge)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            recipe.dietaryAttributes.sorted().forEach { DietaryBadge(it) }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    text = pluralStringResource(R.plurals.servings_count, recipe.servings, recipe.servings),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        SectionTitle(stringResource(R.string.ingredients_title))
        Column {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                if (index > 0) HorizontalDivider()
                IngredientRow(ingredient)
            }
        }

        SectionTitle(stringResource(R.string.instructions_title))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            recipe.instructions.forEachIndexed { index, step -> InstructionStep(number = index + 1, text = step) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        ingredient.quantity?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun InstructionStep(number: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number.toString(), style = MaterialTheme.typography.labelLarge)
            }
        }
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailPreview(state: RecipeDetailUiState) {
    MyRecipeTheme(dynamicColor = false) {
        RecipeDetailContent(state = state, onAction = {}, onBack = {})
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SuccessPreview() = DetailPreview(RecipeDetailUiState.Success(PreviewData.vegetarianRecipe))

@Preview(showSystemUi = true)
@Composable
private fun LongTitlePreview() = DetailPreview(RecipeDetailUiState.Success(PreviewData.longTitleRecipe))

@Preview(showSystemUi = true)
@Composable
private fun LoadingPreview() = DetailPreview(RecipeDetailUiState.Loading)

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorPreview() = DetailPreview(RecipeDetailUiState.Error(R.string.error_recipe_not_found))
