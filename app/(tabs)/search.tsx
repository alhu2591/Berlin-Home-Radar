import { Pressable, SafeAreaView, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { ListingCard } from '@/components/listings/ListingCard';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { tokens } from '@/design/tokens';
import { useFilteredListings } from '@/hooks/useFilteredListings';
import { useFilterStore } from '@/stores/filterStore';
import { useSourceStore } from '@/stores/sourceStore';
import type { PropertyType } from '@/types/listing';

const propertyTypes: PropertyType[] = ['apartment', 'house', 'room', 'studio', 'other'];

export default function SearchScreen() {
  const filter = useFilterStore((state) => state.filter);
  const setFilter = useFilterStore((state) => state.setFilter);
  const resetFilter = useFilterStore((state) => state.resetFilter);
  const { listings, count } = useFilteredListings();
  const sources = useSourceStore((state) => state.sources.filter((item) => item.enabled));

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Search" subtitle="Filter locally against the cached Berlin feed." />
        <View style={styles.panel}>
          <Text style={styles.label}>Keywords</Text>
          <TextInput value={filter.query} onChangeText={(value) => setFilter({ query: value })} placeholder="Berlin, furnished, balcony..." style={styles.input} />
          <Text style={styles.label}>City</Text>
          <TextInput value={filter.city ?? ''} onChangeText={(value) => setFilter({ city: value || null })} placeholder="Berlin" style={styles.input} />
          <View style={styles.row}>
            <View style={styles.flexOne}>
              <Text style={styles.label}>Min price</Text>
              <TextInput value={filter.priceMin ? String(filter.priceMin) : ''} onChangeText={(value) => setFilter({ priceMin: value ? Number(value) : null })} style={styles.input} keyboardType="numeric" />
            </View>
            <View style={styles.flexOne}>
              <Text style={styles.label}>Max price</Text>
              <TextInput value={filter.priceMax ? String(filter.priceMax) : ''} onChangeText={(value) => setFilter({ priceMax: value ? Number(value) : null })} style={styles.input} keyboardType="numeric" />
            </View>
          </View>
          <View style={styles.row}>
            <View style={styles.flexOne}>
              <Text style={styles.label}>Min rooms</Text>
              <TextInput value={filter.roomsMin ? String(filter.roomsMin) : ''} onChangeText={(value) => setFilter({ roomsMin: value ? Number(value) : null })} style={styles.input} keyboardType="numeric" />
            </View>
            <View style={styles.flexOne}>
              <Text style={styles.label}>Listing type</Text>
              <View style={styles.chips}>
                {['rent', 'buy'].map((value) => {
                  const active = filter.listingType === value;
                  return <Pressable key={value} onPress={() => setFilter({ listingType: active ? null : value as 'rent' | 'buy' })} style={[styles.chip, active && styles.chipActive]}><Text style={[styles.chipText, active && styles.chipTextActive]}>{value}</Text></Pressable>;
                })}
              </View>
            </View>
          </View>
          <Text style={styles.label}>Property type</Text>
          <View style={styles.chips}>
            {propertyTypes.map((value) => {
              const active = filter.propertyTypes.includes(value);
              return (
                <Pressable key={value} onPress={() => setFilter({ propertyTypes: active ? filter.propertyTypes.filter((item) => item !== value) : [...filter.propertyTypes, value] })} style={[styles.chip, active && styles.chipActive]}>
                  <Text style={[styles.chipText, active && styles.chipTextActive]}>{value}</Text>
                </Pressable>
              );
            })}
          </View>
          <Text style={styles.label}>Amenities</Text>
          <View style={styles.chips}>
            <Pressable onPress={() => setFilter({ furnished: filter.furnished ? null : true })} style={[styles.chip, filter.furnished && styles.chipActive]}>
              <Text style={[styles.chipText, filter.furnished && styles.chipTextActive]}>furnished</Text>
            </Pressable>
            <Pressable onPress={() => setFilter({ balcony: filter.balcony ? null : true })} style={[styles.chip, filter.balcony && styles.chipActive]}>
              <Text style={[styles.chipText, filter.balcony && styles.chipTextActive]}>balcony</Text>
            </Pressable>
            <Pressable onPress={() => setFilter({ parking: filter.parking ? null : true })} style={[styles.chip, filter.parking && styles.chipActive]}>
              <Text style={[styles.chipText, filter.parking && styles.chipTextActive]}>parking</Text>
            </Pressable>
            <Pressable onPress={() => setFilter({ petsAllowed: filter.petsAllowed ? null : true })} style={[styles.chip, filter.petsAllowed && styles.chipActive]}>
              <Text style={[styles.chipText, filter.petsAllowed && styles.chipTextActive]}>pets</Text>
            </Pressable>
          </View>
          <Text style={styles.label}>Sources</Text>
          <View style={styles.chips}>
            {sources.map((source) => {
              const active = filter.sourceIds.includes(source.adapterId);
              return (
                <Pressable key={source.id} onPress={() => setFilter({ sourceIds: active ? filter.sourceIds.filter((item) => item !== source.adapterId) : [...filter.sourceIds, source.adapterId] })} style={[styles.chip, active && styles.chipActive]}>
                  <Text style={[styles.chipText, active && styles.chipTextActive]}>{source.name}</Text>
                </Pressable>
              );
            })}
          </View>
          <Text style={styles.label}>Sort</Text>
          <View style={styles.chips}>
            {['relevance', 'newest', 'price_asc', 'price_desc', 'size_asc', 'size_desc'].map((value) => (
              <Pressable key={value} onPress={() => setFilter({ sortBy: value as typeof filter.sortBy })} style={[styles.chip, filter.sortBy === value && styles.chipActive]}>
                <Text style={[styles.chipText, filter.sortBy === value && styles.chipTextActive]}>{value}</Text>
              </Pressable>
            ))}
          </View>
          <Text style={styles.label}>Visibility</Text>
          <View style={styles.chips}>
            <Pressable onPress={() => setFilter({ showDismissed: !filter.showDismissed })} style={[styles.chip, filter.showDismissed && styles.chipActive]}>
              <Text style={[styles.chipText, filter.showDismissed && styles.chipTextActive]}>Show dismissed</Text>
            </Pressable>
          </View>
          <Pressable style={styles.reset} onPress={resetFilter}><Text style={styles.resetText}>Reset filters</Text></Pressable>
        </View>
        <Text style={styles.countText}>{count} results</Text>
        {listings.slice(0, 20).map((listing) => <ListingCard key={listing.id} listing={listing} compact />)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  panel: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 10 },
  label: { fontWeight: '700', color: tokens.colors.text },
  input: { backgroundColor: '#F7FAFC', borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 12, paddingHorizontal: 14, paddingVertical: 12 },
  row: { flexDirection: 'row', gap: 12 },
  flexOne: { flex: 1 },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { backgroundColor: '#F7FAFC', paddingHorizontal: 12, paddingVertical: 10, borderRadius: 999, borderWidth: 1, borderColor: tokens.colors.border },
  chipActive: { backgroundColor: tokens.colors.primary, borderColor: tokens.colors.primary },
  chipText: { color: tokens.colors.text, fontWeight: '600' },
  chipTextActive: { color: '#fff' },
  reset: { marginTop: 6, alignItems: 'center' },
  resetText: { color: tokens.colors.primary, fontWeight: '700' },
  countText: { color: tokens.colors.textMuted, fontWeight: '700' },
});
