import type { FilterState } from '@/types/filter';
import type { NormalizedListing } from '@/types/listing';

export function applyFilters(listings: NormalizedListing[], filter: FilterState) {
  const query = filter.query.trim().toLowerCase();
  return listings.filter((listing) => {
    if (filter.listingType && listing.listingType !== filter.listingType) return false;
    if (filter.city && !listing.city.toLowerCase().includes(filter.city.toLowerCase())) return false;
    if (filter.propertyTypes.length > 0 && !filter.propertyTypes.includes(listing.propertyType)) return false;
    if (filter.priceMin !== null && (listing.price ?? 0) < filter.priceMin) return false;
    if (filter.priceMax !== null && (listing.price ?? Number.MAX_SAFE_INTEGER) > filter.priceMax) return false;
    if (filter.roomsMin !== null && (listing.rooms ?? 0) < filter.roomsMin) return false;
    if (filter.furnished && listing.furnished !== true) return false;
    if (filter.balcony && listing.balcony !== true) return false;
    if (filter.parking && listing.parking !== true) return false;
    if (filter.petsAllowed && listing.petsAllowed !== true) return false;
    if (filter.sourceIds.length > 0 && !filter.sourceIds.includes(listing.sourceId)) return false;
    if (!query) return true;
    const haystack = [
      listing.title,
      listing.description ?? '',
      listing.city,
      listing.district ?? '',
      listing.propertyType,
      listing.sourceId,
    ].join(' ').toLowerCase();
    return haystack.includes(query);
  }).sort((a, b) => {
    switch (filter.sortBy) {
      case 'newest':
        return new Date(b.fetchedAt).getTime() - new Date(a.fetchedAt).getTime();
      case 'price_asc':
        return (a.price ?? Number.MAX_SAFE_INTEGER) - (b.price ?? Number.MAX_SAFE_INTEGER);
      case 'price_desc':
        return (b.price ?? 0) - (a.price ?? 0);
      case 'size_asc':
        return (a.sizeSqm ?? Number.MAX_SAFE_INTEGER) - (b.sizeSqm ?? Number.MAX_SAFE_INTEGER);
      case 'size_desc':
        return (b.sizeSqm ?? 0) - (a.sizeSqm ?? 0);
      default:
        return (b.trustScore + b.freshnessScore) - (a.trustScore + a.freshnessScore);
    }
  });
}
