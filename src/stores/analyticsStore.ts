import { create } from 'zustand';
import { kv } from '@/core/kv';
import { usePreferencesStore } from '@/stores/preferencesStore';
import type { AnalyticsEvent, AnalyticsState } from '@/types/analytics';

interface AnalyticsStore extends AnalyticsState {
  track: (event: AnalyticsEvent) => void;
  hydrate: () => void;
  reset: () => void;
}

const defaultCounters: AnalyticsState['counters'] = {
  app_open: 0,
  refresh: 0,
  listing_open: 0,
  favorite_add: 0,
  favorite_remove: 0,
  dismiss: 0,
  alert_run: 0,
  source_test: 0,
};

function persist(state: AnalyticsState) {
  kv.setObject('analytics-store', state);
}

export const useAnalyticsStore = create<AnalyticsStore>((set, get) => ({
  counters: defaultCounters,
  lastEventAt: null,
  track: (event) => {
    if (!usePreferencesStore.getState().analyticsEnabled) return;
    const counters = { ...get().counters, [event]: get().counters[event] + 1 };
    const lastEventAt = new Date().toISOString();
    persist({ counters, lastEventAt });
    set({ counters, lastEventAt });
  },
  hydrate: () => {
    const saved = kv.getObject<AnalyticsState>('analytics-store');
    if (saved) {
      set({ counters: { ...defaultCounters, ...saved.counters }, lastEventAt: saved.lastEventAt });
    }
  },
  reset: () => {
    const state: AnalyticsState = { counters: defaultCounters, lastEventAt: null };
    persist(state);
    set(state);
  },
}));
