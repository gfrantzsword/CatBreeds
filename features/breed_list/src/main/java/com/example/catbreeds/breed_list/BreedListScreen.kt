package com.example.catbreeds.breed_list

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions
import com.example.catbreeds.core.util.ErrorHandler
import com.example.catbreeds.domain.models.Breed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedListScreen(
    viewModel: BreedListViewModel = hiltViewModel(),
    onBreedClick: (String) -> Unit
) {
    val breeds by viewModel.breeds
    val searchQuery by viewModel.searchQuery
    val filteredBreeds by viewModel.filteredBreeds
    var isSearchActive by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Handles snackbar and error messages
    val errorMessage by viewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = viewModel::clearError
    )

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    // Content
    Scaffold(
        topBar = {
            val topAppBarColors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
            if (isSearchActive) {
                TopAppBar(
                    modifier = Modifier.shadow(AppDimensions.BarShadow),
                    colors = topAppBarColors,
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search breeds...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.updateSearchQuery("")
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Close search")
                        }
                    }
                )
            } else {
                TopAppBar(
                    modifier = Modifier.shadow(AppDimensions.BarShadow),
                    colors = topAppBarColors,
                    title = { Text("Cat Breeds") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search breeds")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // List
            if (breeds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing),
                    contentPadding = PaddingValues(
                        start = AppDimensions.ScreenPadding,
                        top = AppDimensions.ScreenPadding,
                        end = AppDimensions.ScreenPadding,
                        bottom = AppDimensions.LazyColumnBottomPaddingForNav
                    )
                ) {
                    items(filteredBreeds) { breed ->
                        BreedCard(
                            breed = breed,
                            onClick = { onBreedClick(breed.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(breed.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreedCard(
    breed: Breed,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            // Cat image
            AsyncImage(
                model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = AppDimensions.CardCornerRadius, topEnd = AppDimensions.CardCornerRadius)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_menu_report_image),
                error = painterResource(id = R.drawable.ic_menu_close_clear_cancel)
            )

            // Name and origin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.CardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = breed.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = breed.origin,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Favorite button
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (breed.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (breed.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (breed.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}