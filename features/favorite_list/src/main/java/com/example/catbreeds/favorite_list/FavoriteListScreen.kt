package com.example.catbreeds.favorite_list

import com.example.catbreeds.core.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions
import com.example.catbreeds.core.ui.theme.AppTypography
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.core.ui.theme.ShadowColor
import com.example.catbreeds.core.util.ErrorHandler
import com.example.catbreeds.domain.models.Breed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteListScreen(
    viewModel: FavoriteListViewModel = hiltViewModel(),
    onBreedClick: (String) -> Unit = {}
) {
    val favoriteBreeds by viewModel.favoriteBreeds
    val isLoading by viewModel.isLoading

    // Handles snackbar and error messages
    val errorMessage by viewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = viewModel::clearError
    )

    // Scroll state for dynamic shadow
    val lazyListState = rememberLazyListState()
    val showTopBarShadow by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    // Content
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = if (showTopBarShadow) {
                    Modifier.shadow(
                        elevation = AppDimensions.BarShadow,
                        spotColor = ShadowColor
                    )
                } else {
                    Modifier
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            favoriteBreeds.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing)
                    ) {
                        // When there are no favorite breeds
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppDimensions.NoFavoritesMessageIconPadding)
                        )
                        Text(
                            text = "No favorite breeds yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing),
                    contentPadding = PaddingValues(
                        start = AppDimensions.ScreenPadding,
                        top = AppDimensions.ScreenPadding,
                        end = AppDimensions.ScreenPadding,
                        bottom = AppDimensions.LazyColumnBottomPaddingForNav
                    )
                ) {
                    items(favoriteBreeds) { breed ->
                        FavoriteBreedCard(
                            breed = breed,
                            onBreedClick = { onBreedClick(breed.id) },
                            onRemoveFromFavorites = { viewModel.removeFromFavorites(breed.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteBreedCard(
    breed: Breed,
    onBreedClick: () -> Unit,
    onRemoveFromFavorites: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBreedClick() }
            .shadow(
                elevation = AppDimensions.BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(AppDimensions.CardCornerRadius)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cat image
            AsyncImage(
                model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .size(AppDimensions.TertiaryItemImageSize)
                    .padding(
                        start = AppDimensions.ThinBorderEffect,
                        top = AppDimensions.ThinBorderEffect,
                        bottom = AppDimensions.ThinBorderEffect,
                        end = AppDimensions.CardPadding
                    )
                    .clip(RoundedCornerShape(
                        topStart = AppDimensions.InnerCornerRadius,
                        bottomStart = AppDimensions.InnerCornerRadius,
                    )),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )

            // Name, origin, and average lifespan
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppDimensions.SecondaryCardPadding)
            ) {
                Text(
                    text = breed.name,
                    style = AppTypography.titleMedium
                )
                Text(
                    text = breed.origin,
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 'Average lifespan' actually uses the lower value of the range ('12 - 15' is 12)
                val lowerLifeSpan = breed.life_span.split(" - ").firstOrNull()?.trim()
                lowerLifeSpan?.let {
                    Text(
                        text = "Average lifespan: $it years",
                        style = AppTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Favorite button
            IconButton(onClick = onRemoveFromFavorites) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = BrandRed
                )
            }
        }
    }
}