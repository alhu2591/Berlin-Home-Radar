import { Linking, SafeAreaView, ScrollView, Share, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { EmptyState } from '@/components/shared/EmptyState';
import { formatArea, formatDateTime, formatPrice } from '@/core/format';
import { tokens } from '@/design/tokens';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useListingStore } from '@/stores/listingStore';

export default function ListingDetailScreen() {
  const params = useLocalSearchParams<{ id: string }>();
  const listing = useListingStore((state) => state.getListingById(params.id));
  const listings = useListingStore((state) => state.listings);
  const toggleFavorite = useFavoritesStore((state) => state.toggleFavorite);
  const favorite = useFavoritesStore((state) => state.getFavorite(params.id));
  const isFavorite = useFavoritesStore((state) => state.isFavorite(params.id));
  const toggleCompare = useCompareStore((state) => state.toggle);
  const compareSelected = useCompareStore((state) => state.selectedIds.includes(params.id));

  if (!listing) {
    return (
      <SafeAreaView style={styles.container}>
        <EmptyState title="Listing not found" description="The listing may have been removed from the local cache." />
      </SafeAreaView>
    );
  }

  const similar = listings
    .filter((item) => item.id !== listing.id && item.city === listing.city && item.propertyType === listing.propertyType)
    .slice(0, 3);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title={listing.title} subtitle={`${listing.city} · ${listing.sourceId}`} />
        <View style={styles.priceCard}>
          <Text style={styles.price}>{formatPrice(listing.price, listing.priceCurrency)}</Text>
          <Text style={styles.meta}>{formatArea(listing.sizeSqm)} · {listing.rooms ? `${listing.rooms} rooms` : '—'} · {listing.pricePerSqm ? `€${listing.pricePerSqm}/m²` : '—'}</Text>
        </View>
        <View style={styles.actionsRow}>
          <Button label={isFavorite ? 'Saved' : 'Save'} onPress={() => toggleFavorite(listing.id)} />
          <Button label={compareSelected ? 'In compare' : 'Compare'} variant="secondary" onPress={() => toggleCompare(listing.id)} />
          <Button label="Open source" variant="ghost" onPress={() => void Linking.openURL(listing.sourceUrl)} />
          <Button label="Share" variant="ghost" onPress={() => void Share.share({ message: `${listing.title}
${listing.city}
${listing.sourceUrl}` })} />
        </View>
        {isFavorite ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Saved notes</Text>
            <Text style={styles.paragraph}>{favorite?.note?.trim() ? favorite.note : 'No private note saved yet.'}</Text>
            {favorite?.tags?.length ? <Text style={styles.paragraph}>Tags: {favorite.tags.join(', ')}</Text> : null}
            <Button label="Edit note & tags" variant="secondary" onPress={() => router.push(`/listing/${listing.id}/notes`)} fullWidth />
          </View>
        ) : null}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Description</Text>
          <Text style={styles.paragraph}>{listing.description ?? 'No description available.'}</Text>
        </View>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Attributes</Text>
          {[
            ['City', listing.city],
            ['District', listing.district ?? '—'],
            ['Type', listing.propertyType],
            ['Rooms', listing.rooms ? String(listing.rooms) : '—'],
            ['Size', formatArea(listing.sizeSqm)],
            ['Balcony', listing.balcony ? 'Yes' : 'No'],
            ['Parking', listing.parking ? 'Yes' : 'No'],
            ['Furnished', listing.furnished ? 'Yes' : 'No'],
            ['Listed', formatDateTime(listing.listedAt)],
          ].map(([label, value]) => <Text key={label} style={styles.paragraph}>{label}: {value}</Text>)}
        </View>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Trust and freshness</Text>
          <Text style={styles.paragraph}>Freshness score: {listing.freshnessScore}/100</Text>
          <Text style={styles.paragraph}>Completeness score: {listing.completenessScore}/100</Text>
          <Text style={styles.paragraph}>Trust score: {listing.trustScore}/100</Text>
          {listing.suspiciousReasons.length > 0 ? <Text style={styles.warning}>Potential issues: {listing.suspiciousReasons.join(', ')}</Text> : null}
        </View>
        {listing.alternateSourceUrls.length > 0 ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Also seen on</Text>
            {listing.alternateSourceUrls.map((item) => (
              <Button key={`${item.sourceId}-${item.url}`} label={item.sourceId} variant="secondary" onPress={() => void Linking.openURL(item.url)} fullWidth />
            ))}
          </View>
        ) : null}
        {similar.length > 0 ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Similar listings</Text>
            {similar.map((item) => (
              <Text key={item.id} style={styles.linkLike} onPress={() => router.push(`/listing/${item.id}`)}>
                • {item.title}
              </Text>
            ))}
          </View>
        ) : null}
        <Button label="Back to feed" variant="secondary" onPress={() => router.back()} fullWidth />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  priceCard: { backgroundColor: '#E8F1FF', borderRadius: 18, padding: 16, gap: 4 },
  price: { fontSize: 28, fontWeight: '900', color: tokens.colors.primary },
  meta: { color: '#31507A' },
  actionsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  section: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 8 },
  sectionTitle: { fontWeight: '800', color: tokens.colors.text, fontSize: 16 },
  paragraph: { color: tokens.colors.text, lineHeight: 22 },
  warning: { color: '#B55400', fontWeight: '700' },
  linkLike: { color: tokens.colors.primary, fontWeight: '700', lineHeight: 22 },
});
