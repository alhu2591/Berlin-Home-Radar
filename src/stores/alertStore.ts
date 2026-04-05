import { create } from 'zustand';
import { buildAlertName } from '@/core/alerts';
import { kv } from '@/core/kv';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import type { AlertFrequency, AlertHistoryItem, AlertRunResult, SavedAlert } from '@/types/alert';
import type { FilterState } from '@/types/filter';

interface AlertStore {
  alerts: SavedAlert[];
  history: AlertHistoryItem[];
  addAlertFromFilter: (filter: FilterState, frequency?: AlertFrequency) => void;
  toggleAlert: (id: string) => void;
  removeAlert: (id: string) => void;
  applyRunResults: (results: AlertRunResult[]) => void;
  clearHistory: () => void;
  hydrate: () => void;
}

function persist(alerts: SavedAlert[], history: AlertHistoryItem[]) {
  kv.setObject('alert-store-v2', { alerts, history });
}

function normalizeLegacyAlerts(alerts: Array<Partial<SavedAlert> & { id: string; name: string; createdAt: string; filter: FilterState; enabled: boolean; matchCount: number; lastMatchAt: string | null }>): SavedAlert[] {
  return alerts.map((item) => ({
    id: item.id,
    name: item.name,
    enabled: item.enabled,
    createdAt: item.createdAt,
    frequency: item.frequency ?? 'daily',
    lastRunAt: item.lastRunAt ?? null,
    lastMatchAt: item.lastMatchAt,
    matchCount: item.matchCount,
    filter: item.filter,
  }));
}

export const useAlertStore = create<AlertStore>((set) => ({
  alerts: [],
  history: [],
  addAlertFromFilter: (filter, frequency = 'daily') =>
    set((state) => {
      const alerts = [
        {
          id: `alert-${Date.now()}`,
          name: buildAlertName(filter),
          enabled: true,
          createdAt: new Date().toISOString(),
          frequency,
          lastRunAt: null,
          lastMatchAt: null,
          matchCount: 0,
          filter,
        },
        ...state.alerts,
      ];
      persist(alerts, state.history);
      return { alerts };
    }),
  toggleAlert: (id) =>
    set((state) => {
      const alerts = state.alerts.map((item) => (item.id === id ? { ...item, enabled: !item.enabled } : item));
      persist(alerts, state.history);
      return { alerts };
    }),
  removeAlert: (id) =>
    set((state) => {
      const alerts = state.alerts.filter((item) => item.id !== id);
      const history = state.history.filter((item) => item.alertId !== id);
      persist(alerts, history);
      return { alerts, history };
    }),
  applyRunResults: (results) =>
    set((state) => {
      const now = new Date().toISOString();
      const alerts = state.alerts.map((item) => {
        const result = results.find((entry) => entry.alertId === item.id);
        if (!result) return item;
        return {
          ...item,
          lastRunAt: now,
          matchCount: result.matchCount,
          lastMatchAt: result.matchCount > 0 ? now : item.lastMatchAt,
        };
      });
      const additions = results
        .filter((item) => item.matchCount > 0)
        .map((item) => ({
          id: `hist-${item.alertId}-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
          alertId: item.alertId,
          alertName: alerts.find((entry) => entry.id === item.alertId)?.name ?? 'Saved search alert',
          timestamp: now,
          matchCount: item.matchCount,
          listingIds: item.listingIds,
        }));
      const history = [...additions, ...state.history].slice(0, 50);
      persist(alerts, history);
      if (results.length) useAnalyticsStore.getState().track('alert_run');
      return { alerts, history };
    }),
  clearHistory: () =>
    set((state) => {
      persist(state.alerts, []);
      return { history: [] };
    }),
  hydrate: () => {
    const saved = kv.getObject<{ alerts: SavedAlert[]; history: AlertHistoryItem[] }>('alert-store-v2');
    if (saved) set({ alerts: normalizeLegacyAlerts(saved.alerts), history: saved.history ?? [] });
    else {
      const legacy = kv.getObject<Array<Partial<SavedAlert> & { id: string; name: string; createdAt: string; filter: FilterState; enabled: boolean; matchCount: number; lastMatchAt: string | null }>>('alert-store');
      if (legacy) set({ alerts: normalizeLegacyAlerts(legacy), history: [] });
    }
  },
}));
