package com.berlin.homeradar.presentation.screen.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.homeradar.domain.usecase.GetListingsUseCase
import com.berlin.homeradar.domain.usecase.ObserveSyncInfoUseCase
import com.berlin.homeradar.domain.usecase.RefreshListingsUseCase
import com.berlin.homeradar.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    observeSyncInfoUseCase: ObserveSyncInfoUseCase,
    private val refreshListingsUseCase: RefreshListingsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val filters = MutableStateFlow(ListingsFilters())

    val uiState = combine(
        filters,
        observeSyncInfoUseCase(),
        filters.flatMapLatest { filter ->
            getListingsUseCase(
                onlyFavorites = filter.showFavoritesOnly,
                minRooms = filter.minRooms,
                district = filter.district,
            )
        },
    ) { filter, syncInfo, listings ->
        ListingsUiState(
            listings = listings,
            syncInfo = syncInfo,
            showFavoritesOnly = filter.showFavoritesOnly,
            minRooms = filter.minRooms,
            district = filter.district,
            isRefreshing = syncInfo.isSyncing,
            message = syncInfo.lastErrorMessage,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ListingsUiState(),
    )

    init {
        manualRefresh()
    }

    fun manualRefresh() {
        viewModelScope.launch {
            refreshListingsUseCase("manual")
        }
    }

    fun toggleFavorite(listingId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(listingId)
        }
    }

    fun toggleFavoritesOnly() {
        filters.value = filters.value.copy(showFavoritesOnly = !filters.value.showFavoritesOnly)
    }

    fun setMinRooms(value: Double?) {
        filters.value = filters.value.copy(minRooms = value)
    }

    fun setDistrict(value: String?) {
        filters.value = filters.value.copy(district = value)
    }
}

private data class ListingsFilters(
    val showFavoritesOnly: Boolean = false,
    val minRooms: Double? = null,
    val district: String? = null,
)
