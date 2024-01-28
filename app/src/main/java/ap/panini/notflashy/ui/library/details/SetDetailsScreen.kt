package ap.panini.notflashy.ui.library.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.navigation.NavigationDestination
import dev.jeziellago.compose.markdowntext.MarkdownText

object SetDetailsDestination : NavigationDestination {
    override val route = "setDetails"
    const val SET_ID_ARG = "setId"
    override val routeWithArgs = "$route/{$SET_ID_ARG}"
}

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun SetDetailsScreen(
    onComposing: (BottomAppBarViewState) -> Unit,
    navigateToStudy: (Long, Boolean, Boolean) -> Unit,
    navigateToSetEdit: (Long, Int) -> Unit,
    viewModel: SetDetailsViewModel = hiltViewModel(),
) {
    val setDetailsUiState = viewModel.state.collectAsState()

    SideEffect {
        onComposing(
            BottomAppBarViewState(),
        )
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding()
                .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = setDetailsUiState.value.set.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                FilterChip(selected = setDetailsUiState.value.filters.filterShuffle, onClick = {
                    viewModel.updateShuffleFilter(
                        !setDetailsUiState.value.filters.filterShuffle,
                    )
                }, label = { Text(text = "Shuffle") }, leadingIcon = {
                    if (setDetailsUiState.value.filters.filterShuffle) {
                        Icon(
                            Icons.Default.Check,
                            "Selected",
                        )
                    } else {
                        Icon(Icons.Default.Shuffle, "Shuffle")
                    }
                })

                FilterChip(selected = setDetailsUiState.value.filters.filterStarred, onClick = {
                    viewModel.updateStarredFilter(
                        !setDetailsUiState.value.filters.filterStarred,
                    )
                }, label = { Text(text = "Starred") }, leadingIcon = {
                    if (setDetailsUiState.value.filters.filterStarred) {
                        Icon(
                            Icons.Default.Check,
                            "Selected",
                        )
                    } else {
                        Icon(Icons.Default.Star, "Starred")
                    }
                })
            }
        }

        item {
            Button(
                onClick = {
                    navigateToStudy(
                        setDetailsUiState.value.set.uid,
                        setDetailsUiState.value.filters.filterShuffle,
                        setDetailsUiState.value.filters.filterStarred,
                    )
                },
                Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Book, "Study")
                Text(text = "Study")
            }
        }

        item {
            Button(
                onClick = { navigateToSetEdit(setDetailsUiState.value.set.uid, 0) },
                Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Edit, "Edit")
                Text(text = "Edit")
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        itemsIndexed(setDetailsUiState.value.cards) { index, item ->
            FlashCard(
                item,
                index,
                viewModel::updateStarred,
                navigateToSetEdit,
                Modifier.animateItemPlacement(),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FlashCard(
    card: Card,
    index: Int,
    updateStarred: (Card) -> Unit,
    navigateToSetEdit: (Long, Int) -> Unit,
    modifier: Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .combinedClickable(
                    onClick = { updateStarred(card.copy(starred = !card.starred)) },
                    onLongClick = { navigateToSetEdit(card.setId, index) },
                ),
        colors =
            if (card.starred) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            } else {
                CardDefaults.cardColors()
            },
    ) {
        Row {
            Column(
                modifier =
                    Modifier
                        .padding(15.dp)
                        .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MarkdownText(
                    modifier = Modifier.fillMaxSize(),
                    markdown = card.frontText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalContentColor.current,
                )

                Spacer(modifier = Modifier.height(5.dp))

                MarkdownText(
                    modifier = Modifier.fillMaxSize(),
                    markdown = card.backText,
                    color = LocalContentColor.current,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(
                modifier =
                    Modifier
                        .requiredWidth(40.dp)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    onClick = {
                        updateStarred(card.copy(starred = !card.starred))
                    },
                ) {
                    if (card.starred) {
                        Icon(Icons.Default.Star, "Starred")
                    } else {
                        Icon(Icons.Default.StarBorder, "Not Starred")
                    }
                }
            }
        }
    }
}
