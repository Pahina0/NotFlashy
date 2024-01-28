package ap.panini.notflashy.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SetWithCards(
    @Embedded val set: Set,
    @Relation(
        parentColumn = "set_id",
        entityColumn = "card_set_id",
    )
    val cards: List<Card>,
)
