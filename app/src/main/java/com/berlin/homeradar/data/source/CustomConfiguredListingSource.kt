package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.source.model.RawListing
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceType
import java.net.URI
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class CustomConfiguredListingSource(
    private val source: SourceDefinition,
    private val okHttpClient: OkHttpClient,
) : ListingSource {

    private val json = Json { ignoreUnknownKeys = true }
    override val sourceId: String = source.id

    override suspend fun fetch(): List<RawListing> = withContext(Dispatchers.IO) {
        when (source.sourceType) {
            SourceType.API -> fetchJsonFeed()
            else -> fetchHtmlPage()
        }
    }

    private fun fetchJsonFeed(): List<RawListing> {
        val body = executeRequest(source.websiteUrl)
        val root = json.parseToJsonElement(body)
        val items = when (root) {
            is JsonArray -> root
            is JsonObject -> root["listings"] as? JsonArray
                ?: root["results"] as? JsonArray
                ?: root["items"] as? JsonArray
                ?: JsonArray(emptyList())
            else -> JsonArray(emptyList())
        }
        return items.mapNotNullIndexed { index, element -> element.toRawListing(index) }
    }

    private fun fetchHtmlPage(): List<RawListing> {
        val body = executeRequest(source.websiteUrl)
        val document = Jsoup.parse(body, source.websiteUrl)

        val specialized = parseWithKnownStrategy(document)
        if (specialized.isNotEmpty()) return specialized

        val jsonLd = parseJsonLd(document)
        if (jsonLd.isNotEmpty()) return jsonLd

        val cards = document.select("article, .listing, .offer-item, .result-list-entry, .expose, li[data-id], .estate-item")
            .ifEmpty { document.select("a[href]").take(50) }

        return cards.take(50).mapNotNullIndexed { index, element -> genericElementToRawListing(index, element) }
    }

    private fun parseWithKnownStrategy(document: Document): List<RawListing> {
        val selectors = knownCardSelectors[source.id] ?: return emptyList()
        val cards = selectors.asSequence().map { document.select(it) }.firstOrNull { it.isNotEmpty() } ?: return emptyList()
        return cards.take(60).mapNotNullIndexed { index, element -> genericElementToRawListing(index, element) }
    }

    private fun parseJsonLd(document: Document): List<RawListing> {
        val scripts = document.select("script[type=application/ld+json]")
        if (scripts.isEmpty()) return emptyList()
        val items = buildJsonArray {
            scripts.forEach { script ->
                val raw = script.data().trim()
                if (raw.isBlank()) return@forEach
                runCatching { json.parseToJsonElement(raw) }.getOrNull()?.flattenJsonForListings()?.forEach { add(it) }
            }
        }
        return items.mapNotNullIndexed { index, element -> element.toRawListing(index) }
    }

    private fun genericElementToRawListing(index: Int, element: Element): RawListing? {
        val text = element.text().trim()
        val link = element.selectFirst("a[href]")?.absUrl("href").orEmpty().ifBlank { source.websiteUrl }
        val title = element.selectFirst("h1, h2, h3, h4, .title, .headline, .result-list-entry__brand-title, [title]")?.text()
            ?.takeIf { it.isNotBlank() }
            ?: text.lineSequence().firstOrNull()?.take(120)
            ?: return null
        val price = extractInt(text, listOf("""(\d[\d\.,]*)\s*€""", """€\s*(\d[\d\.,]*)""", """(\d[\d\.,]*)\s*EUR"""))
        val area = extractDouble(text, listOf("""(\d[\d\.,]*)\s*m²""", """(\d[\d\.,]*)\s*qm"""))
        val rooms = extractDouble(text, listOf("""(\d[\d\.,]*)\s*(Zimmer|rooms?)"""))
        val district = element.selectFirst(".district, .address, .location, .city, .estate-object-address, .offer-address, [class*=address]")?.text()
            ?.takeIf { it.isNotBlank() }
            ?: guessDistrict(text)
        val imageUrl = element.selectFirst("img[src], img[data-src], img[data-lazy-src]")?.let { img ->
            img.absUrl("src").ifBlank { img.absUrl("data-src") }.ifBlank { img.absUrl("data-lazy-src") }
        }?.ifBlank { null }
        return RawListing(
            source = source.id,
            externalId = element.attr("data-id").ifBlank { stableId(link.ifBlank { title + index }) },
            title = title.trim(),
            priceEuro = price ?: 0,
            district = district,
            location = district,
            rooms = rooms ?: 0.0,
            areaSqm = area ?: 0.0,
            imageUrl = imageUrl,
            listingUrl = link,
            isJobcenterSuitable = text.contains("jobcenter", ignoreCase = true),
            isWohngeldEligible = text.contains("wohngeld", ignoreCase = true),
            isWbsRequired = text.contains("wbs", ignoreCase = true),
        )
    }

    private fun JsonElement.toRawListing(index: Int): RawListing? {
        val obj = this as? JsonObject ?: return null
        val title = obj.string("title", "headline", "name", "description") ?: return null
        val listingUrl = obj.string("listingUrl", "url", "link", "href") ?: source.websiteUrl
        val location = obj.string("location", "address", "city", "district").orEmpty()
        return RawListing(
            source = source.id,
            externalId = obj.string("id", "externalId", "slug") ?: stableId("$title-$index-$listingUrl"),
            title = title,
            priceEuro = obj.int("priceEuro", "price", "rent") ?: 0,
            district = obj.string("district", "city", "location").orEmpty(),
            location = location,
            rooms = obj.double("rooms", "roomCount", "numberOfRooms") ?: 0.0,
            areaSqm = obj.double("areaSqm", "area", "size", "floorSize") ?: 0.0,
            imageUrl = obj.string("imageUrl", "image", "thumbnail"),
            listingUrl = listingUrl,
            isJobcenterSuitable = obj.bool("jobcenterSuitable", "isJobcenterSuitable") ?: false,
            isWohngeldEligible = obj.bool("wohngeldEligible", "isWohngeldEligible") ?: false,
            isWbsRequired = obj.bool("wbsRequired", "isWbsRequired") ?: false,
        )
    }

    private fun executeRequest(url: String): String {
        val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0 (Android) BerlinHomeRadar/1.0").build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            return response.body?.string().orEmpty()
        }
    }

    private fun stableId(value: String): String =
        MessageDigest.getInstance("MD5").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun extractInt(text: String, patterns: List<String>): Int? =
        patterns.asSequence()
            .mapNotNull { Regex(it, RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1) }
            .mapNotNull { raw -> raw.normalizeNumber()?.toDoubleOrNull()?.toInt() }
            .firstOrNull()

    private fun extractDouble(text: String, patterns: List<String>): Double? =
        patterns.asSequence()
            .mapNotNull { Regex(it, RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1) }
            .mapNotNull { raw -> raw.normalizeNumber()?.toDoubleOrNull() }
            .firstOrNull()

    private fun guessDistrict(text: String): String {
        val host = runCatching { URI(source.websiteUrl).host.orEmpty() }.getOrDefault("")
        return text.take(80).ifBlank {
            host.replace("www.", "").replace(".", " ").replace("-", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    companion object {
        private val knownCardSelectors: Map<String, List<String>> = mapOf(
            "wg-gesucht" to listOf(".offer_list_item", ".wgg_card", "article"),
            "immowelt" to listOf("[data-testid*=result-item]", ".SearchList-22b2e", "article"),
            "howoge" to listOf(".teaser, .offer-item, article"),
            "gewobag" to listOf(".estate, .offer, article, li"),
            "degewo" to listOf(".teaser, .offer-item, article, li"),
            "gesobau" to listOf(".offer-item, .listing, article, li"),
            "wbm" to listOf(".offer-item, .property, article, li"),
            "berlinovo" to listOf(".apartment, .offer-item, article, li"),
            "inberlinwohnen" to listOf(".offer-item, .estate-item, article, li")
        )
    }
}

private fun JsonElement.flattenJsonForListings(): List<JsonElement> = when (this) {
    is JsonArray -> this.toList()
    is JsonObject -> {
        val direct = listOfNotNull(this["listings"], this["results"], this["items"], this["mainEntity"])
            .flatMap { it.flattenJsonForListings() }
        if (direct.isNotEmpty()) direct else listOf(this)
    }
    else -> emptyList()
}

private fun String.normalizeNumber(): String? {
    val cleaned = replace(" ", " ").replace(" ", "")
    return when {
        cleaned.isBlank() -> null
        cleaned.contains(',') && cleaned.contains('.') -> cleaned.replace(".", "").replace(",", ".")
        cleaned.contains(',') -> cleaned.replace(",", ".")
        else -> cleaned
    }
}

private fun JsonObject.string(vararg keys: String): String? =
    keys.asSequence().mapNotNull { key -> this[key]?.toString()?.trim('"') }.firstOrNull { it.isNotBlank() }

private fun JsonObject.int(vararg keys: String): Int? =
    keys.asSequence().mapNotNull { key -> this[key]?.toString()?.trim('"')?.normalizeNumber()?.toDoubleOrNull()?.toInt() }.firstOrNull()

private fun JsonObject.double(vararg keys: String): Double? =
    keys.asSequence().mapNotNull { key -> this[key]?.toString()?.trim('"')?.normalizeNumber()?.toDoubleOrNull() }.firstOrNull()

private fun JsonObject.bool(vararg keys: String): Boolean? =
    keys.asSequence().mapNotNull { key -> this[key]?.toString()?.trim('"')?.toBooleanStrictOrNull() }.firstOrNull()

private inline fun <T, R : Any> Iterable<T>.mapNotNullIndexed(transform: (Int, T) -> R?): List<R> {
    val destination = ArrayList<R>()
    forEachIndexed { index, item ->
        transform(index, item)?.let(destination::add)
    }
    return destination
}
