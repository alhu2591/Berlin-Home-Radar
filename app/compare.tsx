import { ScrollView, SafeAreaView, StyleSheet, Text, View } from 'react-native';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { EmptyState } from '@/components/shared/EmptyState';
import { formatArea, formatPrice } from '@/core/format';
import { tokens } from '@/design/tokens';
import { useCompareStore } from '@/stores/compareStore';
import { useListingStore } from '@/stores/listingStore';

const rows = [
  ['Price', (v: any) => formatPrice(v.price, v.priceCurrency)],
  ['Price/m²', (v: any) => (v.pricePerSqm ? `€${v.pricePerSqm}` : '—')],
  ['City', (v: any) => v.city],
  ['Rooms', (v: any) => (v.rooms ? String(v.rooms) : '—')],
  ['Size', (v: any) => formatArea(v.sizeSqm)],
  ['Source', (v: any) => v.sourceId],
  ['Freshness', (v: any) => `${v.freshnessScore}/100`],
  ['Trust', (v: any) => `${v.trustScore}/100`],
];

export default function CompareScreen() {
  const selectedIds = useCompareStore((state) => state.selectedIds);
  const clear = useCompareStore((state) => state.clear);
  const listings = useListingStore((state) => state.listings.filter((item) => selectedIds.includes(item.id)));

  return (
    <SafeAreaView style={styles.container}>
      <ScreenHeader title="Compare listings" subtitle="Compare up to four listings side by side." rightAction={listings.length > 0 ? <Button size="sm" label="Clear" variant="ghost" onPress={clear} /> : undefined} />
      {listings.length === 0 ? (
        <EmptyState title="Nothing to compare yet" description="Add items from the feed or the detail screen." />
      ) : (
        <ScrollView horizontal contentContainerStyle={styles.scrollContent}>
          <View style={styles.table}>
            <View style={styles.headerRow}>
              <Text style={[styles.headerCell, styles.labelCell]}>Field</Text>
              {listings.map((listing) => (
                <Text key={listing.id} style={styles.headerCell}>{listing.title}</Text>
              ))}
            </View>
            {rows.map(([label, formatter]) => {
              const values = listings.map((listing) => formatter(listing));
              const differs = new Set(values).size > 1;
              return (
                <View key={label} style={styles.row}>
                  <Text style={[styles.cell, styles.labelCell]}>{label}</Text>
                  {listings.map((listing) => (
                    <Text key={`${label}-${listing.id}`} style={[styles.cell, differs && styles.diffCell]}>{formatter(listing)}</Text>
                  ))}
                </View>
              );
            })}
          </View>
        </ScrollView>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background, padding: tokens.spacing.lg, gap: tokens.spacing.md },
  scrollContent: { paddingBottom: 24 },
  table: { borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 16, overflow: 'hidden' },
  headerRow: { flexDirection: 'row', backgroundColor: '#E8F1FF' },
  row: { flexDirection: 'row', borderTopWidth: 1, borderColor: tokens.colors.border },
  headerCell: { width: 180, padding: 14, fontWeight: '700', color: tokens.colors.text },
  cell: { width: 180, padding: 14, color: tokens.colors.text, lineHeight: 20, backgroundColor: '#fff' },
  labelCell: { width: 120, backgroundColor: '#F7FAFC', fontWeight: '700' },
  diffCell: { backgroundColor: '#FFF8E1' },
});
