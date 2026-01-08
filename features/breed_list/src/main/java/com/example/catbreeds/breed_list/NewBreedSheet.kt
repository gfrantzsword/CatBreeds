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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.catbreeds.core.R
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppConstants.DEFAULT_WEIGHT
import com.example.catbreeds.core.ui.theme.AppDimensions.InnerCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppConstants.MAX_CHAR_COUNT_LARGE
import com.example.catbreeds.core.ui.theme.AppConstants.MAX_CHAR_COUNT_SMALL
import com.example.catbreeds.core.ui.theme.AppConstants.MAX_CHIPS_TO_COLLECT
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SheetBottomPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SmallIconSize
import com.example.catbreeds.core.ui.theme.AppDimensions.TertiaryItemImageSize
import com.example.catbreeds.core.ui.theme.AppTypography.bodySmall
import com.example.catbreeds.core.ui.theme.AppTypography.headlineMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleSmall
import com.example.catbreeds.core.ui.theme.BrandBlue
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewBreedSheetContent(
    allNames: List<String>,
    allOrigins: List<String>,
    allTemperaments: List<String>,
    onDismiss: () -> Unit,
    onDirtyChange: (Boolean) -> Unit,
    onSave: suspend (Breed) -> String?,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.isImeVisible

    // Form State
    val formState = rememberNewBreedFormState(allNames = allNames)

    // Helper States
    val tempImageUri = remember { mutableStateOf<Uri?>(null) }

    // Validation
    val isDirty = remember {
        derivedStateOf { formState.isDirty() }
    }

    LaunchedEffect(isDirty.value) {
        onDirtyChange(isDirty.value)
    }

    LaunchedEffect(formState.createdBreedId.value) {
        formState.createdBreedId.value?.let { id ->
            onSuccess(id)
            formState.onNavigationHandled()
        }
    }

    // Helper Methods
    val filePrefix = stringResource(R.string.temp_jpg_file_prefix)
    val fileExtension = stringResource(R.string.temp_jpg_file_extension)
    val fileProviderStr = stringResource(R.string.create_temp_img_uri, context.packageName)

    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile(
            filePrefix,
            fileExtension,
            context.externalCacheDir
        )

        return FileProvider.getUriForFile(
            context,
            fileProviderStr,
            tempFile
        )
    }

    fun validateAndSubmit() {
        if (formState.validate()) {
            val breed = formState.toBreed()
            scope.launch {
                val newId = onSave(breed)
                if (newId != null) {
                    formState.onAddNewBreed(newId)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) formState.update(imageUri = uri.toString())
    }
    val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success) tempImageUri.value?.let { formState.update(imageUri = it.toString()) }
    }

    // Content
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Text(
                        text = stringResource(R.string.new_breed_title),
                        style = headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_cancel))
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { validateAndSubmit() },
                expanded = !isKeyboardOpen,
                modifier = Modifier.padding(horizontal = ScreenPadding),
                containerColor = BrandRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.cd_checkmark)) },
                text = { Text(stringResource(R.string.action_add_breed)) }
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
            // Image
            NewBreedImageField(
                modifier = Modifier.fillMaxWidth(),
                imageUri = if (formState.imageUri.value.isNotBlank()) formState.imageUri.value.toUri() else null,
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
                value = formState.name.value,
                onValueChange = { formState.update(name = it) },
                label = stringResource(R.string.label_name),
                maxCharCount = MAX_CHAR_COUNT_SMALL,
                isError = formState.nameError.value != null,
                errorMessage = formState.nameError.value?.let { stringResource(it) } ?: ""
            )

            // Origin
            OriginField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.origin.value,
                onValueChange = { formState.update(origin = it) },
                expanded = formState.isOriginDropdownActive.value,
                onExpandedChange = { formState.update(isOriginDropdownActive = it) },
                options = allOrigins,
                maxCharCount = MAX_CHAR_COUNT_SMALL,
                isError = formState.originError.value != null,
                errorMessage = formState.originError.value?.let { stringResource(it) } ?: ""
            )

            // Temperaments
            TemperamentField(
                allTemperaments = allTemperaments,
                selectedTemperaments = formState.selectedTemperaments.value,
                onSelectionChanged = {
                    focusManager.clearFocus()
                    formState.update(selectedTemperaments = it)
                }
            )

            // Life Expectancy
            LifeExpectancyField(
                minLife = formState.minLife.value,
                onMinLifeChange = { formState.update(minLife = it) },
                minLifeError = formState.minLifeError.value?.let { stringResource(it) } ?: "",
                maxLife = formState.maxLife.value,
                onMaxLifeChange = { formState.update(maxLife = it) },
                maxLifeError = formState.maxLifeError.value?.let { stringResource(it) } ?: ""
            )

            // Description
            NewBreedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.description.value,
                onValueChange = { formState.update(description = it) },
                label = stringResource(R.string.label_description),
                maxCharCount = MAX_CHAR_COUNT_LARGE,
                singleLine = false,
                minLines = 3,
                isError = formState.descriptionError.value != null,
                errorMessage = formState.descriptionError.value?.let { stringResource(it) } ?: ""
            )

            Spacer(modifier = Modifier.height(SheetBottomPadding))
        }
    }
}

