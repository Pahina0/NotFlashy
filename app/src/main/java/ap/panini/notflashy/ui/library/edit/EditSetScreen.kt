package ap.panini.notflashy.ui.library.edit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.AppViewModelProvider
import ap.panini.notflashy.ui.navigation.NavigationDestination
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

object EditSetDestination : NavigationDestination {
    override val route = "editSet"
    const val setIdArg = "setId"
    const val editSpecificArg = "editSpecific"
    override val routeWithArgs = "$route?$setIdArg={$setIdArg}&$editSpecificArg={$editSpecificArg}"
}

@Composable
fun EditSetScreen(
    onComposing: (BottomAppBarViewState) -> Unit,
    navigateBack: () -> Unit,
    viewModel: EditSetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (from.index != 0 && to.index != 0) {
                viewModel.moveCard(from.index - 1, to.index - 1)
            }
        }
    )

    var showAlertDialog by remember { mutableStateOf(false) }

    BackHandler {
        showAlertDialog = true
    }

    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = {
                showAlertDialog = false
            },
            confirmButton = {
                Button(onClick = {
                    showAlertDialog = false
                    navigateBack()
                }) {
                    Text(text = "Discard")
                }
            },

            dismissButton = {
                Button(onClick = {
                    showAlertDialog = false
                }) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = "Exit?") },
            text = { Text(text = "Exiting without saving will discard all changes") }
        )
    }

    LaunchedEffect(true) {
        onComposing(
            BottomAppBarViewState(
                actions = {
                    IconButton(onClick = {
                        viewModel.addEmptyCard()
                        coroutineScope.launch {
                            reorderableState.listState.animateScrollToItem(
                                index = reorderableState.listState.layoutInfo.totalItemsCount - 1
                            )
                        }
                    }) {
                        Icon(Icons.Default.Add, "New Card")
                    }

                    IconButton(onClick = { viewModel.removeCard() }) {
                        Icon(Icons.Default.Remove, "Remove Card")
                    }
                },

                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        coroutineScope.launch {
                            viewModel.saveSet()
                        }
                        navigateBack()
                    }) {
                        Icon(Icons.Filled.DoneAll, "Save")
                    }
                }
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(15.dp)
            .reorderable(reorderableState)
            .detectReorder(reorderableState),
        state = reorderableState.listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            OutlinedTextField(
                value = viewModel.editSetUiState.set.title,
                label = { Text(text = "Set Title") },

                onValueChange = {
                    viewModel.updateTitleUiState(
                        viewModel.editSetUiState.set.copy(
                            title = it
                        )
                    )
                },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ).copy(imeAction = ImeAction.Done)
            )
        }

        itemsIndexed(viewModel.editSetUiState.cards) { index, item ->
            ReorderableItem(
                reorderableState = reorderableState,
                index = index + 1,
                key = item
            ) { isDragging ->
                FlashCard(
                    item,
                    index,
                    viewModel::updateCardUiState,
                    isDragging,
                    viewModel.editSetUiState.selectedIndex,
                    viewModel::updateSelected,
                    reorderableState
                )
            }
        }
    }

    SideEffect {
        if (viewModel.initialNavTo != -1) {
            coroutineScope.launch {
                reorderableState.listState.animateScrollToItem(viewModel.initialNavTo)
            }
        }
    }
}

@Composable
private fun FlashCard(
    card: Card,
    index: Int,
    update: (Card, Int) -> Unit,
    isDragging: Boolean,
    selectedIndex: Int,
    updateSelected: (Int) -> Unit,
    reorderableState: ReorderableLazyListState
) {
    val focusManager = LocalFocusManager.current
    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { updateSelected(index) }
            .padding(vertical = 5.dp)
            .shadow(elevation.value),

        colors = if (selectedIndex == index) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            FlashCardMain(Modifier.weight(1f), card, index, update, focusManager)

            FlashCardActions(
                card,
                index,
                update,
                reorderableState
            )
        }
    }
}

@Composable
private fun FlashCardMain(
    modifier: Modifier,
    card: Card,
    index: Int,
    update: (Card, Int) -> Unit,
    focusManager: FocusManager
) {
    // true front, false back
    var sideSelected by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = modifier
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) {
                    sideSelected = true
                } else if (sideSelected != false) sideSelected = null
            },
            value = card.frontText,
            label = { Text(text = "Front Text") },
            onValueChange = {
                update(card.copy(frontText = it), index)
            },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            )

        )

        TextField(
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) {
                    sideSelected = false
                } else if (sideSelected != true) sideSelected = null
            },
            value = card.backText,
            label = { Text(text = "Back Text") },
            onValueChange = {
                update(card.copy(backText = it), index)
            },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        AnimatedVisibility(visible = sideSelected != null) {
            if (sideSelected == null) {
                MarkdownActions { }
            } else {
                if (sideSelected as Boolean) {
                    MarkdownActions { update(card.copy(frontText = card.frontText + it), index) }
                } else {
                    MarkdownActions { update(card.copy(backText = card.backText + it), index) }
                }
            }
        }
    }
}

