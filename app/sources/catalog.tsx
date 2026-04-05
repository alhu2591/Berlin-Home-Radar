import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { tokens } from '@/design/tokens';
import { sourceCatalog } from '@/core/sourceCatalog';
import { useSourceStore } from '@/stores/sourceStore';

export default function SourceCatalogScreen() {
  const sources = useSourceStore((state) => state.sources);
  const addCatalogSource = useSourceStore((state) => state.addCatalogSource);

  function isAdded(adapterId: string) {
    return sources.some((item) => item.adapterId === adapterId);
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Berlin source catalog" subtitle={`${sourceCatalog.length} Berlin-focused source templates across public housing, portals, student housing, room shares, furnished stays, and custom feeds.`} rightAction={<Button size="sm" label="Back" onPress={() => router.back()} />} />
        {sourceCatalog.map((entry) => {
          const existing = sources.find((item) => item.adapterId === entry.id);
          return (
            <View key={entry.id} style={styles.card}>
              <View style={styles.topRow}>
                <View style={styles.copy}>
                  <Text style={styles.title}>{entry.label}</Text>
                  <Text style={styles.meta}>Berlin · {entry.category} · {entry.status}</Text>
                </View>
                <Text style={[styles.badge, existing ? styles.badgeAdded : styles.badgeIdle]}>{existing ? (existing.enabled ? 'added' : 'disabled') : 'new'}</Text>
              </View>
              <Text style={styles.description}>{entry.description}</Text>
              <Text style={styles.meta}>Default city: Berlin · Demo: {entry.supportsDemo ? 'yes' : 'no'} · Worker: {entry.supportsWorker ? 'yes' : 'no'}</Text>
              <Text style={styles.meta}>Strategy: {entry.workerStrategyHint} · Worker path: {entry.workerPath}</Text>
              <View style={styles.actions}>
                <Button size="sm" label={isAdded(entry.id) ? 'Enable / re-add' : 'Quick add'} onPress={() => addCatalogSource(entry.id)} />
                {existing ? <Button size="sm" label="Details" variant="ghost" onPress={() => router.push(`/sources/${existing.id}`)} /> : null}
              </View>
            </View>
          );
        })}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  card: { backgroundColor: '#fff', borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 18, padding: 16, gap: 8 },
  topRow: { flexDirection: 'row', justifyContent: 'space-between', gap: 10 },
  copy: { flex: 1, gap: 4 },
  title: { fontSize: 16, fontWeight: '800', color: tokens.colors.text },
  description: { color: tokens.colors.text, lineHeight: 21 },
  meta: { color: tokens.colors.textMuted, lineHeight: 20 },
  actions: { flexDirection: 'row', gap: 8, flexWrap: 'wrap', marginTop: 4 },
  badge: { borderRadius: 999, paddingHorizontal: 10, paddingVertical: 6, fontWeight: '700', fontSize: 12 },
  badgeAdded: { backgroundColor: '#EEF7EE', color: '#0F7B0F' },
  badgeIdle: { backgroundColor: '#F4F6F8', color: tokens.colors.textMuted },
});
