import { useMemo } from 'react';
import { applyFilters } from '@/core/search';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useFilterStore } from '@/stores/filterStore';
import { useListingStore } from '@/stores/listingStore';
import { useSourceStore } from '@/stores/sourceStore';

export function useFilteredListings() {
  const listings = useListingStore((state) => state.listings);
  const filter = useFilterStore((state) => state.filter);
  const dismissedIds = useFavoritesStore((state) => state.dismissedIds);
  const sources = useSourceStore((state) => state.sources);

  return useMemo(() => {
    const enabledIds = sources.filter((item) => item.enabled).map((item) => item.adapterId);
    const activeListings = listings.filter((item) => enabledIds.includes(item.sourceId));
    const visible = filter.showDismissed ? activeListings : activeListings.filter((item) => !dismissedIds.includes(item.id));
    const filtered = applyFilters(visible, filter);
    return { listings: filtered, count: filtered.length };
  }, [dismissedIds, filter, listings, sources]);
}
