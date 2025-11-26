package com.example.catbreeds.breed_list

import androidx.compose.animation.animateContentSize
import com.example.catbreeds.core.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.catbreeds.core.ui.theme.AppDimensions.BarShadow
import com.example.catbreeds.core.ui.theme.AppDimensions.CardCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.DefaultWeight
import com.example.catbreeds.core.ui.theme.AppDimensions.InnerCornerRadius
import com.example.catbreeds.core.ui.theme.AppDimensions.InterItemSpacing
import com.example.catbreeds.core.ui.theme.AppDimensions.LazyColumnBottomPaddingForNav
import com.example.catbreeds.core.ui.theme.AppDimensions.MaxCharCountLarge
import com.example.catbreeds.core.ui.theme.AppDimensions.MaxCharCountSmall
import com.example.catbreeds.core.ui.theme.AppDimensions.MaxChipsToSelect
import com.example.catbreeds.core.ui.theme.AppDimensions.ScreenPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryCardPadding
import com.example.catbreeds.core.ui.theme.AppDimensions.SheetTopPadding
import com.example.catbreeds.core.ui.theme.AppTypography.bodyMedium
import com.example.catbreeds.core.ui.theme.AppTypography.headlineMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleMedium
import com.example.catbreeds.core.ui.theme.AppTypography.titleSmall
import com.example.catbreeds.core.ui.theme.BrandBlue
import com.example.catbreeds.core.ui.theme.BrandRed
import com.example.catbreeds.core.ui.theme.ShadowColor
import com.example.catbreeds.core.util.ErrorHandler
import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.layout.ContentScale
import com.example.catbreeds.core.ui.theme.AppDimensions.MediumIconSize
import com.example.catbreeds.core.ui.theme.AppDimensions.SecondaryItemImageSize
import com.example.catbreeds.core.ui.theme.AppDimensions.SmallIconSize

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BreedListScreen(
    viewModel: BreedListViewModel = hiltViewModel(),
    onBreedClick: (String) -> Unit
) {
    val breeds by viewModel.breeds
    val searchQuery by viewModel.searchQuery
    val filteredBreeds by viewModel.filteredBreeds
    val isSearchActive = remember { mutableStateOf(false) }

    val isNewBreedActive = remember { mutableStateOf(false) }
    val isSheetDirty = remember { mutableStateOf(false) }
    val showUnsavedChangesDialog = remember { mutableStateOf(false) }

    val newBreedSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && isSheetDirty.value) {
                showUnsavedChangesDialog.value = true
                false
            } else {
                true
            }
        }
    )
    val scope = rememberCoroutineScope()

    val allNames by viewModel.allNames
    val allTemperaments by viewModel.allTemperaments
    val allOrigins by viewModel.allOrigins

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Handles snackbar and error messages
    val errorMessage by viewModel.errorMessage
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onErrorShown = viewModel::clearError
    )

    // Scroll state for dynamic shadow
    val lazyGridState = rememberLazyStaggeredGridState()
    val showTopBarShadow = remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive.value) {
            focusRequester.requestFocus()
        }
    }

    val handleSheetClose: (Boolean) -> Unit = { force ->
        if (force || !isSheetDirty.value) {
            scope.launch {
                isSheetDirty.value = false
                newBreedSheetState.hide()
            }.invokeOnCompletion {
                if (!newBreedSheetState.isVisible) {
                    isNewBreedActive.value = false
                }
            }
        } else {
            showUnsavedChangesDialog.value = true
        }
    }

    if (isNewBreedActive.value) {
        ModalBottomSheet(
            onDismissRequest = { isNewBreedActive.value = false },
            sheetState = newBreedSheetState,
            modifier = Modifier.padding(top = SheetTopPadding),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            NewBreedSheetContent(
                allNames = allNames,
                allOrigins = allOrigins,
                allTemperaments = allTemperaments,
                onDismiss = { handleSheetClose(false) },
                onDirtyChange = { isSheetDirty.value = it },
                onSave = { name, origin, desc, temps, min, max, imageUrl ->
                    viewModel.addNewBreed(
                        name = name,
                        origin = origin,
                        description = desc,
                        temperaments = temps,
                        minLife = min,
                        maxLife = max,
                        imageUrl = imageUrl,
                        onSuccess = { newId ->
                            handleSheetClose(true)
                            onBreedClick(newId)
                        }
                    )
                }
            )
        }
    }

    if (showUnsavedChangesDialog.value) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog.value = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedChangesDialog.value = false
                    handleSheetClose(true)
                }) {
                    Text("Discard", color = BrandRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog.value = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    // Content
    Scaffold(
        topBar = {
            val topAppBarColors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
            val topBarModifier = if (showTopBarShadow.value) {
                Modifier.shadow(
                    elevation = BarShadow,
                    spotColor = ShadowColor
                )
            } else {
                Modifier
            }

            if (isSearchActive.value) {
                TopAppBar(
                    modifier = topBarModifier,
                    colors = topAppBarColors,
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search breeds...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive.value = false
                            viewModel.updateSearchQuery("")
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Close search")
                        }
                    }
                )
            } else {
                TopAppBar(
                    modifier = topBarModifier,
                    colors = topAppBarColors,
                    title = {
                        Text(
                            text = "Cat Breeds",
                            style = headlineMedium
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive.value = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search breeds")
                        }
                        IconButton(onClick = { isNewBreedActive.value = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New breed")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // List
            if (breeds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = lazyGridState,
                    modifier = Modifier.fillMaxSize(),
                    verticalItemSpacing = InterItemSpacing,
                    horizontalArrangement = Arrangement.spacedBy(InterItemSpacing),
                    contentPadding = PaddingValues(
                        start = ScreenPadding,
                        top = ScreenPadding,
                        end = ScreenPadding,
                        bottom = LazyColumnBottomPaddingForNav
                    )
                ) {
                    items(filteredBreeds) { breed ->
                        BreedCard(
                            breed = breed,
                            onClick = { onBreedClick(breed.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(breed.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NewBreedSheetContent(
    allNames: List<String>,
    allOrigins: List<String>,
    allTemperaments: List<String>,
    onDismiss: () -> Unit,
    onDirtyChange: (Boolean) -> Unit,
    onSave: (String, String, String, List<String>, String, String, String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val origin = remember { mutableStateOf("") }
    val minLife = remember { mutableStateOf("") }
    val maxLife = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }

    val selectedTemperaments = remember { mutableStateOf(setOf<String>()) }
    val isTemperamentsExpanded = remember { mutableStateOf(false) }
    val isOriginDropdownActive = remember { mutableStateOf(false) }

    val selectedImageUri = remember { mutableStateOf<android.net.Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri.value = uri
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
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
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
    imageUri: android.net.Uri?,
    onClick: () -> Unit,
    label: String = "Add Photo"
) {
    Column(modifier = modifier) {
        Surface(
            onClick = onClick,
            modifier = Modifier.height(SecondaryItemImageSize),
            shape = RoundedCornerShape(CardCornerRadius),
            color = MaterialTheme.colorScheme.surface,
        ) {
            if (imageUri != null) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(),
                        contentScale = ContentScale.FillHeight
                    )

                    // Edit Overlay
                    Surface(
                        modifier = Modifier.padding(SecondaryCardPadding),
                        shape = RoundedCornerShape(InnerCornerRadius),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(SecondaryCardPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Image",
                                modifier = Modifier.size(SmallIconSize),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(InterItemSpacing))
                            Text(
                                text = "Edit",
                                style = titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(DefaultWeight),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(MediumIconSize),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(SecondaryCardPadding))
                        Text(
                            text = label,
                            style = titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
private fun BreedCard(
    breed: Breed,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(
                elevation = BarShadow,
                spotColor = ShadowColor,
                shape = RoundedCornerShape(CardCornerRadius)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Cat image
            AsyncImage(
                model = breed.imageUrl,
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = CardCornerRadius,
                            topEnd = CardCornerRadius
                        )
                    ),
                placeholder = painterResource(id = R.drawable.ic_cat_placeholder),
                error = painterResource(id = R.drawable.ic_cat_error)
            )

            // Name and origin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SecondaryCardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(DefaultWeight)) {
                    Text(
                        text = breed.name,
                        style = titleMedium
                    )
                    Text(
                        text = breed.origin,
                        style = bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Favorite button
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (breed.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (breed.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = BrandRed
                    )
                }
            }
        }
    }
}