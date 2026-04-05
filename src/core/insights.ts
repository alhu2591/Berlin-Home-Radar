import type { NormalizedListing } from '@/types/listing';

export interface MarketInsights {
  totalListings: number;
  suspiciousCount: number;
  averagePrice: number | null;
  medianPrice: number | null;
  cityBreakdown: Array<{ city: string; count: number; averagePrice: number | null }>;
  sourceBreakdown: Array<{ sourceId: string; count: number }>;
  freshnessBuckets: Array<{ label: string; count: number }>;
}

function average(values: number[]) {
  if (!values.length) return null;
  return Math.round(values.reduce((sum, value) => sum + value, 0) / values.length);
}

function median(values: number[]) {
  if (!values.length) return null;
  const sorted = [...values].sort((a, b) => a - b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 0
    ? Math.round((sorted[mid - 1] + sorted[mid]) / 2)
    : sorted[mid];
}

function ageInDays(value: string | null) {
  if (!value) return Number.POSITIVE_INFINITY;
  return Math.max(0, Math.floor((Date.now() - new Date(value).getTime()) / 86400000));
}

export function buildMarketInsights(listings: NormalizedListing[]): MarketInsights {
  const prices = listings.flatMap((item) => (typeof item.price === 'number' ? [item.price] : []));

  const cityMap = new Map<string, number[]>();
  const sourceMap = new Map<string, number>();
  const freshness = [
    { label: '0-1 days', count: 0 },
    { label: '2-7 days', count: 0 },
    { label: '8-30 days', count: 0 },
    { label: '30+ days', count: 0 },
  ];

  for (const listing of listings) {
    const cityPrices = cityMap.get(listing.city) ?? [];
    if (typeof listing.price === 'number') cityPrices.push(listing.price);
    cityMap.set(listing.city, cityPrices);

    sourceMap.set(listing.sourceId, (sourceMap.get(listing.sourceId) ?? 0) + 1);

    const days = ageInDays(listing.listedAt ?? listing.fetchedAt);
    if (days <= 1) freshness[0].count += 1;
    else if (days <= 7) freshness[1].count += 1;
    else if (days <= 30) freshness[2].count += 1;
    else freshness[3].count += 1;
  }

  return {
    totalListings: listings.length,
    suspiciousCount: listings.filter((item) => item.isSuspicious).length,
    averagePrice: average(prices),
    medianPrice: median(prices),
    cityBreakdown: [...cityMap.entries()]
      .map(([city, values]) => ({ city, count: values.length || listings.filter((item) => item.city === city).length, averagePrice: average(values) }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 6),
    sourceBreakdown: [...sourceMap.entries()]
      .map(([sourceId, count]) => ({ sourceId, count }))
      .sort((a, b) => b.count - a.count),
    freshnessBuckets: freshness,
  };
}
