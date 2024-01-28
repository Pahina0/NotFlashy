package ap.panini.notflashy.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Set(
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "creation_date") var creationDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_viewed_date") var lastViewedDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_modified_date") var lastModifiedDate: Long =
        System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "set_id")
    var uid: Long = 0,
) {
    fun setDatesToCurrent() {
        creationDate = System.currentTimeMillis()
        lastViewedDate = System.currentTimeMillis()
        lastModifiedDate = System.currentTimeMillis()
    }
}
