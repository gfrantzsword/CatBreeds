package com.example.catbreeds.breed_list

import com.example.catbreeds.core.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions.BarShadow
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.LazyColumnBottomPaddingForNav
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.core.ui.theme.ShadowColor
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

    // Scroll state for dynamic shadow
    val lazyGridState = rememberLazyStaggeredGridState()
    val showTopBarShadow by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
        }
    }

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
            val topBarModifier = if (showTopBarShadow) {
                Modifier.shadow(
                    elevation = BarShadow,
                    spotColor = ShadowColor
                )
            } else {
                Modifier
            }

            if (isSearchActive) {
                TopAppBar(
                    modifier = topBarModifier,
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
                    modifier = topBarModifier,
                    colors = topAppBarColors,
                    title = {
                        Text(
                            text = "Cat Breeds",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
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
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = lazyGridState,
                    modifier = Modifier.fillMaxSize(),
                    verticalItemSpacing = InterItemSpacing,
                    horizontalArrangement = Arrangement.spacedBy(InterItemSpacing),
                    contentPadding = PaddingValues(
                        start = ScreenPadding,
                        top = ScreenPadding,
                        end = ScreenPadding,
                        bottom = LazyColumnBottomPaddingForNav
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
            .clickable { onClick() }
            .shadow(
                elevation = BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(CardCornerRadius)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Cat image
            AsyncImage(
                model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = CardCornerRadius,
                            topEnd = CardCornerRadius
                        )
                    ),
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )

            // Name and origin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SecondaryCardPadding),
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
                        tint = BrandRed
                    )
                }
            }
        }
    }
}