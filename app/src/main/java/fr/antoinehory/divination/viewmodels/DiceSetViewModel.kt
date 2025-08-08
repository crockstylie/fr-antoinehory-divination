package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.database.AppDatabase
import fr.antoinehory.divination.data.database.dao.DiceSetDao
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiceSetViewModel(application: Application) : ViewModel() {

    private val diceSetDao: DiceSetDao = AppDatabase.getDatabase(application).diceSetDao()

    // Exposer la liste de tous les sets de dés
    val allDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getAllDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Reste actif 5s après le dernier collecteur
            initialValue = emptyList()
        )

    // Exposer la liste des sets de dés favoris
    val favoriteDiceSets: StateFlow<List<DiceSet>> = diceSetDao.getFavoriteDiceSets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Ajoute un nouveau set de dés à la base de données.
     */
    fun addDiceSet(name: String, diceConfigs: List<DiceConfig>) {
        viewModelScope.launch {
            val newDiceSet = DiceSet(name = name, diceConfigs = diceConfigs, isFavorite = false)
            diceSetDao.insert(newDiceSet)
        }
    }

    /**
     * Met à jour un set de dés existant.
     */
    fun updateDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.update(diceSet)
        }
    }

    /**
     * Supprime un set de dés.
     */
    fun deleteDiceSet(diceSet: DiceSet) {
        viewModelScope.launch {
            diceSetDao.delete(diceSet)
        }
    }

    /**
     * Bascule le statut de favori d'un set de dés.
     */
    fun toggleFavoriteStatus(diceSet: DiceSet) {
        viewModelScope.launch {
            // Crée une nouvelle instance avec le statut de favori inversé
            val updatedSet = diceSet.copy(isFavorite = !diceSet.isFavorite)
            diceSetDao.update(updatedSet)
            // Alternativement, si on utilise la fonction spécifique du DAO :
            // diceSetDao.updateFavoriteStatus(diceSet.id, !diceSet.isFavorite)
            // L'avantage de .update(updatedSet) est que le Flow sera notifié par Room.
            // Si updateFavoriteStatus ne notifie pas le Flow correctement (ce qui peut arriver
            // si Room ne détecte pas le changement comme affectant la query du Flow),
            // alors la première méthode est plus sûre pour la réactivité de l'UI.
            // Généralement, une mise à jour d'une ligne notifie les queries qui sélectionnent cette ligne.
        }
    }

    // Si nous avons besoin de récupérer un DiceSet spécifique par son ID pour l'édition,
    // nous pourrions ajouter une fonction comme celle-ci, bien que souvent l'UI
    // récupère le set depuis la liste 'allDiceSets' par son ID.
    // fun getDiceSetById(id: Long): Flow<DiceSet?> = diceSetDao.getDiceSetById(id)
}

/**
 * Factory pour créer une instance de DiceSetViewModel.
 */
class DiceSetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiceSetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiceSetViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
