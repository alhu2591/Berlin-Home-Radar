import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { tokens } from '@/design/tokens';
import { useFilteredListings } from '@/hooks/useFilteredListings';

export default function MapScreen() {
  const { listings } = useFilteredListings();

  return (
    <SafeAreaView style={styles.container}>
      <ScreenHeader title="Map summary" subtitle="Berlin map summary, ready for a full map integration later." />
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.mapPlaceholder}>
          <Text style={styles.mapTitle}>Berlin MapLibre / react-native-maps target area</Text>
          <Text style={styles.mapSubtitle}>For now, HomeSync Berlin shows geocoded Berlin cards that can be replaced with a real clustered map.</Text>
        </View>
        {listings.filter((item) => item.lat && item.lng).slice(0, 20).map((listing) => (
          <View key={listing.id} style={styles.pinCard}>
            <Text style={styles.pinPrice}>{listing.price ? `€${listing.price}` : 'No price'}</Text>
            <Text style={styles.pinTitle}>{listing.title}</Text>
            <Text style={styles.pinMeta}>{listing.city} · {listing.lat?.toFixed(3)}, {listing.lng?.toFixed(3)}</Text>
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background, padding: tokens.spacing.lg, gap: tokens.spacing.md },
  content: { gap: tokens.spacing.md, paddingBottom: 120 },
  mapPlaceholder: { backgroundColor: '#DDEBFF', borderRadius: 20, padding: 18, gap: 8 },
  mapTitle: { fontSize: 18, fontWeight: '800', color: tokens.colors.text },
  mapSubtitle: { lineHeight: 21, color: '#31507A' },
  pinCard: { backgroundColor: '#fff', borderRadius: 16, padding: 16, borderWidth: 1, borderColor: tokens.colors.border, gap: 4 },
  pinPrice: { fontWeight: '800', color: tokens.colors.primary },
  pinTitle: { color: tokens.colors.text, fontWeight: '700' },
  pinMeta: { color: tokens.colors.textMuted },
});
