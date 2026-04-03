package com.berlin.homeradar.data.source

import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceType

object SourceCatalog {
    val BundledJson = SourceDefinition(
        id = "bundled-json",
        displayName = "Local JSON Demo Feed",
        websiteUrl = "https://example.com/berlin-home-radar/demo-json",
        supportsAutomatedSync = true,
        description = "Offline demo feed bundled with the app.",
        sourceType = SourceType.API,
    )

    val BundledHtml = SourceDefinition(
        id = "bundled-html",
        displayName = "Local HTML Demo Feed",
        websiteUrl = "https://example.com/berlin-home-radar/demo-html",
        supportsAutomatedSync = true,
        description = "Offline HTML parser demo bundled with the app.",
        sourceType = SourceType.HTML,
    )

    val RemoteJson = SourceDefinition(
        id = "remote-json",
        displayName = "Custom Remote JSON Feed",
        websiteUrl = "https://example.com/",
        supportsAutomatedSync = true,
        description = "Optional remote JSON endpoint for your own compliant feed.",
        sourceType = SourceType.API,
    )

    val all: List<SourceDefinition> = listOf(
        BundledJson,
        BundledHtml,
        RemoteJson,
        SourceDefinition("immobilienscout24", "ImmoScout24", "https://www.immobilienscout24.de/Suche/de/berlin/berlin/wohnung-mieten", false, "Large marketplace. Keep as browser-assisted because the site is dynamic.", sourceType = SourceType.WEBVIEW),
        SourceDefinition("immowelt", "immowelt", "https://www.immowelt.de/suche/mieten/wohnung/berlin/berlin-10115/ad08de8634", true, "First-wave dedicated best-effort HTML parser for Berlin rentals.", sourceType = SourceType.HTML),
        SourceDefinition("kleinanzeigen", "Kleinanzeigen", "https://www.kleinanzeigen.de/s-wohnung-mieten/berlin/k0c203l3331", false, "Keep browser-assisted because markup and anti-bot rules change often.", sourceType = SourceType.WEBVIEW),
        SourceDefinition("wg-gesucht", "WG-Gesucht", "https://www.wg-gesucht.de/en/wg-zimmer-in-Berlin.8.0.1.0.html", true, "First-wave dedicated parser for rooms, flatshares, and temporary rentals.", sourceType = SourceType.HTML),
        SourceDefinition("vonovia", "Vonovia", "https://www.vonovia.de/meine-stadt/wohnungen-in-berlin", false, "Keep browser-assisted because inventory rendering changes frequently.", sourceType = SourceType.WEBVIEW),
        SourceDefinition("howoge", "HOWOGE", "https://www.howoge.de/wohnungen-gewerbe/wohnungssuche.html", true, "First-wave dedicated parser for municipal housing offers.", sourceType = SourceType.HTML),
        SourceDefinition("gewobag", "Gewobag", "https://www.gewobag.de/fuer-mieter-und-mietinteressenten/mietangebote/", true, "First-wave dedicated parser for municipal housing offers.", sourceType = SourceType.HTML),
        SourceDefinition("degewo", "degewo", "https://www.degewo.de/wohnungen-gewerbe/wohnungssuche", true, "First-wave dedicated parser for municipal housing offers.", sourceType = SourceType.HTML),
        SourceDefinition("gesobau", "GESOBAU", "https://www.gesobau.de/mieten/wohnungssuche/", true, "First-wave dedicated parser for municipal housing offers.", sourceType = SourceType.HTML),
        SourceDefinition("wbm", "WBM", "https://www.wbm.de/wohnungen-berlin/angebote/", true, "First-wave dedicated parser for municipal housing offers.", sourceType = SourceType.HTML),
        SourceDefinition("berlinovo", "Berlinovo", "https://www.berlinovo.de/de/apartments", true, "First-wave dedicated parser for furnished and temporary apartments.", sourceType = SourceType.HTML),
        SourceDefinition("inberlinwohnen", "inberlinwohnen", "https://inberlinwohnen.de/wohnungsfinder/", true, "First-wave dedicated parser for municipal pooled offers.", sourceType = SourceType.HTML),
    )

    val supportedSyncSourceIds: Set<String> = all.filter { it.supportsAutomatedSync }.map { it.id }.toSet()

    fun nameFor(sourceId: String): String = all.firstOrNull { it.id == sourceId }?.displayName ?: sourceId
    fun sourceFor(sourceId: String): SourceDefinition? = all.firstOrNull { it.id == sourceId }
}
