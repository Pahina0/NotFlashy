package ap.panini.notflashy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ap.panini.notflashy.data.daos.SetWithCardsDao
import ap.panini.notflashy.data.entities.Card
import ap.panini.notflashy.data.entities.Set

@Database(entities = [Card::class, Set::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun setWithCardsDao(): SetWithCardsDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "item_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
