export type AnalyticsEventName =
  | 'app_open'
  | 'refresh'
  | 'listing_open'
  | 'favorite_add'
  | 'favorite_remove'
  | 'dismiss'
  | 'compare_add'
  | 'compare_remove'
  | 'source_test'
  | 'alert_run';

export interface AnalyticsState {
  counters: Record<AnalyticsEventName, number>;
  lastEventAt: string | null;
}
