package fr.antoinehory.divination.viewmodels

// import android.app.Application // Pas nécessaire pour l'instant
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val launchLogRepository: LaunchLogRepository
    // private val application: Application // Pas utilisé pour l'instant
) : ViewModel() {

    // --- Suppression de l'ancienne logique pour "Effacer toutes les statistiques" ---
    // private val _showClearStatsConfirmationDialog = MutableStateFlow(false)
    // val showClearStatsConfirmationDialog: StateFlow<Boolean> = _showClearStatsConfirmationDialog.asStateFlow()
    //
    // fun onClearStatsClicked() {
    // _showClearStatsConfirmationDialog.value = true
    // }
    //
    // fun onConfirmClearStats() {
    // viewModelScope.launch {
    // launchLogRepository.deleteAllLogs()
    // }
    // _showClearStatsConfirmationDialog.value = false
    // }
    //
    // fun onDismissClearStatsDialog() {
    // _showClearStatsConfirmationDialog.value = false
    // }

    // --- Logique unifiée pour la confirmation de suppression ---
    // Renommage de _showClearStatsForGameDialog en _showClearConfirmationDialog
    private val _showClearConfirmationDialog = MutableStateFlow(false)
    val showClearConfirmationDialog: StateFlow<Boolean> = _showClearConfirmationDialog.asStateFlow()

    // _selectedGameForClear:
    // - null signifie que l'option "Tous les jeux" est sélectionnée.
    // - Un GameType signifie qu'un jeu spécifique est sélectionné.
    // Initialisé à null pour que "Tous les jeux" soit l'option par défaut, ou à un placeholder si vous préférez.
    // Pour l'instant, nous considérons que l'UI présentera "Tous les jeux" comme une option sélectionnable.
    // Il est important que l'UI et le ViewModel soient synchronisés sur ce que `null` signifie.
    // Nous allons considérer qu'au démarrage, aucun jeu spécifique N'EST sélectionné (pourrait afficher un placeholder dans l'UI),
    // et l'utilisateur doit activement choisir "Tous les jeux" ou un jeu spécifique.
    // Donc, initialiser à null est ambigu si le placeholder est aussi représenté par null.
    // Pour clarifier, selectedGameForClear ne sera null QUE si "Tous les jeux" est explicitement choisi.
    // Changeons l'initialisation pour être un peu plus explicite, ou laissons l'UI gérer un état "non sélectionné".
    // Pour l'instant, on laisse à null, et l'UI affichera le placeholder si null ET si l'option "Tous les jeux" n'a pas été choisie.
    // Simplifions : null = option "Tous les jeux" sélectionnée. Si au début on veut un placeholder, il faudra une autre valeur ou logique UI.
    // Pour l'instant, selectedGameForClear à null signifie que l'option "Tous les jeux" a été choisie.
    private val _selectedGameForClear = MutableStateFlow<GameType?>(null) // null pour "Tous les jeux", GameType pour un jeu spécifique.
    // Par défaut, on peut considérer qu'aucun n'est pré-sélectionné pour l'action.
    // L'UI affichera le placeholder initialement.
    val selectedGameOrOptionForClear: StateFlow<GameType?> = _selectedGameForClear.asStateFlow()


    val availableGameTypes: List<GameType> = GameType.entries

    // Fonction renommée et modifiée pour accepter null (pour "Tous les jeux")
    fun onGameOrOptionSelectedForClear(gameType: GameType?) {
        _selectedGameForClear.value = gameType
    }

    // Fonction renommée
    fun onClearSelectedStatsClicked() {
        // Le dialogue s'affiche. Le ViewModel sait déjà quelle option est sélectionnée (un jeu ou "Tous les jeux").
        // La condition `selectedGameForClear != null` du bouton "Effacer pour le jeu sélectionné"
        // sera maintenant `true` si "Tous les jeux" (null) est choisi OU si un jeu est choisi.
        // Donc le bouton sera toujours activable une fois qu'une option (jeu ou "tous les jeux") est dans le dropdown.
        _showClearConfirmationDialog.value = true
    }

    // Fonction renommée et modifiée pour gérer la suppression de "Tous les jeux" ou d'un jeu spécifique
    fun onConfirmClearSelectedStats() {
        viewModelScope.launch {
            val selection = _selectedGameForClear.value
            if (selection == null) { // Si null, c'est l'option "Tous les jeux"
                launchLogRepository.deleteAllLogs()
            } else { // Sinon, c'est un jeu spécifique
                launchLogRepository.deleteLogsByGameType(selection)
            }
        }
        _showClearConfirmationDialog.value = false
        // Réinitialiser la sélection pour que le Dropdown revienne au placeholder.
        // Ou, si on veut que "Tous les jeux" reste sélectionné si c'était le cas, il faudrait une logique différente ici.
        // Pour l'instant, on réinitialise toujours pour forcer un nouveau choix actif.
        _selectedGameForClear.value = null // Ceci remettra le dropdown au placeholder si l'UI l'interprète ainsi.
        // Ou à "Tous les jeux" si on considère `null` comme cette option persistante.
        // Il faut être cohérent avec l'UI.
        // Si on veut un placeholder, la valeur par défaut du dropdown dans l'UI ne doit pas être null.
        // Pour le moment, _selectedGameForClear.value = null après confirmation
        // forcera l'UI à afficher le placeholder si l'UI est faite pour.
    }

    // Fonction renommée
    fun onDismissClearConfirmationDialog() {
        _showClearConfirmationDialog.value = false
    }
}

class SettingsViewModelFactory(
    private val launchLogRepository: LaunchLogRepository
    // private val application: Application // Pas utilisé pour l'instant
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(launchLogRepository /*, application */) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
