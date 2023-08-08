package ap.panini.notflashy.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LibraryScreen(
    navigateToSetEdit: (Long) -> Unit,
    navigateToSetEntry: (Long) -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val libraryUiState by viewModel.libraryScreenUiState.collectAsState()
    LazyColumn {
        itemsIndexed(libraryUiState.sets) { index, item ->
            FlashCardsSet(set = item, navigateToSetEntry)
        }
    }
}

@Composable
private fun FlashCardsSet(set: Set, navigateToSetEntry: (Long) -> Unit) {
    val sdfDate = SimpleDateFormat(
        "dd/MM/yyyy hh:mm:ss",
        Locale.getDefault()
    )

    Column(Modifier.clickable { navigateToSetEntry(set.uid) }) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
                .clickable { navigateToSetEntry(set.uid) }

        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = set.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "C: ${sdfDate.format(set.creationDate)}",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = "V: ${sdfDate.format(set.lastViewedDate)}",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = "M: ${sdfDate.format(set.lastModifiedDate)}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
