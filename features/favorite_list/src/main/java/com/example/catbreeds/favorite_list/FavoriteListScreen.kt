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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions.BarShadow
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.CardPadding
import com.example.catbreeds.core.ui.theme.AppConstants.DEFAULT_WEIGHT
import com.example.catbreeds.core.ui.theme.AppDimensions.InnerCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.LazyColumnBottomPaddingForNav
import com.example.catbreeds.core.ui.theme.AppDimensions.NoFavoritesMessageIconPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.TertiaryItemImageSize
import com.example.catbreeds.core.ui.theme.AppDimensions.ThinBorderEffect
import com.example.catbreeds.core.ui.theme.AppTypography.bodyMedium
import com.example.catbreeds.core.ui.theme.AppTypography.headlineMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleMedium
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
    val showTopBarShadow = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    // Content
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = if (showTopBarShadow.value) {
                    Modifier.shadow(
                        elevation = BarShadow,
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
                        text = stringResource(R.string.favorites_title),
                        style = headlineMedium
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
                        verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
                    ) {
                        // When there are no favorite breeds
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(NoFavoritesMessageIconPadding)
                        )
                        Text(
                            text = stringResource(R.string.empty_favorites_message),
                            style = titleMedium,
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
                    verticalArrangement = Arrangement.spacedBy(InterItemSpacing),
                    contentPadding = PaddingValues(
                        start = ScreenPadding,
                        top = ScreenPadding,
                        end = ScreenPadding,
                        bottom = LazyColumnBottomPaddingForNav
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
private fun FavoriteBreedCard(
    breed: Breed,
    onBreedClick: () -> Unit,
    onRemoveFromFavorites: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBreedClick() }
            .shadow(
                elevation = BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(CardCornerRadius)
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
                model = breed.imageUrl,
                contentDescription = stringResource(R.string.cd_breed_image, breed.name),
                modifier = Modifier
                    .size(TertiaryItemImageSize)
                    .padding(
                        start = ThinBorderEffect,
                        top = ThinBorderEffect,
                        bottom = ThinBorderEffect,
                        end = CardPadding
                    )
                    .clip(RoundedCornerShape(
                        topStart = InnerCornerRadius,
                        bottomStart = InnerCornerRadius,
                    )),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )

            // Name, origin, and average lifespan
            Column(
                modifier = Modifier
                    .weight(DEFAULT_WEIGHT)
                    .padding(SecondaryCardPadding)
            ) {
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
            IconButton(onClick = onRemoveFromFavorites) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = stringResource(R.string.cd_remove_favorite),
                    tint = BrandRed
                )
            }
        }
    }
}