import { evaluateAlerts } from '@/core/alerts';
import type { SavedAlert } from '@/types/alert';
import type { FilterState } from '@/types/filter';
import type { NormalizedListing } from '@/types/listing';

const filter: FilterState = {
  query: '',
  listingType: 'rent',
  propertyTypes: ['apartment'],
  priceMin: null,
  priceMax: 1600,
  roomsMin: null,
  city: 'Berlin',
  furnished: null,
  petsAllowed: null,
  parking: null,
  balcony: null,
  sourceIds: [],
  sortBy: 'relevance',
  showDismissed: false,
};

const alerts: SavedAlert[] = [{
  id: 'alert-1',
  name: 'Berlin alert',
  enabled: true,
  createdAt: '2026-04-05T10:00:00.000Z',
  frequency: 'daily',
  lastRunAt: null,
  lastMatchAt: null,
  matchCount: 0,
  filter,
}];

const listings: NormalizedListing[] = [{
  id: '1', canonicalId: 'c1', dedupeKey: 'c1', sourceId: 'immoscout-de', sourceListingId: '1', sourceUrl: 'https://example.com/1', alternateSourceUrls: [], listingType: 'rent', propertyType: 'apartment', title: 'Bright Berlin apartment', description: 'Furnished place', price: 1500, priceCurrency: 'EUR', priceType: 'per_month', pricePerSqm: 25, country: 'DE', city: 'Berlin', district: 'Mitte', street: 'Tor', houseNumber: '1', zip: '10119', lat: 0, lng: 0, sizeSqm: 60, rooms: 2, furnished: true, petsAllowed: false, parking: false, balcony: true, elevator: true, images: [], thumbnailUrl: null, contactName: null, freshnessScore: 90, completenessScore: 90, trustScore: 90, isSuspicious: false, suspiciousReasons: [], listedAt: '2026-04-04T09:00:00.000Z', fetchedAt: '2026-04-05T09:00:00.000Z', lastSeenAt: '2026-04-05T09:00:00.000Z', isActive: true,
}];

test('evaluates alerts against current listings', () => {
  const result = evaluateAlerts(alerts, listings);
  expect(result).toHaveLength(1);
  expect(result[0]?.matchCount).toBe(1);
  expect(result[0]?.listingIds).toEqual(['1']);
});
