package com.example.catbreeds.breed_list

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions
import com.example.catbreeds.core.ui.theme.AppTypography
import com.example.catbreeds.core.ui.theme.BrandBlue
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.core.ui.theme.ShadowColor
import com.example.catbreeds.core.util.ErrorHandler
import com.example.catbreeds.domain.models.Breed
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BreedListScreen(
    viewModel: BreedListViewModel = hiltViewModel(),
    onBreedClick: (String) -> Unit
) {
    val breeds by viewModel.breeds
    val searchQuery by viewModel.searchQuery
    val filteredBreeds by viewModel.filteredBreeds
    var isSearchActive by remember { mutableStateOf(false) }

    var isNewBreedActive by remember { mutableStateOf(false) }
    val newBreedSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val allTemperaments by viewModel.allTemperaments

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

    if (isNewBreedActive) {
        ModalBottomSheet(
            onDismissRequest = { isNewBreedActive = false },
            sheetState = newBreedSheetState,
            modifier = Modifier.padding(top = AppDimensions.SheetTopPadding),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            NewBreedSheetContent(
                allTemperaments = allTemperaments,
                onDismiss = { isNewBreedActive = false }
            )
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
                    elevation = AppDimensions.BarShadow,
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
                        IconButton(onClick = { isNewBreedActive = true }) {
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
                    verticalItemSpacing = AppDimensions.InterItemSpacing,
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewBreedSheetContent(
    allTemperaments: List<String>,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var minLife by remember { mutableStateOf("") }
    var maxLife by remember { mutableStateOf("") }

    var selectedTemperaments by remember { mutableStateOf(setOf<String>()) }
    var isTemperamentsExpanded by remember { mutableStateOf(false) }
    val maxCollapsedTemperaments = AppDimensions.MaxChipsToShow

    val temperamentsToShow = if (!isTemperamentsExpanded) {
        allTemperaments.take(maxCollapsedTemperaments)
    } else {
        allTemperaments
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Text(
                        text = "New Breed",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppDimensions.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing)
        ) {
            // Name
            NewBreedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                modifier = Modifier.fillMaxWidth()
            )

            // Origin
            // TODO: Make it a dropdown + input field
            NewBreedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = "Origin",
                modifier = Modifier.fillMaxWidth()
            )

            // Temperaments
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(
                    AppDimensions.InterItemSpacing,
                    Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing)
            ) {
                temperamentsToShow.forEach { temperament ->
                    SelectTemperamentChip(
                        text = temperament,
                        isSelected = selectedTemperaments.contains(temperament),
                        onClick = {
                            if (selectedTemperaments.contains(temperament)) {
                                selectedTemperaments = selectedTemperaments - temperament
                            } else if (selectedTemperaments.size < AppDimensions.MaxChipsToSelect) {
                                selectedTemperaments = selectedTemperaments + temperament
                            }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { isTemperamentsExpanded = !isTemperamentsExpanded },
                ) {
                    Text(
                        text = if (isTemperamentsExpanded) "Show less" else "Show more",
                        style = AppTypography.titleSmall,
                        color = BrandBlue
                    )
                }
            }

            // Life expectancy
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing),
            ) {
                // Min
                NewBreedTextField(
                    value = minLife,
                    onValueChange = { newValue ->
                        minLife = newValue.filter { it.isDigit() }
                    },
                    label = "Min",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Max
                NewBreedTextField(
                    value = maxLife,
                    onValueChange = { newValue ->
                        maxLife = newValue.filter { it.isDigit() }
                    },
                    label = "Max",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }


            Button(
                onClick = {
                    // TODO: Add new breed logic
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandRed
                )
            ) {
                Text("Add new breed")
            }
            Spacer(modifier = Modifier.height(AppDimensions.ScreenPadding))
        }
    }
}

@Composable
fun NewBreedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(AppDimensions.CardCornerRadius),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun SelectTemperamentChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) BrandBlue else MaterialTheme.colorScheme.tertiary
    val textColor = if (isSelected) Color.White else BrandBlue

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(AppDimensions.InnerCornerRadius),
        color = backgroundColor,
    ) {
        Text(
            text = text,
            style = AppTypography.titleSmall,
            color = textColor,
            modifier = Modifier.padding(AppDimensions.SecondaryCardPadding)
        )
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
                elevation = AppDimensions.BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(AppDimensions.CardCornerRadius)
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
                            topStart = AppDimensions.CardCornerRadius,
                            topEnd = AppDimensions.CardCornerRadius
                        )
                    ),
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )

            // Name and origin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.SecondaryCardPadding),
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