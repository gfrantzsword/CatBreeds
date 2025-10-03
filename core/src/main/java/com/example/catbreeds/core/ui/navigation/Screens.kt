package com.example.catbreeds.core.ui.navigation

sealed class Screen(val route: String) {
    object BreedList : Screen("breeds")
    object Favorites : Screen("favorites")
    object BreedDetail : Screen("breed_detail/{breedId}") {
        fun createRoute(breedId: String) = "breed_detail/$breedId"
    }
}