import { CacheService } from '@/core/cache';
import { defaultFilterState } from '@/stores/filterStore';
import { defaultRuntimeConfig, sanitizeRuntimeConfig } from '@/core/runtime';
import { kv } from '@/core/kv';
import { useAlertStore } from '@/stores/alertStore';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useFilterStore } from '@/stores/filterStore';
import { usePreferencesStore } from '@/stores/preferencesStore';
import { useRuntimeStore } from '@/stores/runtimeStore';
import { useSourceStore } from '@/stores/sourceStore';
import type { AnalyticsState } from '@/types/analytics';
import type { SavedAlert, AlertHistoryItem } from '@/types/alert';
import type { BackupPayload } from '@/types/backup';
import type { FilterState } from '@/types/filter';
import type { Favorite } from '@/types/listing';
import type { PreferencesState } from '@/types/preferences';
import type { RuntimeConfig } from '@/types/runtime';
import type { Source } from '@/types/source';

function getStoredObject<T>(key: string, fallback: T): T {
  return kv.getObject<T>(key) ?? fallback;
}

export const BackupService = {
  async exportPayload(): Promise<BackupPayload> {
    return {
      version: '1.0',
      exportedAt: new Date().toISOString(),
      preferences: getStoredObject<PreferencesState>('preferences-store', {
        locale: 'en', themeMode: 'system', hasCompletedOnboarding: false, analyticsEnabled: true,
      }),
      filter: getStoredObject<FilterState>('filter-store', defaultFilterState),
      favorites: getStoredObject<{ favorites: Favorite[]; recentlyViewed: string[]; dismissedIds: string[] }>('favorites-store', {
        favorites: [], recentlyViewed: [], dismissedIds: [],
      }),
      compare: getStoredObject<string[]>('compare-store', []),
      alerts: getStoredObject<{ alerts: SavedAlert[]; history: AlertHistoryItem[] }>('alert-store-v2', { alerts: [], history: [] }),
      analytics: getStoredObject<AnalyticsState>('analytics-store', { counters: useAnalyticsStore.getState().counters, lastEventAt: null }),
      sources: getStoredObject<Source[]>('source-store', useSourceStore.getState().sources),
      runtime: sanitizeRuntimeConfig(getStoredObject<RuntimeConfig>('runtime-store', defaultRuntimeConfig)),
      cache: {
        syncSummary: await CacheService.getSyncSummary(),
        syncHistory: await CacheService.getSyncHistory(20),
      },
    };
  },
  async exportJson() {
    return JSON.stringify(await this.exportPayload(), null, 2);
  },
  async restoreFromJson(json: string) {
    const parsed = JSON.parse(json) as Partial<BackupPayload>;
    if (parsed.version !== '1.0') throw new Error('Unsupported backup version');

    await CacheService.clearAll();

    kv.setObject('preferences-store', parsed.preferences ?? { locale: 'en', themeMode: 'system', hasCompletedOnboarding: false, analyticsEnabled: true });
    kv.setObject('filter-store', parsed.filter ?? defaultFilterState);
    kv.setObject('favorites-store', parsed.favorites ?? { favorites: [], recentlyViewed: [], dismissedIds: [] });
    kv.setObject('compare-store', parsed.compare ?? []);
    kv.setObject('alert-store-v2', parsed.alerts ?? { alerts: [], history: [] });
    kv.setObject('analytics-store', parsed.analytics ?? { counters: useAnalyticsStore.getState().counters, lastEventAt: null });
    kv.setObject('source-store', parsed.sources ?? useSourceStore.getState().sources);
    kv.setObject('runtime-store', sanitizeRuntimeConfig(parsed.runtime));

    if (parsed.cache?.syncSummary) await CacheService.setSyncSummary(parsed.cache.syncSummary);
    if (parsed.cache?.syncHistory?.length) {
      for (const item of parsed.cache.syncHistory.slice().reverse()) {
        await CacheService.addSyncHistory(item);
      }
    }

    usePreferencesStore.getState().hydrate();
    useFilterStore.getState().hydrate();
    useFavoritesStore.getState().hydrate();
    useCompareStore.getState().hydrate();
    useAlertStore.getState().hydrate();
    useAnalyticsStore.getState().hydrate();
    useSourceStore.getState().hydrate();
    useRuntimeStore.getState().hydrate();
  },
};
