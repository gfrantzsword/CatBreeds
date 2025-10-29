package com.example.catbreeds.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private object BaseTypography {
    val FontSizeSmall = 12.sp
    val FontSizeMedium = 14.sp
    val FontSizeLarge = 16.sp
    val FontSizeTitle = 18.sp
    val FontSizeHeadline = 24.sp

    val FontWeightNormal = FontWeight.Normal
    val FontWeightBold = FontWeight.Bold

    val LineHeightDefault = 24.sp
}

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = BaseTypography.FontWeightBold,
        fontSize = BaseTypography.FontSizeHeadline
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = BaseTypography.FontWeightBold,
        fontSize = BaseTypography.FontSizeTitle
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = BaseTypography.FontWeightBold,
        fontSize = BaseTypography.FontSizeTitle
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = BaseTypography.FontWeightNormal,
        fontSize = BaseTypography.FontSizeLarge,
        lineHeight = BaseTypography.LineHeightDefault
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = BaseTypography.FontWeightNormal,
        fontSize = BaseTypography.FontSizeMedium
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = BaseTypography.FontWeightNormal,
        fontSize = BaseTypography.FontSizeSmall
    )
)