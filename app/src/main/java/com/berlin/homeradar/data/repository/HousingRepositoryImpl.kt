package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SourceMetricDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import com.berlin.homeradar.data.local.mapper.toDomain
import com.berlin.homeradar.data.message.UserFacingMessages
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.data.source.ListingSourceRegistry
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.data.source.model.RawListing
import com.berlin.homeradar.data.telemetry.AnalyticsLogger
import com.berlin.homeradar.data.telemetry.CrashReporter
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceReliabilityMetrics
import com.berlin.homeradar.domain.model.SyncInfo
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * التنفيذ الفعلي لـ [HousingRepository].
 *
 * يُجسّد هذا الكلاس طبقة البيانات الكاملة ويُنسّق بين:
 * - **Room Database**: عبر [HousingListingDao], [SyncStatusDao], [SourceMetricDao].
 * - **تفضيلات المستخدم**: عبر [UserPreferencesRepository] (DataStore).
 * - **مصادر الإعلانات**: عبر [ListingSourceRegistry] للوصول للمصادر المفعّلة.
 * - **سياسة إزالة التكرار**: عبر [DeduplicationPolicy].
 * - **التتبع والتحليل**: عبر [AnalyticsLogger] و [CrashReporter].
 *
 * ## تدفق المزامنة [refreshListings]:
 * 1. تحديث حالة المزامنة في DB إلى "جارية".
 * 2. الجلب من كل مصدر مفعّل بشكل تسلسلي مع تسجيل كل نتيجة.
 * 3. دمج الإعلانات الجديدة مع القديمة عبر [DeduplicationPolicy].
 * 4. تحديث حالات الإعلانات القديمة (غير المرئية) إلى inactive.
 * 5. حفظ الكل في DB وتحديث مقاييس كل مصدر.
 * 6. تحديث حالة المزامنة النهائية في DB.
 *
 * @constructor يُحقن بواسطة Hilt كـ Singleton.
 */
