import type { NormalizedListing, PropertyType, RawListingInput, SourcePayload } from './types';
import { getSourceProfile } from './profiles';
import { absoluteUrl, extractListingUrls, extractNumber, normalizeWhitespace, stripTags, toNormalizedListing, unique } from './utils';

const COMMON_STOP_LABELS = [
  'Warmmiete', 'Kaltmiete', 'Gesamtmiete', 'Grundmiete', 'Nebenkosten', 'Heizkosten', 'Kaution',
  'Wohnfläche', 'Fläche in m²', 'Fläche', 'Zimmer', 'Anzahl Zimmer', 'Bezugsfrei', 'Frei ab',
  'Adresse', 'Anschrift', 'Bezirk/Ortsteil', 'Objektdetails', 'Mietkosten', 'Mietpreis', 'Merkmale',
  'Objektbeschreibung', 'Beschreibung', 'Wi-Nr.', 'Objektnummer', 'Etage', 'Etagenzahl', 'Grundriss',
  'Energiekennzahlen', 'Weitere Informationen zur Immobilie', 'Allgemeine Angebotsdaten', 'Mietdauer',
  'Kategorien', 'Mietpreis'
];

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function normalizedText(html: string) {
  return normalizeWhitespace(stripTags(html).replace(/\r/g, ' '));
}

function extractBetween(text: string, startLabels: string[], stopLabels = COMMON_STOP_LABELS) {
  const startPattern = startLabels.map(escapeRegExp).join('|');
  const stopPattern = stopLabels.map(escapeRegExp).join('|');
  const regex = new RegExp(`(?:${startPattern})\\s*(?:[:|]\\s*)?(.+?)(?=\\s+(?:${stopPattern})\\b|$)`, 'i');
  const match = text.match(regex);
  return normalizeWhitespace(match?.[1] ?? '');
}

function parseAddressParts(text: string) {
  const address = normalizeWhitespace(text.replace(/^Adresse[:|]?/i, '').replace(/^Anschrift[:|]?/i, ''));
  const zipMatch = address.match(/(10\d{3}|11\d{3}|12\d{3}|13\d{3}|14\d{3})\s+Berlin(?:,?\s*([^,]+))?/i);
  const beforeZip = zipMatch ? normalizeWhitespace(address.slice(0, zipMatch.index).replace(/,$/, '')) : address;
  const streetMatch = beforeZip.match(/^(.+?)\s+(\d+[A-Za-z\-\/]*)$/);
  return {
    street: streetMatch ? streetMatch[1] : beforeZip || null,
    houseNumber: streetMatch ? streetMatch[2] : null,
    zip: zipMatch?.[1] ?? null,
    district: normalizeWhitespace(zipMatch?.[2] ?? '') || null,
  };
}

function boolFromKeywords(text: string, positive: string[]) {
  const haystack = text.toLowerCase();
  return positive.some((item) => haystack.includes(item.toLowerCase()));
}

function inferPropertyType(source: SourcePayload, text: string): PropertyType {
  const input = `${source.adapterId} ${text}`.toLowerCase();
  if (input.includes('wg') || input.includes('zimmer') || input.includes('room')) return 'room';
  if (input.includes('studio')) return 'studio';
  if (input.includes('haus') || input.includes('house')) return 'house';
  return 'apartment';
}

function descriptionBits(text: string) {
  const bits: string[] = [];
  if (boolFromKeywords(text, ['WBS erforderlich', 'WBS'])) bits.push('WBS required');
  if (boolFromKeywords(text, ['möbliert', 'furnished'])) bits.push('Furnished');
  if (boolFromKeywords(text, ['student', 'studierende', 'studenten'])) bits.push('Student housing');
  return bits.length > 0 ? bits.join(' · ') : null;
}

