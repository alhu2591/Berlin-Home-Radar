import { SafeAreaView, ScrollView, StyleSheet } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { EmptyState } from '@/components/shared/EmptyState';
import { ListingCard } from '@/components/listings/ListingCard';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { applyFilters } from '@/core/search';
import { tokens } from '@/design/tokens';
import { useAlertStore } from '@/stores/alertStore';
import { useListingStore } from '@/stores/listingStore';

export default function AlertResultsScreen() {
  const params = useLocalSearchParams<{ id: string }>();
  const alert = useAlertStore((state) => state.alerts.find((item) => item.id === params.id));
  const listings = useListingStore((state) => state.listings);

  if (!alert) {
    return <SafeAreaView style={styles.container}><EmptyState title="Alert not found" description="The saved alert may have been deleted." /></SafeAreaView>;
  }

  const matched = applyFilters(listings, { ...alert.filter, showDismissed: true });

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title={alert.name} subtitle={`${matched.length} current matches`} />
        {matched.length === 0 ? <EmptyState title="No current matches" description="Try widening the filter or enabling more sources." /> : matched.map((listing) => <ListingCard key={listing.id} listing={listing} compact={false} />)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
});
