import { create } from 'zustand';
import type { FilterState } from '@/types/filter';
import { kv } from '@/core/kv';

export const defaultFilterState: FilterState = {
  query: '',
  listingType: null,
  propertyTypes: [],
  priceMin: null,
  priceMax: null,
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

interface FilterStore {
  filter: FilterState;
  setFilter: (patch: Partial<FilterState>) => void;
  resetFilter: () => void;
  hydrate: () => void;
}

export const useFilterStore = create<FilterStore>((set) => ({
  filter: defaultFilterState,
  setFilter: (patch) =>
    set((state) => {
      const filter = { ...state.filter, ...patch };
      kv.setObject('filter-store', filter);
      return { filter };
    }),
  resetFilter: () => {
    kv.setObject('filter-store', defaultFilterState);
    set({ filter: defaultFilterState });
  },
  hydrate: () => {
    const saved = kv.getObject<FilterState>('filter-store');
    if (saved) set({ filter: { ...defaultFilterState, ...saved, city: saved.city ?? 'Berlin' } });
  },
}));