function buildDetailInput(source: SourcePayload, url: string, title: string, text: string, extra: Partial<RawListingInput> = {}): RawListingInput {
  const addressLine = extractBetween(text, ['Adresse', 'Anschrift']);
  const districtLine = extractBetween(text, ['Bezirk/Ortsteil']);
  const address = parseAddressParts(addressLine || districtLine);
  const district = address.district ?? (normalizeWhitespace((districtLine.split('/').pop() ?? '').trim()) || source.config.district || null);
  const price = extractNumber(extractBetween(text, ['Warmmiete', 'Gesamtmiete', 'Kaltmiete', 'Grundmiete']));
  const sizeSqm = extractNumber(extractBetween(text, ['Wohnfläche', 'Fläche in m²', 'Fläche']));
  const rooms = extractNumber(extractBetween(text, ['Anzahl Zimmer', 'Zimmer']));
  const listedAt = normalizeWhitespace(extractBetween(text, ['Bezugsfrei', 'Frei ab'])) || null;
  const featureBlock = extractBetween(text, ['Merkmale'], ['Objektdetails', 'Mietkosten', 'Energiekennzahlen', 'Objektbeschreibung', 'Grundriss']);
  return {
    sourceListingId: extra.sourceListingId ?? url,
    sourceUrl: absoluteUrl(url, url),
    title,
    description: extra.description ?? (extractBetween(text, ['Objektbeschreibung', 'Beschreibung']) || descriptionBits(text)),
    city: 'Berlin',
    district,
    street: address.street,
    houseNumber: address.houseNumber,
    zip: address.zip,
    listingType: source.config.listingType === 'buy' ? 'buy' : 'rent',
    propertyType: extra.propertyType ?? inferPropertyType(source, title),
    price,
    sizeSqm,
    rooms,
    furnished: extra.furnished ?? (featureBlock ? boolFromKeywords(featureBlock, ['möbliert', 'furnished']) : null),
    balcony: extra.balcony ?? (featureBlock ? boolFromKeywords(featureBlock, ['balkon', 'loggia', 'terrasse']) : null),
    elevator: extra.elevator ?? (featureBlock ? boolFromKeywords(featureBlock, ['aufzug', 'fahrstuhl', 'elevator']) : null),
    parking: extra.parking ?? (featureBlock ? boolFromKeywords(featureBlock, ['stellplatz', 'parking', 'garage']) : null),
    petsAllowed: extra.petsAllowed ?? (featureBlock ? boolFromKeywords(featureBlock, ['haustiere erlaubt', 'pets allowed']) : null),
    listedAt,
  };
}

function parseHowoge(source: SourcePayload, html: string, url: string, fetchedAt: string) {
  const text = normalizedText(html);
  const title = normalizeWhitespace(html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i)?.[1] ?? text.match(/(.+?)\s+Adresse:/i)?.[1] ?? '');
  if (!title) return null;
  return toNormalizedListing(source, buildDetailInput(source, url, title, text, {
    description: descriptionBits(text),
  }), fetchedAt);
}

function parseGewobagLike(source: SourcePayload, html: string, url: string, fetchedAt: string) {
  const text = normalizedText(html);
  const title = normalizeWhitespace(html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i)?.[1] ?? text.match(/(?:Detailansicht\s*)?(.+?)\s+Mietpreis/i)?.[1] ?? text.match(/(.+?)\s+Grundriss/i)?.[1] ?? '');
  const safeTitle = title && !/^Mietangebote/i.test(title) ? title : normalizeWhitespace(text.match(/([^.]{10,140})\s+Mietpreis/i)?.[1] ?? '');
  if (!safeTitle) return null;
  return toNormalizedListing(source, buildDetailInput(source, url, safeTitle, text, {
    description: descriptionBits(text),
  }), fetchedAt);
}

function parseStadtUndLand(source: SourcePayload, html: string, url: string, fetchedAt: string) {
  const text = normalizedText(html);
  const title = normalizeWhitespace(text.match(/Weitere Informationen zur Immobilie[:|]?\s*([^\.]+(?:\.[^\s]+)?)/i)?.[1] ?? text.match(/Verfügbare Wohnungen\s+([^\.]+(?:\.[^\s]+)?)/i)?.[1] ?? html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i)?.[1] ?? '');
  if (!title) return null;
  return toNormalizedListing(source, buildDetailInput(source, url, title, text, {
    description: descriptionBits(text),
  }), fetchedAt);
}

