package ap.panini.notflashy.ui.library.edit

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.AppViewModelProvider
import ap.panini.notflashy.ui.navigation.NavigationDestination
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

@OptIn(ExperimentalMaterial3Api::class)
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

    BackHandler() {
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
    Column(
        modifier = modifier
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = card.frontText,
            label = { Text(text = "Front Text") },

            onValueChange = {
                update(card.copy(frontText = it), index)
            },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        TextField(
            value = card.backText,
            label = { Text(text = "Back Text") },

            onValueChange = {
                update(card.copy(backText = it), index)
            },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )
    }
}

@Composable
private fun FlashCardActions(
    card: Card,
    index: Int,
    update: (Card, Int) -> Unit,
    reorderableState: ReorderableLazyListState
) {
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

        Text(
            text = (index + 1).toString(),
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
