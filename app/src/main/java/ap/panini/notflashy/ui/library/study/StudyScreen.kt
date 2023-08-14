package ap.panini.notflashy.ui.library.study

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.AppViewModelProvider
import ap.panini.notflashy.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object StudyDestination : NavigationDestination {
    override val route = "studySet"
    const val setIdArg = "setId"
    const val isShuffledArg = "isShuffled"
    const val onlyStaredArg = "onlyStared"
    override val routeWithArgs = "$route/{$setIdArg}?$isShuffledArg={$isShuffledArg}" +
        "&$onlyStaredArg={$onlyStaredArg}"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyScreen(
    onComposing: (BottomAppBarViewState) -> Unit,
    navigateBack: () -> Unit,
    viewModel: StudyViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val studyUiState = viewModel.state.collectAsState()
    val pagerState = rememberPagerState { studyUiState.value.cards.size + 1 }
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState
    ) { currentPage ->
        if (currentPage < studyUiState.value.cards.size) {
            FlashCard(
                card = studyUiState.value.cards[currentPage],
                studyUiState.value.flipped[currentPage],
                currentPage,
                viewModel::updateFlipped
            )
        } else {
            EndScreen(studyUiState.value.marks)
        }
    }

    LaunchedEffect(true) {
        onComposing(
            BottomAppBarViewState(
                actions = {
                    with(pagerState.currentPage - 1) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(this@with)
                                }
                            },
                            enabled = this >= 0
                        ) {
                            Icon(Icons.Default.ArrowLeft, "Left")
                        }
                    }

                    with(pagerState.currentPage + 1) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(this@with)
                                }
                            },
                            enabled = this <= studyUiState.value.cards.size
                        ) {
                            Icon(Icons.Default.ArrowRight, "Right")
                        }
                    }

                    if (pagerState.currentPage < studyUiState.value.cards.size) {
                        IconButton(onClick = {
                            viewModel.updateMark(pagerState.currentPage, Marks.Correct)
                            with(pagerState.currentPage + 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(this@with)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Check, "Correct")
                        }

                        IconButton(onClick = {
                            viewModel.updateMark(pagerState.currentPage, Marks.Incorrect)
                            with(pagerState.currentPage + 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(this@with)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Close, "Incorrect")
                        }

                        IconButton(onClick = { viewModel.updateFlipped(pagerState.currentPage) }) {
                            Icon(Icons.Default.Sync, "Flip")
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.initiateStudy()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }) {
                            Icon(Icons.Default.Replay, "Restart")
                        }
                    }
                },
                floatingActionButton = {
                    if (pagerState.currentPage < studyUiState.value.cards.size) {
                        FloatingActionButton(onClick = {
                            if (studyUiState.value.cards.isNotEmpty()) {
                                viewModel.updateStared(
                                    studyUiState.value.cards[pagerState.currentPage]
                                )
                            }
                        }) {
                            if (studyUiState.value.cards.isNotEmpty()) {
                                if (studyUiState.value.cards[pagerState.currentPage].stared) {
                                    Icon(Icons.Default.Star, "Stared")
                                } else {
                                    Icon(Icons.Default.StarBorder, "Not Stared")
                                }
                            }
                        }
                    } else {
                        FloatingActionButton(onClick = {
                            navigateBack()
                        }) {
                            Icon(Icons.Default.Done, "Finish Study")
                        }
                    }
                }
            )
        )
    }
}

@Composable
private fun FlashCard(
    card: Card,
    isFlipped: Boolean,
    index: Int,
    updateFlipped: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .scrollable(state = scrollState, orientation = Orientation.Vertical)
            .fillMaxSize()
            .clickable { updateFlipped(index) }
            .padding(15.dp)
    ) {
        Text(text = if (!isFlipped) card.frontText else card.backText)
    }
}

@Composable
private fun EndScreen(marks: List<Marks>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(text = "Correct: ${marks.filter { it == Marks.Correct }.size}/${marks.size}")
            Text(text = "Incorrect: ${marks.filter { it == Marks.Incorrect }.size}/${marks.size}")
            Text(text = "Skipped: ${marks.filter { it == Marks.Skipped }.size}/${marks.size}")
        }
    }
}