function districtFromStudentText(text: string) {
  if (/schlachtensee/i.test(text)) return 'Schlachtensee';
  if (/adlershof/i.test(text)) return 'Adlershof';
  if (/charlottenburg/i.test(text)) return 'Charlottenburg';
  return null;
}

function extractListBlocksByLinks(html: string, baseUrl: string, detailPatterns: RegExp[]) {
  const matches: Array<{ url: string; index: number }> = [];
  for (const pattern of detailPatterns) {
    for (const match of html.matchAll(pattern)) {
      const href = match[1] || match[0];
      const url = absoluteUrl(baseUrl, href);
      const index = typeof match.index === 'number' ? match.index : html.indexOf(match[0]);
      if (index >= 0 && !matches.some((item) => item.url === url && item.index === index)) {
        matches.push({ url, index });
      }
    }
  }
  matches.sort((a, b) => a.index - b.index);
  return matches.map((match, idx) => {
    const start = Math.max(0, match.index - 60);
    const nextIndex = matches[idx + 1]?.index ?? Math.min(html.length, match.index + 2400);
    const end = Math.min(html.length, Math.max(nextIndex, match.index + 420));
    return { url: match.url, html: html.slice(start, end) };
  });
}

function metricNumber(blockText: string, label: string) {
  const valueFirst = blockText.match(new RegExp(`([\\d\\.,]+)\\s*(?:€|m²|qm)?\\s*${escapeRegExp(label)}`, 'i'));
  if (valueFirst?.[1]) return extractNumber(valueFirst[1]);
  const labelFirst = blockText.match(new RegExp(`${escapeRegExp(label)}\\s*[:|]?\\s*([\\d\\.,]+)`, 'i'));
  return extractNumber(labelFirst?.[1] ?? null);
}

function parseListAddress(blockText: string) {
  const lines = blockText
    .split(/(?:(?<=Berlin)|\n|\r)/)
    .map((item) => normalizeWhitespace(item))
    .filter(Boolean);
  const addressLine = lines.find((line) => /(10\d{3}|11\d{3}|12\d{3}|13\d{3}|14\d{3})\s+Berlin/i.test(line))
    ?? lines.find((line) => /Berlin/i.test(line) && /\d/.test(line))
    ?? '';
  return parseAddressParts(addressLine);
}

function extractListTitle(blockHtml: string, blockText: string) {
  const heading = normalizeWhitespace(blockHtml.match(/<(?:h1|h2|h3|a)[^>]*>([^<]{6,160})<\/(?:h1|h2|h3|a)>/i)?.[1] ?? '');
  if (heading && !/^(ansehen|zum expos[eé]|merken|teilen|mehr details|jetzt bewerben)$/i.test(heading)) return heading;
  const textTitle = normalizeWhitespace(blockText.match(/([A-ZÄÖÜ0-9][^\n]{8,160}?)(?=\s+(?:Warmmiete|Kaltmiete|Zimmer|Wohnfläche|Größe|Adresse|Ansehen|Zum Exposé|Ab|From))/i)?.[1] ?? '');
  return textTitle;
}