// Field Composables
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemperamentField(
    allTemperaments: List<String>,
    selectedTemperaments: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
    ) {
        FieldTitle(
            title = stringResource(R.string.label_temperament),
            subtitle = stringResource(R.string.format_selected_count, selectedTemperaments.size, MAX_CHIPS_TO_COLLECT)
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
            maxLines = if (isExpanded.value) Int.MAX_VALUE else 2,
            overflow = FlowRowOverflow.expandIndicator {
                SelectChip(
                    text = stringResource(R.string.action_see_more),
                    backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    textColor = Color.White,
                    onClick = { isExpanded.value = true }
                )
            }
        ) {
            allTemperaments.forEach { temperament ->
                val isSelected = selectedTemperaments.contains(temperament)
                SelectChip(
                    text = temperament,
                    backgroundColor = if (isSelected) BrandBlue else MaterialTheme.colorScheme.tertiary,
                    textColor = if (isSelected) Color.White else BrandBlue,
                    onClick = {
                        val newSelection = selectedTemperaments.toMutableSet()
                        if (isSelected) newSelection.remove(temperament)
                        else if (newSelection.size < MAX_CHIPS_TO_COLLECT) newSelection.add(temperament)
                        onSelectionChanged(newSelection)
                    }
                )
            }
            if (isExpanded.value) {
                SelectChip(
                    text = stringResource(R.string.action_show_less),
                    backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    textColor = Color.White,
                    onClick = { isExpanded.value = false }
                )
            }
        }
    }
}

@Composable
private fun LifeExpectancyField(
    minLife: String,
    minLifeError: String,
    maxLife: String,
    maxLifeError: String,
    onMinLifeChange: (String) -> Unit,
    onMaxLifeChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
    ) {
        FieldTitle(
            title = stringResource(R.string.label_life_expectancy),
            subtitle = stringResource(R.string.subtitle_life_expectancy)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(InterItemSpacing),
        ) {
            val numberFilter = { input: String ->
                if (input.length <= 2) input.filter { it.isDigit() } else null
            }

            NewBreedTextField(
                modifier = Modifier.weight(DEFAULT_WEIGHT),
                value = minLife,
                onValueChange = { numberFilter(it)?.let(onMinLifeChange) },
                label = stringResource(R.string.label_min),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = minLifeError.isNotEmpty(),
                errorMessage = minLifeError
            )
            NewBreedTextField(
                modifier = Modifier.weight(DEFAULT_WEIGHT),
                value = maxLife,
                onValueChange = { numberFilter(it)?.let(onMaxLifeChange) },
                label = stringResource(R.string.label_max),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = maxLifeError.isNotEmpty(),
                errorMessage = maxLifeError
            )
        }
    }
}

@Composable
private fun NewBreedImageField(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    FieldTitle(
        title = stringResource(R.string.label_photo),
        subtitle = stringResource(R.string.subtitle_photo_selection)
    )
    Row(
        modifier = modifier
            .height(TertiaryItemImageSize)
            .padding(bottom = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(InterItemSpacing)
    ) {
        Surface(
            modifier = Modifier
                .weight(DEFAULT_WEIGHT)
                .fillMaxHeight(),
            shape = RoundedCornerShape(CardCornerRadius),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.cd_selected_image),
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
                        contentDescription = stringResource(R.string.cd_placeholder_image)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(DEFAULT_WEIGHT)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(InterItemSpacing)
        ) {
            ImageSourceOption(
                label = stringResource(R.string.label_gallery),
                icon = Icons.Default.PhotoLibrary,
                onClick = onGalleryClick,
                modifier = Modifier.weight(DEFAULT_WEIGHT)
            )
            ImageSourceOption(
                label = stringResource(R.string.label_camera),
                icon = Icons.Default.PhotoCamera,
                onClick = onCameraClick,
                modifier = Modifier.weight(DEFAULT_WEIGHT)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OriginField(
    modifier: Modifier = Modifier,
    value: String,
    expanded: Boolean,
    options: List<String>,
    maxCharCount: Int? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    onValueChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit
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
            label = stringResource(R.string.label_origin),
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
    label: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    maxCharCount: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = {
            if (maxCharCount == null || it.length <= maxCharCount) {
                onValueChange(it)
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
                Box(modifier = Modifier.weight(DEFAULT_WEIGHT)) {
                    if (isError) Text(text = errorMessage, color = BrandRed)
                }
                if (maxCharCount != null) {
                    Text(
                        text = stringResource(R.string.subtitle_max_char_count, value.length, maxCharCount),
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

// Helper Composables
@Composable
private fun ImageSourceOption(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
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

@Composable
private fun FieldTitle(
    title: String,
    subtitle: String = ""
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            InterItemSpacing,
            Alignment.Start
        ),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = title, style = titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}