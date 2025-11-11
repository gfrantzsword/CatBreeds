package com.example.catbreeds.breed_detail

import com.example.catbreeds.core.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions
import com.example.catbreeds.core.ui.theme.AppTypography
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.core.ui.theme.BrandBlue
import com.example.catbreeds.core.ui.theme.ShadowColor
import com.example.catbreeds.core.util.ErrorHandler
import com.example.catbreeds.domain.models.Breed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BreedDetailScreen(
    viewModel: BreedDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onBreedClick: (String) -> Unit
) {
    val breed by viewModel.breed
    val similarBreeds by viewModel.similarBreeds

    // To handle snackbar and error messages
    val errorMessage by viewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = viewModel::clearError
    )

    // Scroll state for dynamic shadow
    val scrollState = rememberScrollState()
    val showTopBarShadow = scrollState.value > 0

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
                        text = breed?.name ?: "Breed Details",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    breed?.let { breedData ->
                        IconButton(onClick = { viewModel.toggleFavorite(breedData.id) }) {
                            Icon(
                                imageVector = if (breedData.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (breedData.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = BrandRed
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        breed?.let { breed ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.DetailsVerticalSpacing)
            ) {
                // Cat image
                AsyncImage(
                    model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                    contentDescription = "Image of ${breed.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.ScreenPadding)
                        .clip(RoundedCornerShape(AppDimensions.CardCornerRadius)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                    error = painterResource(id = R.drawable.ic_cat_error)
                )

                // Origin / Life Expectancy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCard(
                        "Origin",
                        breed.origin,
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Life Expectancy",
                        breed.life_span,
                        Modifier.weight(1f)
                    )
                }

                // Temperament
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimensions.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(
                        AppDimensions.InterItemSpacing,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing)
                ) {
                    val temperaments = breed.temperament.split(", ")
                    temperaments.forEach { temperament ->
                        TemperamentChip(text = temperament)
                    }
                }

                // Description
                StatCard(
                    "About the ${breed.name}",
                    breed.description,
                    Modifier.padding(horizontal = AppDimensions.ScreenPadding)
                )

                // Similar Breeds
                if (similarBreeds.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Similar Breeds",
                            style = AppTypography.titleMedium,
                            modifier = Modifier
                                .padding(bottom = AppDimensions.InterItemSpacing)
                                .padding(horizontal = AppDimensions.ScreenPadding)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = AppDimensions.ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.InterItemSpacing),
                        ) {
                            items(similarBreeds) { similarBreed ->
                                SimilarBreedCard(
                                    breed = similarBreed,
                                    onClick = { onBreedClick(similarBreed.id) }
                                )
                            }
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(
                elevation = AppDimensions.BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(AppDimensions.CardCornerRadius)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(AppDimensions.CardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .padding(AppDimensions.CardPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = AppTypography.bodyMedium
            )
            Spacer(modifier = Modifier.height(AppDimensions.SecondaryCardPadding))
            Text(
                text = value,
                style = AppTypography.titleMedium
            )
        }
    }
}

@Composable
fun TemperamentChip(text: String) {
    Surface(
        shape = RoundedCornerShape(AppDimensions.InnerCornerRadius),
        color = MaterialTheme.colorScheme.tertiary,
    ) {
        Text(
            text = text,
            style = AppTypography.titleSmall,
            color = BrandBlue,
            modifier = Modifier.padding(AppDimensions.SecondaryCardPadding)
        )
    }
}

@Composable
fun SimilarBreedCard(
    breed: Breed,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(
                elevation = AppDimensions.BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(AppDimensions.CardCornerRadius)
            )
            .width(AppDimensions.SecondaryItemImageSize),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            AsyncImage(
                model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .size(AppDimensions.SecondaryItemImageSize)
                    .padding(
                        start = AppDimensions.ThinBorderEffect,
                        top = AppDimensions.ThinBorderEffect,
                        end = AppDimensions.ThinBorderEffect,
                    )
                    .clip(RoundedCornerShape(
                        topStart = AppDimensions.InnerCornerRadius,
                        topEnd = AppDimensions.InnerCornerRadius
                    )),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )
            Column (modifier = Modifier.padding(AppDimensions.CardPadding)) {
                Text(
                    text = breed.name,
                    style = AppTypography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = breed.origin,
                    style = AppTypography.bodyMedium,
                )
            }

        }
    }
}