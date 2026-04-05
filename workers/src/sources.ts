import type { NormalizedListing, SourcePayload, SourceProbeResult, WorkerEnv } from './types';
import { berlinDistrictMatches } from './berlinDistricts';
import { getSourceProfile } from './profiles';
import { buildPublicUrlsForSource, buildSourceFetchPlan, resolveFetchStrategy } from './fetchPlan';
import { listingFromKnownDetailPage, listingsFromKnownListPage, previewKnownListPageLinks } from './siteParsers';
import { dedupeByUrl, extractListingUrls, listingFromDetailHeuristics, listingsFromJsonLd, listingsFromSnippetText, normalizeWhitespace, parseRss, takeLimit, toNormalizedListing } from './utils';

const DEFAULT_HEADERS = {
  'user-agent': 'Mozilla/5.0 (compatible; HomeSyncBerlinWorker/1.7; +https://example.invalid)',
  accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
  'accept-language': 'de-DE,de;q=0.9,en;q=0.8',
};

async function fetchText(url: string, init?: RequestInit) {
  const response = await fetch(url, { redirect: 'follow', ...init, headers: { ...DEFAULT_HEADERS, ...(init?.headers ?? {}) } });
  if (!response.ok) throw new Error(`Fetch failed ${response.status} for ${url}`);
  return response.text();
}

