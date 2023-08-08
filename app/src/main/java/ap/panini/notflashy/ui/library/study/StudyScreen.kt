package ap.panini.notflashy.ui.library.study

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.ui.AppViewModelProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyScreen(viewModel: StudyViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val studyUiState = viewModel.state.collectAsState()

    val pagerState = rememberPagerState()

    HorizontalPager(pageCount = studyUiState.value.cards.size, state = pagerState) { currentPage ->
        FlashCard(
            card = studyUiState.value.cards[currentPage],
            studyUiState.value.flipped[currentPage],
            currentPage,
            viewModel::updateFlipped
        )
    }
}

@Composable
private fun FlashCard(
    card: Card,
    isFlipped: Boolean,
    index: Int,
    updateFlipped: (Int, Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .scrollable(state = scrollState, orientation = Orientation.Vertical)
            .fillMaxSize()
            .clickable { updateFlipped(index, !isFlipped) }
    ) {
        Text(text = if (!isFlipped) card.frontText else card.backText)
    }
}
