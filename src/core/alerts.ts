import { applyFilters } from '@/core/search';
import type { AlertRunResult, SavedAlert } from '@/types/alert';
import type { FilterState } from '@/types/filter';
import type { NormalizedListing } from '@/types/listing';

export function buildAlertName(filter: FilterState) {
  const parts = [filter.city, filter.listingType, filter.propertyTypes[0]].filter(Boolean);
  return parts.length ? `${parts.join(' · ')} alert` : 'Saved search alert';
}

export function evaluateAlerts(alerts: SavedAlert[], listings: NormalizedListing[]): AlertRunResult[] {
  return alerts
    .filter((alert) => alert.enabled)
    .map((alert) => {
      const matched = applyFilters(listings, { ...alert.filter, showDismissed: true });
      return {
        alertId: alert.id,
        listingIds: matched.map((item) => item.id),
        matchCount: matched.length,
      };
    });
}
