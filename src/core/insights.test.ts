import { buildMarketInsights } from './insights';
import type { NormalizedListing } from '@/types/listing';

const base: NormalizedListing = {
  id: '1',
  canonicalId: 'c1',
  dedupeKey: 'k1',
  sourceId: 'immoscout24',
  sourceListingId: 's1',
  sourceUrl: 'https://example.com/1',
  alternateSourceUrls: [],
  listingType: 'rent',
  propertyType: 'apartment',
  title: 'Test listing',
  description: 'Desc',
  price: 1000,
  priceCurrency: 'EUR',
  priceType: 'per_month',
  pricePerSqm: 20,
  country: 'DE',
  city: 'Berlin',
  district: null,
  street: null,
  houseNumber: null,
  zip: null,
  lat: null,
  lng: null,
  sizeSqm: 50,
  rooms: 2,
  furnished: false,
  petsAllowed: false,
  parking: false,
  balcony: false,
  elevator: false,
  images: [],
  thumbnailUrl: null,
  contactName: null,
  freshnessScore: 80,
  completenessScore: 90,
  trustScore: 75,
  isSuspicious: false,
  suspiciousReasons: [],
  listedAt: new Date().toISOString(),
  fetchedAt: new Date().toISOString(),
  lastSeenAt: new Date().toISOString(),
  isActive: true,
};

const insights = buildMarketInsights([
  base,
  { ...base, id: '2', canonicalId: 'c2', dedupeKey: 'k2', sourceId: 'wg-gesucht', city: 'Berlin', price: 1200 },
  { ...base, id: '3', canonicalId: 'c3', dedupeKey: 'k3', sourceId: 'kleinanzeigen', city: 'Berlin', price: 800, isSuspicious: true, suspiciousReasons: ['low_price'] },
]);

if (insights.totalListings !== 3) throw new Error('Expected total listings to equal 3');
if (insights.suspiciousCount !== 1) throw new Error('Expected suspicious count to equal 1');
if (!insights.cityBreakdown.find((item) => item.city === 'Berlin' && item.count === 3)) throw new Error('Expected Berlin breakdown');
if (insights.medianPrice !== 1000) throw new Error('Expected median price to equal 1000');
