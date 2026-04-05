import type { FilterState } from './filter';

export type AlertFrequency = 'manual' | 'hourly' | 'daily';

export interface SavedAlert {
  id: string;
  name: string;
  enabled: boolean;
  createdAt: string;
  frequency: AlertFrequency;
  lastRunAt: string | null;
  lastMatchAt: string | null;
  matchCount: number;
  filter: FilterState;
}

export interface AlertHistoryItem {
  id: string;
  alertId: string;
  alertName: string;
  timestamp: string;
  matchCount: number;
  listingIds: string[];
}

export interface AlertRunResult {
  alertId: string;
  listingIds: string[];
  matchCount: number;
}
