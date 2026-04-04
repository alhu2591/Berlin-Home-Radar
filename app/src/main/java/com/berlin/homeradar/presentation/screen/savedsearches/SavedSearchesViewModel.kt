package com.berlin.homeradar.presentation.screen.savedsearches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.usecase.DeleteSavedSearchUseCase
import com.berlin.homeradar.domain.usecase.ObserveSavedSearchesUseCase
import com.berlin.homeradar.domain.usecase.UpdateSavedSearchAlertUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedSearchesUiState(
    val searches: List<SavedSearch> = emptyList(),
)

@HiltViewModel
class SavedSearchesViewModel @Inject constructor(
    observeSavedSearchesUseCase: ObserveSavedSearchesUseCase,
    private val deleteSavedSearchUseCase: DeleteSavedSearchUseCase,
    private val updateSavedSearchAlertUseCase: UpdateSavedSearchAlertUseCase,
) : ViewModel() {
    val uiState: StateFlow<SavedSearchesUiState> = observeSavedSearchesUseCase()
        .map { SavedSearchesUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedSearchesUiState())

    fun delete(id: String) {
        viewModelScope.launch { deleteSavedSearchUseCase(id) }
    }

    fun setAlertEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch { updateSavedSearchAlertUseCase(id, enabled) }
    }

    companion object {
        fun newSearch(name: String, filters: ListingFilterPreset): SavedSearch =
            SavedSearch(UUID.randomUUID().toString(), name, filters, alertsEnabled = false)
    }
}
