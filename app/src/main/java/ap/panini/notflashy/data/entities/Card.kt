package ap.panini.notflashy.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Set::class,
            parentColumns = arrayOf("set_id"),
            childColumns = arrayOf("card_set_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("card_id")
    ]
)
data class Card(
    @ColumnInfo(name = "front_text") var frontText: String = "",
    @ColumnInfo(name = "front_image") var frontImage: String = "",

    @ColumnInfo(name = "back_text") var backText: String = "",
    @ColumnInfo(name = "back_image") var backImage: String = "",

    @ColumnInfo(name = "stared") var stared: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    var uid: Long = 0,

    @ColumnInfo(name = "card_set_id", index = true)
    var setId: Long = 0
)
