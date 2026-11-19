package com.gaxima.myrecipe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxima.myrecipe.domain.repository.RecipeRepository
import com.gaxima.myrecipe.presentation.common.toUserMessage
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Loads one recipe by id; the id comes from the navigation key via assisted injection. */
@HiltViewModel(assistedFactory = RecipeDetailViewModel.Factory::class)
class RecipeDetailViewModel @AssistedInject constructor(
    @Assisted private val recipeId: String,
    private val repository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onAction(action: RecipeDetailAction) {
        when (action) {
            RecipeDetailAction.Retry -> load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = RecipeDetailUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = repository.getRecipe(recipeId).fold(
                onSuccess = { RecipeDetailUiState.Success(it) },
                onFailure = { RecipeDetailUiState.Error(it.toUserMessage()) },
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: String): RecipeDetailViewModel
    }
}
