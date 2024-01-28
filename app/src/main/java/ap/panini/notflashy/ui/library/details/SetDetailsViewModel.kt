package ap.panini.notflashy.ui.library.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.entities.SetWithCards
import ap.panini.notflashy.data.repositories.SetWithCardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetDetailsViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val setWithCardsRepository: SetWithCardsRepository,
    ) : ViewModel() {
        private val setId = savedStateHandle.get<Long>(SetDetailsDestination.SET_ID_ARG) ?: -1

        private val filters = MutableStateFlow(FilterUiState())

        private val setDetails: StateFlow<SetWithCards> =
            setWithCardsRepository.getSetWithCards(setId).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = SetWithCards(Set(), listOf()),
            )

        private val _state = MutableStateFlow(SetDetailsUiState())
        val state: StateFlow<SetDetailsUiState>
            get() = _state

        init {
            viewModelScope.launch {
                combine(
                    filters,
                    setDetails,
                ) { filters, setDetailsUiState ->
                    SetDetailsUiState(
                        setDetailsUiState.set,
                        setDetailsUiState.cards,
                        filters,
                    )
                }.collect {
                    _state.value = it
                }
            }
        }

        companion object {
            private const val TIMEOUT_MILLIS = 5_000L
        }

        fun updateStarred(card: Card) {
            viewModelScope.launch {
                setWithCardsRepository.insertCard(card)
            }
        }

        fun updateShuffleFilter(filterState: Boolean) {
            filters.value = filters.value.copy(filterShuffle = filterState)
        }

        fun updateStarredFilter(filterState: Boolean) {
            filters.value = filters.value.copy(filterStarred = filterState)
        }
    }

data class SetDetailsUiState(
    val set: Set = Set(),
    val cards: List<Card> = listOf(Card()),
    val filters: FilterUiState = FilterUiState(),
)

data class FilterUiState(
    val filterShuffle: Boolean = false,
    val filterStarred: Boolean = false,
)
