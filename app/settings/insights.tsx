import { useEffect, useState } from 'react';
import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { formatPrice } from '@/core/format';
import { tokens } from '@/design/tokens';
import { DiagnosticsService } from '@/services/DiagnosticsService';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import type { MarketInsights } from '@/core/insights';

export default function InsightsScreen() {
  const counters = useAnalyticsStore((state) => state.counters);
  const lastEventAt = useAnalyticsStore((state) => state.lastEventAt);
  const [marketInsights, setMarketInsights] = useState<MarketInsights | null>(null);

  useEffect(() => {
    void DiagnosticsService.getMarketInsights().then(setMarketInsights);
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Local insights" subtitle="Counts only, no personal content or remote tracking." />
        <Section title="Usage counters">
          {Object.entries(counters).map(([key, value]) => (
            <View key={key} style={styles.card}>
              <Text style={styles.label}>{key}</Text>
              <Text style={styles.value}>{value}</Text>
            </View>
          ))}
          <Text style={styles.footer}>Last event: {lastEventAt ?? '—'}</Text>
        </Section>
        <Section title="Market snapshot">
          <Text style={styles.footer}>Listings cached: {marketInsights?.totalListings ?? '—'}</Text>
          <Text style={styles.footer}>Average price: {formatPrice(marketInsights?.averagePrice ?? null)}</Text>
          <Text style={styles.footer}>Median price: {formatPrice(marketInsights?.medianPrice ?? null)}</Text>
          <Text style={styles.footer}>Suspicious listings: {marketInsights?.suspiciousCount ?? '—'}</Text>
        </Section>
        <Section title="Top cities">
          {marketInsights?.cityBreakdown.length ? marketInsights.cityBreakdown.map((item) => (
            <View key={item.city} style={styles.card}>
              <Text style={styles.label}>{item.city}</Text>
              <Text style={styles.smallValue}>{item.count} listings · {formatPrice(item.averagePrice ?? null)}</Text>
            </View>
          )) : <Text style={styles.footer}>No city data yet.</Text>}
        </Section>
        <Section title="Freshness buckets">
          {marketInsights?.freshnessBuckets.map((item) => (
            <View key={item.label} style={styles.card}>
              <Text style={styles.label}>{item.label}</Text>
              <Text style={styles.smallValue}>{item.count}</Text>
            </View>
          ))}
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  card: { backgroundColor: '#fff', borderRadius: 16, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, flexDirection: 'row', justifyContent: 'space-between' },
  label: { color: tokens.colors.text, fontWeight: '700' },
  value: { color: tokens.colors.primary, fontWeight: '900', fontSize: 22 },
  smallValue: { color: tokens.colors.primary, fontWeight: '800', fontSize: 16 },
  footer: { color: tokens.colors.textMuted, lineHeight: 22 },
});
