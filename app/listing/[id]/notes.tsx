import { useMemo, useState } from 'react';
import { Alert, SafeAreaView, ScrollView, StyleSheet, Text, TextInput } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { Section } from '@/components/shared/Section';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { tokens } from '@/design/tokens';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useListingStore } from '@/stores/listingStore';

export default function ListingNotesScreen() {
  const params = useLocalSearchParams<{ id: string }>();
  const listing = useListingStore((state) => state.getListingById(params.id));
  const favorite = useFavoritesStore((state) => state.getFavorite(params.id));
  const updateFavoriteMeta = useFavoritesStore((state) => state.updateFavoriteMeta);
  const [note, setNote] = useState(favorite?.note ?? '');
  const [tags, setTags] = useState((favorite?.tags ?? []).join(', '));

  const normalizedTags = useMemo(
    () => tags.split(',').map((item) => item.trim()).filter(Boolean),
    [tags]
  );

  if (!listing || !favorite) {
    return (
      <SafeAreaView style={styles.container}>
        <ScrollView contentContainerStyle={styles.content}>
          <ScreenHeader title="Saved note" subtitle="Save the listing first to attach notes and tags." />
          <Button label="Go back" variant="secondary" onPress={() => router.back()} fullWidth />
        </ScrollView>
      </SafeAreaView>
    );
  }

  function onSave() {
    updateFavoriteMeta(listing.id, { note: note.trim() || null, tags: normalizedTags });
    Alert.alert('Saved', 'Your private note and tags were updated locally on this device.');
    router.back();
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Saved note" subtitle={listing.title} />
        <Section title="Private note">
          <TextInput
            multiline
            value={note}
            onChangeText={setNote}
            placeholder="Good fit for work relocation, near U-Bahn, ask about pets..."
            style={styles.textArea}
            textAlignVertical="top"
          />
        </Section>
        <Section title="Tags">
          <Text style={styles.helper}>Comma-separated tags such as: family, furnished, top-priority</Text>
          <TextInput
            value={tags}
            onChangeText={setTags}
            placeholder="furnished, balcony, shortlist"
            style={styles.input}
          />
          {normalizedTags.length ? <Text style={styles.preview}>Preview: {normalizedTags.join(' • ')}</Text> : null}
        </Section>
        <Button label="Save note" onPress={onSave} fullWidth />
        <Button label="Back" variant="secondary" onPress={() => router.back()} fullWidth />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  input: { borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 14, backgroundColor: '#fff', paddingHorizontal: 14, paddingVertical: 12 },
  textArea: { minHeight: 140, borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 14, backgroundColor: '#fff', paddingHorizontal: 14, paddingVertical: 12 },
  helper: { color: tokens.colors.textMuted, lineHeight: 20, marginBottom: 10 },
  preview: { color: tokens.colors.text, fontWeight: '700', marginTop: 10 },
});
