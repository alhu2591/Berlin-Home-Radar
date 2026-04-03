package com.berlin.homeradar.presentation.screen.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.usecase.GetListingByIdUseCase
import com.berlin.homeradar.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ListingDetailsUiState(
    val listing: HousingListing? = null,
)

@HiltViewModel
class ListingDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getListingByIdUseCase: GetListingByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {
    private val listingId: Long = checkNotNull(savedStateHandle["listingId"])
    private val _uiState = MutableStateFlow(ListingDetailsUiState())
    val uiState: StateFlow<ListingDetailsUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = ListingDetailsUiState(getListingByIdUseCase(listingId))
        }
    }

    fun toggleFavorite() {
        val id = _uiState.value.listing?.id ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
            _uiState.value = ListingDetailsUiState(getListingByIdUseCase(id))
        }
    }
}
