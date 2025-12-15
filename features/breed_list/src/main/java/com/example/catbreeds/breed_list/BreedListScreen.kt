package com.example.catbreeds.breed_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.catbreeds.core.R
import com.example.catbreeds.core.ui.theme.AppDimensions.BarShadow
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppConstants.DEFAULT_WEIGHT
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.LazyColumnBottomPaddingForNav
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SheetTopPadding
import com.example.catbreeds.core.ui.theme.AppTypography.bodyMedium
import com.example.catbreeds.core.ui.theme.AppTypography.headlineMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleMedium
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.core.ui.theme.ShadowColor
import com.example.catbreeds.core.util.ErrorHandler
import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedListScreen(
    viewModel: BreedListViewModel = hiltViewModel(),
    onBreedClick: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshBreeds()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val breeds by viewModel.breeds
    val searchQuery by viewModel.searchQuery
    val filteredBreeds by viewModel.filteredBreeds
    val isSearchActive = remember { mutableStateOf(false) }

    val isNewBreedActive = remember { mutableStateOf(false) }
    val isSheetDirty = remember { mutableStateOf(false) }
    val showUnsavedChangesDialog = remember { mutableStateOf(false) }

    val newBreedSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && isSheetDirty.value) {
                showUnsavedChangesDialog.value = true
                false
            } else {
                true
            }
        }
    )
    val scope = rememberCoroutineScope()

    val allNames by viewModel.allNames
    val allTemperaments by viewModel.allTemperaments
    val allOrigins by viewModel.allOrigins

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
    val showTopBarShadow = remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive.value) {
            focusRequester.requestFocus()
        }
    }

    val handleSheetClose: (Boolean) -> Unit = { force ->
        if (force || !isSheetDirty.value) {
            scope.launch {
                isSheetDirty.value = false
                newBreedSheetState.hide()
            }.invokeOnCompletion {
                if (!newBreedSheetState.isVisible) {
                    isNewBreedActive.value = false
                }
            }
        } else {
            showUnsavedChangesDialog.value = true
        }
    }

    if (isNewBreedActive.value) {
        ModalBottomSheet(
            onDismissRequest = { isNewBreedActive.value = false },
            sheetState = newBreedSheetState,
            modifier = Modifier.padding(top = SheetTopPadding),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            NewBreedSheetContent(
                allNames = allNames,
                allOrigins = allOrigins,
                allTemperaments = allTemperaments,
                onDismiss = { handleSheetClose(false) },
                onDirtyChange = { isSheetDirty.value = it },
                onSave = { breed ->
                    viewModel.addNewBreed(
                        breed = breed,
                        onSuccess = { newId ->
                            handleSheetClose(true)
                            onBreedClick(newId)
                        }
                    )
                }
            )
        }
    }

    if (showUnsavedChangesDialog.value) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog.value = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedChangesDialog.value = false
                    handleSheetClose(true)
                }) {
                    Text("Discard", color = BrandRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog.value = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    // Content
    Scaffold(
        topBar = {
            val topAppBarColors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
            val topBarModifier = if (showTopBarShadow.value) {
                Modifier.shadow(
                    elevation = BarShadow,
                    spotColor = ShadowColor
                )
            } else {
                Modifier
            }

            if (isSearchActive.value) {
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
                            isSearchActive.value = false
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
                            style = headlineMedium
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive.value = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search breeds")
                        }
                        IconButton(onClick = { isNewBreedActive.value = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New breed")
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
private fun BreedCard(
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
                model = breed.imageUrl,
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
                Column(modifier = Modifier.weight(DEFAULT_WEIGHT)) {
                    Text(
                        text = breed.name,
                        style = titleMedium
                    )
                    Text(
                        text = breed.origin,
                        style = bodyMedium,
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