@Singleton
class HousingRepositoryImpl @Inject constructor(
    private val housingListingDao: HousingListingDao,
    private val sourceMetricDao: SourceMetricDao,
    private val syncStatusDao: SyncStatusDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listingSourceRegistry: ListingSourceRegistry,
    private val deduplicationPolicy: DeduplicationPolicy,
    private val analyticsLogger: AnalyticsLogger,
    private val crashReporter: CrashReporter,
) : HousingRepository {

    /**
     * يراقب الإعلانات النشطة مع تطبيق الفلاتر على مستوى SQL.
     *
     * الاستعلام يُنفَّذ في Room مباشرةً للكفاءة، مع تطبيع نص البحث والحي
     * للتعامل الصحيح مع الأحرف الألمانية.
     *
     * **ملاحظة**: [safeSourceIds] دائماً تحتوي على عنصر واحد على الأقل
     * لتجنب خطأ SQL عند تمرير قائمة فارغة في شرط IN.
     */
    override fun observeListings(filter: ListingFilterPreset): Flow<List<HousingListing>> {
        val normalizedQuery = deduplicationPolicy.normalize(filter.query)
        val normalizedDistrict = filter.district?.takeIf { it.isNotBlank() }?.let(deduplicationPolicy::normalize)
        val safeSourceIds = if (filter.selectedSourceIds.isEmpty()) listOf("__all__") else filter.selectedSourceIds.toList()
        return housingListingDao.observeListings(
            onlyFavorites = filter.showFavoritesOnly,
            minRooms = filter.minRooms,
            minArea = filter.minArea,
            maxPrice = filter.maxPrice,
            districtNormalized = normalizedDistrict,
            queryNormalized = normalizedQuery,
            onlyJobcenter = filter.onlyJobcenter,
            onlyWohngeld = filter.onlyWohngeld,
            onlyWbs = filter.onlyWbs,
            sourceFilteringDisabled = filter.selectedSourceIds.isEmpty(),
            selectedSourceIds = safeSourceIds,
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeAllActiveListings(): Flow<List<HousingListing>> =
        housingListingDao.observeActiveListings().map { entities -> entities.map { it.toDomain() } }

    override fun observeSourceReliabilityMetrics(): Flow<Map<String, SourceReliabilityMetrics>> =
        sourceMetricDao.observeAll().map { entities -> entities.associate { it.sourceId to it.toDomain() } }

    /**
     * يدمج حالة المزامنة من DB مع إعدادات المستخدم من DataStore في Flow واحد.
     *
     * يستخدم [combine] لأن البيانات تأتي من مصدرين مختلفين:
     * - [SyncStatusDao]: آخر مزامنة، هل جارية، رسالة الخطأ.
     * - [UserPreferencesRepository.appSettings]: الإعدادات الكاملة للمستخدم.
     */
    override fun observeSyncInfo(): Flow<SyncInfo> {
        return combine(
            syncStatusDao.observe(),
            userPreferencesRepository.appSettings,
        ) { status, settings ->
            status.toDomain(
                backgroundSyncEnabled = settings.backgroundSyncEnabled,
                remoteSourceEnabled = settings.remoteSourceEnabled,
            ).copy(
                syncInterval = settings.syncInterval,
                appLanguage = settings.language,
                themeMode = settings.themeMode,
                enabledSourceIds = settings.enabledSourceIds,
                customSources = settings.customSources,
                sourceOrder = settings.sourceOrder,
            )
        }
    }

    override fun observeSavedSearches(): Flow<List<SavedSearch>> = userPreferencesRepository.savedSearches

    override fun getKnownSources(): List<SourceDefinition> = SourceCatalog.all

    override suspend fun getListingById(id: Long): HousingListing? = withContext(Dispatchers.IO) {
        housingListingDao.getById(id)?.toDomain()
    }

    /**
     * يُنفّذ دورة المزامنة الكاملة من كل المصادر المفعّلة.
     *
     * ## منطق الفشل الجزئي:
     * - إذا فشل مصدر واحد أو أكثر لكن نجح غيره → تُكمل المزامنة وتُحفظ رسائل الخطأ.
     * - إذا فشلت **جميع** المصادر ولم تُجلب أي إعلانات → تُعتبر المزامنة فاشلة كلياً.
     *
     * ## إدارة حالة الإعلانات (Lifecycle):
     * - الإعلانات التي ظهرت في المزامنة الحالية → تُحدَّث/تُدرج كـ ACTIVE.
     * - الإعلانات القديمة التي لم تظهر من مصادرها الناجحة → تُعلَّم كـ inactive.
     *
     * @param trigger سبب المزامنة للتتبع والتحليل ("manual", "workmanager", "startup").
     */
    override suspend fun refreshListings(trigger: String): Result<Unit> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val currentStatus = syncStatusDao.get() ?: SyncStatusEntity()

        // إشارة بداية المزامنة في قاعدة البيانات
        syncStatusDao.upsert(
            currentStatus.copy(
                lastAttemptMillis = now,
                isSyncing = true,
                lastErrorMessage = null,
            ),
        )

        runCatching {
            analyticsLogger.logEvent("refresh_started", mapOf("trigger" to trigger))

            val enabledSources = listingSourceRegistry.getEnabledSources()
            val collectedListings = mutableListOf<RawListing>()
            val failedSources = mutableListOf<String>()
            val outcomes = mutableListOf<SourceFetchOutcome>()

            // جلب من كل مصدر بشكل مستقل حتى لا يوقف فشل مصدر واحد البقية
            enabledSources.forEach { source ->
                val startedAt = System.currentTimeMillis()
                runCatching { source.fetch() }
                    .onSuccess { listings ->
                        val duration = System.currentTimeMillis() - startedAt
                        collectedListings += listings
                        outcomes += SourceFetchOutcome.success(source.sourceId, listings.size, duration)
                        analyticsLogger.logEvent(
                            "source_refresh_success",
                            mapOf(
                                "source" to source.sourceId,
                                "count" to listings.size.toString(),
                                "durationMs" to duration.toString(),
                            ),
                        )
                    }
                    .onFailure { throwable ->
                        val duration = System.currentTimeMillis() - startedAt
                        failedSources += UserFacingMessages.sourceFailure(
                            SourceCatalog.nameFor(source.sourceId),
                            throwable.toUserMessage(),
                        )
                        outcomes += SourceFetchOutcome.failure(
                            sourceId = source.sourceId,
                            durationMillis = duration,
                            errorMessage = throwable.toUserMessage(),
                        )
                        crashReporter.recordNonFatal(
                            throwable,
                            mapOf("trigger" to trigger, "source" to source.sourceId),
                        )
                        analyticsLogger.logEvent(
                            "source_refresh_failed",
                            mapOf(
                                "trigger" to trigger,
                                "source" to source.sourceId,
                                "durationMs" to duration.toString(),
                            ),
                        )
                    }
            }

            // فشل كلي: لا إعلانات وكل المصادر فشلت
            if (collectedListings.isEmpty() && failedSources.isNotEmpty()) {
                error(failedSources.joinToString(separator = "\n"))
            }

            // دمج الإعلانات الجديدة مع القديمة في قاعدة البيانات
            val existingListings = housingListingDao.getAll()
            val existingMetrics = sourceMetricDao.getAll().associateBy { it.sourceId }
            val entitiesToUpsert = buildMergedEntities(
                incomingListings = collectedListings,
                existingListings = existingListings,
                now = now,
                deduplicationPolicy = deduplicationPolicy,
            )
            // تعليم الإعلانات القديمة التي لم تظهر من مصادرها الناجحة كغير نشطة
            val inactiveEntities = buildInactiveEntities(
                existingListings = existingListings,
                refreshedEntities = entitiesToUpsert,
                successfulSourceIds = outcomes.filter { it.success }.map { it.sourceId }.toSet(),
                now = now,
            )

            // إزالة أي تكرار في القائمة المدمجة قبل الحفظ
            val stagedListings = (entitiesToUpsert + inactiveEntities)
                .distinctBy { entity -> entity.id.takeIf { it != 0L } ?: "${entity.source}|${entity.externalId}" }

            if (stagedListings.isNotEmpty()) {
                housingListingDao.upsertAll(stagedListings)
            }

            updateSourceMetrics(sourceMetricDao, outcomes, existingMetrics)

            // تسجيل نجاح المزامنة مع الاحتفاظ بأي رسائل خطأ جزئية
            syncStatusDao.upsert(
                currentStatus.copy(
                    lastAttemptMillis = now,
                    lastSuccessfulSyncMillis = now,
                    isSyncing = false,
                    lastErrorMessage = failedSources.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n"),
                ),
            )

            analyticsLogger.logEvent(
                "refresh_completed",
                mapOf(
                    "trigger" to trigger,
                    "count" to collectedListings.size.toString(),
                    "mergedCount" to entitiesToUpsert.size.toString(),
                    "inactiveCount" to inactiveEntities.size.toString(),
                    "failedSources" to failedSources.size.toString(),
                ),
            )
        }.onFailure { throwable ->
            // خطأ كلي غير متوقع: تسجيله وتحديث حالة المزامنة
            crashReporter.recordNonFatal(throwable, mapOf("trigger" to trigger))
            analyticsLogger.logEvent(
                "refresh_failed",
                mapOf("trigger" to trigger, "reason" to throwable::class.java.simpleName),
            )
            syncStatusDao.upsert(
                currentStatus.copy(
                    lastAttemptMillis = now,
                    isSyncing = false,
                    lastErrorMessage = throwable.toUserMessage(),
                ),
            )
        }
    }

    override suspend fun toggleFavorite(listingId: Long) {
        housingListingDao.toggleFavorite(listingId)
    }

    // ── تفويض الإعدادات إلى UserPreferencesRepository ──────────────────────

    override suspend fun setBackgroundSyncEnabled(enabled: Boolean) = userPreferencesRepository.setBackgroundSyncEnabled(enabled)
    override suspend fun setRemoteSourceEnabled(enabled: Boolean) = userPreferencesRepository.setRemoteSourceEnabled(enabled)
    override suspend fun setAppLanguage(language: AppLanguage) = userPreferencesRepository.setAppLanguage(language)
    override suspend fun setThemeMode(themeMode: ThemeMode) = userPreferencesRepository.setThemeMode(themeMode)
    override suspend fun setSyncInterval(option: SyncIntervalOption) = userPreferencesRepository.setSyncInterval(option)
    override suspend fun setSourceEnabled(sourceId: String, enabled: Boolean) = userPreferencesRepository.setSourceEnabled(sourceId, enabled)
    override suspend fun setAllSupportedSourcesEnabled(enabled: Boolean) = userPreferencesRepository.setAllSupportedSourcesEnabled(enabled)
    override suspend fun moveSource(sourceId: String, moveUp: Boolean) = userPreferencesRepository.moveSource(sourceId, moveUp)
    override suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String) =
        userPreferencesRepository.addCustomSource(displayName, websiteUrl, description)

    override suspend fun removeCustomSource(sourceId: String) = userPreferencesRepository.removeCustomSource(sourceId)
    override suspend fun saveSearch(search: SavedSearch) = userPreferencesRepository.saveSearch(search)
    override suspend fun deleteSavedSearch(searchId: String) = userPreferencesRepository.deleteSavedSearch(searchId)
    override suspend fun updateSavedSearchAlerts(searchId: String, enabled: Boolean) =
        userPreferencesRepository.updateSavedSearchAlerts(searchId, enabled)

    override suspend fun exportBackupJson(): String = userPreferencesRepository.exportBackupJson()
    override suspend fun importBackupJson(json: String): Result<Unit> = userPreferencesRepository.replaceAllFromBackup(json)
}
