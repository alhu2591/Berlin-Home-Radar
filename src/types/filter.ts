import type { ListingType, PropertyType } from './listing';

export type SortBy = 'relevance' | 'newest' | 'price_asc' | 'price_desc' | 'size_asc' | 'size_desc';

export interface FilterState {
  query: string;
  listingType: ListingType | null;
  propertyTypes: PropertyType[];
  priceMin: number | null;
  priceMax: number | null;
  roomsMin: number | null;
  city: string | null;
  furnished: boolean | null;
  petsAllowed: boolean | null;
  parking: boolean | null;
  balcony: boolean | null;
  sourceIds: string[];
  sortBy: SortBy;
  showDismissed: boolean;
}
