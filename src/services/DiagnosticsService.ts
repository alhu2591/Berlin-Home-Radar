import { CacheService } from '@/core/cache';
import { useAlertStore } from '@/stores/alertStore';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useListingStore } from '@/stores/listingStore';
import { useRuntimeStore } from '@/stores/runtimeStore';
import { useSourceStore } from '@/stores/sourceStore';
import type { DiagnosticsSnapshot } from '@/types/sync';

export const DiagnosticsService = {
  async buildSnapshot(): Promise<DiagnosticsSnapshot> {
    const cacheStats = await CacheService.getStats();
    const latestSync = await CacheService.getSyncSummary();
    const syncHistory = await CacheService.getSyncHistory(10);
    const runtime = useRuntimeStore.getState();
    const sourceHealth = useSourceStore.getState().sources.map((source) => ({
      id: source.id,
      name: source.name,
      enabled: source.enabled,
      status: source.health.status,
      latencyMs: source.health.latencyMs,
      listingCount: source.health.listingCount,
      lastSuccessAt: source.health.lastSuccessAt,
      lastErrorAt: source.health.lastErrorAt,
      lastErrorMessage: source.health.lastErrorMessage,
    }));

    return {
      exportedAt: new Date().toISOString(),
      cacheStats,
      latestSync,
      syncHistory,
      runtime: {
        mode: runtime.mode,
        workerBaseUrl: runtime.workerBaseUrl,
        requestTimeoutMs: runtime.requestTimeoutMs,
        allowDemoFallback: runtime.allowDemoFallback,
        debugNetwork: runtime.debugNetwork,
        lastValidatedAt: runtime.lastValidatedAt,
      },
      sourceHealth,
      analyticsCounters: useAnalyticsStore.getState().counters,
      favoritesCount: useFavoritesStore.getState().favorites.length,
      dismissedCount: useFavoritesStore.getState().dismissedIds.length,
      alertCount: useAlertStore.getState().alerts.length,
    };
  },
  async exportAsJson() {
    const snapshot = await this.buildSnapshot();
    return JSON.stringify(snapshot, null, 2);
  },
  async getMarketInsights() {
    return import('@/core/insights').then(({ buildMarketInsights }) => buildMarketInsights(useListingStore.getState().listings));
  },
};
