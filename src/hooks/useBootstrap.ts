import { useEffect, useState } from 'react';
import { evaluateAlerts } from '@/core/alerts';
import { deduplicate, fetchSourceListings } from '@/core/adapters';
import { CacheService } from '@/core/cache';
import { NotificationService } from '@/services/NotificationService';
import { useAlertStore } from '@/stores/alertStore';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useFilterStore } from '@/stores/filterStore';
import { useListingStore } from '@/stores/listingStore';
import { usePreferencesStore } from '@/stores/preferencesStore';
import { useRuntimeStore } from '@/stores/runtimeStore';
import { useSourceStore } from '@/stores/sourceStore';
import type { SyncSummary } from '@/types/sync';

export async function refreshAllListings() {
  const sources = useSourceStore.getState().sources.filter((item) => item.enabled).sort((a, b) => a.priority - b.priority);
  const listingStore = useListingStore.getState();
  const startedAt = new Date().toISOString();
  listingStore.setRefreshing(true);
  useAnalyticsStore.getState().track('refresh');

  const results = await Promise.allSettled(
    sources.map(async (source) => {
      const start = Date.now();
      useSourceStore.getState().setHealthStatus(source.id, { status: 'testing' });
      try {
        const listings = await fetchSourceListings(source);
        useSourceStore.getState().setHealthStatus(source.id, { status: 'ok', latencyMs: Date.now() - start, listingCount: listings.length, lastSuccessAt: new Date().toISOString(), lastErrorMessage: undefined });
        listingStore.setSourceError(source.id, null);
        return { sourceId: source.id, listings };
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Unknown source error';
        useSourceStore.getState().setHealthStatus(source.id, { status: 'error', latencyMs: Date.now() - start, lastErrorAt: new Date().toISOString(), lastErrorMessage: message });
        listingStore.setSourceError(source.id, message);
        return { sourceId: source.id, listings: [], error: message };
      }
    })
  );

  const fulfilled = results.filter((r): r is PromiseFulfilledResult<{ sourceId: string; listings: any[]; error?: string }> => r.status === 'fulfilled').map((r) => r.value);
  const flattened = fulfilled.flatMap((r) => r.listings);
  const unique = deduplicate(flattened);
  await CacheService.upsertListings(unique);
  const cached = await CacheService.getListings();
  listingStore.setListings(cached);

  const summary: SyncSummary = {
    startedAt,
    finishedAt: new Date().toISOString(),
    sourceCount: sources.length,
    successfulSources: fulfilled.filter((item) => !item.error).length,
    failedSources: fulfilled.filter((item) => item.error).length,
    rawListingCount: flattened.length,
    dedupedListingCount: unique.length,
    issues: fulfilled.filter((item) => item.error).map((item) => ({ sourceId: item.sourceId, message: item.error ?? 'Unknown source error' })),
  };

  await CacheService.setSyncSummary(summary);
  await CacheService.addSyncHistory(summary);
  listingStore.setSyncSummary(summary);
  listingStore.setSyncHistory(await CacheService.getSyncHistory(10));

  const alertResults = evaluateAlerts(useAlertStore.getState().alerts, cached);
  useAlertStore.getState().applyRunResults(alertResults);
  const notificationSummary = alertResults.filter((item) => item.matchCount > 0).reduce((acc, item) => acc + item.matchCount, 0);
  if (notificationSummary > 0) {
    await NotificationService.notify('HomeSync alerts updated', `${notificationSummary} matching listings found across your saved searches.`);
  }

  listingStore.setRefreshing(false);
}

export function useBootstrap() {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let mounted = true;
    void (async () => {
      await CacheService.init();
      usePreferencesStore.getState().hydrate();
      useRuntimeStore.getState().hydrate();
      useFilterStore.getState().hydrate();
      useFavoritesStore.getState().hydrate();
      useCompareStore.getState().hydrate();
      useAnalyticsStore.getState().hydrate();
      useAlertStore.getState().hydrate();
      useSourceStore.getState().hydrate();
      await NotificationService.requestPermissions();
      useAnalyticsStore.getState().track('app_open');
      useListingStore.getState().setListings(await CacheService.getListings());
      useListingStore.getState().setSyncSummary(await CacheService.getSyncSummary());
      useListingStore.getState().setSyncHistory(await CacheService.getSyncHistory(10));
      if (useListingStore.getState().listings.length === 0) {
        await refreshAllListings();
      }
      if (mounted) setReady(true);
    })();
    return () => { mounted = false; };
  }, []);

  return { ready };
}
