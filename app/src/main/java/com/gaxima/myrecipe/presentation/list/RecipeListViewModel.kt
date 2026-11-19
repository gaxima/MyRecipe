package com.gaxima.myrecipe.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.domain.repository.RecipeRepository
import com.gaxima.myrecipe.presentation.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val restoredParams: RecipeSearchParams =
        savedStateHandle.get<String>(SEARCH_PARAMS_KEY)
            ?.let { runCatching { decodeSearchParams(it) }.getOrNull() }
            ?: RecipeSearchParams()

    private val _uiState = MutableStateFlow(
        RecipeListUiState(searchText = restoredParams.query, appliedParams = restoredParams),
    )
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecipeListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RecipeListEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        search(restoredParams)
    }

    fun onAction(action: RecipeListAction) {
        val state = _uiState.value
        val params = state.appliedParams
        when (action) {
            is RecipeListAction.SearchTextChange -> _uiState.update { it.copy(searchText = action.text) }
            RecipeListAction.Search -> search(params.copy(query = state.searchText.trim()))
            RecipeListAction.ClearSearch -> {
                _uiState.update { it.copy(searchText = "") }
                search(params.copy(query = ""))
            }
            is RecipeListAction.ApplyFilters -> search(action.params.copy(query = params.query))
            is RecipeListAction.RemoveFilter -> search(params.without(action.filter))
            RecipeListAction.ClearAllFilters -> {
                _uiState.update { it.copy(searchText = "") }
                search(RecipeSearchParams())
            }
            RecipeListAction.Retry -> search(params)
        }
    }

    /**
     * Runs a search, cancelling any in-flight one so a stale response never overwrites a newer one.
     * With results already on screen they stay visible ([RecipeListUiState.isSearching]) and a
     * failure becomes a snackbar; otherwise the whole content area shows loading / error.
     */
    private fun search(params: RecipeSearchParams) {
        searchJob?.cancel()
        savedStateHandle[SEARCH_PARAMS_KEY] = params.encode()
        _uiState.update { state ->
            val hasResults = state.results is RecipeResultsState.Success
            state.copy(
                appliedParams = params,
                results = if (hasResults) state.results else RecipeResultsState.Loading,
                isSearching = hasResults,
            )
        }
        searchJob = viewModelScope.launch {
            repository.searchRecipes(params)
                .onSuccess { recipes ->
                    _uiState.update { it.copy(results = RecipeResultsState.Success(recipes), isSearching = false) }
                }
                .onFailure { error ->
                    val message = error.toUserMessage()
                    if (_uiState.value.results is RecipeResultsState.Success) {
                        _uiState.update { it.copy(isSearching = false) }
                        _events.tryEmit(RecipeListEvent.ShowSnackbar(message))
                    } else {
                        _uiState.update { it.copy(results = RecipeResultsState.Error(message), isSearching = false) }
                    }
                }
        }
    }
}
