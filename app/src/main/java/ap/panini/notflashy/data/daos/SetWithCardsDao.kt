package ap.panini.notflashy.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set
import ap.panini.notflashy.data.entities.SetWithCards
import kotlinx.coroutines.flow.Flow

@Dao
interface SetWithCardsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: Set): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Card>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Transaction
    suspend fun insertSetWithCards(set: Set, cards: List<Card>): Long {
        val setId = insertSet(set)

        cards.forEach { it.setId = setId }

        insertCards(cards)

        return setId
    }

    @Query("SELECT * FROM `Set`")
    fun getAllSets(): Flow<List<Set>>

    @Query("SELECT * FROM `Set` WHERE set_id = :setId")
    fun getSet(setId: Long): Flow<Set>

    @Query("SELECT * FROM 'Card' WHERE card_set_id = :setId")
    fun getCards(setId: Long): Flow<List<Card>>

    @Transaction
    @Query("SELECT * FROM `Set` JOIN `Card` ON card_set_id = set_id WHERE set_id = :setId")
    fun getSetWithCards(setId: Long): Flow<SetWithCards>
}
