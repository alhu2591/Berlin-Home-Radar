import type { ListingType, NormalizedListing, PropertyType, RawListingInput, SourcePayload } from './types';

export function normalizeWhitespace(value: string | null | undefined) {
  return (value ?? '').replace(/\s+/g, ' ').trim();
}

export function decodeHtmlEntities(value: string) {
  return value
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>');
}

export function stripTags(html: string) {
  return decodeHtmlEntities(html.replace(/<script[\s\S]*?<\/script>/gi, ' ').replace(/<style[\s\S]*?<\/style>/gi, ' ').replace(/<[^>]+>/g, ' '));
}

export function unique<T>(items: T[]) {
  return Array.from(new Set(items));
}

export function absoluteUrl(base: string, value: string | null | undefined) {
  if (!value) return '';
  try {
    return new URL(value, base).toString();
  } catch {
    return value;
  }
}

export function extractNumber(value: unknown): number | null {
  if (typeof value === 'number' && Number.isFinite(value)) return value;
  if (typeof value !== 'string') return null;
  const match = value.replace(/\./g, '').replace(',', '.').match(/-?\d+(?:\.\d+)?/);
  return match ? Number(match[0]) : null;
}

export function extractJsonLd(html: string): unknown[] {
  const matches = [...html.matchAll(/<script[^>]*type=["']application\/ld\+json["'][^>]*>([\s\S]*?)<\/script>/gi)];
  const values: unknown[] = [];
  for (const match of matches) {
    const raw = match[1]?.trim();
    if (!raw) continue;
    try {
      values.push(JSON.parse(raw));
    } catch {
      try {
        values.push(JSON.parse(raw.replace(/\u0000/g, '')));
      } catch {
        // ignore malformed blocks
      }
    }
  }
  return values;
}

export function flattenJson(value: unknown): Record<string, unknown>[] {
  const results: Record<string, unknown>[] = [];
  const walk = (node: unknown) => {
    if (Array.isArray(node)) {
      node.forEach(walk);
      return;
    }
    if (!node || typeof node !== 'object') return;
    const record = node as Record<string, unknown>;
    results.push(record);
    Object.values(record).forEach(walk);
  };
  walk(value);
  return results;
}

function getAddressParts(address: unknown) {
  const record = (address && typeof address === 'object' ? address : {}) as Record<string, unknown>;
  const locality = normalizeWhitespace(String(record.addressLocality ?? record.addressRegion ?? '')) || 'Berlin';
  const streetAddress = normalizeWhitespace(String(record.streetAddress ?? ''));
  const postal = normalizeWhitespace(String(record.postalCode ?? '')) || null;
  const district = locality.toLowerCase() === 'berlin' ? null : locality.replace(/^Berlin\s+/i, '');
  const streetMatch = streetAddress.match(/^(.+?)\s+(\d+[A-Za-z-]*)$/);
  return {
    city: locality.includes('Berlin') ? 'Berlin' : locality || 'Berlin',
    district,
    street: streetMatch ? streetMatch[1] : streetAddress || null,
    houseNumber: streetMatch ? streetMatch[2] : null,
    zip: postal,
  };
}

function inferPropertyType(value: string, fallback?: PropertyType | null): PropertyType {
  const input = value.toLowerCase();
  if (input.includes('wg') || input.includes('room') || input.includes('zimmer')) return 'room';
  if (input.includes('studio')) return 'studio';
  if (input.includes('house') || input.includes('haus') || input.includes('townhouse')) return 'house';
  if (input.includes('apartment') || input.includes('wohnung') || input.includes('flat') || input.includes('penthouse') || input.includes('loft')) return 'apartment';
  return fallback ?? 'other';
}

function inferListingType(source: SourcePayload, value?: string | null): ListingType {
  const hint = `${source.config.listingType ?? ''} ${value ?? ''}`.toLowerCase();
  if (hint.includes('kauf') || hint.includes('buy')) return 'buy';
  return 'rent';
}

function simpleHash(input: string): string {
  let hash = 0;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return `h${Math.abs(hash)}`;
}

export function toNormalizedListing(source: SourcePayload, input: RawListingInput, fetchedAt = new Date().toISOString()): NormalizedListing {
  const city = normalizeWhitespace(input.city ?? source.config.city ?? 'Berlin') || 'Berlin';
  const district = normalizeWhitespace(input.district ?? source.config.district ?? '') || null;
  const street = normalizeWhitespace(input.street ?? '');
  const dedupeKey = `${city.toLowerCase()}|${district?.toLowerCase() ?? street.toLowerCase()}|${Math.round((input.price ?? 0) / 10) * 10}`;
  const canonicalId = simpleHash(dedupeKey);
  const id = simpleHash(`${source.adapterId}:${input.sourceListingId}`);
  const title = normalizeWhitespace(input.title) || 'Untitled listing';
  const description = normalizeWhitespace(input.description ?? '') || null;
  const images = unique((input.images ?? []).filter(Boolean));
  const listingType = inferListingType(source, input.listingType ?? null);
  const propertyType = input.propertyType ?? inferPropertyType(`${title} ${description ?? ''}`);
  const completenessBase = [title, input.price, city, input.sizeSqm, input.rooms, images[0], input.lat, input.lng].filter((value) => value !== null && value !== undefined && value !== '').length;
  const completenessScore = Math.round((completenessBase / 8) * 100);
  const freshnessScore = input.listedAt ? Math.max(20, 100 - Math.floor((Date.now() - new Date(input.listedAt).getTime()) / 86400000) * 8) : 50;
  const suspiciousReasons: string[] = [];
  if (!images.length) suspiciousReasons.push('No images attached');
  if (!street && !district) suspiciousReasons.push('Weak location details');
  if ((input.price ?? 0) > 0 && (input.price ?? 0) < 500 && (input.sizeSqm ?? 0) > 40) suspiciousReasons.push('Price looks unusually low');
  const trustScore = Math.min(100, Math.round(completenessScore * 0.55 + freshnessScore * 0.35 + (images.length > 0 ? 10 : 0)));
  return {
    id,
    canonicalId,
    dedupeKey,
    sourceId: source.adapterId,
    sourceListingId: input.sourceListingId,
    sourceUrl: input.sourceUrl,
    alternateSourceUrls: [],
    listingType,
    propertyType,
    title,
    description,
    price: input.price ?? null,
    priceCurrency: 'EUR',
    priceType: listingType === 'rent' ? 'per_month' : 'total',
    pricePerSqm: input.price && input.sizeSqm ? Math.round(input.price / input.sizeSqm) : null,
    country: source.country || 'DE',
    city,
    district,
    street: street || null,
    houseNumber: normalizeWhitespace(input.houseNumber ?? '') || null,
    zip: normalizeWhitespace(input.zip ?? '') || null,
    lat: input.lat ?? null,
    lng: input.lng ?? null,
    sizeSqm: input.sizeSqm ?? null,
    rooms: input.rooms ?? null,
    furnished: input.furnished ?? null,
    petsAllowed: input.petsAllowed ?? null,
    parking: input.parking ?? null,
    balcony: input.balcony ?? null,
    elevator: input.elevator ?? null,
    images,
    thumbnailUrl: images[0] ?? null,
    contactName: normalizeWhitespace(input.contactName ?? '') || null,
    freshnessScore,
    completenessScore,
    trustScore,
    isSuspicious: suspiciousReasons.length > 0,
    suspiciousReasons,
    listedAt: input.listedAt ?? null,
    fetchedAt,
    lastSeenAt: fetchedAt,
    isActive: true,
  };
}

export function listingsFromJsonLd(source: SourcePayload, html: string, baseUrl: string, fetchedAt = new Date().toISOString()): NormalizedListing[] {
  const roots = extractJsonLd(html);
  const candidates: Record<string, unknown>[] = [];
  for (const root of roots) {
    const flat = flattenJson(root);
    for (const record of flat) {
      const typeValue = String(record['@type'] ?? '');
      const looksLikeListing = 'url' in record && ('name' in record || 'headline' in record || 'offers' in record || 'floorSize' in record || typeValue);
      if (looksLikeListing) candidates.push(record);
    }
  }

  const listings = candidates.map((record) => {
    const offers = (record.offers && typeof record.offers === 'object' ? record.offers : {}) as Record<string, unknown>;
    const floorSize = (record.floorSize && typeof record.floorSize === 'object' ? record.floorSize : {}) as Record<string, unknown>;
    const geo = (record.geo && typeof record.geo === 'object' ? record.geo : {}) as Record<string, unknown>;
    const addressParts = getAddressParts(record.address ?? offers.address ?? null);
    const images = Array.isArray(record.image) ? record.image.map((item) => String(item)) : record.image ? [String(record.image)] : [];
    const url = absoluteUrl(baseUrl, String(record.url ?? record['@id'] ?? ''));
    const title = String(record.name ?? record.headline ?? record.description ?? '');
    return toNormalizedListing(source, {
      sourceListingId: String(((record.identifier ?? record.sku ?? record['@id'] ?? url) || title)),
      sourceUrl: url || baseUrl,
      title,
      description: String(record.description ?? record.alternateName ?? ''),
      city: addressParts.city,
      district: addressParts.district,
      street: addressParts.street,
      houseNumber: addressParts.houseNumber,
      zip: addressParts.zip,
      listingType: inferListingType(source, String(record['@type'] ?? '')),
      propertyType: inferPropertyType(`${String(record['@type'] ?? '')} ${title}`),
      price: extractNumber(offers.price ?? offers.lowPrice ?? record.price),
      sizeSqm: extractNumber(floorSize.value ?? record.floorSize ?? record.areaServed),
      rooms: extractNumber(record.numberOfRooms ?? record.numberOfRoomsTotal ?? record.roomCount),
      lat: extractNumber(geo.latitude),
      lng: extractNumber(geo.longitude),
      listedAt: normalizeWhitespace(String(record.datePosted ?? record.dateCreated ?? record.validFrom ?? '')) || null,
      images,
      contactName: normalizeWhitespace(String(((record.seller as Record<string, unknown> | undefined)?.name ?? (offers.seller as Record<string, unknown> | undefined)?.name ?? ''))) || null,
    }, fetchedAt);
  });

  return dedupeByUrl(listings).filter((item) => !!item.title && !!item.sourceUrl);
}

export function dedupeByUrl(listings: NormalizedListing[]) {
  const seen = new Set<string>();
  return listings.filter((item) => {
    const key = `${item.sourceId}:${item.sourceUrl}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

export function extractListingUrls(html: string, baseUrl: string, patterns: RegExp[]) {
  const urls: string[] = [];
  for (const pattern of patterns) {
    for (const match of html.matchAll(pattern)) {
      const value = match[1] || match[0];
      urls.push(absoluteUrl(baseUrl, value));
    }
  }
  return unique(urls);
}

export function listingsFromSnippetText(source: SourcePayload, html: string, baseUrl: string, fetchedAt = new Date().toISOString()): NormalizedListing[] {
  const text = stripTags(html).replace(/\r/g, '\n');
  const pattern = /([^\n]{12,120})\n\s*([\d\.,]+)\s*€[^\n]*\n\s*([\d\.,]+)\s*m²(?:[^\n]*\n)?\s*([\d\.,]+)\s*(?:Zi\.|Zimmer)(?:[^\n]*\n)?\s*([^\n]*Berlin[^\n]*)/g;
  const urls = extractListingUrls(html, baseUrl, [
    /href=["']([^"']*\/(?:expose|s-anzeige|wohnung-mieten|haus-mieten|[0-9]{6,}\.html)[^"']*)["']/gi,
    /https:\/\/www\.wg-gesucht\.de\/\d+\.html/gi,
  ]);
  const listings: NormalizedListing[] = [];
  let index = 0;
  for (const match of text.matchAll(pattern)) {
    const title = normalizeWhitespace(match[1]);
    const price = extractNumber(match[2]);
    const sizeSqm = extractNumber(match[3]);
    const rooms = extractNumber(match[4]);
    const location = normalizeWhitespace(match[5]);
    const parts = location.split('|').map((item) => item.trim());
    const districtMatch = location.match(/Berlin\s+([A-Za-zÄÖÜäöüß\-]+)/);
    listings.push(toNormalizedListing(source, {
      sourceListingId: `${source.adapterId}-snippet-${index + 1}`,
      sourceUrl: urls[index] ?? baseUrl,
      title,
      city: 'Berlin',
      district: districtMatch?.[1] ?? source.config.district ?? null,
      price,
      sizeSqm,
      rooms,
      listingType: inferListingType(source),
      propertyType: inferPropertyType(title),
      description: parts.join(' · '),
    }, fetchedAt));
    index += 1;
  }
  return dedupeByUrl(listings);
}

function extractMetaContent(html: string, names: string[]) {
  for (const name of names) {
    const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const patterns = [
      new RegExp(`<meta[^>]+property=["']${escaped}["'][^>]+content=["']([^"']+)["'][^>]*>`, 'i'),
      new RegExp(`<meta[^>]+name=["']${escaped}["'][^>]+content=["']([^"']+)["'][^>]*>`, 'i'),
      new RegExp(`<meta[^>]+content=["']([^"']+)["'][^>]+property=["']${escaped}["'][^>]*>`, 'i'),
      new RegExp(`<meta[^>]+content=["']([^"']+)["'][^>]+name=["']${escaped}["'][^>]*>`, 'i'),
    ];
    for (const pattern of patterns) {
      const match = html.match(pattern);
      if (match?.[1]) return decodeHtmlEntities(match[1]);
    }
  }
  return '';
}

function extractTagText(html: string, tag: string) {
  const match = html.match(new RegExp(`<${tag}[^>]*>([\\s\\S]*?)<\/${tag}>`, 'i'));
  return normalizeWhitespace(stripTags(match?.[1] ?? ''));
}

function firstBerlinAddress(text: string) {
  const zipMatch = text.match(/(10\d{3}|11\d{3}|12\d{3}|13\d{3}|14\d{3})\s+Berlin(?:\s+([A-Za-zÄÖÜäöüß\-\/]+))?/i);
  if (zipMatch) return { zip: zipMatch[1], district: normalizeWhitespace(zipMatch[2] ?? '') || null };
  const districtMatch = text.match(/Berlin\s+([A-Za-zÄÖÜäöüß\-\/]+)/i);
  return { zip: null, district: normalizeWhitespace(districtMatch?.[1] ?? '') || null };
}

export function listingFromDetailHeuristics(source: SourcePayload, html: string, url: string, fetchedAt = new Date().toISOString()): NormalizedListing | null {
  const title = extractMetaContent(html, ['og:title', 'twitter:title']) || extractTagText(html, 'h1') || extractTagText(html, 'title');
  if (!title) return null;
  const description = extractMetaContent(html, ['description', 'og:description', 'twitter:description']) || '';
  const text = normalizeWhitespace(stripTags(html));
  const image = extractMetaContent(html, ['og:image', 'twitter:image']);
  const price = extractNumber(text.match(/(?:warmmiete|kaltmiete|miete|preis|rent|monthly)[^\d]{0,25}([\d\.,]+)\s*€/i)?.[1] ?? text.match(/([\d\.,]+)\s*€/i)?.[1] ?? null);
  const sizeSqm = extractNumber(text.match(/([\d\.,]+)\s*m²/i)?.[1] ?? null);
  const rooms = extractNumber(text.match(/([\d\.,]+)\s*(?:zi\.|zimmer|rooms?)/i)?.[1] ?? null);
  const address = firstBerlinAddress(text);
  const sourceListingId = absoluteUrl(url, url).split('#')[0];
  return toNormalizedListing(source, {
    sourceListingId,
    sourceUrl: url,
    title,
    description,
    city: 'Berlin',
    district: address.district ?? source.config.district ?? null,
    zip: address.zip,
    listingType: inferListingType(source),
    propertyType: inferPropertyType(`${title} ${description}`),
    price,
    sizeSqm,
    rooms,
    images: image ? [image] : [],
  }, fetchedAt);
}

export function takeLimit<T>(items: T[], maxResults: string | undefined) {
  const limit = Math.max(1, Math.min(Number(maxResults ?? '20') || 20, 60));
  return items.slice(0, limit);
}

export function parseRss(xml: string) {
  const items = [...xml.matchAll(/<item>([\s\S]*?)<\/item>/gi)].map((match) => match[1]);
  const entries = [...xml.matchAll(/<entry>([\s\S]*?)<\/entry>/gi)].map((match) => match[1]);
  const blocks = items.length > 0 ? items : entries;
  return blocks.map((block) => {
    const get = (tag: string) => {
      const match = block.match(new RegExp(`<${tag}[^>]*>([\\s\\S]*?)<\/${tag}>`, 'i'));
      return normalizeWhitespace(stripTags(match?.[1] ?? '')) || null;
    };
    const linkMatch = block.match(/<link[^>]*href=["']([^"']+)["'][^>]*\/?>(?:<\/link>)?/i) ?? block.match(/<link>([^<]+)<\/link>/i);
    return {
      title: get('title'),
      description: get('description') ?? get('summary') ?? get('content'),
      url: normalizeWhitespace(linkMatch?.[1] ?? '') || null,
      publishedAt: get('pubDate') ?? get('updated') ?? get('published'),
    };
  });
}
