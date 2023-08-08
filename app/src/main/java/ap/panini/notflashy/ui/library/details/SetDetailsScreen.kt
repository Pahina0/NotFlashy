package ap.panini.notflashy.ui.library.details

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.AppViewModelProvider

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SetDetailsScreen(
    navigateToStudy: (Long, Boolean, Boolean) -> Unit,
    navigateToSetEdit: (Long, Int) -> Unit,
    viewModel: SetDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val setDetailsUiState = viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = setDetailsUiState.value.set.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                FilterChip(selected = setDetailsUiState.value.filters.filterShuffle, onClick = {
                    viewModel.updateShuffleFilter(
                        !setDetailsUiState.value.filters.filterShuffle
                    )
                }, label = { Text(text = "Shuffle") }, leadingIcon = {
                    if (setDetailsUiState.value.filters.filterShuffle) {
                        Icon(
                            Icons.Default.Check,
                            "Selected"
                        )
                    } else {
                        Icon(Icons.Default.Shuffle, "Shuffle")
                    }
                })

                FilterChip(selected = setDetailsUiState.value.filters.filterStared, onClick = {
                    viewModel.updateStaredFilter(
                        !setDetailsUiState.value.filters.filterStared
                    )
                }, label = { Text(text = "Stared") }, leadingIcon = {
                    if (setDetailsUiState.value.filters.filterStared) {
                        Icon(
                            Icons.Default.Check,
                            "Selected"
                        )
                    } else {
                        Icon(Icons.Default.Star, "Stared")
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
                        setDetailsUiState.value.filters.filterStared
                    )
                },
                Modifier.fillMaxWidth()

            ) {
                Icon(Icons.Default.Book, "Study")
                Text(text = "Study")
            }
        }

        item {
            Button(
                onClick = { navigateToSetEdit(setDetailsUiState.value.set.uid, 0) },
                Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, "Edit")
                Text(text = "Edit")
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        itemsIndexed(setDetailsUiState.value.cards) { index, item ->
            FlashCard(item, index, viewModel::updateStared, navigateToSetEdit)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FlashCard(
    card: Card,
    index: Int,
    updateStared: (Card) -> Unit,
    navigateToSetEdit: (Long, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .combinedClickable(
                onClick = { updateStared(card.copy(stared = !card.stared)) },
                onLongClick = { navigateToSetEdit(card.setId, index) }
            ),
        // onClick = { updateStared(card.copy(stared = !card.stared)) },

        colors = if (card.stared) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }

    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.frontText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold

            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = card.backText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
