import { create } from 'zustand';
import type { SyncHistoryItem, SyncSummary } from '@/types/sync';
import type { NormalizedListing } from '@/types/listing';

interface ListingStore {
  listings: NormalizedListing[];
  isRefreshing: boolean;
  sourceErrors: Record<string, string>;
  syncSummary: SyncSummary | null;
  syncHistory: SyncHistoryItem[];
  setListings: (listings: NormalizedListing[]) => void;
  setRefreshing: (value: boolean) => void;
  setSourceError: (sourceId: string, error: string | null) => void;
  setSyncSummary: (summary: SyncSummary | null) => void;
  setSyncHistory: (items: SyncHistoryItem[]) => void;
  getListingById: (id: string) => NormalizedListing | undefined;
}

export const useListingStore = create<ListingStore>((set, get) => ({
  listings: [],
  isRefreshing: false,
  sourceErrors: {},
  syncSummary: null,
  syncHistory: [],
  setListings: (listings) => set({ listings }),
  setRefreshing: (value) => set({ isRefreshing: value }),
  setSyncSummary: (summary) => set({ syncSummary: summary }),
  setSyncHistory: (items) => set({ syncHistory: items }),
  setSourceError: (sourceId, error) =>
    set((state) => ({
      sourceErrors: error
        ? { ...state.sourceErrors, [sourceId]: error }
        : Object.fromEntries(Object.entries(state.sourceErrors).filter(([key]) => key !== sourceId)),
    })),
  getListingById: (id) => get().listings.find((item) => item.id === id),
}));
