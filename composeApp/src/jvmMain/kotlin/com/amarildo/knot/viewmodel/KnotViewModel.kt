package com.amarildo.knot.viewmodel

import androidx.lifecycle.ViewModel
import com.amarildo.knot.data.Information
import com.amarildo.knot.service.FileLocator
import com.amarildo.knot.service.SearchService
import com.amarildo.knot.view.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class KnotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _searchResults = MutableStateFlow<List<Information>>(emptyList())
    val searchResults: StateFlow<List<Information>> = _searchResults

    private var searchService: SearchService? = null

    suspend fun selectDatabase(): String {
        val result: Result<String> = FileLocator().selectFile()
        return try {
            val databaseFilePath: String = result.getOrThrow()
            _uiState.value = _uiState.value.copy(
                databaseFilePath = databaseFilePath,
            )
            return databaseFilePath
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Unexpected error: ${e.message}",
            )
            ""
        }
    }

    fun loadDatabase(databasePath: String) {
        try {
            val service = SearchService(databasePath)
            searchService = service
            _searchResults.value = service.getAll()
            _uiState.update {
                it.copy(isDatabaseLoaded = true, databaseFilePath = databasePath, error = null)
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Errore durante il caricamento: ${e.message}") }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val service = searchService ?: run {
            _searchResults.value = emptyList()
            return
        }
        _searchResults.value = if (query.isBlank()) service.getAll() else service.search(query)
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        _searchResults.value = searchService?.getAll().orEmpty()
    }

    fun clearMessage() {
        _uiState.update { it.copy(isLoading = false, successMessage = null, error = null) }
    }
}
