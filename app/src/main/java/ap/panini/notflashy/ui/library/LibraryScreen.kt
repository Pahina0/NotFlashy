package ap.panini.notflashy.ui.library

import android.content.ContentResolver
import android.database.Cursor
import android.icu.text.DateFormat
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.ui.navigation.NavigationDestination
import com.opencsv.CSVReader
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils

object LibraryDestination : NavigationDestination {
    override val route = "library"
    override val routeWithArgs = route
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onComposing: (BottomAppBarViewState) -> Unit,
    navigateToOss: () -> Unit,
    navigateToSetStudy: (Long) -> Unit,
    navigateToSetEdit: (Long) -> Unit,
    navigateToSetEntry: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val libraryUiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var poorInputDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            val inS = context.contentResolver.openInputStream(uri)!!
            val reader = CSVReader(InputStreamReader(inS))

            try {
                viewModel.importSet(queryName(context.contentResolver, uri), reader.readAll())
            } catch (e: Exception) {
                poorInputDialog = true
            }

            withContext(Dispatchers.IO) {
                inS.close()
            }
            reader.close()
        }
    }

    if (poorInputDialog) {
        AlertDialog(
            onDismissRequest = { poorInputDialog = false },

            title = {
                Text(text = "CSV Incorrect Format")
            },

            text = {
                MarkdownText(
                    markdown = "- Max 2 Columns\n" +
                        "- First Column is the Front of The Card\n" +
                        "- Second Column is the Back of The Card\n",
                    color = LocalContentColor.current
                )
            },

            confirmButton = {
                Button(onClick = { poorInputDialog = false }) {
                    Text(text = "Dismiss")
                }
            }
        )
    }

    LaunchedEffect(true) {
        onComposing(

            BottomAppBarViewState(
                actions = {
                    AnimatedVisibility(
                        visible = libraryUiState.selected == null
                    ) {
                        Row {
                            IconButton(
                                onClick = {
                                    if (libraryUiState.sets.isNotEmpty()) {
                                        navigateToSetStudy(libraryUiState.sets[0].uid)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.OpenInNew, "Open Recent")
                            }

                            PlainTooltipBox(tooltip = { Text("Import CSV") }) {
                                IconButton(
                                    onClick = {
                                        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
                                        filePickerLauncher.launch(
                                            "text/comma-separated-values"
                                        )
                                    },
                                    modifier = Modifier.tooltipAnchor()
                                ) {
                                    Icon(Icons.Default.UploadFile, "Import Csv")
                                }
                            }
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

                    IconButton(onClick = {
                        navigateToOss()
                    }) { Icon(Icons.Default.Info, "OSS") }
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

private fun queryName(resolver: ContentResolver, uri: Uri): String {
    val returnCursor: Cursor = resolver.query(uri, null, null, null, null)!!
    val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name: String = returnCursor.getString(nameIndex)
    returnCursor.close()
    return FilenameUtils.removeExtension(name)
}
