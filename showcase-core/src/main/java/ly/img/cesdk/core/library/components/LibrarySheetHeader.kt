package ly.img.cesdk.core.library.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ly.img.cesdk.core.R
import ly.img.cesdk.core.iconpack.Arrowback
import ly.img.cesdk.core.iconpack.Close
import ly.img.cesdk.core.iconpack.Expandmore
import ly.img.cesdk.core.iconpack.IconPack
import ly.img.cesdk.core.iconpack.Search
import ly.img.cesdk.core.library.state.AssetLibraryUiState
import ly.img.cesdk.core.library.util.LibraryEvent
import ly.img.cesdk.core.theme.surface3

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun LibrarySearchHeader(
    uiState: State<AssetLibraryUiState>,
    onLibraryEvent: (LibraryEvent) -> Unit,
    onBack: () -> Unit,
    onSearchFocus: () -> Unit
) {
    val uiStateValue = uiState.value
    AnimatedContent(
        targetState = uiStateValue.isInSearchMode,
        transitionSpec = {
            if (targetState) {
                fadeIn(animationSpec = tween(250)) + slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(250)
                ) with fadeOut(animationSpec = tween(250))
            } else {
                fadeIn(animationSpec = tween(100, delayMillis = 100)) with
                    fadeOut(animationSpec = tween(100)) + slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(100)
                )
            }
        }, label = "SearchAnimation"
    ) { isInSearchMode ->
        if (isInSearchMode) {
            val focusRequester = remember { FocusRequester() }
            var textFieldValue by remember {
                mutableStateOf(
                    TextFieldValue(uiStateValue.searchText, TextRange(uiStateValue.searchText.length))
                )
            }
            SearchTextField(
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            onSearchFocus()
                        }
                    }
                    .focusRequester(focusRequester)
                    .padding(8.dp)
                    .fillMaxWidth(),
                textFieldValue = textFieldValue,
                placeholder = {
                    Text(stringResource(R.string.cesdk_search_placeholder, stringResource(id = uiStateValue.titleRes)))
                },
                onSearch = {
                    onLibraryEvent(LibraryEvent.OnEnterSearchMode(enter = false, uiStateValue.libraryCategory))
                },
                onValueChange = {
                    textFieldValue = it
                    onLibraryEvent(LibraryEvent.OnSearchTextChange(it.text, uiStateValue.libraryCategory, debounce = true))
                },
                leadingIcon = {
                    IconButton(onClick = {
                        onLibraryEvent(LibraryEvent.OnEnterSearchMode(enter = false, uiStateValue.libraryCategory))
                    }) {
                        Icon(IconPack.Arrowback, contentDescription = stringResource(R.string.cesdk_back))
                    }
                },
                trailingIcon = {
                    if (uiStateValue.searchText.isNotEmpty()) {
                        IconButton(onClick = {
                            textFieldValue = textFieldValue.copy(text = "", selection = TextRange(0))
                            onLibraryEvent(LibraryEvent.OnSearchTextChange("", uiStateValue.libraryCategory))
                        }) {
                            Icon(IconPack.Close, contentDescription = stringResource(R.string.cesdk_search_clear))
                        }
                    }
                }
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = uiStateValue.titleRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiStateValue.isRoot) {
                            onBack()
                        } else {
                            onLibraryEvent(LibraryEvent.OnPopStack(uiStateValue.libraryCategory))
                        }
                    }) {
                        Icon(
                            if (uiStateValue.isRoot) IconPack.Expandmore else IconPack.Arrowback,
                            contentDescription = stringResource(R.string.cesdk_back)
                        )
                    }
                },
                actions = {
                    val searchQuery = uiStateValue.searchText
                    if (searchQuery.isNotEmpty()) {
                        InputChip(
                            selected = true,
                            onClick = {
                                onLibraryEvent(LibraryEvent.OnEnterSearchMode(enter = true, uiStateValue.libraryCategory))
                            },
                            label = {
                                Text(
                                    searchQuery,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    onLibraryEvent(LibraryEvent.OnSearchTextChange("", uiStateValue.libraryCategory))
                                }, Modifier.size(InputChipDefaults.IconSize)) {
                                    Icon(
                                        IconPack.Close,
                                        contentDescription = stringResource(R.string.cesdk_search_clear),
                                    )
                                }
                            },
                            shape = ShapeDefaults.Large,
                            modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                onLibraryEvent(LibraryEvent.OnEnterSearchMode(enter = true, uiStateValue.libraryCategory))
                            },
                        ) {
                            Icon(IconPack.Search, contentDescription = stringResource(id = R.string.cesdk_search))
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTextField(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.extraLarge,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface3,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface3,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val mergedTextStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = textColor))

    BasicTextField(
        value = textFieldValue,
        modifier = modifier
            .indicatorLine(
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                colors = colors,
                focusedIndicatorLineThickness = 0.dp,  // to hide the indicator line
                unfocusedIndicatorLineThickness = 0.dp // to hide the indicator line
            ),
        onValueChange = onValueChange,
        enabled = true,
        readOnly = false,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(textFieldValue.text) }),
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            // places leading icon, text field with label and placeholder, trailing icon
            TextFieldDefaults.TextFieldDecorationBox(
                value = textFieldValue.text,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = null,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = null,
                shape = shape,
                singleLine = singleLine,
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(vertical = 8.dp),
                colors = colors
            )
        }
    )
}