import { districtAliasesFor } from './berlinDistricts';
import { getSourceProfile } from './profiles';
import type { SourcePayload, WorkerEnv } from './types';

export type ResolvedFetchStrategy = 'rss' | 'official_api' | 'public_html';

export interface SourceFetchPlan {
  adapterId: string;
  strategyRequested: string;
  strategyResolved: ResolvedFetchStrategy;
  district: string | null;
  districtAliases: string[];
  publicUrls: string[];
  officialApiConfigured: boolean;
  rssConfigured: boolean;
  maxResults: number;
  notes: string[];
}

function clampMaxResults(value: string | undefined) {
  const parsed = Number(value ?? '20');
  if (!Number.isFinite(parsed) || parsed <= 0) return 20;
  return Math.min(Math.round(parsed), 50);
}

function withQuery(url: string, params: Record<string, string | null | undefined>) {
  const next = new URL(url);
  for (const [key, value] of Object.entries(params)) {
    if (!value) continue;
    next.searchParams.set(key, value);
  }
  return next.toString();
}

function districtVariants(district: string | undefined) {
  const raw = district?.trim();
  if (!raw) return [];
  const aliases = districtAliasesFor(raw);
  return Array.from(new Set([raw, ...aliases])).filter(Boolean);
}

function districtToken(district: string | undefined) {
  return districtVariants(district)[0] ?? '';
}

export function buildPublicUrlsForSource(source: SourcePayload): string[] {
  if (source.config.customSearchUrl?.trim()) return [source.config.customSearchUrl.trim()];
  const profile = getSourceProfile(source.adapterId);
  if (!profile) return [];
  const listingType = source.config.listingType ?? 'rent';
  const includeHouses = source.config.includeHouses === 'yes';
  const district = districtToken(source.config.district);

  switch (source.adapterId) {
    case 'immoscout-de': {
      const urls: string[] = [];
      if (listingType === 'rent' || listingType === 'both') {
        urls.push(withQuery('https://www.immobilienscout24.de/Suche/de/berlin/berlin/wohnung-mieten', district ? { enteredFrom: 'search_form', q: district } : { enteredFrom: 'search_form' }));
        if (includeHouses) urls.push(withQuery('https://www.immobilienscout24.de/Suche/de/berlin/berlin/haus-mieten', district ? { enteredFrom: 'search_form', q: district } : { enteredFrom: 'search_form' }));
      }
      if (listingType === 'buy' || listingType === 'both') {
        urls.push(withQuery('https://www.immobilienscout24.de/Suche/de/berlin/berlin/wohnung-kaufen', district ? { enteredFrom: 'search_form', q: district } : { enteredFrom: 'search_form' }));
        if (includeHouses) urls.push(withQuery('https://www.immobilienscout24.de/Suche/de/berlin/berlin/haus-kaufen', district ? { enteredFrom: 'search_form', q: district } : { enteredFrom: 'search_form' }));
      }
      return urls;
    }
    case 'immowelt-de': {
      const urls: string[] = [];
      const params = district ? { q: district } : {};
      if (listingType === 'rent' || listingType === 'both') {
        urls.push(withQuery('https://www.immowelt.de/suche/mieten/wohnung/berlin/berlin-10115/ad08de8634', params));
        if (includeHouses) urls.push(withQuery('https://www.immowelt.de/suche/mieten/haus/berlin/berlin-10115/ad08de8634', params));
      }
      if (listingType === 'buy' || listingType === 'both') {
        urls.push(withQuery('https://www.immowelt.de/suche/kaufen/wohnung/berlin/berlin-10115/ad08de8634', params));
        if (includeHouses) urls.push(withQuery('https://www.immowelt.de/suche/kaufen/haus/berlin/berlin-10115/ad08de8634', params));
      }
      return urls;
    }
    case 'immonet-de': {
      const urls: string[] = [];
      const params = district ? { q: district } : {};
      if (listingType === 'rent' || listingType === 'both') {
        urls.push(withQuery('https://www.immonet.de/berlin/wohnung-mieten.html', params));
        if (includeHouses) urls.push(withQuery('https://www.immonet.de/berlin/haus-mieten.html', params));
      }
      if (listingType === 'buy' || listingType === 'both') {
        urls.push(withQuery('https://www.immonet.de/berlin/wohnung-kaufen.html', params));
        if (includeHouses) urls.push(withQuery('https://www.immonet.de/berlin/haus-kaufen.html', params));
      }
      return urls;
    }
    case 'wg-gesucht-de': {
      const mode = source.config.wgMode ?? 'both';
      const urls: string[] = [];
      if (mode === 'rooms' || mode === 'both') urls.push(withQuery('https://www.wg-gesucht.de/wg-zimmer-in-Berlin.8.0.1.0.html', district ? { category: district } : {}));
      if (mode === 'apartments' || mode === 'both') urls.push(withQuery('https://www.wg-gesucht.de/wohnungen-in-Berlin.8.2.1.1.html', district ? { category: district } : {}));
      return urls;
    }
    case 'wohnungsboerse-de':
      return [withQuery(listingType === 'buy' ? 'https://www.wohnungsboerse.net/Berlin/kaufen/wohnungen' : 'https://www.wohnungsboerse.net/Berlin/mieten/wohnungen', district ? { q: district } : {})];
    case 'local-agency-rss-de': {
      const urls = (source.config.rssUrl ?? '').split(',').map((item) => item.trim()).filter(Boolean);
      return urls;
    }
    default:
      return profile.liveSearchUrls.map((item) => withQuery(item, district ? { q: district } : {}));
  }
}