function listInputFromBlock(source: SourcePayload, blockHtml: string, blockUrl: string): RawListingInput | null {
  const blockText = normalizedText(blockHtml);
  const title = extractListTitle(blockHtml, blockText);
  if (!title) return null;
  const address = parseListAddress(blockText);
  const districtHint = source.config.district ?? null;
  const featureBlock = blockText;
  const listedAtMatch = blockText.match(/(?:Bezugsfrei|Frei ab)\s*[:|]?\s*([^\s].{0,32})/i);
  return {
    sourceListingId: blockUrl,
    sourceUrl: blockUrl,
    title,
    description: descriptionBits(blockText),
    city: 'Berlin',
    district: address.district ?? districtHint,
    street: address.street,
    houseNumber: address.houseNumber,
    zip: address.zip,
    listingType: source.config.listingType === 'buy' ? 'buy' : 'rent',
    propertyType: inferPropertyType(source, title),
    price: metricNumber(blockText, 'Warmmiete') ?? metricNumber(blockText, 'Kaltmiete') ?? metricNumber(blockText, 'Gesamtmiete') ?? metricNumber(blockText, 'Grundmiete'),
    sizeSqm: metricNumber(blockText, 'Wohnfläche') ?? metricNumber(blockText, 'Fläche') ?? metricNumber(blockText, 'Größe'),
    rooms: metricNumber(blockText, 'Zimmer') ?? metricNumber(blockText, 'Anzahl Zimmer'),
    listedAt: normalizeWhitespace(listedAtMatch?.[1] ?? '') || null,
    balcony: boolFromKeywords(featureBlock, ['balkon', 'loggia', 'terrasse']),
    elevator: boolFromKeywords(featureBlock, ['aufzug', 'fahrstuhl', 'elevator']),
    parking: boolFromKeywords(featureBlock, ['stellplatz', 'garage', 'tiefgarage']),
    petsAllowed: boolFromKeywords(featureBlock, ['haustiere erlaubt', 'pets allowed']),
    furnished: boolFromKeywords(featureBlock, ['möbliert', 'furnished']),
  };
}

function studentRoomInput(source: SourcePayload, blockHtml: string, blockUrl: string): RawListingInput | null {
  const blockText = normalizedText(blockHtml);
  const title = extractListTitle(blockHtml, blockText);
  if (!title) return null;
  const descriptionParts: string[] = [];
  if (boolFromKeywords(blockText, ['möbliert', 'furnished'])) descriptionParts.push('Furnished');
  const roommateCount = blockText.match(/(\d+\s*(?:-|–|to)?\s*\d*\s*(?:Mitbewohner|roommates|Bewohner))/i)?.[1];
  if (roommateCount) descriptionParts.push(normalizeWhitespace(roommateCount));
  const privateBath = boolFromKeywords(blockText, ['eigenes bad', 'private bathroom']);
  if (privateBath) descriptionParts.push('Private bathroom');
  return {
    sourceListingId: blockUrl,
    sourceUrl: blockUrl,
    title,
    description: descriptionParts.join(' · ') || descriptionBits(blockText),
    city: 'Berlin',
    district: districtFromStudentText(blockText) ?? source.config.district ?? null,
    listingType: 'rent',
    propertyType: 'room',
    price: extractNumber(blockText.match(/(?:ab|from)\s*([\d\.,]+)\s*€/i)?.[1] ?? blockText.match(/([\d\.,]+)\s*€/i)?.[1] ?? null),
    sizeSqm: extractNumber(blockText.match(/(?:ca\.?\s*)?([\d\.,]+)\s*(?:m²|qm)/i)?.[1] ?? null),
    rooms: 1,
    furnished: boolFromKeywords(blockText, ['möbliert', 'furnished']) || true,
  };
}

function parseStudentendorfDetail(source: SourcePayload, html: string, url: string, fetchedAt: string) {
  const text = normalizedText(html);
  const title = normalizeWhitespace(html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i)?.[1] ?? text.match(/WG-Zimmer\s+([^\.]+)/i)?.[1] ?? '');
  if (!title) return null;
  return toNormalizedListing(source, buildDetailInput(source, url, title, text, {
    propertyType: 'room',
    furnished: true,
    district: districtFromStudentText(text) ?? source.config.district ?? null,
    price: extractNumber(text.match(/(?:mind\.\s*11\s*Monate|mind\.\s*6\s*Monate|Mietpreis)\s*([\d\.,]+)\s*€/i)?.[1] ?? text.match(/([\d\.,]+)\s*€/i)?.[1] ?? null),
    sizeSqm: extractNumber(text.match(/(?:ca\.?\s*)?([\d\.,]+)\s*(?:m²|qm)/i)?.[1] ?? null),
    rooms: 1,
    description: descriptionBits(text) ?? 'Student housing',
  }), fetchedAt);
}

