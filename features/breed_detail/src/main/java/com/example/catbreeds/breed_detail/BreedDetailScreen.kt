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
import com.example.catbreeds.core.ui.theme.AppDimensions.BarShadow
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.CardPadding
import com.example.catbreeds.core.ui.theme.AppConstants.DEFAULT_WEIGHT
import com.example.catbreeds.core.ui.theme.AppDimensions.DetailsVerticalSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.InnerCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryItemImageSize
import com.example.catbreeds.core.ui.theme.AppDimensions.ThinBorderEffect
import com.example.catbreeds.core.ui.theme.AppTypography.bodyMedium
import com.example.catbreeds.core.ui.theme.AppTypography.headlineMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleSmall
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
    val showTopBarShadow = remember {
        derivedStateOf { scrollState.value > 0 }
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
                        text = breed?.name ?: "Breed Details",
                        style = headlineMedium
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
                verticalArrangement = Arrangement.spacedBy(DetailsVerticalSpacing)
            ) {
                // Cat image
                AsyncImage(
                    model = breed.imageUrl,
                    contentDescription = "Image of ${breed.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding)
                        .clip(RoundedCornerShape(CardCornerRadius)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                    error = painterResource(id = R.drawable.ic_cat_error)
                )

                // Origin / Life Expectancy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding)
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(InterItemSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCard(
                        modifier = Modifier.weight(DEFAULT_WEIGHT),
                        label = "Origin",
                        value = breed.origin
                    )
                    StatCard(
                        modifier = Modifier.weight(DEFAULT_WEIGHT),
                        label = "Life Expectancy",
                        value = "${breed.lifeSpan} years"
                    )
                }

                // Temperament
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(
                        InterItemSpacing,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
                ) {
                    breed.temperament
                        .filter { it.isNotEmpty() }
                        .forEach { temperament ->
                            TemperamentChip(text = temperament)
                        }
                }

                // Description
                StatCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                    label = "About the ${breed.name}",
                    value = breed.description
                )

                // Similar Breeds
                if (similarBreeds.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Similar Breeds",
                            style = titleMedium,
                            modifier = Modifier
                                .padding(bottom = InterItemSpacing)
                                .padding(horizontal = ScreenPadding)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(InterItemSpacing),
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
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier
            .shadow(
                elevation = BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(CardCornerRadius)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .padding(CardPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = bodyMedium
            )
            Spacer(modifier = Modifier.height(SecondaryCardPadding))
            Text(
                text = value,
                style = titleMedium
            )
        }
    }
}

@Composable
private fun TemperamentChip(text: String) {
    Surface(
        shape = RoundedCornerShape(InnerCornerRadius),
        color = MaterialTheme.colorScheme.tertiary,
    ) {
        Text(
            text = text,
            style = titleSmall,
            color = BrandBlue,
            modifier = Modifier.padding(SecondaryCardPadding)
        )
    }
}

@Composable
private fun SimilarBreedCard(
    breed: Breed,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(
                elevation = BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(CardCornerRadius)
            )
            .width(SecondaryItemImageSize),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            AsyncImage(
                model = breed.imageUrl,
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .size(SecondaryItemImageSize)
                    .padding(
                        start = ThinBorderEffect,
                        top = ThinBorderEffect,
                        end = ThinBorderEffect,
                    )
                    .clip(RoundedCornerShape(
                        topStart = InnerCornerRadius,
                        topEnd = InnerCornerRadius
                    )),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )
            Column (modifier = Modifier.padding(CardPadding)) {
                Text(
                    text = breed.name,
                    style = titleMedium,
                    maxLines = 2
                )
                Text(
                    text = breed.origin,
                    style = bodyMedium,
                )
            }

        }
    }
}