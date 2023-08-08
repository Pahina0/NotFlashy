package ap.panini.notflashy.data

import android.content.Context
import ap.panini.notflashy.data.repositories.OfflineSetWithCardsRepository
import ap.panini.notflashy.data.repositories.SetWithCardsRepository

interface AppContainer {
    val setWithCardsRepository: SetWithCardsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val setWithCardsRepository: SetWithCardsRepository by lazy {
        OfflineSetWithCardsRepository(AppDatabase.getDatabase(context).setWithCardsDao())
    }
}