function basePatternsForSource(adapterId: string) {
  const profile = getSourceProfile(adapterId);
  if (profile?.detailPatterns.length) return profile.detailPatterns.map((pattern) => new RegExp(`href=["']([^"']*${pattern}[^"']*)["']`, 'gi'));
  return [/href=["']([^"']*(?:wohnung|haus|apartment|immobilie|anzeige|mietangebote|wohnungssuche|angebote|apartments|housing)[^"']*)["']/gi];
}

async function fetchDetailListings(source: SourcePayload, urls: string[], fetchedAt: string) {
  const detailUrls = urls.slice(0, Math.min(Number(source.config.maxResults ?? '15') || 15, 15));
  const pages = await Promise.all(detailUrls.map(async (url) => {
    try {
      const html = await fetchText(url);
      const parsed = listingsFromJsonLd(source, html, url, fetchedAt);
      if (parsed.length > 0) return parsed[0];
      return listingFromKnownDetailPage(source, html, url, fetchedAt) ?? listingFromDetailHeuristics(source, html, url, fetchedAt);
    } catch {
      return null;
    }
  }));
  return pages.filter(Boolean) as NormalizedListing[];
}


async function probeDetailPages(source: SourcePayload, urls: string[], fetchedAt: string) {
  const detailUrls = urls.slice(0, 3);
  let detailKnownCount = 0;
  let detailJsonLdCount = 0;
  let detailHeuristicCount = 0;
  const sampleTitles: string[] = [];

  for (const detailUrl of detailUrls) {
    try {
      const html = await fetchText(detailUrl);
      const known = listingFromKnownDetailPage(source, html, detailUrl, fetchedAt);
      if (known) {
        detailKnownCount += 1;
        if (known.title) sampleTitles.push(known.title);
        continue;
      }
      const jsonLd = listingsFromJsonLd(source, html, detailUrl, fetchedAt);
      if (jsonLd.length > 0) {
        detailJsonLdCount += 1;
        if (jsonLd[0]?.title) sampleTitles.push(jsonLd[0].title);
        continue;
      }
      const heuristic = listingFromDetailHeuristics(source, html, detailUrl, fetchedAt);
      if (heuristic) {
        detailHeuristicCount += 1;
        if (heuristic.title) sampleTitles.push(heuristic.title);
      }
    } catch {
      // ignore individual probe errors
    }
  }

  return {
    detailFetchedCount: detailUrls.length,
    detailKnownCount,
    detailJsonLdCount,
    detailHeuristicCount,
    sampleTitles: Array.from(new Set(sampleTitles)).slice(0, 4),
  };
}

async function fetchPublicHtmlListings(source: SourcePayload): Promise<NormalizedListing[]> {
  const urls = buildPublicUrlsForSource(source);
  const fetchedAt = new Date().toISOString();
  const all: NormalizedListing[] = [];
  for (const url of urls) {
    const html = await fetchText(url);
    const fromKnownListPage = listingsFromKnownListPage(source, html, url, fetchedAt);
    const fromJsonLd = listingsFromJsonLd(source, html, url, fetchedAt);
    const fromText = listingsFromSnippetText(source, html, url, fetchedAt);
    const listingUrls = extractListingUrls(html, url, basePatternsForSource(source.adapterId));
    const detailThreshold = Math.max(4, Math.ceil((Number(source.config.maxResults ?? '12') || 12) / 2));
    const detailListings = (fromKnownListPage.length + fromJsonLd.length + fromText.length < detailThreshold && listingUrls.length > 0) ? await fetchDetailListings(source, listingUrls, fetchedAt) : [];
    all.push(...fromKnownListPage, ...fromJsonLd, ...fromText, ...detailListings);
  }
  const filtered = source.config.district
    ? dedupeByUrl(all).filter((item) => berlinDistrictMatches(`${item.title} ${item.district ?? ''} ${item.description ?? ''}`, source.config.district))
    : dedupeByUrl(all);
  return takeLimit(filtered, source.config.maxResults);
}

function percentEncode(value: string) {
  return encodeURIComponent(value)
    .replace(/!/g, '%21')
    .replace(/\*/g, '%2A')
    .replace(/'/g, '%27')
    .replace(/\(/g, '%28')
    .replace(/\)/g, '%29');
}

async function signHmacSha1(key: string, message: string) {
  const cryptoKey = await crypto.subtle.importKey('raw', new TextEncoder().encode(key), { name: 'HMAC', hash: 'SHA-1' }, false, ['sign']);
  const signature = await crypto.subtle.sign('HMAC', cryptoKey, new TextEncoder().encode(message));
  return btoa(String.fromCharCode(...new Uint8Array(signature)));
}

async function buildOAuthHeader(url: string, consumerKey: string, consumerSecret: string) {
  const oauth: Record<string, string> = {
    oauth_consumer_key: consumerKey,
    oauth_nonce: crypto.randomUUID().replace(/-/g, ''),
    oauth_signature_method: 'HMAC-SHA1',
    oauth_timestamp: Math.floor(Date.now() / 1000).toString(),
    oauth_version: '1.0',
  };
  const parsedUrl = new URL(url);
  const params: Array<readonly [string, string]> = [];
  parsedUrl.searchParams.forEach((value, key) => { params.push([key, value] as const); });
  const baseParams = [...params, ...Object.entries(oauth)]
    .sort(([aKey, aValue], [bKey, bValue]) => `${aKey}=${aValue}`.localeCompare(`${bKey}=${bValue}`))
    .map(([key, value]) => `${percentEncode(key)}=${percentEncode(value)}`)
    .join('&');
  const normalizedUrl = `${parsedUrl.protocol}//${parsedUrl.host}${parsedUrl.pathname}`;
  const baseString = ['GET', percentEncode(normalizedUrl), percentEncode(baseParams)].join('&');
  const signingKey = `${percentEncode(consumerSecret)}&`;
  oauth.oauth_signature = await signHmacSha1(signingKey, baseString);
  const header = Object.entries(oauth)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([key, value]) => `${percentEncode(key)}="${percentEncode(value)}"`)
    .join(', ');
  return `OAuth ${header}`;
}

function immoscoutRealEstateTypes(source: SourcePayload) {
  const listingType = source.config.listingType ?? 'rent';
  const includeHouses = source.config.includeHouses === 'yes';
  const types: string[] = [];
  if (listingType === 'rent' || listingType === 'both') {
    types.push('apartmentrent');
    if (includeHouses) types.push('houserent');
  }
  if (listingType === 'buy' || listingType === 'both') {
    types.push('apartmentbuy');
    if (includeHouses) types.push('housebuy');
  }
  return types;
}

function gatherScoutEntries(node: unknown, out: Record<string, unknown>[] = []) {
  if (Array.isArray(node)) {
    node.forEach((item) => gatherScoutEntries(item, out));
    return out;
  }
  if (!node || typeof node !== 'object') return out;
  const record = node as Record<string, unknown>;
  if ('realEstateId' in record || 'title' in record || 'realEstate' in record) out.push(record);
  Object.values(record).forEach((value) => gatherScoutEntries(value, out));
  return out;
}

function parseScoutJson(source: SourcePayload, json: unknown): NormalizedListing[] {
  const entries = gatherScoutEntries(json);
  return dedupeByUrl(entries.map((entry, index) => {
    const realEstate = (entry.realEstate && typeof entry.realEstate === 'object' ? entry.realEstate : entry) as Record<string, unknown>;
    const address = (realEstate.address && typeof realEstate.address === 'object' ? realEstate.address : {}) as Record<string, unknown>;
    const geo = (address.wgs84Coordinate && typeof address.wgs84Coordinate === 'object' ? address.wgs84Coordinate : {}) as Record<string, unknown>;
    const realtor = (entry.realtor && typeof entry.realtor === 'object' ? entry.realtor : {}) as Record<string, unknown>;
    const pictures = Array.isArray(realEstate.attachments) ? realEstate.attachments : Array.isArray(entry.attachments) ? entry.attachments : [];
    const exposeId = String(entry.realEstateId ?? realEstate['@id'] ?? realEstate.id ?? index + 1);
    const title = normalizeWhitespace(String(entry.title ?? realEstate.title ?? 'ImmobilienScout24 listing'));
    return toNormalizedListing(source, {
      sourceListingId: exposeId,
      sourceUrl: `https://www.immobilienscout24.de/expose/${exposeId}`,
      title,
      description: normalizeWhitespace(String(realEstate.descriptionNote ?? realEstate.description ?? '')) || null,
      city: normalizeWhitespace(String(address.city ?? 'Berlin')) || 'Berlin',
      district: normalizeWhitespace(String(address.quarter ?? source.config.district ?? '')) || null,
      street: normalizeWhitespace(String(address.street ?? '')) || null,
      houseNumber: normalizeWhitespace(String(address.houseNumber ?? '')) || null,
      zip: normalizeWhitespace(String(address.postcode ?? '')) || null,
      listingType: source.config.listingType === 'buy' ? 'buy' : 'rent',
      propertyType: String(realEstate['@xsi.type'] ?? '').toLowerCase().includes('house') ? 'house' : 'apartment',
      price: Number((((realEstate.price as Record<string, unknown> | undefined)?.value ?? (realEstate.baseRent as Record<string, unknown> | undefined)?.value ?? (realEstate.calculatedTotalRent as Record<string, unknown> | undefined)?.value) ?? 0)) || null,
      sizeSqm: Number((realEstate.livingSpace ?? 0) || 0) || null,
      rooms: Number((realEstate.numberOfRooms ?? 0) || 0) || null,
      lat: Number((geo.latitude ?? 0) || 0) || null,
      lng: Number((geo.longitude ?? 0) || 0) || null,
      listedAt: normalizeWhitespace(String(realEstate.creationDate ?? realEstate.lastModificationDate ?? '')) || null,
      images: pictures.map((item) => String((item as Record<string, unknown>).url ?? '')).filter(Boolean),
      contactName: normalizeWhitespace(String(realtor.companyName ?? realtor.firstname ?? '')) || null,
    });
  }));
}

async function fetchImmoScoutOfficial(source: SourcePayload, env: WorkerEnv) {
  const consumerKey = env.IMMOSCOUT_CONSUMER_KEY?.trim();
  const consumerSecret = env.IMMOSCOUT_CONSUMER_SECRET?.trim();
  const geocode = env.IMMOSCOUT_REGION_GEOCODE?.trim();
  if (!consumerKey || !consumerSecret || !geocode) throw new Error('ImmoScout24 official API requires consumer key, secret, and region geocode');

  const listings: NormalizedListing[] = [];
  for (const type of immoscoutRealEstateTypes(source)) {
    const url = `https://rest.immobilienscout24.de/restapi/api/search/v1.0/search/region?realestatetype=${encodeURIComponent(type)}&geocodes=${encodeURIComponent(geocode)}`;
    const authHeader = await buildOAuthHeader(url, consumerKey, consumerSecret);
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json',
        Authorization: authHeader,
      },
    });
    if (!response.ok) throw new Error(`ImmoScout24 official API error ${response.status}`);
    const json = await response.json();
    listings.push(...parseScoutJson(source, json));
  }
  return takeLimit(dedupeByUrl(listings), source.config.maxResults);
}

