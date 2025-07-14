package com.example.catbreeds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.catbreeds.ui.breedDetail.BreedDetailScreen
import com.example.catbreeds.ui.breedList.BreedListScreen
import com.example.catbreeds.ui.favoriteList.FavoriteListScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.contains

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "breeds",
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        ) {
            composable("breeds") {
                BreedListScreen(
                    onBreedClick = { breedId ->
                        navController.navigate("breed_detail/$breedId")
                    }
                )
            }
            composable("favorites") {
                FavoriteListScreen(
                    onBreedClick = { breedId ->
                        navController.navigate("breed_detail/$breedId")
                    }
                )
            }
            composable("breed_detail/{breedId}") { backStackEntry ->
                val breedId = backStackEntry.arguments?.getString("breedId")
                breedId?.let {
                    BreedDetailScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("breeds", "favorites")

    if (showBottomBar) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Breeds") },
                label = { Text("Breeds") },
                selected = currentRoute == "breeds",
                onClick = {
                    if (currentRoute != "breeds") {
                        navController.navigate("breeds") {
                            popUpTo("breeds") { inclusive = true }
                        }
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                label = { Text("Favorites") },
                selected = currentRoute == "favorites",
                onClick = {
                    if (currentRoute != "favorites") {
                        navController.navigate("favorites") {
                            popUpTo("breeds")
                        }
                    }
                }
            )
        }
    }
}