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

/**
 * حالة UI لشاشة البحوث المحفوظة.
 *
 * @property searches قائمة البحوث المحفوظة بالترتيب المحفوظ.
 */
data class SavedSearchesUiState(
    val searches: List<SavedSearch> = emptyList(),
)

/**
 * ViewModel لشاشة إدارة البحوث المحفوظة.
 *
 * يُوفّر عمليات:
 * - **مراقبة**: تحديث تفاعلي للقائمة عبر [ObserveSavedSearchesUseCase].
 * - **حذف**: إزالة بحث محفوظ بمعرّفه.
 * - **تنبيهات**: تفعيل أو تعطيل تنبيهات بحث محدد.
 *
 * يُحتضن أيضاً دالة [newSearch] كـ factory مشتركة بين الـ ViewModels
 * لإنشاء بحث جديد بمعرّف UUID فريد.
 *
 * @constructor يُحقن بواسطة Hilt.
 */
@HiltViewModel
class SavedSearchesViewModel @Inject constructor(
    observeSavedSearchesUseCase: ObserveSavedSearchesUseCase,
    private val deleteSavedSearchUseCase: DeleteSavedSearchUseCase,
    private val updateSavedSearchAlertUseCase: UpdateSavedSearchAlertUseCase,
) : ViewModel() {

    /**
     * الحالة الكاملة للشاشة، تتحدث تلقائياً عند تغيير البحوث المحفوظة.
     */
    val uiState: StateFlow<SavedSearchesUiState> = observeSavedSearchesUseCase()
        .map { SavedSearchesUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedSearchesUiState())

    /** يحذف بحثاً محفوظاً بمعرّفه. */
    fun delete(id: String) {
        viewModelScope.launch { deleteSavedSearchUseCase(id) }
    }

    /**
     * يُفعّل أو يُعطّل التنبيهات لبحث محفوظ.
     *
     * @param id معرّف البحث المراد تعديله.
     * @param enabled true لتفعيل التنبيهات، false لإيقافها.
     */
    fun setAlertEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch { updateSavedSearchAlertUseCase(id, enabled) }
    }

    companion object {
        /**
         * ينشئ كائن [SavedSearch] جديد مع UUID فريد وتنبيهات مُعطَّلة افتراضياً.
         *
         * مُشتركة بين [SavedSearchesViewModel] و [ListingsViewModel] لضمان
         * إنشاء البحوث بنفس الطريقة بغض النظر عن مكان الحفظ.
         *
         * @param name الاسم الذي اختاره المستخدم للبحث.
         * @param filters الفلاتر الحالية المراد حفظها.
         * @return [SavedSearch] جاهز للحفظ.
         */
        fun newSearch(name: String, filters: ListingFilterPreset): SavedSearch =
            SavedSearch(UUID.randomUUID().toString(), name, filters, alertsEnabled = false)
    }
}
