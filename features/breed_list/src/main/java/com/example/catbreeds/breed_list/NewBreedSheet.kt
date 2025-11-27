package com.example.catbreeds.breed_list

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.catbreeds.core.R
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.DefaultWeight
import com.example.catbreeds.core.ui.theme.AppDimensions.InnerCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.MaxCharCountLarge
import com.example.catbreeds.core.ui.theme.AppDimensions.MaxCharCountSmall
import com.example.catbreeds.core.ui.theme.AppDimensions.MaxChipsToSelect
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SmallIconSize
import com.example.catbreeds.core.ui.theme.AppDimensions.TertiaryItemImageSize
import com.example.catbreeds.core.ui.theme.AppTypography.headlineMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleSmall
import com.example.catbreeds.core.ui.theme.BrandBlue
import com.example.catbreeds.core.ui.theme.BrandRed
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewBreedSheetContent(
    allNames: List<String>,
    allOrigins: List<String>,
    allTemperaments: List<String>,
    onDismiss: () -> Unit,
    onDirtyChange: (Boolean) -> Unit,
    onSave: (String, String, String, List<String>, String, String, String) -> Unit
) {
    val context = LocalContext.current

    val name = remember { mutableStateOf("") }
    val origin = remember { mutableStateOf("") }
    val minLife = remember { mutableStateOf("") }
    val maxLife = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }

    val selectedTemperaments = remember { mutableStateOf(setOf<String>()) }
    val isTemperamentsExpanded = remember { mutableStateOf(false) }
    val isOriginDropdownActive = remember { mutableStateOf(false) }

    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val tempImageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        if(uri != null) selectedImageUri.value = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri.value = tempImageUri.value
        }
    }

    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile("cat_breed_", ".jpg", context.externalCacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val hasSubmitted = remember { mutableStateOf(false) }

    val isDirty by remember {
        derivedStateOf {
            name.value.isNotBlank() ||
                    origin.value.isNotBlank() ||
                    minLife.value.isNotBlank() ||
                    maxLife.value.isNotBlank() ||
                    description.value.isNotBlank() ||
                    selectedTemperaments.value.isNotEmpty() ||
                    selectedImageUri.value != null
        }
    }

    LaunchedEffect(isDirty) {
        onDirtyChange(isDirty)
    }

    // Validation
    val isNameDuplicate = remember {
        derivedStateOf {
            name.value.trim().isNotEmpty() &&
                    allNames.any { it.equals(name.value.trim(), ignoreCase = true) }
        }
    }
    val minLifeInt = minLife.value.toIntOrNull()
    val maxLifeInt = maxLife.value.toIntOrNull()
    val isMaxLessThanMin = if (minLifeInt != null && maxLifeInt != null) {
        maxLifeInt < minLifeInt
    } else false

    fun validate(value: String, customError: String? = null): String {
        if (customError != null) return customError
        return if (hasSubmitted.value && value.isBlank()) "Required" else ""
    }

    // Errors
    val nameError = remember {
        derivedStateOf {
            validate(name.value, if (isNameDuplicate.value) "Name already exists" else null)
        }
    }
    val originError = remember { derivedStateOf { validate(origin.value) } }
    val minLifeError = remember { derivedStateOf { validate(minLife.value) } }
    val maxLifeError = remember(isMaxLessThanMin) {
        derivedStateOf {
            validate(maxLife.value, if (isMaxLessThanMin) "Must be >= Min" else null)
        }
    }
    val descriptionError = remember { derivedStateOf { validate(description.value) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Text(
                        text = "Add a new breed",
                        style = headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
        ) {

            NewBreedImageField(
                modifier = Modifier.fillMaxWidth(),
                imageUri = selectedImageUri.value,
                onGalleryClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                    )
                },
                onCameraClick = {
                    val uri = createTempImageUri()
                    tempImageUri.value = uri
                    cameraLauncher.launch(uri)
                }
            )

            // Name
            NewBreedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name.value,
                onValueChange = { name.value = it },
                label = "Name",
                maxCharCount = MaxCharCountSmall,
                isError = nameError.value.isNotEmpty(),
                errorMessage = nameError.value
            )

            // Origin
            NewBreedDropdownTextField(
                modifier = Modifier.fillMaxWidth(),
                value = origin.value,
                onValueChange = { origin.value = it },
                expanded = isOriginDropdownActive.value,
                onExpandedChange = { isOriginDropdownActive.value = it },
                label = "Origin",
                options = allOrigins,
                maxCharCount = MaxCharCountSmall,
                isError = originError.value.isNotEmpty(),
                errorMessage = originError.value
            )

            // Temperaments
            Text(
                text = "Temperaments",
                style = titleSmall
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(bottom = ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(
                    InterItemSpacing,
                    Alignment.Start
                ),
                verticalArrangement = Arrangement.spacedBy(InterItemSpacing),
                maxLines = if (isTemperamentsExpanded.value) Int.MAX_VALUE else 2,
                overflow = FlowRowOverflow.expandIndicator {
                    SelectChip(
                        text = "See more",
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        textColor = Color.White,
                        onClick = { isTemperamentsExpanded.value = true }
                    )
                }
            ) {
                allTemperaments.forEach { temperament ->
                    val isSelected = selectedTemperaments.value.contains(temperament)
                    SelectChip(
                        text = temperament,
                        backgroundColor = if (isSelected) BrandBlue else MaterialTheme.colorScheme.tertiary,
                        textColor = if (isSelected) Color.White else BrandBlue,
                        onClick = {
                            if (isSelected) {
                                selectedTemperaments.value -= temperament
                            } else if (selectedTemperaments.value.size < MaxChipsToSelect) {
                                selectedTemperaments.value += temperament
                            }
                        }
                    )
                }

                if (isTemperamentsExpanded.value) {
                    SelectChip(
                        text = "Show less",
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        textColor = Color.White,
                        onClick = { isTemperamentsExpanded.value = false }
                    )
                }
            }

            // Life expectancy
            Text(
                text = "Life Expectancy",
                style = titleSmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(InterItemSpacing),
            ) {
                // Min
                NewBreedTextField(
                    modifier = Modifier.weight(DefaultWeight),
                    value = minLife.value,
                    onValueChange = { newValue ->
                        if (newValue.length <= 2) {
                            minLife.value = newValue.filter { it.isDigit() }
                        }
                    },
                    label = "Min",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = minLifeError.value.isNotEmpty(),
                    errorMessage = minLifeError.value
                )
                // Max
                NewBreedTextField(
                    modifier = Modifier.weight(DefaultWeight),
                    value = maxLife.value,
                    onValueChange = { newValue ->
                        if (newValue.length <= 2) {
                            maxLife.value = newValue.filter { it.isDigit() }
                        }
                    },
                    label = "Max",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = maxLifeError.value.isNotEmpty(),
                    errorMessage = maxLifeError.value
                )
            }

            // Description
            NewBreedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = description.value,
                onValueChange = { description.value = it },
                label = "Description",
                maxCharCount = MaxCharCountLarge,
                singleLine = false,
                minLines = 3,
                isError = descriptionError.value.isNotEmpty(),
                errorMessage = descriptionError.value
            )

            Spacer(Modifier.weight(DefaultWeight))

            Button(
                onClick = {
                    hasSubmitted.value = true

                    val hasLogicErrors = isNameDuplicate.value || isMaxLessThanMin

                    val hasEmptyFields = name.value.isBlank() ||
                            origin.value.isBlank() ||
                            minLife.value.isBlank() ||
                            maxLife.value.isBlank() ||
                            description.value.isBlank()

                    if (!hasLogicErrors && !hasEmptyFields) {
                        onSave(
                            name.value.trim(),
                            origin.value.trim(),
                            description.value.trim(),
                            selectedTemperaments.value.toList(),
                            minLife.value.trim(),
                            maxLife.value.trim(),
                            selectedImageUri.value?.toString() ?: ""
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandRed
                )
            ) {
                Text("Add new breed")
            }
            Spacer(modifier = Modifier.height(ScreenPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewBreedDropdownTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: String = "",
    options: List<String>,
    maxCharCount: Int? = null,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    val filteredOptions = remember(value, options) {
        if (value.isEmpty()) {
            options
        } else {
            options.filter { it.contains(value, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        NewBreedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
            value = value,
            onValueChange = {
                onValueChange(it)
                onExpandedChange(true)
            },
            label = label,
            maxCharCount = maxCharCount,
            isError = isError,
            errorMessage = errorMessage
        )

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NewBreedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    supportingText: String = "",
    maxCharCount: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = { newValue ->
            if (maxCharCount == null || newValue.length <= maxCharCount) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(CardCornerRadius),
        keyboardOptions = keyboardOptions,
        isError = isError,
        supportingText = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (isError) {
                        Text(
                            text = errorMessage,
                            color = BrandRed
                        )
                    } else if (supportingText.isNotEmpty()) {
                        Text(text = supportingText)
                    }
                }
                if (maxCharCount != null) {
                    Text(
                        text = "${value.length} / $maxCharCount",
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = SecondaryCardPadding)
                    )
                }
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,

            errorIndicatorColor = Color.Transparent,
            errorContainerColor = MaterialTheme.colorScheme.surface,
            errorCursorColor = BrandRed,
            errorLabelColor = BrandRed
        )
    )
}

@Composable
private fun NewBreedImageField(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(TertiaryItemImageSize)
            .padding(bottom = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(InterItemSpacing)
    ) {
        Surface(
            modifier = Modifier
                .weight(DefaultWeight)
                .fillMaxHeight(),
            shape = RoundedCornerShape(CardCornerRadius),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.FillHeight
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_cat_placeholder),
                        contentDescription = "Placeholder image"
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(DefaultWeight)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
        ) {
            ImageSourceOption(
                label = "Gallery",
                icon = Icons.Default.PhotoLibrary,
                onClick = onGalleryClick,
                modifier = Modifier.weight(DefaultWeight)
            )
            ImageSourceOption(
                label = "Camera",
                icon = Icons.Default.PhotoCamera,
                onClick = onCameraClick,
                modifier = Modifier.weight(DefaultWeight)
            )
        }
    }
}

@Composable
private fun ImageSourceOption(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = ScreenPadding)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(SmallIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(InterItemSpacing))
            Text(
                text = label,
                style = titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(InnerCornerRadius),
        color = backgroundColor,
    ) {
        Text(
            text = text,
            style = titleSmall,
            color = textColor,
            modifier = Modifier.padding(SecondaryCardPadding)
        )
    }
}