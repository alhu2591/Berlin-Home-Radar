import { Pressable, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { tokens } from '@/design/tokens';
import { Button } from '@/components/shared/Button';
import { formatPrice, formatRelativeDays } from '@/core/format';
import type { NormalizedListing } from '@/types/listing';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoritesStore } from '@/stores/favoritesStore';

export function ListingCard({ listing, compact = false }: { listing: NormalizedListing; compact?: boolean }) {
  const toggleFavorite = useFavoritesStore((state) => state.toggleFavorite);
  const markViewed = useFavoritesStore((state) => state.markViewed);
  const dismiss = useFavoritesStore((state) => state.dismiss);
  const restoreDismissed = useFavoritesStore((state) => state.restoreDismissed);
  const dismissed = useFavoritesStore((state) => state.dismissedIds.includes(listing.id));
  const toggleCompare = useCompareStore((state) => state.toggle);
  const isFavorite = useFavoritesStore((state) => state.isFavorite(listing.id));
  const compareSelected = useCompareStore((state) => state.selectedIds.includes(listing.id));

  return (
    <Pressable style={[styles.card, dismissed && styles.cardDismissed]} onPress={() => { markViewed(listing.id); router.push(`/listing/${listing.id}`); }}>
      <View style={styles.topRow}>
        <Text style={styles.badge}>{listing.sourceId}</Text>
        <Text style={styles.badge}>{formatRelativeDays(listing.listedAt)}</Text>
      </View>
      <Text style={styles.title}>{listing.title}</Text>
      <Text style={styles.price}>{formatPrice(listing.price, listing.priceCurrency)}</Text>
      <Text style={styles.meta}>{listing.city}{listing.district ? ` · ${listing.district}` : ''} · {listing.rooms ? `${listing.rooms} rooms` : '—'} · {listing.sizeSqm ? `${listing.sizeSqm} m²` : '—'}</Text>
      {!compact && listing.description ? <Text style={styles.description}>{listing.description}</Text> : null}
      {!compact ? <Text style={styles.trust}>Trust {listing.trustScore}/100 · Freshness {listing.freshnessScore}/100</Text> : null}
      <View style={styles.actions}>
        <Button size="sm" label={isFavorite ? 'Saved' : 'Save'} variant={isFavorite ? 'secondary' : 'primary'} onPress={() => toggleFavorite(listing.id)} />
        <Button size="sm" label={compareSelected ? 'In compare' : 'Compare'} variant="secondary" onPress={() => toggleCompare(listing.id)} />
        <Button size="sm" label={dismissed ? 'Undo dismiss' : 'Dismiss'} variant="ghost" onPress={() => dismissed ? restoreDismissed(listing.id) : dismiss(listing.id)} />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 10 },
  cardDismissed: { opacity: 0.78 },
  topRow: { flexDirection: 'row', justifyContent: 'space-between', gap: 8, flexWrap: 'wrap' },
  badge: { backgroundColor: '#EEF4FF', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999, fontSize: 12, fontWeight: '700', color: tokens.colors.text },
  title: { fontWeight: '800', fontSize: 16, color: tokens.colors.text },
  price: { fontWeight: '900', fontSize: 24, color: tokens.colors.primary },
  meta: { color: tokens.colors.textMuted, lineHeight: 21 },
  description: { color: tokens.colors.text, lineHeight: 22 },
  trust: { color: tokens.colors.textMuted, fontSize: 12, fontWeight: '700' },
  actions: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
});
