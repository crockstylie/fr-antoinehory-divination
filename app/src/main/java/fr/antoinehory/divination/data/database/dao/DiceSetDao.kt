package fr.antoinehory.divination.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.antoinehory.divination.data.model.DiceSet
import kotlinx.coroutines.flow.Flow

@Dao
interface DiceSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diceSet: DiceSet): Long // Retourne l'ID du set inséré ou remplacé

    @Update
    suspend fun update(diceSet: DiceSet)

    @Delete
    suspend fun delete(diceSet: DiceSet)

    @Query("DELETE FROM dice_sets WHERE id = :setId")
    suspend fun deleteById(setId: Long)

    @Query("SELECT * FROM dice_sets WHERE id = :setId")
    fun getDiceSetById(setId: Long): Flow<DiceSet?> // Flow pour observer les changements

    @Query("SELECT * FROM dice_sets ORDER BY name ASC")
    fun getAllDiceSets(): Flow<List<DiceSet>> // Flow pour observer la liste complète

    @Query("SELECT * FROM dice_sets WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteDiceSets(): Flow<List<DiceSet>> // Flow pour observer les favoris

    // Peut-être une requête pour mettre à jour rapidement le statut de favori
    @Query("UPDATE dice_sets SET is_favorite = :isFavorite WHERE id = :setId")
    suspend fun updateFavoriteStatus(setId: Long, isFavorite: Boolean)

    // Vous pourriez aussi vouloir une fonction pour récupérer un set par son nom,
    // mais attention aux noms dupliqués si vous ne les interdisez pas.
    // @Query("SELECT * FROM dice_sets WHERE name = :name LIMIT 1")
    // fun getDiceSetByName(name: String): Flow<DiceSet?>
}

