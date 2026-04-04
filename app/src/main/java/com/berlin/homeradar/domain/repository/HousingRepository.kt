package com.berlin.homeradar.domain.repository

import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceReliabilityMetrics
import com.berlin.homeradar.domain.model.SyncInfo
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * عقد (Contract) طبقة البيانات الرئيسي للتطبيق.
 *
 * يعرّف هذا الـ interface جميع العمليات المتاحة على بيانات السكن وإعدادات التطبيق.
 * التنفيذ الفعلي في [com.berlin.homeradar.data.repository.HousingRepositoryImpl].
 *
 * ## تصميم الـ API:
 * - دوال **observe*** تُرجع [Flow] تنبعث بالبيانات المحدَّثة تلقائياً عند أي تغيير في قاعدة البيانات.
 * - دوال **get*** تُرجع قيمة مرة واحدة (one-shot) وتحتاج `suspend`.
 * - دوال **set/add/remove/move/save/delete/toggle*** تُعدّل الحالة وتحتاج `suspend`.
 */
interface HousingRepository {

    /**
     * يراقب قائمة الإعلانات النشطة مع تطبيق الفلاتر المحددة.
     * يُبعث تلقائياً عند أي تغيير في قاعدة البيانات.
     *
     * @param filter الفلاتر المراد تطبيقها (سعر، غرف، حي، مصدر... إلخ). الافتراضي بلا فلاتر.
     */
    fun observeListings(filter: ListingFilterPreset = ListingFilterPreset()): Flow<List<HousingListing>>

    /** يراقب جميع الإعلانات النشطة بدون أي فلاتر، يُستخدم لاشتقاق قوائم الحي والمصدر المتاحة. */
    fun observeAllActiveListings(): Flow<List<HousingListing>>

    /** يراقب مقاييس موثوقية كل مصدر ويُعيدها كـ Map (sourceId → metrics). */
    fun observeSourceReliabilityMetrics(): Flow<Map<String, SourceReliabilityMetrics>>

    /** يراقب معلومات حالة المزامنة وإعداداتها (آخر مزامنة، هل جارية، الأخطاء... إلخ). */
    fun observeSyncInfo(): Flow<SyncInfo>

    /** يراقب قائمة عمليات البحث المحفوظة للمستخدم. */
    fun observeSavedSearches(): Flow<List<SavedSearch>>

    /** يُرجع قائمة جميع المصادر المعروفة للتطبيق (مدمجة ومخصصة) في لحظتها. */
    fun getKnownSources(): List<SourceDefinition>

    /**
     * يُرجع إعلاناً واحداً بمعرّفه الداخلي، أو null إن لم يُوجد.
     * @param id المعرّف الداخلي في قاعدة البيانات.
     */
    suspend fun getListingById(id: Long): HousingListing?

    /**
     * يُنفّذ عملية جلب الإعلانات من جميع المصادر المفعّلة وتحديث قاعدة البيانات.
     *
     * @param trigger سبب تشغيل المزامنة (مثل "manual", "workmanager", "startup").
     * @return [Result.success] عند نجاح جلب جزء على الأقل من المصادر،
     *   أو [Result.failure] إذا فشلت جميع المصادر.
     */
    suspend fun refreshListings(trigger: String): Result<Unit>

    /** يعكس حالة المفضلة للإعلان المحدد (يُضيف إن لم يكن، يُزيل إن كان). */
    suspend fun toggleFavorite(listingId: Long)

    /** يُفعّل أو يُعطّل المزامنة التلقائية في الخلفية عبر WorkManager. */
    suspend fun setBackgroundSyncEnabled(enabled: Boolean)

    /** يُفعّل أو يُعطّل استخدام مصدر الإعلانات البعيد (Remote API). */
    suspend fun setRemoteSourceEnabled(enabled: Boolean)

    /** يُغيّر لغة واجهة التطبيق. */
    suspend fun setAppLanguage(language: AppLanguage)

    /** يُغيّر ثيم التطبيق (فاتح/داكن/تلقائي). */
    suspend fun setThemeMode(themeMode: ThemeMode)

    /** يُحدّث فترة المزامنة التلقائية في الخلفية. */
    suspend fun setSyncInterval(option: SyncIntervalOption)

    /** يُفعّل أو يُعطّل مصدراً محدداً بمعرّفه. */
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean)

    /** يُفعّل أو يُعطّل جميع المصادر التي تدعم المزامنة التلقائية دفعةً واحدة. */
    suspend fun setAllSupportedSourcesEnabled(enabled: Boolean)

    /**
     * يُحرّك مصدراً في قائمة الترتيب لأعلى أو لأسفل.
     * @param moveUp true للتحريك لأعلى، false للتحريك لأسفل.
     */
    suspend fun moveSource(sourceId: String, moveUp: Boolean)

    /** يُضيف مصدراً مخصصاً جديداً أضافه المستخدم. */
    suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String)

    /** يحذف مصدراً مخصصاً أضافه المستخدم مسبقاً. */
    suspend fun removeCustomSource(sourceId: String)

    /** يحفظ بحثاً مع إعداداته وتفضيلات التنبيهات. */
    suspend fun saveSearch(search: SavedSearch)

    /** يحذف بحثاً محفوظاً بمعرّفه. */
    suspend fun deleteSavedSearch(searchId: String)

    /**
     * يُفعّل أو يُعطّل التنبيهات لبحث محفوظ محدد.
     * @param searchId معرّف البحث المحفوظ.
     * @param enabled true لتفعيل التنبيهات، false لإيقافها.
     */
    suspend fun updateSavedSearchAlerts(searchId: String, enabled: Boolean)

    /**
     * يُصدّر جميع إعدادات المستخدم كـ JSON نصي للنسخ الاحتياطي.
     * @return نص JSON يحتوي على كامل إعدادات المستخدم.
     */
    suspend fun exportBackupJson(): String

    /**
     * يستورد إعدادات المستخدم من نسخة احتياطية JSON ويستبدل الإعدادات الحالية.
     * @param json نص JSON من نسخة احتياطية سابقة.
     * @return [Result.success] عند نجاح الاستيراد، أو [Result.failure] عند وجود خطأ في البيانات.
     */
    suspend fun importBackupJson(json: String): Result<Unit>
}