export function resolveFetchStrategy(source: SourcePayload, env: WorkerEnv): ResolvedFetchStrategy {
  const requested = source.config.fetchStrategy ?? 'auto';
  if (source.adapterId === 'local-agency-rss-de' || requested === 'rss') return 'rss';
  const officialApiConfigured = !!(env.IMMOSCOUT_CONSUMER_KEY?.trim() && env.IMMOSCOUT_CONSUMER_SECRET?.trim() && env.IMMOSCOUT_REGION_GEOCODE?.trim());
  if (source.adapterId === 'immoscout-de' && (requested === 'official_api' || (requested === 'auto' && officialApiConfigured))) return 'official_api';
  return 'public_html';
}

export function buildSourceFetchPlan(source: SourcePayload, env: WorkerEnv): SourceFetchPlan {
  const profile = getSourceProfile(source.adapterId);
  const officialApiConfigured = !!(env.IMMOSCOUT_CONSUMER_KEY?.trim() && env.IMMOSCOUT_CONSUMER_SECRET?.trim() && env.IMMOSCOUT_REGION_GEOCODE?.trim());
  const rssConfigured = !!(source.config.rssUrl?.trim() || env.DEFAULT_LOCAL_RSS_URLS?.trim());
  const publicUrls = buildPublicUrlsForSource(source);
  const strategyResolved = resolveFetchStrategy(source, env);
  const notes: string[] = [];
  if (strategyResolved === 'official_api') notes.push('Using official API flow when credentials are present.');
  if (strategyResolved === 'public_html') notes.push('Using public search/detail pages with heuristic extraction.');
  if (strategyResolved === 'rss') notes.push('Using RSS/Atom feed ingestion.');
  if (source.config.district?.trim()) notes.push('District is applied both in fetch URLs (when possible) and in post-filtering.');
  if (profile?.supportsDistrictFilter === false && source.config.district?.trim()) notes.push('This source does not expose a reliable district filter; only post-filtering will apply.');
  if (profile?.parserFamily) notes.push(`Primary parser family: ${profile.parserFamily}.`);

  return {
    adapterId: source.adapterId,
    strategyRequested: source.config.fetchStrategy ?? 'auto',
    strategyResolved,
    district: source.config.district?.trim() || null,
    districtAliases: districtVariants(source.config.district),
    publicUrls,
    officialApiConfigured,
    rssConfigured,
    maxResults: clampMaxResults(source.config.maxResults),
    notes,
  };
}
