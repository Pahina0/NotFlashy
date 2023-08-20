package ap.panini.notflashy.data.repositories

import ap.panini.notflashy.data.daos.SetWithCardsDao
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.entities.SetWithCards
import kotlinx.coroutines.flow.Flow

interface SetWithCardsRepository {
    suspend fun insertSet(set: Set): Long

    suspend fun insertCards(cards: List<Card>)

    suspend fun insertSetWithCards(set: Set, cards: List<Card>): Long

    suspend fun insertCard(card: Card)

    fun getAllSets(): Flow<List<Set>>

    fun getSet(setId: Long): Flow<Set>

    fun getCards(setId: Long): Flow<List<Card>>

    fun getCardsStudy(setId: Long, isShuffled: Boolean, onlyStarred: Boolean): Flow<List<Card>>

    fun getSetWithCards(setId: Long): Flow<SetWithCards>

    suspend fun deleteSet(set: Set)

    suspend fun deleteCard(card: Card)
}

class OfflineSetWithCardsRepository(private val setWithCardsDao: SetWithCardsDao) :
    SetWithCardsRepository {
    override suspend fun insertSet(set: Set): Long = setWithCardsDao.insertSet(set)

    override suspend fun insertCards(cards: List<Card>) = setWithCardsDao.insertCards(cards)

    override suspend fun insertCard(card: Card) = setWithCardsDao.insertCard(card)

    override suspend fun insertSetWithCards(set: Set, cards: List<Card>): Long =
        setWithCardsDao.insertSetWithCards(set, cards)

    override fun getAllSets(): Flow<List<Set>> = setWithCardsDao.getAllSets()

    override fun getSet(setId: Long): Flow<Set> = setWithCardsDao.getSet(setId)

    override fun getCards(setId: Long): Flow<List<Card>> = setWithCardsDao.getCards(setId)

    override fun getCardsStudy(setId: Long, isShuffled: Boolean, onlyStarred: Boolean) =
        setWithCardsDao.getCardsStudy(setId, isShuffled, onlyStarred)

    override fun getSetWithCards(setId: Long): Flow<SetWithCards> =
        setWithCardsDao.getSetWithCards(setId)

    override suspend fun deleteSet(set: Set) = setWithCardsDao.deleteSet(set)

    override suspend fun deleteCard(card: Card) = setWithCardsDao.deleteCard(card)
}
