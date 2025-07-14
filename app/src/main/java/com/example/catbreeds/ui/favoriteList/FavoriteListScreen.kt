package com.example.catbreeds.ui.favoriteList

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.domain.models.Breed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.catbreeds.ui.util.ErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteListScreen(
    viewModel: FavoriteListViewModel = hiltViewModel(),
    onBreedClick: (String) -> Unit = {}
) {
    val favoriteBreeds by viewModel.favoriteBreeds
    val isLoading by viewModel.isLoading

    val errorMessage by viewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }

    ErrorHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = viewModel::clearError
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") }
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "No favorite breeds yet",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://cdn2.thecatapi.com/images/${breed.reference_image_id}.jpg",
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .size(height = 100.dp, width = 100.dp)
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_menu_report_image),
                error = painterResource(id = R.drawable.ic_menu_close_clear_cancel)
            )

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = breed.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = breed.origin,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val lowerLifeSpan = breed.life_span.split(" - ").firstOrNull()?.trim()
                lowerLifeSpan?.let {
                    Text(
                        text = "Average lifespan: $it years",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemoveFromFavorites) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = Color.Red
                )
            }
        }
    }
}