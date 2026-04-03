package com.berlin.homeradar.domain.model

enum class SourceHealthStatus {
    IDLE,
    CHECKING,
    SUCCESS,
    FAILED,
    UNSUPPORTED,
}

data class SourceHealth(
    val sourceId: String,
    val status: SourceHealthStatus,
    val message: String? = null,
    val checkedAtMillis: Long? = null,
)
