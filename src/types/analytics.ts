export type AnalyticsEvent =
  | 'app_open'
  | 'refresh'
  | 'listing_open'
  | 'favorite_add'
  | 'favorite_remove'
  | 'dismiss'
  | 'alert_run'
  | 'source_test';

export interface AnalyticsState {
  counters: Record<AnalyticsEvent, number>;
  lastEventAt: string | null;
}
