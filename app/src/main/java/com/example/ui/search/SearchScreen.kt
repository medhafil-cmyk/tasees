package com.example.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.LocationResult
import com.example.data.db.SavedCity
import com.example.domain.WeatherRepository
import com.example.ui.home.HomeViewModel
import com.example.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<LocationResult>>(emptyList())
    val searchResults: StateFlow<List<LocationResult>> = _searchResults

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            searchQuery.debounce(500).collect { query ->
                if (query.isNotBlank()) {
                    try {
                        val results = repository.searchCity(query)
                        _searchResults.value = results
                    } catch (e: Exception) {
                        _searchResults.value = emptyList()
                    }
                } else {
                    _searchResults.value = emptyList()
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun saveCity(location: LocationResult) {
        viewModelScope.launch {
            repository.saveCity(
                SavedCity(
                    id = location.id,
                    name = location.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    country = location.country,
                    admin1 = location.admin1
                )
            )
        }
    }

    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(repository) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val results by searchViewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            searchViewModel.onSearchQueryChanged(it)
                        },
                        placeholder = { Text(stringResource(R.string.search_city_hint)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.padding(end=16.dp))
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(results) { location ->
                ListItem(
                    headlineContent = { Text(location.name) },
                    supportingContent = { Text("${location.admin1 ?: ""}, ${location.country ?: ""}") },
                    modifier = Modifier.clickable {
                        searchViewModel.saveCity(location)
                        homeViewModel.fetchWeather(location.latitude, location.longitude, location.name)
                        onNavigateBack()
                    }
                )
                Divider()
            }
        }
    }
}
