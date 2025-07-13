package com.example.catbreeds.ui.navigation

sealed class Screen(val route: String) {
    object BreedListScreen : Screen("breedList")
    object FavoriteListScreen : Screen("favoriteList")
    object BreedDetailScreen : Screen("breedDetail")
}