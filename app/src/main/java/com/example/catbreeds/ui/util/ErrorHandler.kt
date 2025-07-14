package com.example.catbreeds.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarHostState

@Composable
fun ErrorHandler(
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onErrorShown: () -> Unit
) {
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }
}