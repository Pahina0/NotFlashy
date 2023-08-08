package ap.panini.notflashy.ui.library.study

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.repositories.SetWithCardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyViewModel(
    savedStateHandle: SavedStateHandle,
    setWithCardsRepository: SetWithCardsRepository
) : ViewModel() {
    private val setId = savedStateHandle.get<Long>("setId") ?: -1

    private val isShuffled = savedStateHandle.get<Boolean>("isShuffled") ?: false

    private val onlyStared = savedStateHandle.get<Boolean>("onlyStared") ?: false

    private val setDetails: StateFlow<StudyUiState> =
        setWithCardsRepository.getSetWithCards(setId).map {
            StudyUiState(
                it.set,
                it.cards
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = StudyUiState()
        )

    private val flippedCards = MutableStateFlow(listOf<Boolean>())

    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState>
        get() = _state

    init {
        viewModelScope.launch {
            flippedCards.value =
                List(
                    setWithCardsRepository
                        .getCards(setId)
                        .first()
                        .size
                ) { false }

            Log.d("PIE", ":${flippedCards.value.size} ")
            combine(
                flippedCards,
                setDetails
            ) { flippedCards, setDetails ->
                StudyUiState(
                    setDetails.set,
                    setDetails.cards,
                    flippedCards
                )
            }.collect {
                _state.value = it
            }
        }
    }

    fun updateFlipped(index: Int, flippedTo: Boolean) {
        flippedCards.value = flippedCards.value.toMutableList().apply { this[index] = flippedTo }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class StudyUiState(
    val set: Set = Set(),
    val cards: List<Card> = listOf(),
    val flipped: List<Boolean> = listOf()
)
