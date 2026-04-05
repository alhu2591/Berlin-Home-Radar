import { useEffect, useState } from 'react';
import { Alert, SafeAreaView, ScrollView, Share, StyleSheet, Text, View } from 'react-native';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { formatDateTime } from '@/core/format';
import { tokens } from '@/design/tokens';
import { DiagnosticsService } from '@/services/DiagnosticsService';
import type { DiagnosticsSnapshot } from '@/types/sync';

export default function DiagnosticsScreen() {
  const [snapshot, setSnapshot] = useState<DiagnosticsSnapshot | null>(null);

  async function load() {
    setSnapshot(await DiagnosticsService.buildSnapshot());
  }

  useEffect(() => {
    void load();
  }, []);

  async function exportJson() {
    try {
      const json = await DiagnosticsService.exportAsJson();
      await Share.share({ message: json });
    } catch (error) {
      Alert.alert('Export failed', error instanceof Error ? error.message : 'Unknown export error');
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Diagnostics" subtitle="Local snapshot of cache, sync history, runtime, and source health." />
        <Section title="Snapshot overview">
          <Text style={styles.copy}>Exported at: {formatDateTime(snapshot?.exportedAt ?? null)}</Text>
          <Text style={styles.copy}>Cached listings: {snapshot?.cacheStats.listingCount ?? '—'}</Text>
          <Text style={styles.copy}>Alerts: {snapshot?.alertCount ?? '—'}</Text>
          <Text style={styles.copy}>Favorites: {snapshot?.favoritesCount ?? '—'}</Text>
          <Text style={styles.copy}>Dismissed: {snapshot?.dismissedCount ?? '—'}</Text>
          <View style={styles.actions}>
            <Button label="Refresh snapshot" variant="secondary" onPress={() => void load()} fullWidth />
            <Button label="Share JSON snapshot" variant="ghost" onPress={() => void exportJson()} fullWidth />
          </View>
        </Section>
        <Section title="Runtime">
          <Text style={styles.copy}>Mode: {snapshot?.runtime.mode ?? '—'}</Text>
          <Text style={styles.copy}>Worker URL: {snapshot?.runtime.workerBaseUrl || 'Not configured'}</Text>
          <Text style={styles.copy}>Timeout: {snapshot?.runtime.requestTimeoutMs ?? '—'} ms</Text>
          <Text style={styles.copy}>Demo fallback: {snapshot?.runtime.allowDemoFallback ? 'Enabled' : 'Disabled'}</Text>
        </Section>
        <Section title="Recent sync runs">
          {snapshot?.syncHistory.length ? snapshot.syncHistory.map((item) => (
            <View key={item.id} style={styles.historyItem}>
              <Text style={styles.historyTitle}>{formatDateTime(item.finishedAt)}</Text>
              <Text style={styles.copy}>{item.dedupedListingCount} listings · {item.successfulSources}/{item.sourceCount} sources OK</Text>
              {item.issues.length ? <Text style={styles.warning}>Issues: {item.issues.map((issue) => `${issue.sourceId}: ${issue.message}`).join(' | ')}</Text> : null}
            </View>
          )) : <Text style={styles.copy}>No sync history yet.</Text>}
        </Section>
        <Section title="Analytics counters">
          {snapshot ? Object.entries(snapshot.analyticsCounters).map(([key, value]) => (
            <View key={key} style={styles.counterRow}>
              <Text style={styles.counterLabel}>{key}</Text>
              <Text style={styles.counterValue}>{value}</Text>
            </View>
          )) : <Text style={styles.copy}>Loading analytics…</Text>}
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  copy: { color: tokens.colors.textMuted, lineHeight: 22 },
  actions: { gap: 10, marginTop: 12 },
  historyItem: { paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: tokens.colors.border, gap: 4 },
  historyTitle: { color: tokens.colors.text, fontWeight: '800' },
  warning: { color: '#8B2B18', lineHeight: 20 },
  counterRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8 },
  counterLabel: { color: tokens.colors.text, fontWeight: '700' },
  counterValue: { color: tokens.colors.primary, fontWeight: '900' },
});
