import { applyFilters } from '@/core/search';
import type { FilterState } from '@/types/filter';
import type { NormalizedListing } from '@/types/listing';

const baseFilter: FilterState = {
  query: '',
  listingType: null,
  propertyTypes: [],
  priceMin: null,
  priceMax: null,
  roomsMin: null,
  city: null,
  furnished: null,
  petsAllowed: null,
  parking: null,
  balcony: null,
  sourceIds: [],
  sortBy: 'relevance',
  showDismissed: false,
};

const listings: NormalizedListing[] = [
  {
    id: '1', canonicalId: 'c1', dedupeKey: 'c1', sourceId: 'immoscout-de', sourceListingId: '1', sourceUrl: 'https://example.com/1', alternateSourceUrls: [], listingType: 'rent', propertyType: 'apartment', title: 'Bright Berlin apartment', description: 'Furnished place', price: 1500, priceCurrency: 'EUR', priceType: 'per_month', pricePerSqm: 25, country: 'DE', city: 'Berlin', district: 'Mitte', street: 'Tor', houseNumber: '1', zip: '10119', lat: 0, lng: 0, sizeSqm: 60, rooms: 2, furnished: true, petsAllowed: false, parking: false, balcony: true, elevator: true, images: [], thumbnailUrl: null, contactName: null, freshnessScore: 90, completenessScore: 90, trustScore: 90, isSuspicious: false, suspiciousReasons: [], listedAt: '2026-04-04T09:00:00.000Z', fetchedAt: '2026-04-05T09:00:00.000Z', lastSeenAt: '2026-04-05T09:00:00.000Z', isActive: true,
  },
  {
    id: '2', canonicalId: 'c2', dedupeKey: 'c2', sourceId: 'wg-gesucht-de', sourceListingId: '2', sourceUrl: 'https://example.com/2', alternateSourceUrls: [], listingType: 'rent', propertyType: 'room', title: 'WG room', description: 'Student room', price: 900, priceCurrency: 'EUR', priceType: 'per_month', pricePerSqm: 30, country: 'DE', city: 'Berlin', district: 'Neukolln', street: 'Pannier', houseNumber: '4', zip: '12047', lat: 0, lng: 0, sizeSqm: 30, rooms: 1, furnished: true, petsAllowed: null, parking: false, balcony: false, elevator: false, images: [], thumbnailUrl: null, contactName: null, freshnessScore: 80, completenessScore: 85, trustScore: 75, isSuspicious: false, suspiciousReasons: [], listedAt: '2026-04-03T09:00:00.000Z', fetchedAt: '2026-04-05T09:00:00.000Z', lastSeenAt: '2026-04-05T09:00:00.000Z', isActive: true,
  }
];

test('filters by property type and source', () => {
  const result = applyFilters(listings, { ...baseFilter, propertyTypes: ['apartment'], sourceIds: ['immoscout-de'] });
  expect(result).toHaveLength(1);
  expect(result[0]?.id).toBe('1');
});

test('sorts by size desc', () => {
  const result = applyFilters(listings, { ...baseFilter, sortBy: 'size_desc' });
  expect(result[0]?.id).toBe('1');
});