function compileProfileDetailPatterns(source: SourcePayload) {
  const profile = getSourceProfile(source.adapterId);
  if (!profile) return [];
  return profile.detailPatterns.map((pattern) => new RegExp(`href=["']([^"']*${pattern}[^"']*)["']`, 'gi'));
}

function listDetailPatternsFor(source: SourcePayload) {
  switch (source.adapterId) {
    case 'wbm-de':
      return [/href=["']([^"']*\/wohnungen-berlin\/angebote\/details\/[^"']+)["']/gi];
    case 'howoge-de':
      return [/href=["']([^"']*\/wohnungssuche\/detail\/[^"']+)["']/gi];
    case 'gewobag-de':
    case 'degewo-de':
    case 'gesobau-de':
    case 'stadtundland-de':
    case 'inberlinwohnen-de':
      return compileProfileDetailPatterns(source);
    case 'studentendorf-berlin-de':
      return [/href=["']([^"']*\/(?:de\/)?wg-zimmer\/[^"']+)["']/gi, /href=["']([^"']*\/(?:de\/)?wohnen\/[^"']+)["']/gi];
    default:
      return compileProfileDetailPatterns(source);
  }
}

function parseGenericListBlocks(source: SourcePayload, html: string, url: string, fetchedAt: string) {
  const patterns = listDetailPatternsFor(source);
  if (!patterns.length) return [];
  const blocks = extractListBlocksByLinks(html, url, patterns);
  return unique(blocks.map((block) => {
    const input = listInputFromBlock(source, block.html, block.url);
    return input ? JSON.stringify(input) : null;
  }).filter(Boolean) as string[]).map((raw) => toNormalizedListing(source, JSON.parse(raw) as RawListingInput, fetchedAt));
}

function parseStudentendorfList(source: SourcePayload, html: string, url: string, fetchedAt: string) {
  const patterns = listDetailPatternsFor(source);
  const blocks = extractListBlocksByLinks(html, url, patterns);
  return unique(blocks.map((block) => {
    const input = studentRoomInput(source, block.html, block.url);
    return input ? JSON.stringify(input) : null;
  }).filter(Boolean) as string[]).map((raw) => toNormalizedListing(source, JSON.parse(raw) as RawListingInput, fetchedAt));
}

export function listingsFromKnownListPage(source: SourcePayload, html: string, url: string, fetchedAt = new Date().toISOString()): NormalizedListing[] {
  switch (source.adapterId) {
    case 'wbm-de':
    case 'howoge-de':
    case 'gewobag-de':
    case 'degewo-de':
    case 'gesobau-de':
    case 'stadtundland-de':
    case 'inberlinwohnen-de':
      return parseGenericListBlocks(source, html, url, fetchedAt);
    case 'studentendorf-berlin-de':
      return parseStudentendorfList(source, html, url, fetchedAt);
    default:
      return [];
  }
}

export function previewKnownListPageLinks(source: SourcePayload, html: string, url: string) {
  const specificPatterns = listDetailPatternsFor(source);
  const genericPatterns = specificPatterns.length ? specificPatterns : [
    /href=["']([^"']*(?:expose|wohnungssuche|wohnungsfinder|angebote\/details|mietangebote|immosuche|s-anzeige|student-rooms|wg-zimmer|\/\d+\.html)[^"']*)["']/gi,
  ];
  return extractListingUrls(html, url, genericPatterns).slice(0, 12);
}

export function listingFromKnownDetailPage(source: SourcePayload, html: string, url: string, fetchedAt = new Date().toISOString()): NormalizedListing | null {
  switch (source.adapterId) {
    case 'howoge-de':
      return parseHowoge(source, html, url, fetchedAt);
    case 'gewobag-de':
    case 'degewo-de':
    case 'gesobau-de':
    case 'wbm-de':
    case 'inberlinwohnen-de':
      return parseGewobagLike(source, html, url, fetchedAt);
    case 'stadtundland-de':
      return parseStadtUndLand(source, html, url, fetchedAt);
    case 'studentendorf-berlin-de':
      return parseStudentendorfDetail(source, html, url, fetchedAt);
    default:
      return null;
  }
}
