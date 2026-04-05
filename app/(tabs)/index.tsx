import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { EmptyState } from '@/components/shared/EmptyState';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { ListingCard } from '@/components/listings/ListingCard';
import { formatDateTime } from '@/core/format';
import { tokens } from '@/design/tokens';
import { useFilteredListings } from '@/hooks/useFilteredListings';
import { refreshAllListings } from '@/hooks/useBootstrap';
import { useAlertStore } from '@/stores/alertStore';
import { useCompareStore } from '@/stores/compareStore';
import { useFilterStore } from '@/stores/filterStore';
import { useListingStore } from '@/stores/listingStore';

function ActiveFilterChip({ label, onClear }: { label: string; onClear: () => void }) {
  return (
    <Text style={styles.chip} onPress={onClear}>
      {label} ×
    </Text>
  );
}

export default function DiscoverScreen() {
  const { listings, count } = useFilteredListings();
  const isRefreshing = useListingStore((state) => state.isRefreshing);
  const sourceErrors = useListingStore((state) => state.sourceErrors);
  const syncSummary = useListingStore((state) => state.syncSummary);
  const selectedIds = useCompareStore((state) => state.selectedIds);
  const alerts = useAlertStore((state) => state.alerts);
  const filter = useFilterStore((state) => state.filter);
  const setFilter = useFilterStore((state) => state.setFilter);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Discover" subtitle="Unified and deduplicated Berlin listings across your configured sources." rightAction={<Button size="sm" label={isRefreshing ? 'Refreshing...' : 'Refresh'} onPress={() => void refreshAllListings()} />} />
        <View style={styles.metricsRow}>
          <View style={styles.metricCard}><Text style={styles.metricLabel}>Visible listings</Text><Text style={styles.metricValue}>{count}</Text></View>
          <View style={styles.metricCard}><Text style={styles.metricLabel}>Compare queue</Text><Text style={styles.metricValue}>{selectedIds.length}</Text></View>
          <View style={styles.metricCard}><Text style={styles.metricLabel}>Alerts</Text><Text style={styles.metricValue}>{alerts.length}</Text></View>
        </View>
        {syncSummary ? (
          <View style={styles.syncCard}>
            <Text style={styles.syncTitle}>Last sync</Text>
            <Text style={styles.syncText}>{syncSummary.dedupedListingCount} listings from {syncSummary.successfulSources}/{syncSummary.sourceCount} sources</Text>
            <Text style={styles.syncText}>Finished {formatDateTime(syncSummary.finishedAt)}</Text>
          </View>
        ) : null}
        <View style={styles.actionsRow}>
          <Button label="Sources" variant="secondary" onPress={() => router.push('/sources')} />
          <Button label="Map" variant="secondary" onPress={() => router.push('/map')} />
          <Button label="Compare" variant="secondary" onPress={() => router.push('/compare')} />
        </View>
        {(filter.city || filter.listingType || filter.propertyTypes.length > 0 || filter.query || filter.sourceIds.length > 0 || filter.showDismissed) ? (
          <View style={styles.filterRow}>
            {filter.city ? <ActiveFilterChip label={`City: ${filter.city}`} onClear={() => setFilter({ city: null })} /> : null}
            {filter.listingType ? <ActiveFilterChip label={`Type: ${filter.listingType}`} onClear={() => setFilter({ listingType: null })} /> : null}
            {filter.query ? <ActiveFilterChip label={`Query: ${filter.query}`} onClear={() => setFilter({ query: '' })} /> : null}
            {filter.propertyTypes.map((item) => <ActiveFilterChip key={item} label={item} onClear={() => setFilter({ propertyTypes: filter.propertyTypes.filter((value) => value !== item) })} />)}
            {filter.sourceIds.map((item) => <ActiveFilterChip key={item} label={item} onClear={() => setFilter({ sourceIds: filter.sourceIds.filter((value) => value !== item) })} />)}
            {filter.showDismissed ? <ActiveFilterChip label="Show dismissed" onClear={() => setFilter({ showDismissed: false })} /> : null}
          </View>
        ) : null}
        {Object.entries(sourceErrors).map(([sourceId, error]) => (
          <View key={sourceId} style={styles.errorBox}>
            <Text style={styles.errorTitle}>{sourceId}</Text>
            <Text style={styles.errorBody}>{error}</Text>
          </View>
        ))}
        {listings.length === 0 ? <EmptyState title="No listings found" description="Adjust filters or enable more sources from the manager." /> : listings.map((listing) => <ListingCard key={listing.id} listing={listing} compact={false} />)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  metricsRow: { flexDirection: 'row', gap: tokens.spacing.md },
  metricCard: { flex: 1, backgroundColor: '#fff', borderRadius: 16, borderWidth: 1, borderColor: tokens.colors.border, padding: 16 },
  metricLabel: { color: tokens.colors.textMuted, fontSize: 13 },
  metricValue: { color: tokens.colors.primary, fontSize: 24, fontWeight: '800' },
  syncCard: { backgroundColor: '#EEF7ED', borderRadius: 16, borderWidth: 1, borderColor: '#CFE7CC', padding: 16, gap: 4 },
  syncTitle: { fontWeight: '800', color: '#204A22' },
  syncText: { color: '#3A6140', lineHeight: 20 },
  actionsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  filterRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { backgroundColor: '#EEF4FF', paddingHorizontal: 12, paddingVertical: 8, borderRadius: 999, color: tokens.colors.text, fontWeight: '700' },
  errorBox: { borderRadius: 14, padding: 14, backgroundColor: '#FFF4F2', borderWidth: 1, borderColor: '#FFD8D2', gap: 4 },
  errorTitle: { fontWeight: '800', color: '#8B2B18' },
  errorBody: { color: '#8B2B18', lineHeight: 20 },
});
