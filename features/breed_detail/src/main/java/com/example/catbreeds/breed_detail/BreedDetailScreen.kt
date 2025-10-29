package com.example.catbreeds.breed_detail

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions
import com.example.catbreeds.core.ui.theme.FavoriteIconColor
import com.example.catbreeds.core.util.ErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedDetailScreen(
    viewModel: BreedDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val breed by viewModel.breed

    // To handle snackbar and error messages
    val errorMessage by viewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = viewModel::clearError
    )

    // Content
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(breed?.name ?: "Breed Details") },
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
                                tint = if (breedData.isFavorite) FavoriteIconColor else MaterialTheme.colorScheme.onSurfaceVariant
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
                    .padding(AppDimensions.ScreenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.DetailsVerticalSpacing)
            ) {
                // Cat image
                AsyncImage(
                    model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                    contentDescription = "Image of ${breed.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppDimensions.CardCornerRadius)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_menu_report_image),
                    error = painterResource(id = R.drawable.ic_menu_close_clear_cancel)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = breed.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Origin: ${breed.origin}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Temperament
                Column {
                    Text(
                        text = "Temperament",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = breed.temperament,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Description
                Column {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = breed.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
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