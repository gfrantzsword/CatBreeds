package com.example.catbreeds.core.ui.theme

import androidx.compose.ui.unit.dp

private object BaseValues {
    val SpacingXSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 32.dp
    val SpacingExtraLarge = 96.dp

    val InnerCornerRadius = 8.dp
    val CornerRadius = 16.dp
    val ImageSizeSmall = 120.dp
    val ImageSizeMedium = 160.dp

    val ShadowSmall = 6.dp

    val ConstNumberSmall = 6
    val ConstNumberMedium = 10

    val MaxCharCountSmall = 30
    val MaxCharCountLarge = 300

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
    val SheetTopPadding = BaseValues.SpacingLarge

    // Corners
    val CardCornerRadius = BaseValues.CornerRadius
    val InnerCornerRadius = BaseValues.InnerCornerRadius

    // Sizes
    val SecondaryItemImageSize = BaseValues.ImageSizeMedium
    val TertiaryItemImageSize = BaseValues.ImageSizeSmall

    val BarShadow = BaseValues.ShadowSmall

    // Constants
    val MaxChipsToSelect = BaseValues.ConstNumberSmall
    val MaxChipsToShow = BaseValues.ConstNumberMedium
    val MaxCharCountSmall = BaseValues.MaxCharCountSmall
    val MaxCharCountLarge = BaseValues.MaxCharCountLarge

    val DefaultWeight = BaseValues.DefaultWeight
}