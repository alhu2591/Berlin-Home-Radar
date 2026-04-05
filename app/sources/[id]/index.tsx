import { useState } from 'react';
import { Linking, SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { EmptyState } from '@/components/shared/EmptyState';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { testSourceConnection } from '@/core/adapters';
import { RemoteSourceService } from '@/services/RemoteSourceService';
import { RuntimeSecretsService } from '@/services/RuntimeSecretsService';
import { formatDateTime } from '@/core/format';
import { getSourceCatalogEntry } from '@/core/sourceCatalog';
import { tokens } from '@/design/tokens';
import { refreshAllListings } from '@/hooks/useBootstrap';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import { useListingStore } from '@/stores/listingStore';
import { useSourceStore } from '@/stores/sourceStore';
import { useRuntimeStore } from '@/stores/runtimeStore';
import type { WorkerSourceDiagnosis, WorkerSourceProbe } from '@/types/worker';

export default function SourceDetailScreen() {
  const params = useLocalSearchParams<{ id: string }>();
  const source = useSourceStore((state) => state.sources.find((item) => item.id === params.id));
  const listings = useListingStore((state) => state.listings.filter((item) => item.sourceId === source?.adapterId));
  const [testResult, setTestResult] = useState<string | null>(null);
  const [diagnosis, setDiagnosis] = useState<WorkerSourceDiagnosis | null>(null);
  const [probe, setProbe] = useState<WorkerSourceProbe | null>(null);
  const runtime = useRuntimeStore();
  const catalogEntry = source ? getSourceCatalogEntry(source.adapterId) : null;

  if (!source) {
    return <SafeAreaView style={styles.container}><EmptyState title="Source not found" description="The source may have been removed." /></SafeAreaView>;
  }

  async function runTest() {
    const result = await testSourceConnection(source);
    useAnalyticsStore.getState().track('source_test');
    setTestResult(result.ok ? `${result.mode.toUpperCase()} · ${result.listingCount} listings · ${result.sampleTitle ?? 'No sample title'} · ${result.latencyMs} ms` : 'No listings returned');
  }

  async function runDiagnosis() {
    const token = await RuntimeSecretsService.getWorkerToken();
    const payload = await RemoteSourceService.diagnoseSource(source, runtime, token);
    setDiagnosis(payload);
  }

  async function runProbe() {
    const token = await RuntimeSecretsService.getWorkerToken();
    const payload = await RemoteSourceService.probeSource(source, runtime, token);
    setProbe(payload);
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title={source.name} subtitle={`${source.adapterId} · ${source.enabled ? 'Enabled' : 'Disabled'}`} rightAction={<Button size="sm" label="Edit" onPress={() => router.push(`/sources/${source.id}/edit`)} />} />
        {catalogEntry ? (
          <View style={styles.card}>
            <Text style={styles.title}>Catalog metadata</Text>
            <Text style={styles.meta}>{catalogEntry.market} · {catalogEntry.category} · {catalogEntry.status}</Text>
            <Text style={styles.meta}>{catalogEntry.description}</Text>
            <Text style={styles.meta}>City scope: {catalogEntry.cities.join(', ')}</Text>
            <Text style={styles.meta}>Worker strategy hint: {catalogEntry.workerStrategyHint}</Text>
            <Text style={styles.meta}>Demo support: {catalogEntry.supportsDemo ? 'yes' : 'no'} · Worker support: {catalogEntry.supportsWorker ? 'yes' : 'no'}</Text>
            {catalogEntry.docsUrl ? <Button label="Open docs" variant="ghost" onPress={() => void Linking.openURL(catalogEntry.docsUrl!)} fullWidth /> : null}
          </View>
        ) : null}
        <View style={styles.card}>
          <Text style={styles.title}>Health</Text>
          <Text style={styles.meta}>Status: {source.health.status}</Text>
          <Text style={styles.meta}>Latency: {source.health.latencyMs ?? 0} ms</Text>
          <Text style={styles.meta}>Listings last seen: {source.health.listingCount ?? 0}</Text>
          <Text style={styles.meta}>Last success: {formatDateTime(source.health.lastSuccessAt ?? null)}</Text>
          <Text style={styles.meta}>Last error: {source.health.lastErrorMessage ?? 'None'}</Text>
          <View style={styles.actions}>
            <Button label="Refresh Berlin sources" variant="secondary" onPress={() => void refreshAllListings()} fullWidth />
            <Button label="Test this source" variant="ghost" onPress={() => void runTest()} fullWidth />
            <Button label="Diagnose live fetch plan" variant="ghost" onPress={() => void runDiagnosis()} fullWidth />
            <Button label="Probe live search page" variant="ghost" onPress={() => void runProbe()} fullWidth />
          </View>
          {testResult ? <Text style={styles.testNote}>{testResult}</Text> : null}
        </View>
        {diagnosis ? (
          <View style={styles.card}>
            <Text style={styles.title}>Live diagnosis</Text>
            <Text style={styles.meta}>Resolved strategy: {diagnosis.plan.strategyResolved}</Text>
            <Text style={styles.meta}>Requested strategy: {diagnosis.plan.strategyRequested}</Text>
            <Text style={styles.meta}>District aliases: {diagnosis.plan.districtAliases.join(', ') || '—'}</Text>
            <Text style={styles.meta}>Official API configured: {diagnosis.plan.officialApiConfigured ? 'yes' : 'no'} · RSS configured: {diagnosis.plan.rssConfigured ? 'yes' : 'no'}</Text>
            <Text style={styles.meta}>Fetch URLs:</Text>
            {diagnosis.plan.publicUrls.map((item) => <Text key={item} style={styles.meta}>• {item}</Text>)}
            {diagnosis.plan.notes.map((item) => <Text key={item} style={styles.meta}>• {item}</Text>)}
          </View>
        ) : null}

        {probe ? (
          <View style={styles.card}>
            <Text style={styles.title}>Live probe</Text>
            <Text style={styles.meta}>Resolved strategy: {probe.strategyResolved}</Text>
            {probe.urls.length === 0 ? <Text style={styles.meta}>No public URL probe available for this strategy.</Text> : null}
            {probe.urls.map((item) => (
              <View key={item.url} style={styles.probeBlock}>
                <Text style={styles.meta}>URL: {item.url}</Text>
                <Text style={styles.meta}>Parsed list cards: {item.parsedListCount} · JSON-LD: {item.jsonLdCount} · Snippets: {item.snippetCount}</Text>
                <Text style={styles.meta}>Candidate detail links: {item.candidateDetailUrls.length} · HTML bytes: {item.htmlBytes}</Text>
                <Text style={styles.meta}>Detail previews fetched: {item.detailFetchedCount} · Known detail parsers: {item.detailKnownCount} · Detail JSON-LD: {item.detailJsonLdCount} · Detail heuristics: {item.detailHeuristicCount}</Text>
                {item.sampleTitles.length > 0 ? <Text style={styles.meta}>Sample parsed titles: {item.sampleTitles.join(' · ')}</Text> : null}
                {item.candidateDetailUrls.slice(0, 5).map((candidate) => <Text key={candidate} style={styles.meta}>• {candidate}</Text>)}
              </View>
            ))}
          </View>
        ) : null}

        <View style={styles.card}>
          <Text style={styles.title}>Config</Text>
          {Object.entries(source.config).map(([key, value]) => <Text key={key} style={styles.meta}>{key}: {value}</Text>)}
        </View>
        <View style={styles.card}>
          <Text style={styles.title}>Recent normalized listings</Text>
          {listings.length === 0 ? <Text style={styles.meta}>No cached listings yet for this source.</Text> : listings.slice(0, 5).map((listing) => <Text key={listing.id} style={styles.meta}>• {listing.title}</Text>)}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 6 },
  title: { fontWeight: '800', fontSize: 16, color: tokens.colors.text },
  meta: { color: tokens.colors.textMuted, lineHeight: 21 },
  actions: { gap: 10, marginTop: 8 },
  testNote: { color: tokens.colors.primary, fontWeight: '700', lineHeight: 20, marginTop: 8 },
  probeBlock: { gap: 4, paddingTop: 6, borderTopWidth: 1, borderTopColor: tokens.colors.border, marginTop: 6 },
});
