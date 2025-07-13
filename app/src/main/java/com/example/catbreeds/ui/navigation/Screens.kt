package com.example.catbreeds.ui.navigation

sealed class Screen(val route: String) {
    object BreedList : Screen("breedList")
    object Favorites : Screen("favorites")
    object BreedDetail : Screen("breedDetail/{breedId}") {
        fun createRoute(breedId: String) = "breedDetail/$breedId"
    }
}