package ap.panini.notflashy.ui.library.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.repositories.SetWithCardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSetViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val setWithCardsRepository: SetWithCardsRepository,
    ) : ViewModel() {
        private val setId = savedStateHandle.get<Long>(EditSetDestination.SET_ID_ARG) ?: -1

        val initialNavTo = savedStateHandle.get<Int>(EditSetDestination.EDIT_SPECIFIC_ARG) ?: -1

        var editSetUiState by mutableStateOf(EditSetUiState())
            private set

        init {
            viewModelScope.launch {
                if (setId != -1L) {
                    editSetUiState =
                        setWithCardsRepository.getSetWithCards(setId).filterNotNull().first().let {
                            EditSetUiState(it.set, it.cards)
                        }
                }
            }
        }

        fun updateTitleUiState(set: Set) {
            editSetUiState = EditSetUiState(set, editSetUiState.cards)
        }

        fun addEmptyCard() {
            val index =
                if (editSetUiState.selectedIndex == -1) {
                    editSetUiState.cards.size
                } else {
                    editSetUiState.selectedIndex
                }

            editSetUiState =
                editSetUiState.copy(
                    cards = editSetUiState.cards.toMutableList().apply { add(index, Card()) },
                )
        }

        fun removeCard() {
            val index =
                if (editSetUiState.selectedIndex == -1) {
                    editSetUiState.cards.size - 1
                } else {
                    editSetUiState.selectedIndex
                }

            if (index < 0) return

            editSetUiState =
                editSetUiState.copy(
                    cards = editSetUiState.cards.toMutableList().apply { removeAt(index) },
                )
        }

        fun updateCardUiState(
            card: Card,
            index: Int,
        ) {
            editSetUiState =
                editSetUiState.copy(
                    cards = editSetUiState.cards.toMutableList().apply { this[index] = card },
                )
        }

        fun moveCard(
            from: Int,
            to: Int,
        ) {
            editSetUiState =
                editSetUiState.copy(
                    cards =
                        editSetUiState.cards.toMutableList().also { list ->
                            list[from] = list[to].also { list[to] = list[from] }
                        },
                    selectedIndex = -1,
                )
        }

        fun updateSelected(index: Int) {
            editSetUiState =
                editSetUiState.copy(
                    selectedIndex = if (index == editSetUiState.selectedIndex) -1 else index,
                )
        }

        suspend fun saveSet() {
            editSetUiState = editSetUiState.copy(cards = editSetUiState.cards.filter { !it.isEmpty() })
            if (editSetUiState.cards.isEmpty()) {
                setWithCardsRepository.deleteSet(editSetUiState.set)
                return
            }

            if (editSetUiState.set.uid == 0L) {
                editSetUiState.set.setDatesToCurrent()
            } else {
                editSetUiState.set.lastModifiedDate = System.currentTimeMillis()
            }

            editSetUiState.set.uid =
                setWithCardsRepository.insertSetWithCards(
                    editSetUiState.set,
                    editSetUiState.cards,
                )
        }
    }

data class EditSetUiState(
    val set: Set = Set(),
    val cards: List<Card> = listOf(Card()),
    val selectedIndex: Int = -1,
)
