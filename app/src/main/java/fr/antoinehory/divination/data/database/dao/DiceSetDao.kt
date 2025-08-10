package fr.antoinehory.divination.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.antoinehory.divination.data.model.DiceSet
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [DiceSet] entities.
 * Provides methods to interact with the "dice_sets" table in the database.
 */
@Dao
interface DiceSetDao {

    /**
     * Inserts a [DiceSet] into the database. If a dice set with the same ID already exists,
     * it will be replaced.
     *
     * @param diceSet The [DiceSet] to insert.
     * @return The row ID of the newly inserted dice set.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diceSet: DiceSet): Long

    /**
     * Updates an existing [DiceSet] in the database.
     *
     * @param diceSet The [DiceSet] to update.
     */
    @Update
    suspend fun update(diceSet: DiceSet)

    /**
     * Deletes a [DiceSet] from the database.
     *
     * @param diceSet The [DiceSet] to delete.
     */
    @Delete
    suspend fun delete(diceSet: DiceSet)

    /**
     * Deletes a [DiceSet] from the database by its ID.
     *
     * @param setId The ID of the dice set to delete.
     */
    @Query("DELETE FROM dice_sets WHERE id = :setId")
    suspend fun deleteById(setId: Long)

    /**
     * Retrieves a [DiceSet] from the database by its ID.
     *
     * @param setId The ID of the dice set to retrieve.
     * @return A [Flow] emitting the [DiceSet] if found, or `null` otherwise.
     */
    @Query("SELECT * FROM dice_sets WHERE id = :setId")
    fun getDiceSetById(setId: Long): Flow<DiceSet?>

    /**
     * Retrieves a [DiceSet] from the database by its ID. This is a blocking call.
     *
     * @param id The ID of the dice set to retrieve.
     * @return The [DiceSet] if found, or `null` otherwise.
     */
    @Query("SELECT * FROM dice_sets WHERE id = :id")
    suspend fun getDiceSetByIdBlocking(id: Long): DiceSet?

    /**
     * Retrieves all [DiceSet]s from the database, ordered primarily by favorite status (favorites first),
     * and then by name in ascending order.
     *
     * @return A [Flow] emitting a list of all [DiceSet]s.
     */
    @Query("SELECT * FROM dice_sets ORDER BY is_favorite DESC, name ASC")
    fun getAllDiceSets(): Flow<List<DiceSet>>

    /**
     * Retrieves all favorite [DiceSet]s from the database, ordered by name in ascending order.
     *
     * @return A [Flow] emitting a list of favorite [DiceSet]s.
     */
    @Query("SELECT * FROM dice_sets WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteDiceSets(): Flow<List<DiceSet>>

    /**
     * Updates the favorite status of a [DiceSet] in the database.
     *
     * @param setId The ID of the dice set to update.
     * @param isFavorite The new favorite status (`true` if favorite, `false` otherwise).
     */
    @Query("UPDATE dice_sets SET is_favorite = :isFavorite WHERE id = :setId")
    suspend fun updateFavoriteStatus(setId: Long, isFavorite: Boolean)
}

