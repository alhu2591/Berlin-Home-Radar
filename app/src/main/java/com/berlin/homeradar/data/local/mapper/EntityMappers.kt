package com.berlin.homeradar.data.local.mapper

import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.local.entity.SourceMetricEntity
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingLifecycleStatus
import com.berlin.homeradar.domain.model.SourceReliabilityMetrics
import com.berlin.homeradar.domain.model.SyncInfo

/**
 * يُحوّل كيان قاعدة البيانات [HousingListingEntity] إلى نموذج الـ Domain [HousingListing].
 *
 * يُعيد تحويل [lifecycleStatus] من String المخزَّن إلى Enum عبر [ListingLifecycleStatus.fromStorage].
 * الحقول المُطبَّعة ([titleNormalized], [districtNormalized], [fingerprint]) لا تنتقل للـ Domain
 * لأنها تفاصيل داخلية تخص طبقة البيانات فقط.
 */
fun HousingListingEntity.toDomain(): HousingListing = HousingListing(
    id = id,
    source = source,
    externalId = externalId,
    title = title,
    priceEuro = priceEuro,
    district = district,
    location = location,
    rooms = rooms,
    areaSqm = areaSqm,
    imageUrl = imageUrl,
    listingUrl = listingUrl,
    isJobcenterSuitable = isJobcenterSuitable,
    isWohngeldEligible = isWohngeldEligible,
    isWbsRequired = isWbsRequired,
    isFavorite = isFavorite,
    updatedAtEpochMillis = updatedAtEpochMillis,
    lastSeenAtEpochMillis = lastSeenAtEpochMillis,
    isActive = isActive,
    lifecycleStatus = ListingLifecycleStatus.fromStorage(lifecycleStatus),
)

/**
 * يُحوّل كيان مقاييس المصدر [SourceMetricEntity] إلى نموذج الـ Domain [SourceReliabilityMetrics].
 *
 * تحويل مباشر بدون منطق إضافي، الحقول المحسوبة تُعرَّف في [SourceReliabilityMetrics] نفسه.
 */
fun SourceMetricEntity.toDomain(): SourceReliabilityMetrics = SourceReliabilityMetrics(
    sourceId = sourceId,
    successCount = successCount,
    failureCount = failureCount,
    lastAttemptMillis = lastAttemptMillis,
    lastSuccessfulPullMillis = lastSuccessfulPullMillis,
    lastItemCount = lastItemCount,
    averageItemCount = averageItemCount,
    averageDurationMillis = averageDurationMillis,
    consecutiveZeroItemPulls = consecutiveZeroItemPulls,
    lastErrorMessage = lastErrorMessage,
)

/**
 * يُحوّل كيان حالة المزامنة [SyncStatusEntity] إلى نموذج [SyncInfo] في الـ Domain.
 *
 * يقبل nullable receiver لأن الجدول قد يكون فارغاً (لا مزامنة حدثت بعد).
 * في هذه الحالة تُستخدم القيم الافتراضية (null timestamps, false للحالات).
 *
 * @receiver كيان حالة المزامنة، أو null إن لم تحدث أي مزامنة بعد.
 * @param backgroundSyncEnabled حالة المزامنة التلقائية من DataStore.
 * @param remoteSourceEnabled حالة المصدر البعيد من DataStore.
 */
fun SyncStatusEntity?.toDomain(
    backgroundSyncEnabled: Boolean,
    remoteSourceEnabled: Boolean,
): SyncInfo = SyncInfo(
    lastSuccessfulSyncMillis = this?.lastSuccessfulSyncMillis,
    lastAttemptMillis = this?.lastAttemptMillis,
    lastErrorMessage = this?.lastErrorMessage,
    isSyncing = this?.isSyncing ?: false,
    backgroundSyncEnabled = backgroundSyncEnabled,
    remoteSourceEnabled = remoteSourceEnabled,
)
