package com.example.catbreeds

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.catbreeds.breed_detail.BreedDetailScreen
import com.example.catbreeds.breed_list.BreedListScreen
import com.example.catbreeds.core.ui.navigation.Screen
import com.example.catbreeds.core.ui.theme.AppDimensions
import com.example.catbreeds.core.ui.theme.CatBreedsTheme
import com.example.catbreeds.core.ui.theme.ShadowColor
import com.example.catbreeds.favorite_list.FavoriteListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatBreedsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppContent()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppContent() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.BreedList.route,
        ) {
            composable(Screen.BreedList.route) {
                BreedListScreen(
                    onBreedClick = { breedId ->
                        navController.navigate(Screen.BreedDetail.createRoute(breedId))
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoriteListScreen(
                    onBreedClick = { breedId ->
                        navController.navigate(Screen.BreedDetail.createRoute(breedId))
                    }
                )
            }
            composable(Screen.BreedDetail.route) { backStackEntry ->
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

    val showBottomBar = currentRoute in listOf(Screen.BreedList.route, Screen.Favorites.route)

    if (showBottomBar) {
        NavigationBar(
            modifier = Modifier.shadow(
                elevation = AppDimensions.BarShadow,
                spotColor = ShadowColor
            ),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surface
            )

            NavigationBarItem(
                colors = itemColors,
                icon = { Icon(Icons.Default.Home, contentDescription = "Breeds") },
                label = { Text("Breeds") },
                selected = currentRoute == Screen.BreedList.route,
                onClick = {
                    if (currentRoute != Screen.BreedList.route) {
                        navController.navigate(Screen.BreedList.route) {
                            popUpTo(Screen.BreedList.route) { inclusive = true }
                        }
                    }
                }
            )
            NavigationBarItem(
                colors = itemColors,
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                label = { Text("Favorites") },
                selected = currentRoute == Screen.Favorites.route,
                onClick = {
                    if (currentRoute != Screen.Favorites.route) {
                        navController.navigate(Screen.Favorites.route) {
                            popUpTo(Screen.BreedList.route)
                        }
                    }
                }
            )
        }
    }
}