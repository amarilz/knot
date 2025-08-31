package com.amarildo.knot.view

data class UiState(
    val databaseFilePath: String = "",
    val isDatabaseLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val searchQuery: String = "",
)
