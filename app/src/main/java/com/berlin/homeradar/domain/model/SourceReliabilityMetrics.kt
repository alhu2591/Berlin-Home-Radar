package com.berlin.homeradar.domain.model

data class SourceReliabilityMetrics(
    val sourceId: String,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val lastAttemptMillis: Long? = null,
    val lastSuccessfulPullMillis: Long? = null,
    val lastItemCount: Int = 0,
    val averageItemCount: Double = 0.0,
    val averageDurationMillis: Double = 0.0,
    val consecutiveZeroItemPulls: Int = 0,
    val lastErrorMessage: String? = null,
) {
    val totalAttempts: Int get() = successCount + failureCount
    val successRatePercent: Int get() = if (totalAttempts == 0) 0 else ((successCount * 100.0) / totalAttempts).toInt()
    val hasZeroResultsAnomaly: Boolean get() = consecutiveZeroItemPulls >= 2
}
