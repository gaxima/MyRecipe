package com.gaxima.myrecipe.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Wires a [RecipeDetailViewModel] created for [recipeId] to the stateless [RecipeDetailContent]. */
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel<RecipeDetailViewModel, RecipeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(recipeId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeDetailContent(state = state, onAction = viewModel::onAction, onBack = onBack)
}
