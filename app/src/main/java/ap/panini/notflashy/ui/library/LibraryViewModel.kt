package ap.panini.notflashy.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
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
class LibraryViewModel
    @Inject
    constructor(
        private val setWithCardsRepository: SetWithCardsRepository,
    ) : ViewModel() {
        private val selected = MutableStateFlow(null)

        private val sets: StateFlow<List<Set>> =
            setWithCardsRepository.getAllSets()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = listOf(),
                )

        private val _state = MutableStateFlow(LibraryUiState())
        val state: StateFlow<LibraryUiState>
            get() = _state

        init {
            viewModelScope.launch {
                combine(
                    selected,
                    sets,
                ) { selected, sets ->
                    LibraryUiState(
                        sets,
                        selected,
                    )
                }.collect {
                    _state.value = it
                }
            }
        }

        fun updateSelected(set: Set?) {
            _state.value = _state.value.copy(selected = if (state.value.selected == set) null else set)
        }

        fun deleteSelected() {
            with(_state.value.selected) {
                if (this != null) {
                    viewModelScope.launch {
                        setWithCardsRepository.deleteSet(this@with)
                    }
                }
            }
        }

        @Throws(Exception::class)
        suspend fun importSet(
            setName: String,
            cards: MutableList<Array<String>>,
        ) {
            if (cards.isEmpty()) return

            val cardsMapped = mutableListOf<Card>()

            try {
                cards.forEach { row ->
                    if (row.size > 2) throw Exception("Incorrect Number of Columns")
                    cardsMapped.add(Card(row[0], row[1]))
                }

                setWithCardsRepository.insertSetWithCards(Set(setName), cardsMapped)
            } catch (e: Exception) {
                throw e
            }
        }

        companion object {
            private const val TIMEOUT_MILLIS = 5_000L
        }
    }

data class LibraryUiState(
    val sets: List<Set> = listOf(),
    val selected: Set? = null,
    val sort: String = "",
    val ascending: Boolean = false,
)
