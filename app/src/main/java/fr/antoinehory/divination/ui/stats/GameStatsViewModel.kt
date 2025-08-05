// In ui/stats/GameStatsViewModel.kt (ou un package similaire)
package fr.antoinehory.divination.ui.stats // Ou le package approprié

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.database.dao.GameLaunchCount
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Data class pour l'état de l'UI des statistiques d'un jeu
data class GameStatsUiState(
    val launchCounts: List<GameLaunchCount> = emptyList(),
    val recentLaunches: List<LaunchLog> = emptyList(),
    val isLoading: Boolean = true // Pourrait être utile plus tard
)

class GameStatsViewModel(
    private val launchLogRepository: LaunchLogRepository,
    private val gameType: GameType
) : ViewModel() {

    val uiState: StateFlow<GameStatsUiState> =
        launchLogRepository.getLaunchCountsByGameType(gameType)
            .map { counts ->
                // Ici, on pourrait aussi charger les `recentLaunches` si nécessaire
                // Pour l'instant, concentrons-nous sur les comptes pour le camembert
                GameStatsUiState(launchCounts = counts, isLoading = false)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GameStatsUiState(isLoading = true)
            )

    // Si vous voulez aussi afficher la liste des lancers récents sur le même écran :
    val recentLaunches: StateFlow<List<LaunchLog>> =
        launchLogRepository.getLogsByGameType(gameType)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}

// Factory pour pouvoir passer des arguments au ViewModel
class GameStatsViewModelFactory(
    private val launchLogRepository: LaunchLogRepository,
    private val gameType: GameType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameStatsViewModel(launchLogRepository, gameType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
