package com.example.catbreeds.core.ui.theme

import androidx.compose.ui.unit.dp

private object BaseValues {
    val SpacingXSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingExtraLarge = 96.dp

    val InnerCornerRadius = 8.dp
    val CornerRadius = 16.dp
    val ImageSizeSmall = 120.dp
    val ImageSizeMedium = 160.dp

    val ShadowSmall = 6.dp

    val DefaultWeight = 1f
}

object AppDimensions {
    // Paddings
    val ScreenPadding = BaseValues.SpacingMedium
    val CardPadding = BaseValues.SpacingMedium
    val SecondaryCardPadding = BaseValues.SpacingSmall
    val InterItemSpacing = BaseValues.SpacingSmall
    val DetailsVerticalSpacing = BaseValues.SpacingMedium
    val NoFavoritesMessageIconPadding = BaseValues.SpacingMedium
    val LazyColumnBottomPaddingForNav = BaseValues.SpacingExtraLarge
    val ThinBorderEffect = BaseValues.SpacingXSmall

    // Corners
    val CardCornerRadius = BaseValues.CornerRadius
    val InnerCornerRadius = BaseValues.InnerCornerRadius

    // Sizes
    val SecondaryItemImageSize = BaseValues.ImageSizeMedium
    val TertiaryItemImageSize = BaseValues.ImageSizeSmall

    val BarShadow = BaseValues.ShadowSmall

    val DefaultWeight = BaseValues.DefaultWeight
}