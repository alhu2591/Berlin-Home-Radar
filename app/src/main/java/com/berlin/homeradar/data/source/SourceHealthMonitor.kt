package com.berlin.homeradar.data.source

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
                    SourceType.WEBVIEW -> "WebView-assisted source. Open externally or add a dedicated adapter later."
                    SourceType.HTML -> "Catalog-only HTML source. Add adapter before automated sync."
                    SourceType.API -> "API source metadata only. Provide adapter or endpoint."
                    SourceType.CATALOG -> "Catalog source only."
                },
            )
            return Result.success(Unit)
        }

        update(sourceId, SourceHealthStatus.CHECKING, "Testing source adapter…")
        val result = registry.adapterFor(definition)?.healthCheck() ?: Result.failure(IllegalStateException("Adapter missing"))
        result
            .onSuccess { update(sourceId, SourceHealthStatus.SUCCESS, "Source adapter is healthy.") }
            .onFailure { update(sourceId, SourceHealthStatus.FAILED, it.message ?: "Source test failed") }
        return result
    }

    private fun update(sourceId: String, status: SourceHealthStatus, message: String?) {
        _statuses.value = _statuses.value + (sourceId to SourceHealth(sourceId, status, message, System.currentTimeMillis()))
    }
}
