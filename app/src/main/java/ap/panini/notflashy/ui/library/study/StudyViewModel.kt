package ap.panini.notflashy.ui.library.study

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyViewModel(
    savedStateHandle: SavedStateHandle,
    private val setWithCardsRepository: SetWithCardsRepository
) : ViewModel() {
    private val setId = savedStateHandle.get<Long>("setId") ?: -1

    private val isShuffled = savedStateHandle.get<Boolean>("isShuffled") ?: false

    private val onlyStared = savedStateHandle.get<Boolean>("onlyStared") ?: false

    private val marks = MutableStateFlow(listOf<Marks>())

    private val cards: StateFlow<List<Card>> =
        setWithCardsRepository.getCardsStudy(setId, isShuffled, onlyStared).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    private val set: StateFlow<Set> =
        setWithCardsRepository.getSet(setId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = Set()
        )

    private val flippedCards = MutableStateFlow(listOf<Boolean>())

    private val _state = MutableStateFlow(StudyUiState())

    val state: StateFlow<StudyUiState>
        get() = _state

    init {
        initiateStudy()
    }

    fun initiateStudy() {
        viewModelScope.launch {
            marks.value = List(

                setWithCardsRepository.getCardsStudy(setId, isShuffled, onlyStared).first().size
            ) { Marks.Skipped }

            with(setWithCardsRepository.getSetWithCards(setId).first()) {
                setWithCardsRepository.insertSetWithCards(
                    set.copy(lastViewedDate = System.currentTimeMillis()),
                    cards
                )
            }

            flippedCards.value =
                List(
                    setWithCardsRepository.getCardsStudy(setId, isShuffled, onlyStared).first().size
                ) { false }

            combine(
                flippedCards,
                cards,
                set,
                marks
            ) { flippedCards, cards, set, marks ->
                StudyUiState(
                    set,
                    cards,
                    flippedCards,
                    marks
                )
            }.collect {
                _state.value = it
            }
        }
    }

    fun updateFlipped(index: Int) {
        if (index < 0 || index >= flippedCards.value.size) return

        flippedCards.value = flippedCards.value.toMutableList().apply {
            this[index] = !this[index]
        }
    }

    fun updateStared(card: Card) {
        viewModelScope.launch {
            setWithCardsRepository.insertCard(card.copy(stared = !card.stared))
        }
    }

    fun updateMark(index: Int, newMark: Marks) {
        if (index < 0 || index >= marks.value.size) return

        marks.value = marks.value.toMutableList().apply { this[index] = newMark }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class StudyUiState(
    val set: Set = Set(),
    val cards: List<Card> = listOf(),
    val flipped: List<Boolean> = listOf(),
    val marks: List<Marks> = listOf()
)

enum class Marks {
    Correct,
    Incorrect,
    Skipped
}
