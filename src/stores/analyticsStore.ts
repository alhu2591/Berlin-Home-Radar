import { create } from 'zustand';
import { kv } from '@/core/kv';
import { usePreferencesStore } from '@/stores/preferencesStore';
import type { AnalyticsEventName, AnalyticsState } from '@/types/analytics';

const defaultCounters: AnalyticsState['counters'] = {
  app_open: 0,
  refresh: 0,
  listing_open: 0,
  favorite_add: 0,
  favorite_remove: 0,
  dismiss: 0,
  compare_add: 0,
  compare_remove: 0,
  source_test: 0,
  alert_run: 0,
};

interface AnalyticsStore extends AnalyticsState {
  hydrate: () => void;
  track: (event: AnalyticsEventName) => void;
  reset: () => void;
}

const defaultState: AnalyticsState = { counters: defaultCounters, lastEventAt: null };

function persist(state: AnalyticsState) {
  kv.setObject('analytics-store', state);
}

export const useAnalyticsStore = create<AnalyticsStore>((set) => ({
  ...defaultState,
  hydrate: () => {
    const saved = kv.getObject<AnalyticsState>('analytics-store');
    if (saved) {
      set({ counters: { ...defaultCounters, ...saved.counters }, lastEventAt: saved.lastEventAt });
    }
  },
  track: (event) => {
    if (!usePreferencesStore.getState().analyticsEnabled) return;
    set((state) => {
      const next: AnalyticsState = {
        counters: { ...state.counters, [event]: state.counters[event] + 1 },
        lastEventAt: new Date().toISOString(),
      };
      persist(next);
      return next;
    });
  },
  reset: () => {
    persist(defaultState);
    set(defaultState);
  },
}));
