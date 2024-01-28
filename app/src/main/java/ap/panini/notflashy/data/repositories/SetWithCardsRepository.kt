package ap.panini.notflashy.data.repositories

import ap.panini.notflashy.data.daos.SetWithCardsDao
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.entities.SetWithCards
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetWithCardsRepository
    @Inject
    constructor(
        private val setWithCardsDao: SetWithCardsDao,
    ) {
        suspend fun insertCard(card: Card) = setWithCardsDao.insertCard(card)

        suspend fun insertSetWithCards(
            set: Set,
            cards: List<Card>,
        ): Long = setWithCardsDao.insertSetWithCards(set, cards)

        fun getAllSets(): Flow<List<Set>> = setWithCardsDao.getAllSets()

        fun getSet(setId: Long): Flow<Set> = setWithCardsDao.getSet(setId)

        fun getCardsStudy(
            setId: Long,
            isShuffled: Boolean,
            onlyStarred: Boolean,
        ) = setWithCardsDao.getCardsStudy(setId, isShuffled, onlyStarred)

        fun getSetWithCards(setId: Long): Flow<SetWithCards> = setWithCardsDao.getSetWithCards(setId)

        suspend fun deleteSet(set: Set) = setWithCardsDao.deleteSet(set)
    }
