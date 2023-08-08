package ap.panini.notflashy.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ap.panini.notflashy.NotFlashyApplication
import ap.panini.notflashy.ui.library.EditSetViewModel
import ap.panini.notflashy.ui.library.LibraryViewModel
import ap.panini.notflashy.ui.library.SetDetailsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            LibraryViewModel(notFlashyApplication().container.setWithCardsRepository)
        }

        initializer {
            EditSetViewModel(
                this.createSavedStateHandle(),
                notFlashyApplication().container.setWithCardsRepository
            )
        }

        initializer {
            SetDetailsViewModel(
                this.createSavedStateHandle(),
                notFlashyApplication().container.setWithCardsRepository
            )
        }
    }
}

fun CreationExtras.notFlashyApplication(): NotFlashyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NotFlashyApplication)
