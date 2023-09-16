package ap.panini.notflashy.ui.library.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.navigation.NavigationDestination
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

object StudyDestination : NavigationDestination {
    override val route = "studySet"
    const val setIdArg = "setId"
    const val isShuffledArg = "isShuffled"
    const val onlyStarredArg = "onlyStarred"
    override val routeWithArgs = "$route/{$setIdArg}?$isShuffledArg={$isShuffledArg}" +
        "&$onlyStarredArg={$onlyStarredArg}"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyScreen(
    onComposing: (BottomAppBarViewState) -> Unit,
    navigateBack: () -> Unit,
    viewModel: StudyViewModel = hiltViewModel()
) {
    val studyUiState = viewModel.state.collectAsState()
    val pagerState = rememberPagerState { studyUiState.value.cards.size + 1 }
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        Modifier.fillMaxSize()
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

    SideEffect {
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

                    AnimatedVisibility(
                        visible = pagerState.currentPage < studyUiState.value.cards.size
                    ) {
                        Row {
                            // if (pagerState.currentPage < studyUiState.value.cards.size) {
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

                            IconButton(onClick = {
                                viewModel.updateFlipped(pagerState.currentPage)
                            }) {
                                Icon(Icons.Default.Sync, "Flip")
                            }

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = Color.Green)) {
                                        append(
                                            studyUiState.value.marks.filter {
                                                it == Marks.Correct
                                            }.size.toString()
                                        )
                                    }

                                    append("/")

                                    withStyle(style = SpanStyle(color = Color.Red)) {
                                        append(
                                            studyUiState.value.marks.filter {
                                                it == Marks.Incorrect
                                            }.size.toString()
                                        )
                                    }

                                    append("/")

                                    append(
                                        studyUiState.value.marks.filter {
                                            it == Marks.Skipped
                                        }.size.toString()
                                    )
                                },
                                modifier = Modifier.padding(15.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = pagerState.currentPage >= studyUiState.value.cards.size
                    ) {
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
                                viewModel.updateStarred(
                                    studyUiState.value.cards[pagerState.currentPage]
                                )
                            }
                        }) {
                            if (studyUiState.value.cards.isNotEmpty()) {
                                if (studyUiState.value.cards[pagerState.currentPage].starred) {
                                    Icon(Icons.Default.Star, "Starred")
                                } else {
                                    Icon(Icons.Default.StarBorder, "Not Starred")
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
            .fillMaxSize()
            .clickable { updateFlipped(index) }
            .padding(start = 15.dp, end = 15.dp),
        contentAlignment = Alignment.Center

    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()

        ) {
            MarkdownText(
                modifier = Modifier.fillMaxSize(),
                markdown = if (!isFlipped) card.frontText else card.backText,
                style = MaterialTheme.typography.headlineLarge,
                color = LocalContentColor.current,
                onClick = { updateFlipped(index) }
            )
        }
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
