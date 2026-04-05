import { Alert, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { tokens } from '@/design/tokens';
import { Button } from '@/components/shared/Button';
import type { Source } from '@/types/source';
import { getSourceCatalogEntry } from '@/core/sourceCatalog';
import { useSourceStore } from '@/stores/sourceStore';

export function SourceCard({ source }: { source: Source }) {
  const toggleSource = useSourceStore((state) => state.toggleSource);
  const removeSource = useSourceStore((state) => state.removeSource);
  const entry = getSourceCatalogEntry(source.adapterId);

  return (
    <View style={styles.card}>
      <View style={styles.row}>
        <View style={styles.copy}>
          <Text style={styles.title}>{source.name}</Text>
          <Text style={styles.meta}>{entry?.market ?? source.country} · {source.adapterId} · {source.config.city ?? 'Berlin'} · {source.config.executionMode ?? 'auto'} · priority {source.priority}</Text>
          {entry ? <Text style={styles.meta}>{entry.category} · {entry.status}</Text> : null}
        </View>
        <Text style={[styles.badge, source.enabled ? styles.badgeOn : styles.badgeOff]}>{source.health.status}</Text>
      </View>
      <View style={styles.row}>
        <Button size="sm" label={source.enabled ? 'Disable' : 'Enable'} variant="secondary" onPress={() => toggleSource(source.id)} />
        <Button size="sm" label="Details" variant="ghost" onPress={() => router.push(`/sources/${source.id}`)} />
        <Button
          size="sm"
          label="Delete"
          variant="ghost"
          onPress={() =>
            Alert.alert('Delete source', 'Remove this source?', [
              { text: 'Cancel', style: 'cancel' },
              { text: 'Delete', style: 'destructive', onPress: () => removeSource(source.id) },
            ])
          }
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 12 },
  row: { flexDirection: 'row', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 },
  copy: { flex: 1, gap: 4 },
  title: { fontWeight: '800', color: tokens.colors.text, fontSize: 16 },
  meta: { color: tokens.colors.textMuted, lineHeight: 20 },
  badge: { paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999, fontSize: 12, fontWeight: '700' },
  badgeOn: { backgroundColor: '#EEF4FF', color: tokens.colors.primary },
  badgeOff: { backgroundColor: '#F2F4F7', color: tokens.colors.textMuted },
});
