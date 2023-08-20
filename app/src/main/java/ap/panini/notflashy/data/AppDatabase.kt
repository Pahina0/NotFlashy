package ap.panini.notflashy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ap.panini.notflashy.data.daos.SetWithCardsDao
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set

@Database(entities = [Card::class, Set::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun setWithCardsDao(): SetWithCardsDao
}
