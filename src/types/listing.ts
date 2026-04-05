export type ListingType = 'rent' | 'buy';
export type PropertyType = 'apartment' | 'house' | 'room' | 'studio' | 'other';

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

export interface Favorite {
  listingId: string;
  savedAt: string;
  note: string | null;
  tags: string[];
}