async function fetchLocalAgencyRss(source: SourcePayload, env: WorkerEnv) {
  const urls = (source.config.rssUrl || env.DEFAULT_LOCAL_RSS_URLS || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
  if (urls.length === 0) return [];
  const listings: NormalizedListing[] = [];
  for (const url of urls) {
    const xml = await fetchText(url, { headers: { Accept: 'application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.8' } });
    const items = parseRss(xml);
    for (const item of items) {
      if (!item.title || !item.url) continue;
      const districtHint = source.config.district || null;
      listings.push(toNormalizedListing(source, {
        sourceListingId: item.url,
        sourceUrl: item.url,
        title: item.title,
        description: item.description,
        city: 'Berlin',
        district: districtHint,
        listingType: source.config.listingType === 'buy' ? 'buy' : 'rent',
        propertyType: 'apartment',
        listedAt: item.publishedAt,
      }));
    }
  }
  return takeLimit(dedupeByUrl(listings), source.config.maxResults);
}


export async function probeSource(source: SourcePayload, env: WorkerEnv): Promise<SourceProbeResult> {
  const strategyResolved = resolveFetchStrategy(source, env);
  if (strategyResolved === 'rss') {
    const urls = buildPublicUrlsForSource(source).map((url) => ({
      url,
      candidateDetailUrls: [],
      parsedListCount: 0,
      jsonLdCount: 0,
      snippetCount: 0,
      htmlBytes: 0,
      detailFetchedCount: 0,
      detailKnownCount: 0,
      detailJsonLdCount: 0,
      detailHeuristicCount: 0,
      sampleTitles: [],
    }));
    return { adapterId: source.adapterId, strategyResolved, urls };
  }
  if (strategyResolved === 'official_api') {
    return { adapterId: source.adapterId, strategyResolved, urls: [] };
  }
  const urls = buildPublicUrlsForSource(source);
  const results = [];
  for (const url of urls.slice(0, 4)) {
    const html = await fetchText(url);
    const candidateDetailUrls = previewKnownListPageLinks(source, html, url);
    const detailProbe = await probeDetailPages(source, candidateDetailUrls, new Date().toISOString());
    results.push({
      url,
      candidateDetailUrls,
      parsedListCount: listingsFromKnownListPage(source, html, url).length,
      jsonLdCount: listingsFromJsonLd(source, html, url).length,
      snippetCount: listingsFromSnippetText(source, html, url).length,
      htmlBytes: html.length,
      ...detailProbe,
    });
  }
  return { adapterId: source.adapterId, strategyResolved, urls: results };
}

export function diagnoseSource(source: SourcePayload, env: WorkerEnv) {
  return {
    plan: buildSourceFetchPlan(source, env),
    profile: getSourceProfile(source.adapterId) ?? null,
  };
}

export async function fetchListingsForSource(source: SourcePayload, env: WorkerEnv): Promise<NormalizedListing[]> {
  const strategy = resolveFetchStrategy(source, env);
  if (strategy === 'rss') return fetchLocalAgencyRss(source, env);
  if (strategy === 'official_api') return fetchImmoScoutOfficial(source, env);
  return fetchPublicHtmlListings(source);
}
