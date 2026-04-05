import { create } from 'zustand';
import { kv } from '@/core/kv';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import type { Favorite } from '@/types/listing';

interface FavoritePatch {
  note?: string | null;
  tags?: string[];
}

interface FavoritesStore {
  favorites: Favorite[];
  recentlyViewed: string[];
  dismissedIds: string[];
  toggleFavorite: (listingId: string) => void;
  isFavorite: (listingId: string) => boolean;
  getFavorite: (listingId: string) => Favorite | undefined;
  updateFavoriteMeta: (listingId: string, patch: FavoritePatch) => void;
  markViewed: (listingId: string) => void;
  dismiss: (listingId: string) => void;
  restoreDismissed: (listingId: string) => void;
  clearDismissed: () => void;
  hydrate: () => void;
}

function persist(data: Pick<FavoritesStore, 'favorites' | 'recentlyViewed' | 'dismissedIds'>) {
  kv.setObject('favorites-store', data);
}

export const useFavoritesStore = create<FavoritesStore>((set, get) => ({
  favorites: [],
  recentlyViewed: [],
  dismissedIds: [],
  toggleFavorite: (listingId) =>
    set((state) => {
      const exists = state.favorites.some((item) => item.listingId === listingId);
      const favorites = exists
        ? state.favorites.filter((item) => item.listingId !== listingId)
        : [...state.favorites, { listingId, savedAt: new Date().toISOString(), note: null, tags: [] }];
      persist({ favorites, recentlyViewed: state.recentlyViewed, dismissedIds: state.dismissedIds });
      useAnalyticsStore.getState().track(exists ? 'favorite_remove' : 'favorite_add');
      return { favorites };
    }),
  isFavorite: (listingId) => get().favorites.some((item) => item.listingId === listingId),
  getFavorite: (listingId) => get().favorites.find((item) => item.listingId === listingId),
  updateFavoriteMeta: (listingId, patch) =>
    set((state) => {
      const favorites = state.favorites.map((item) => item.listingId === listingId ? {
        ...item,
        note: patch.note !== undefined ? patch.note : item.note,
        tags: patch.tags !== undefined ? patch.tags : item.tags,
      } : item);
      persist({ favorites, recentlyViewed: state.recentlyViewed, dismissedIds: state.dismissedIds });
      return { favorites };
    }),
  markViewed: (listingId) =>
    set((state) => {
      const recentlyViewed = [listingId, ...state.recentlyViewed.filter((id) => id !== listingId)].slice(0, 50);
      persist({ favorites: state.favorites, recentlyViewed, dismissedIds: state.dismissedIds });
      useAnalyticsStore.getState().track('listing_open');
      return { recentlyViewed };
    }),
  dismiss: (listingId) =>
    set((state) => {
      const dismissedIds = Array.from(new Set([...state.dismissedIds, listingId]));
      persist({ favorites: state.favorites, recentlyViewed: state.recentlyViewed, dismissedIds });
      useAnalyticsStore.getState().track('dismiss');
      return { dismissedIds };
    }),
  restoreDismissed: (listingId) =>
    set((state) => {
      const dismissedIds = state.dismissedIds.filter((id) => id !== listingId);
      persist({ favorites: state.favorites, recentlyViewed: state.recentlyViewed, dismissedIds });
      return { dismissedIds };
    }),
  clearDismissed: () =>
    set((state) => {
      persist({ favorites: state.favorites, recentlyViewed: state.recentlyViewed, dismissedIds: [] });
      return { dismissedIds: [] };
    }),
  hydrate: () => {
    const saved = kv.getObject<Pick<FavoritesStore, 'favorites' | 'recentlyViewed' | 'dismissedIds'>>('favorites-store');
    if (saved) set(saved);
  },
}));
