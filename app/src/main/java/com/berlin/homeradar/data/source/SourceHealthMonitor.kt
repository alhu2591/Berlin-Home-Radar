package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.message.UserFacingMessages
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceHealth
import com.berlin.homeradar.domain.model.SourceHealthStatus
import com.berlin.homeradar.domain.model.SourceType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SourceHealthMonitor @Inject constructor(
    private val registry: ListingSourceRegistry,
) {
    private val _statuses = MutableStateFlow<Map<String, SourceHealth>>(emptyMap())
    val statuses: StateFlow<Map<String, SourceHealth>> = _statuses.asStateFlow()

    suspend fun testSource(definition: SourceDefinition): Result<Unit> {
        val sourceId = definition.id
        if (!definition.supportsAutomatedSync && !definition.isUserAdded) {
            update(
                sourceId = sourceId,
                status = if (definition.sourceType == SourceType.CATALOG) SourceHealthStatus.UNSUPPORTED else SourceHealthStatus.IDLE,
                message = when (definition.sourceType) {
                    SourceType.WEBVIEW -> UserFacingMessages.SOURCE_WEBVIEW
                    SourceType.HTML -> UserFacingMessages.SOURCE_HTML_CATALOG
                    SourceType.API -> UserFacingMessages.SOURCE_API_METADATA
                    SourceType.CATALOG -> UserFacingMessages.SOURCE_CATALOG_ONLY
                },
            )
            return Result.success(Unit)
        }

        update(sourceId, SourceHealthStatus.CHECKING, UserFacingMessages.SOURCE_TESTING)
        val result = registry.adapterFor(definition)?.healthCheck() ?: Result.failure(IllegalStateException(UserFacingMessages.SOURCE_ADAPTER_MISSING))
        result
            .onSuccess { update(sourceId, SourceHealthStatus.SUCCESS, UserFacingMessages.SOURCE_ADAPTER_HEALTHY) }
            .onFailure { update(sourceId, SourceHealthStatus.FAILED, it.message ?: UserFacingMessages.SOURCE_TEST_FAILED) }
        return result
    }

    private fun update(sourceId: String, status: SourceHealthStatus, message: String?) {
        _statuses.value = _statuses.value + (sourceId to SourceHealth(sourceId, status, message, System.currentTimeMillis()))
    }
}
