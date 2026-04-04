package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.dao.SourceMetricDao
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.local.entity.SourceMetricEntity
import com.berlin.homeradar.data.message.UserFacingMessages
import com.berlin.homeradar.data.source.model.RawListing
import com.berlin.homeradar.domain.model.ListingLifecycleStatus
import java.security.cert.CertPathValidatorException
import javax.net.ssl.SSLException

internal fun buildMergedEntities(
    incomingListings: List<RawListing>,
    existingListings: List<HousingListingEntity>,
    now: Long,
    deduplicationPolicy: DeduplicationPolicy,
): List<HousingListingEntity> {
    val existingByDirectKey = existingListings.associateBy { ListingIdentity(it.source, it.externalId) }.toMutableMap()
    val existingByFingerprint = existingListings.associateBy { it.fingerprint }.toMutableMap()
    val stagedByDirectKey = linkedMapOf<ListingIdentity, HousingListingEntity>()
    val stagedByFingerprint = mutableMapOf<String, HousingListingEntity>()

    incomingListings.forEach { incoming ->
        val directKey = ListingIdentity(incoming.source, incoming.externalId)
        val fingerprint = deduplicationPolicy.fingerprint(incoming)
        val existing = stagedByDirectKey[directKey]
            ?: stagedByFingerprint[fingerprint]
            ?: existingByDirectKey[directKey]
            ?: existingByFingerprint[fingerprint]

        val mergedEntity = if (existing != null) {
            deduplicationPolicy.merge(existing, incoming, now)
        } else {
            deduplicationPolicy.toEntity(raw = incoming, existingId = null, isFavorite = false, now = now)
        }

        stagedByDirectKey[directKey] = mergedEntity
        stagedByFingerprint[mergedEntity.fingerprint] = mergedEntity
        existingByDirectKey[ListingIdentity(mergedEntity.source, mergedEntity.externalId)] = mergedEntity
        existingByFingerprint[mergedEntity.fingerprint] = mergedEntity
    }

    return stagedByDirectKey.values.distinctBy { it.id.takeIf { id -> id != 0L }?.let { id -> "id:$id" } ?: "${it.source}|${it.externalId}" }
}

internal fun buildInactiveEntities(
    existingListings: List<HousingListingEntity>,
    refreshedEntities: List<HousingListingEntity>,
    successfulSourceIds: Set<String>,
    now: Long,
): List<HousingListingEntity> {
    if (successfulSourceIds.isEmpty()) return emptyList()
    val refreshedIds = refreshedEntities.mapNotNull { it.id.takeIf { id -> id != 0L } }.toSet()
    return existingListings
        .filter { it.source in successfulSourceIds }
        .filter { it.id == 0L || it.id !in refreshedIds }
        .map { existing ->
            val lastSeen = existing.lastSeenAtEpochMillis.takeIf { it > 0L } ?: existing.updatedAtEpochMillis
            val archived = now - lastSeen >= ARCHIVE_AFTER_MS
            existing.copy(
                isActive = false,
                lifecycleStatus = if (archived) ListingLifecycleStatus.ARCHIVED.storageValue else ListingLifecycleStatus.STALE.storageValue,
            )
        }
}

internal suspend fun updateSourceMetrics(
    sourceMetricDao: SourceMetricDao,
    outcomes: List<SourceFetchOutcome>,
    existingMetrics: Map<String, SourceMetricEntity>,
) {
    if (outcomes.isEmpty()) return
    val updated = outcomes.map { outcome ->
        val previous = existingMetrics[outcome.sourceId] ?: SourceMetricEntity(sourceId = outcome.sourceId)
        val newSuccessCount = previous.successCount + if (outcome.success) 1 else 0
        val newFailureCount = previous.failureCount + if (outcome.success) 0 else 1
        val totalSuccessfulPulls = maxOf(1, newSuccessCount)
        val previousSuccessCount = maxOf(0, previous.successCount)

        previous.copy(
            successCount = newSuccessCount,
            failureCount = newFailureCount,
            lastAttemptMillis = outcome.finishedAtMillis,
            lastSuccessfulPullMillis = if (outcome.success) outcome.finishedAtMillis else previous.lastSuccessfulPullMillis,
            lastItemCount = outcome.itemCount,
            averageItemCount = if (outcome.success) ((previous.averageItemCount * previousSuccessCount) + outcome.itemCount) / totalSuccessfulPulls else previous.averageItemCount,
            averageDurationMillis = if (previous.totalAttempts() == 0) outcome.durationMillis.toDouble() else ((previous.averageDurationMillis * previous.totalAttempts()) + outcome.durationMillis) / (previous.totalAttempts() + 1),
            consecutiveZeroItemPulls = when {
                !outcome.success -> previous.consecutiveZeroItemPulls
                outcome.itemCount == 0 -> previous.consecutiveZeroItemPulls + 1
                else -> 0
            },
            lastErrorMessage = outcome.errorMessage,
        )
    }
    sourceMetricDao.upsertAll(updated)
}

private fun SourceMetricEntity.totalAttempts(): Int = successCount + failureCount

internal data class ListingIdentity(val source: String, val externalId: String)

internal data class SourceFetchOutcome(
    val sourceId: String,
    val success: Boolean,
    val itemCount: Int,
    val durationMillis: Long,
    val finishedAtMillis: Long,
    val errorMessage: String?,
) {
    companion object {
        fun success(sourceId: String, itemCount: Int, durationMillis: Long): SourceFetchOutcome = SourceFetchOutcome(sourceId, true, itemCount, durationMillis, System.currentTimeMillis(), null)
        fun failure(sourceId: String, durationMillis: Long, errorMessage: String): SourceFetchOutcome = SourceFetchOutcome(sourceId, false, 0, durationMillis, System.currentTimeMillis(), errorMessage)
    }
}

internal fun Throwable.toUserMessage(): String {
    val message = message.orEmpty()
    return when {
        this is SSLException || this is CertPathValidatorException || message.contains("CertPathValidatorException", ignoreCase = true) -> UserFacingMessages.SECURE_CONNECTION_FAILED
        message.contains("Unable to resolve host", ignoreCase = true) -> UserFacingMessages.NO_INTERNET
        else -> message.ifBlank { UserFacingMessages.REFRESH_FAILED_GENERIC }
    }
}

private const val ARCHIVE_AFTER_MS = 7L * 24 * 60 * 60 * 1000