@Composable
private fun FlashCardActions(
    card: Card,
    index: Int,
    update: (Card, Int) -> Unit,
    reorderableState: ReorderableLazyListState
) {
    var showPreview by remember { mutableStateOf(false) }

    if (showPreview) {
        val scrollState = rememberScrollState()

        AlertDialog(
            title = { Text(text = "Preview") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    MarkdownText(
                        modifier = Modifier.fillMaxWidth(),
                        markdown = card.frontText,
                        color = LocalContentColor.current
                    )

                    Divider()

                    MarkdownText(
                        modifier = Modifier.fillMaxWidth(),
                        markdown = card.backText,
                        color = LocalContentColor.current
                    )
                }
            },
            onDismissRequest = { showPreview = false },
            confirmButton = {
                Button(onClick = {
                    showPreview = false
                }) {
                    Text(text = "Save")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .requiredWidth(40.dp)
            .fillMaxHeight()
            .detectReorder(reorderableState),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                update(
                    card.copy(stared = !card.stared),
                    index
                )
            }
        ) {
            if (card.stared) {
                Icon(Icons.Default.Star, "Stared")
            } else {
                Icon(Icons.Default.StarBorder, "Not Stared")
            }
        }

        Icon(
            Icons.Default.DragIndicator,
            "Drag"
        )

        IconButton(onClick = { showPreview = true }) {
            Icon(Icons.Default.Preview, "Preview")
        }

        Text(
            text = (index + 1).toString(),
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun MarkdownActions(
    updateText: (String) -> Unit
) {
    var showAlertDialog by remember { mutableIntStateOf(-1) }

    MarkdownAlertDialogs(
        updateSelected = { showAlertDialog = it },
        showAlertDialog = showAlertDialog,
        newText = { updateText(it) }
    )

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = {
            showAlertDialog = 0
        }) {
            Icon(Icons.Default.FormatBold, "Bold")
        }

        IconButton(onClick = { showAlertDialog = 1 }) {
            Icon(Icons.Default.FormatItalic, "Italicize")
        }

        IconButton(onClick = { showAlertDialog = 2 }) {
            Icon(Icons.Default.AddPhotoAlternate, "Add Image")
        }

        IconButton(onClick = { showAlertDialog = 3 }) {
            Icon(Icons.Default.AddLink, "Add Link")
        }
    }
}

@Composable
fun MarkdownAlertDialogs(
    updateSelected: (Int) -> Unit,
    showAlertDialog: Int,
    newText: (String) -> Unit
) {
    when (showAlertDialog) {
        0 -> {
            var text by remember { mutableStateOf("") }

            AlertDialog(
                title = { Text(text = "Bold") },
                text = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(text = "Text") }
                    )
                },
                onDismissRequest = { updateSelected(-1) },
                dismissButton = {
                    Button(onClick = { updateSelected(-1) }) {
                        Text(text = "Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        updateSelected(-1)
                        newText("**$text**")
                    }) {
                        Text(text = "Save")
                    }
                }
            )
        }

        1 -> {
            var text by remember { mutableStateOf("") }

            AlertDialog(
                title = { Text(text = "Italicize") },
                text = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(text = "Text") }
                    )
                },
                onDismissRequest = { updateSelected(-1) },
                dismissButton = {
                    Button(onClick = { updateSelected(-1) }) {
                        Text(text = "Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        updateSelected(-1)
                        newText("*$text*")
                    }) {
                        Text(text = "Save")
                    }
                }
            )
        }

        2 -> {
            var url by remember { mutableStateOf("") }
            var disc by remember { mutableStateOf("") }

            AlertDialog(
                title = { Text(text = "Add Image") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text(text = "Image Link") }
                        )

                        OutlinedTextField(
                            value = disc,
                            onValueChange = { disc = it },
                            label = { Text(text = "Description") }
                        )
                    }
                },
                onDismissRequest = { updateSelected(-1) },
                dismissButton = {
                    Button(onClick = { updateSelected(-1) }) {
                        Text(text = "Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        updateSelected(-1)
                        newText("![$disc]($url)")
                    }) {
                        Text(text = "Save")
                    }
                }
            )
        }

        3 -> {
            var url by remember { mutableStateOf("") }
            var text by remember { mutableStateOf("") }

            AlertDialog(
                title = { Text(text = "Embed Link") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text(text = "Link") }
                        )

                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text(text = "Text") }
                        )
                    }
                },
                onDismissRequest = { updateSelected(-1) },
                dismissButton = {
                    Button(onClick = { updateSelected(-1) }) {
                        Text(text = "Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        updateSelected(-1)
                        newText("[$text]($url)")
                    }) {
                        Text(text = "Save")
                    }
                }
            )
        }
    }
}
