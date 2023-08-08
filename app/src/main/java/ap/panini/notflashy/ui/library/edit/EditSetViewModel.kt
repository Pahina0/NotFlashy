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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditSetViewModel(
    savedStateHandle: SavedStateHandle,
    private val setWithCardsRepository: SetWithCardsRepository
) : ViewModel() {

    private val setId = savedStateHandle.get<Long>("setId") ?: -1

    var initialNavTo = savedStateHandle.get<Int>("editSpecific") ?: -1

    var editSetUiState by mutableStateOf(EditSetUiState())
        private set

    init {
        viewModelScope.launch {
            editSetUiState =
                setWithCardsRepository.getSetWithCards(setId).filterNotNull().first().let {
                    EditSetUiState(it.set, it.cards)
                }
        }
    }

    fun updateTitleUiState(set: Set) {
        editSetUiState = EditSetUiState(set, editSetUiState.cards)
    }

    fun addEmptyCard() {
        editSetUiState = editSetUiState.copy(
            cards = editSetUiState.cards.plus(Card())
        )
    }

    fun updateCardUiState(card: Card, index: Int) {
        editSetUiState = editSetUiState.copy(
            cards = editSetUiState.cards.toMutableList().apply { this[index] = card }
        )
    }

    suspend fun saveSet() {
        if (editSetUiState.set.uid == 0L) {
            editSetUiState.set.setDatesToCurrent()
        } else {
            editSetUiState.set.lastModifiedDate = System.currentTimeMillis()
        }

        editSetUiState.set.uid = setWithCardsRepository.insertSetWithCards(
            editSetUiState.set,
            editSetUiState.cards
        )
    }
}

data class EditSetUiState(
    val set: Set = Set(),
    val cards: List<Card> = listOf(Card())
)
