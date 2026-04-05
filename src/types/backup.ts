import type { SavedAlert, AlertHistoryItem } from '@/types/alert';
import type { AnalyticsState } from '@/types/analytics';
import type { FilterState } from '@/types/filter';
import type { Favorite } from '@/types/listing';
import type { PreferencesState } from '@/types/preferences';
import type { RuntimeConfig } from '@/types/runtime';
import type { Source } from '@/types/source';
import type { SyncHistoryItem, SyncSummary } from '@/types/sync';

export interface BackupPayload {
  version: '1.0';
  exportedAt: string;
  preferences: PreferencesState;
  filter: FilterState;
  favorites: {
    favorites: Favorite[];
    recentlyViewed: string[];
    dismissedIds: string[];
  };
  compare: string[];
  alerts: {
    alerts: SavedAlert[];
    history: AlertHistoryItem[];
  };
  analytics: AnalyticsState;
  sources: Source[];
  runtime: RuntimeConfig;
  cache: {
    syncSummary: SyncSummary | null;
    syncHistory: SyncHistoryItem[];
  };
}
