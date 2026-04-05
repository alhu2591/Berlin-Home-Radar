import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { formatDateTime } from '@/core/format';
import { tokens } from '@/design/tokens';
import { getSourceCatalogEntry } from '@/core/sourceCatalog';
import { useRuntimeStore } from '@/stores/runtimeStore';
import { useSourceStore } from '@/stores/sourceStore';

function statusColor(status: 'idle' | 'ok' | 'error' | 'testing') {
  switch (status) {
    case 'ok': return '#1E7F37';
    case 'error': return '#B42318';
    case 'testing': return '#B26A00';
    default: return '#667085';
  }
}

export default function SourceHealthScreen() {
  const sources = useSourceStore((state) => state.sources);
  const runtimeMode = useRuntimeStore((state) => state.mode);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Source health" subtitle="Per-source status, latency, execution mode, and most recent errors." />
        <Section title="Adapters">
          <Text style={styles.runtimeBanner}>Runtime mode: {runtimeMode}</Text>
          {sources.map((source) => {
            const catalog = getSourceCatalogEntry(source.adapterId);
            return (
            <View key={source.id} style={styles.card}>
              <View style={styles.row}>
                <Text style={styles.title}>{source.name}</Text>
                <Text style={[styles.badge, { color: statusColor(source.health.status) }]}>{source.health.status}</Text>
              </View>
              <Text style={styles.copy}>Adapter: {source.adapterId}</Text>
              <Text style={styles.copy}>Category: {catalog?.category ?? 'unknown'} · Strategy hint: {catalog?.workerStrategyHint ?? 'auto'}</Text>
              <Text style={styles.copy}>Enabled: {source.enabled ? 'Yes' : 'No'}</Text>
              <Text style={styles.copy}>Execution: {source.config.executionMode ?? 'auto'} · Path: {source.config.workerPath ?? '/sources/fetch'}</Text>
              <Text style={styles.copy}>Latency: {source.health.latencyMs ?? '—'} ms</Text>
              <Text style={styles.copy}>Listings seen: {source.health.listingCount ?? '—'}</Text>
              <Text style={styles.copy}>Last success: {formatDateTime(source.health.lastSuccessAt ?? null)}</Text>
              {catalog?.docsUrl ? <Text style={styles.copy}>Docs: {catalog.docsUrl}</Text> : null}
              {source.health.lastErrorMessage ? <Text style={styles.error}>Last error: {source.health.lastErrorMessage}</Text> : null}
            </View>
          )})}
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  runtimeBanner: { color: tokens.colors.primary, fontWeight: '800', marginBottom: 10 },
  card: { paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: tokens.colors.border, gap: 4 },
  row: { flexDirection: 'row', justifyContent: 'space-between', gap: 10 },
  title: { color: tokens.colors.text, fontWeight: '800' },
  badge: { fontWeight: '800', textTransform: 'uppercase' },
  copy: { color: tokens.colors.textMuted, lineHeight: 20 },
  error: { color: '#B42318', lineHeight: 20 },
});
