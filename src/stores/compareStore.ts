import { create } from 'zustand';
import { kv } from '@/core/kv';
import { useAnalyticsStore } from '@/stores/analyticsStore';

interface CompareStore {
  selectedIds: string[];
  toggle: (listingId: string) => void;
  clear: () => void;
  hydrate: () => void;
}

export const useCompareStore = create<CompareStore>((set) => ({
  selectedIds: [],
  toggle: (listingId) =>
    set((state) => {
      const exists = state.selectedIds.includes(listingId);
      const selectedIds = exists ? state.selectedIds.filter((id) => id !== listingId) : [...state.selectedIds, listingId].slice(-4);
      kv.setObject('compare-store', selectedIds);
      useAnalyticsStore.getState().track(exists ? 'compare_remove' : 'compare_add');
      return { selectedIds };
    }),
  clear: () => {
    kv.setObject('compare-store', []);
    set({ selectedIds: [] });
  },
  hydrate: () => {
    const saved = kv.getObject<string[]>('compare-store');
    if (saved) set({ selectedIds: saved });
  },
}));
