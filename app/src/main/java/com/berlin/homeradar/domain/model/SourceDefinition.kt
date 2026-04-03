package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SourceDefinition(
    val id: String,
    val displayName: String,
    val websiteUrl: String,
    val supportsAutomatedSync: Boolean,
    val description: String,
    val isUserAdded: Boolean = false,
    val sourceType: SourceType = if (supportsAutomatedSync) SourceType.HTML else SourceType.CATALOG,
)
