export type ListingType = 'rent' | 'buy';
export type PropertyType = 'apartment' | 'house' | 'room' | 'studio' | 'other';

export interface WorkerEnv {
  WORKER_BEARER_TOKEN?: string;
  DEFAULT_LOCAL_RSS_URLS?: string;
  IMMOSCOUT_CONSUMER_KEY?: string;
  IMMOSCOUT_CONSUMER_SECRET?: string;
  IMMOSCOUT_REGION_GEOCODE?: string;
}

export interface SourcePayload {
  sourceId: string;
  adapterId: string;
  country: string;
  config: Record<string, string>;
}

export interface RawListingInput {
  sourceListingId: string;
  sourceUrl: string;
  title: string;
  description?: string | null;
  city?: string | null;
  district?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  zip?: string | null;
  listingType?: ListingType | null;
  propertyType?: PropertyType | null;
  price?: number | null;
  sizeSqm?: number | null;
  rooms?: number | null;
  lat?: number | null;
  lng?: number | null;
  furnished?: boolean | null;
  balcony?: boolean | null;
  elevator?: boolean | null;
  parking?: boolean | null;
  petsAllowed?: boolean | null;
  listedAt?: string | null;
  images?: string[];
  contactName?: string | null;
}

export interface NormalizedListing {
  id: string;
  canonicalId: string;
  dedupeKey: string;
  sourceId: string;
  sourceListingId: string;
  sourceUrl: string;
  alternateSourceUrls: { sourceId: string; url: string }[];
  listingType: ListingType;
  propertyType: PropertyType;
  title: string;
  description: string | null;
  price: number | null;
  priceCurrency: string;
  priceType: 'per_month' | 'total';
  pricePerSqm: number | null;
  country: string;
  city: string;
  district: string | null;
  street: string | null;
  houseNumber: string | null;
  zip: string | null;
  lat: number | null;
  lng: number | null;
  sizeSqm: number | null;
  rooms: number | null;
  furnished: boolean | null;
  petsAllowed: boolean | null;
  parking: boolean | null;
  balcony: boolean | null;
  elevator: boolean | null;
  images: string[];
  thumbnailUrl: string | null;
  contactName: string | null;
  freshnessScore: number;
  completenessScore: number;
  trustScore: number;
  isSuspicious: boolean;
  suspiciousReasons: string[];
  listedAt: string | null;
  fetchedAt: string;
  lastSeenAt: string;
  isActive: boolean;
}


export interface SourceProbeUrlResult {
  url: string;
  candidateDetailUrls: string[];
  parsedListCount: number;
  jsonLdCount: number;
  snippetCount: number;
  htmlBytes: number;
  detailFetchedCount: number;
  detailKnownCount: number;
  detailJsonLdCount: number;
  detailHeuristicCount: number;
  sampleTitles: string[];
}

export interface SourceProbeResult {
  adapterId: string;
  strategyResolved: 'rss' | 'official_api' | 'public_html';
  urls: SourceProbeUrlResult[];
}
