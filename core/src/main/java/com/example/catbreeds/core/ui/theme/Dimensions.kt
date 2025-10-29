package com.example.catbreeds.core.ui.theme

import androidx.compose.ui.unit.dp

private object BaseValues {
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingExtraLarge = 96.dp

    val CornerRadius = 8.dp
    val ImageSizeMedium = 100.dp
}

object AppDimensions {
    // Paddings
    val ScreenPadding = BaseValues.SpacingMedium
    val CardPadding = BaseValues.SpacingMedium
    val InterItemSpacing = BaseValues.SpacingSmall
    val DetailsVerticalSpacing = BaseValues.SpacingMedium
    val NoFavoritesMessageIconPadding = BaseValues.SpacingMedium
    val LazyColumnBottomPaddingForNav = BaseValues.SpacingExtraLarge

    // Corners
    val CardCornerRadius = BaseValues.CornerRadius

    // Sizes
    val FavoriteListItemImageSize = BaseValues.ImageSizeMedium
}