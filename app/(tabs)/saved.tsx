import { Pressable, SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useMemo, useState } from 'react';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { EmptyState } from '@/components/shared/EmptyState';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { ListingCard } from '@/components/listings/ListingCard';
import { tokens } from '@/design/tokens';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useListingStore } from '@/stores/listingStore';

export default function SavedScreen() {
  const [tab, setTab] = useState<'favorites' | 'recent' | 'dismissed'>('favorites');
  const favorites = useFavoritesStore((state) => state.favorites);
  const recentlyViewed = useFavoritesStore((state) => state.recentlyViewed);
  const dismissedIds = useFavoritesStore((state) => state.dismissedIds);
  const clearDismissed = useFavoritesStore((state) => state.clearDismissed);
  const listings = useListingStore((state) => state.listings);

  const currentIds = tab === 'favorites' ? favorites.map((item) => item.listingId) : tab === 'recent' ? recentlyViewed : dismissedIds;
  const currentListings = useMemo(() => listings.filter((item) => currentIds.includes(item.id)), [currentIds, listings]);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Saved" subtitle="Favorites, recently viewed, and dismissed listings." rightAction={tab === 'dismissed' && currentListings.length > 0 ? <Button size="sm" label="Clear" variant="ghost" onPress={clearDismissed} /> : undefined} />
        <View style={styles.segmentRow}>
          {['favorites', 'recent', 'dismissed'].map((value) => {
            const active = tab === value;
            return (
              <Pressable key={value} style={[styles.segment, active && styles.segmentActive]} onPress={() => setTab(value as typeof tab)}>
                <Text style={[styles.segmentText, active && styles.segmentTextActive]}>{value}</Text>
              </Pressable>
            );
          })}
        </View>
        <Text style={styles.count}>{currentListings.length} items</Text>
        {tab === 'favorites' && favorites.length > 0 ? (
          <View style={styles.notesCard}>
            <Text style={styles.notesTitle}>Saved notes & tags</Text>
            {favorites.slice(0, 4).map((favorite) => {
              const listing = listings.find((item) => item.id === favorite.listingId);
              if (!listing) return null;
              return (
                <Text key={favorite.listingId} style={styles.noteLine} onPress={() => router.push(`/listing/${favorite.listingId}/notes`)}>
                  • {listing.title}: {favorite.note?.trim() ? favorite.note : 'Add a note'}{favorite.tags.length ? ` [${favorite.tags.join(', ')}]` : ''}
                </Text>
              );
            })}
          </View>
        ) : null}
        {currentListings.length === 0 ? <EmptyState title={`No ${tab} listings`} description="Interact with the feed or detail screen to populate this list." /> : currentListings.map((listing) => <ListingCard key={listing.id} listing={listing} compact={false} />)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  segmentRow: { flexDirection: 'row', gap: 8 },
  segment: { flex: 1, paddingVertical: 12, alignItems: 'center', borderRadius: 12, backgroundColor: '#F0F4F8' },
  segmentActive: { backgroundColor: tokens.colors.primary },
  segmentText: { color: tokens.colors.text, fontWeight: '700' },
  segmentTextActive: { color: '#fff' },
  count: { color: tokens.colors.textMuted, fontWeight: '700' },
  notesCard: { backgroundColor: '#fff', borderRadius: 16, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 10 },
  notesTitle: { color: tokens.colors.text, fontWeight: '800' },
  noteLine: { color: tokens.colors.primary, lineHeight: 22 },
});
