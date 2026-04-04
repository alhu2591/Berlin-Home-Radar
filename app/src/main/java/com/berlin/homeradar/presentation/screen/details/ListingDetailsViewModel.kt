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

/**
 * حالة UI لشاشة تفاصيل إعلان واحد.
 *
 * @property listing الإعلان المراد عرض تفاصيله، أو null أثناء التحميل أو إن لم يُوجد.
 */
data class ListingDetailsUiState(
    val listing: HousingListing? = null,
)

/**
 * ViewModel لشاشة تفاصيل إعلان واحد.
 *
 * ## آلية الحصول على معرّف الإعلان:
 * يُستخرج [listingId] من [SavedStateHandle] الذي يحمل arguments التنقل.
 * يستخدم [checkNotNull] للتأكد من وجوده، لأن فتح الشاشة بدون ID خطأ برمجي.
 *
 * ## تحديث المفضلة:
 * بعد [toggleFavorite]، يُعاد جلب الإعلان من قاعدة البيانات لعكس التغيير فوراً.
 * هذا النهج بسيط ومباشر نظراً لأن شاشة التفاصيل تعرض إعلاناً واحداً فقط.
 *
 * @constructor يُحقن بواسطة Hilt مع [SavedStateHandle] تلقائياً.
 */
@HiltViewModel
class ListingDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getListingByIdUseCase: GetListingByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    /** معرّف الإعلان المُمرَّر كـ argument في التنقل. */
    private val listingId: Long = checkNotNull(savedStateHandle["listingId"])

    private val _uiState = MutableStateFlow(ListingDetailsUiState())
    val uiState: StateFlow<ListingDetailsUiState> = _uiState

    init {
        // جلب تفاصيل الإعلان فور إنشاء الـ ViewModel
        viewModelScope.launch {
            _uiState.value = ListingDetailsUiState(getListingByIdUseCase(listingId))
        }
    }

    /**
     * يعكس حالة المفضلة ويُعيد جلب الإعلان لتحديث الواجهة.
     *
     * يُتجاهل الاستدعاء إذا لم يُحمَّل الإعلان بعد ([listing] = null).
     */
    fun toggleFavorite() {
        val id = _uiState.value.listing?.id ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
            // إعادة الجلب لعكس حالة المفضلة الجديدة في الواجهة
            _uiState.value = ListingDetailsUiState(getListingByIdUseCase(id))
        }
    }
}
