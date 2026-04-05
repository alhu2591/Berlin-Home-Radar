import type { RuntimeConfig } from '@/types/runtime';

export interface SyncIssue {
  sourceId: string;
  message: string;
}

export interface SyncSummary {
  startedAt: string;
  finishedAt: string;
  sourceCount: number;
  successfulSources: number;
  failedSources: number;
  rawListingCount: number;
  dedupedListingCount: number;
  issues: SyncIssue[];
}

export interface SyncHistoryItem extends SyncSummary {
  id: string;
}

export interface CacheStats {
  listingCount: number;
  approximateSizeKb: number;
  lastSyncAt: string | null;
}

export interface DiagnosticsSnapshot {
  exportedAt: string;
  cacheStats: CacheStats;
  latestSync: SyncSummary | null;
  syncHistory: SyncHistoryItem[];
  runtime: RuntimeConfig;
  sourceHealth: Array<{
    id: string;
    name: string;
    enabled: boolean;
    status: 'idle' | 'ok' | 'error' | 'testing';
    latencyMs?: number;
    listingCount?: number;
    lastSuccessAt?: string;
    lastErrorAt?: string;
    lastErrorMessage?: string;
  }>;
  analyticsCounters: Record<string, number>;
  favoritesCount: number;
  dismissedCount: number;
  alertCount: number;
}
