package ap.panini.notflashy.ui.library.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.AppViewModelProvider
import kotlinx.coroutines.launch

@Composable
fun EditSetScreen(
    navigateBack: () -> Unit,
    viewModel: EditSetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().padding(15.dp),
        state = listState,
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
            FlashCard(item, index, viewModel::updateCardUiState)
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround

            ) {
                Button(onClick = {
                    viewModel.addEmptyCard()
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = listState.layoutInfo.totalItemsCount - 1
                        )
                    }
                }) {
                    Icon(Icons.Filled.Add, "New Card")
                }
                Button(onClick = {
                    coroutineScope.launch {
                        viewModel.saveSet()
                    }
                    navigateBack()
                }) {
                    Icon(Icons.Filled.Done, "Save")
                }
            }
        }
    }

    SideEffect {
        if (viewModel.initialNavTo != -1) {
            coroutineScope.launch {
                listState.animateScrollToItem(viewModel.initialNavTo)
            }
        }
    }
}

@Composable
private fun FlashCard(card: Card, index: Int, update: (Card, Int) -> Unit) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
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
        }

        Text(
            text = (index + 1).toString(),
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
