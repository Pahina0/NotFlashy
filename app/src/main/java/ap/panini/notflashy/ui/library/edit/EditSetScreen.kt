package ap.panini.notflashy.ui.library.edit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
        modifier = Modifier.fillMaxSize()
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
        modifier = Modifier.fillMaxWidth()
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
            Column(
                modifier = Modifier.padding(20.dp).weight(1f),
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
                    ).copy(
                        imeAction = ImeAction.Done
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
                    ).copy(
                        imeAction = ImeAction.Done
                    )
                )
            }

            Box(
                modifier = Modifier
                    .requiredWidth(40.dp)
                    .fillMaxHeight()
                    .detectReorder(reorderableState)
            ) {
                IconButton(
                    onClick = {
                        update(
                            card.copy(stared = !card.stared),
                            index
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(10.dp)
                ) {
                    if (card.stared) {
                        Icon(Icons.Default.Star, "Stared")
                    } else {
                        Icon(Icons.Default.StarBorder, "Not Stared")
                    }
                }

                Icon(
                    Icons.Default.DragIndicator,
                    "Drag",
                    Modifier.align(Alignment.Center).fillMaxHeight()
                )

                Text(
                    text = (index + 1).toString(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
