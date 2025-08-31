package com.amarildo.knot.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amarildo.knot.data.Information
import com.amarildo.knot.viewmodel.KnotViewModel
import com.composables.icons.lucide.Database
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun Knot(vm: KnotViewModel = viewModel()) {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var isPathLoading by remember { mutableStateOf(false) }
        val uiState by vm.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val searchResults by vm.searchResults.collectAsStateWithLifecycle()

        LaunchedEffect(uiState.successMessage, uiState.error) {
            uiState.successMessage?.let { msg ->
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short,
                )
                vm.clearMessage()
            }
            uiState.error?.let { error ->
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long,
                )
                vm.clearMessage()
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (data.visuals.message.contains("Error", ignoreCase = true)) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (data.visuals.message.contains("Error", ignoreCase = true)) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                    )
                }
            },
        ) { paddingValues ->
            if (!uiState.isDatabaseLoaded) {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    item {
                        ConfigurationFileInput(uiState, vm, isPathLoading) { isPathLoading = it }
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    vm.loadDatabase(uiState.databaseFilePath)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                            ),
                        ) {
                            Text("Load database")
                        }
                    }
                }
            } else {
                SearchScreen(
                    searchQuery = uiState.searchQuery,
                    searchResults = searchResults,
                    onSearchQueryChange = { vm.onSearchQueryChange(it) },
                    onClearSearch = { vm.clearSearch() },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: List<Information>,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { },
                placeholder = {
                    Text(
                        text = "Type something...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Lucide.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Lucide.Delete,
                                contentDescription = "Delete search",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                },
                content = {
                    SearchResults(
                        searchResults = searchResults,
                        searchQuery = searchQuery,
                    )
                },
                active = true,
                onActiveChange = { },
                tonalElevation = 0.dp,
            )
        }
    }
}

@Composable
fun SearchResults(
    searchResults: List<Information>,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (searchQuery.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Results for: \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium,
                    )
                    Badge {
                        Text(
                            text = "${searchResults.size}",
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
            HorizontalDivider()
        }

        if (searchResults.isEmpty()) {
            EmptyState(searchQuery = searchQuery)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    count = searchResults.size,
                    key = { index -> "${searchResults[index].key}_${searchResults[index].value}_$index" },
                ) { index ->
                    ExpandableInformationCard(information = searchResults[index])
                }
            }
        }
    }
}

@Composable
fun ExpandableInformationCard(information: Information) {
    var expandedState by remember { mutableStateOf(false) }
    val maxLines = if (expandedState) Int.MAX_VALUE else 3
    val textOverflow = if (expandedState) TextOverflow.Clip else TextOverflow.Ellipsis

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                onClick = { expandedState = !expandedState },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = information.key,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = information.value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = maxLines,
                    overflow = textOverflow,
                )
            }
            Badge {
                Text(
                    text = "Score: ${information.score}",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Lucide.Info,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )

        Text(
            text = if (searchQuery.isEmpty()) {
                "Type something..."
            } else {
                "No results found for: \"$searchQuery\""
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )

        if (searchQuery.isNotEmpty()) {
            Text(
                text = "Try with other words",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ConfigurationFileInput(
    uiState: UiState,
    vm: KnotViewModel,
    isPathLoading: Boolean,
    setPathLoading: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()

    EnhancedInputRow(
        label = "Configuration File Path",
        value = uiState.databaseFilePath,
        onValueChange = vm::loadDatabase,
        placeholder = "Select the configuration file...",
        isLoading = isPathLoading,
        onFocusAction = {
            scope.launch {
                setPathLoading(true)
                try {
                    vm.selectDatabase()
                } finally {
                    setPathLoading(false)
                }
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Lucide.Database,
                contentDescription = "Configuration File",
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

@Composable
fun EnhancedInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusAction: () -> Unit,
    placeholder: String = "",
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                    ) {
                        Icon(
                            imageVector = Lucide.Delete,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !isLoading) {
                        onFocusAction()
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}
