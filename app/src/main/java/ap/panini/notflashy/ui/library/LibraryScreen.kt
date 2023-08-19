package ap.panini.notflashy.ui.library

import android.icu.text.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.ui.AppViewModelProvider
import ap.panini.notflashy.ui.navigation.NavigationDestination

object LibraryDestination : NavigationDestination {
    override val route = "library"
    override val routeWithArgs = route
}

@Composable
fun LibraryScreen(
    onComposing: (BottomAppBarViewState) -> Unit,
    navigateToSetStudy: (Long) -> Unit,
    navigateToSetEdit: (Long) -> Unit,
    navigateToSetEntry: (Long) -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val libraryUiState by viewModel.state.collectAsState()

    LaunchedEffect(true) {
        onComposing(

            BottomAppBarViewState(
                actions = {
                    AnimatedVisibility(
                        visible = libraryUiState.selected == null
                    ) {
                        IconButton(
                            onClick = {
                                if (libraryUiState.sets.isNotEmpty()) {
                                    navigateToSetStudy(libraryUiState.sets[0].uid)
                                }
                            }
                        ) {
                            Icon(Icons.Default.OpenInNew, "Open Recent")
                        }
                    }

                    AnimatedVisibility(
                        visible = libraryUiState.selected != null
                    ) {
                        Row {
                            IconButton(onClick = { viewModel.updateSelected(null) }) {
                                Icon(Icons.Default.Close, "Close")
                            }

                            IconButton(onClick = {
                                navigateToSetEntry(libraryUiState.selected!!.uid)
                            }) {
                                Icon(Icons.Default.OpenInNew, "Open")
                            }

                            IconButton(onClick = {
                                navigateToSetEdit(libraryUiState.selected!!.uid)
                            }) {
                                Icon(Icons.Default.Edit, "Edit")
                            }
                        }
                    }
                },

                floatingActionButton = {
                    if (libraryUiState.selected == null) {
                        FloatingActionButton(onClick = { navigateToSetEdit(-1) }) {
                            Icon(Icons.Default.Add, "New Set")
                        }
                    } else {
                        FloatingActionButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(Icons.Default.Delete, "Delete Set")
                        }
                    }
                }
            )
        )
    }

    LazyColumn {
        itemsIndexed(libraryUiState.sets) { _, item ->
            FlashCardsSet(
                set = item,
                navigateToSetEntry,
                libraryUiState.selected,
                viewModel::updateSelected
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FlashCardsSet(
    set: Set,
    navigateToSetEntry: (Long) -> Unit,
    selectedSet: Set?,
    updateSelectedSet: (Set?) -> Unit
) {
    val dateFormat = DateFormat.getDateInstance()

    Column(Modifier.clickable { navigateToSetEntry(set.uid) }) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
                .combinedClickable(
                    onClick = { navigateToSetEntry(set.uid) },
                    onLongClick = { updateSelectedSet(set) }
                ),

            colors = if (selectedSet == set) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            } else {
                CardDefaults.cardColors()
            }

        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = set.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "C: ${dateFormat.format(set.creationDate)}",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = "V: ${dateFormat.format(set.lastViewedDate)}",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = "M: ${dateFormat.format(set.lastModifiedDate)}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
