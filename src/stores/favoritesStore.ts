import { create } from 'zustand';
import { kv } from '@/core/kv';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import type { Favorite } from '@/types/listing';

interface FavoritesStore {
  favorites: Favorite[];
  recentlyViewed: string[];
  dismissedIds: string[];
  toggleFavorite: (listingId: string) => void;
  isFavorite: (listingId: string) => boolean;
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
        : [...state.favorites, { listingId, savedAt: new Date().toISOString() }];
      persist({ favorites, recentlyViewed: state.recentlyViewed, dismissedIds: state.dismissedIds });
      useAnalyticsStore.getState().track(exists ? 'favorite_remove' : 'favorite_add');
      return { favorites };
    }),
  isFavorite: (listingId) => get().favorites.some((item) => item.listingId === listingId),
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
