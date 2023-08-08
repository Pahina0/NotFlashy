package ap.panini.notflashy.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.repositories.SetWithCardsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(setWithCardsRepository: SetWithCardsRepository) : ViewModel() {

    val libraryScreenUiState: StateFlow<LibraryUiState> =
        setWithCardsRepository.getAllSets().map { LibraryUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = LibraryUiState()
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class LibraryUiState(
    val sets: List<Set> = listOf()
)
