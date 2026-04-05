import { useEffect, useMemo, useState } from 'react';
import { Alert, SafeAreaView, ScrollView, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { tokens } from '@/design/tokens';
import { useRuntimeStore } from '@/stores/runtimeStore';
import { RuntimeSecretsService } from '@/services/RuntimeSecretsService';
import { RemoteSourceService } from '@/services/RemoteSourceService';

export default function RuntimeSettingsScreen() {
  const runtime = useRuntimeStore();
  const [token, setToken] = useState('');
  const [testing, setTesting] = useState(false);
  const [savedNote, setSavedNote] = useState<string | null>(null);

  useEffect(() => {
    void RuntimeSecretsService.getWorkerToken().then((value) => setToken(value ?? ''));
  }, []);

  const runtimeSummary = useMemo(() => {
    if (runtime.mode === 'demo') return 'Demo-only mode.';
    if (runtime.mode === 'worker_hybrid') return 'Worker first, with demo fallback enabled.';
    return 'Worker-only mode. Demo adapters are blocked.';
  }, [runtime.mode]);

  async function saveSecrets() {
    await RuntimeSecretsService.setWorkerToken(token.trim() || null);
    setSavedNote('Worker token saved locally in SecureStore.');
  }

  async function pingWorker() {
    try {
      setTesting(true);
      const result = await RemoteSourceService.ping(runtime, token.trim() || null);
      runtime.update({ lastValidatedAt: new Date().toISOString() });
      Alert.alert('Worker reachable', `Health endpoint responded in ${result.latencyMs} ms.`);
    } catch (error) {
      Alert.alert('Worker test failed', error instanceof Error ? error.message : 'Unknown worker error');
    } finally {
      setTesting(false);
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Runtime & connectors" subtitle="Switch between demo and worker-backed fetching, and save worker credentials." />
        <Section title="Fetch mode">
          <View style={styles.rowWrap}>
            {['demo', 'worker_hybrid', 'worker_only'].map((value) => (
              <Button key={value} size="sm" variant={runtime.mode === value ? 'primary' : 'secondary'} label={value} onPress={() => runtime.update({ mode: value as typeof runtime.mode })} />
            ))}
          </View>
          <Text style={styles.copy}>{runtimeSummary}</Text>
        </Section>
        <Section title="Worker endpoint">
          <Text style={styles.label}>Base URL</Text>
          <TextInput value={runtime.workerBaseUrl} onChangeText={(workerBaseUrl) => runtime.update({ workerBaseUrl })} autoCapitalize="none" autoCorrect={false} placeholder="https://your-worker.example.workers.dev" style={styles.input} />
          <Text style={styles.label}>Timeout (ms)</Text>
          <TextInput value={String(runtime.requestTimeoutMs)} onChangeText={(value) => runtime.update({ requestTimeoutMs: Number(value) || 8000 })} keyboardType="numeric" style={styles.input} />
          <Text style={styles.meta}>Last validated: {runtime.lastValidatedAt ?? 'Never'}</Text>
          <View style={styles.actions}>
            <Button label="Ping /health" variant="secondary" loading={testing} onPress={() => void pingWorker()} fullWidth />
          </View>
        </Section>
        <Section title="Worker auth token">
          <TextInput value={token} onChangeText={setToken} autoCapitalize="none" autoCorrect={false} secureTextEntry placeholder="Optional bearer token" style={styles.input} />
          <View style={styles.actions}>
            <Button label="Save token" variant="secondary" onPress={() => void saveSecrets()} fullWidth />
          </View>
          {savedNote ? <Text style={styles.success}>{savedNote}</Text> : null}
        </Section>
        <Section title="Fallback and diagnostics">
          <View style={styles.switchRow}>
            <View style={styles.switchCopy}>
              <Text style={styles.switchTitle}>Allow demo fallback</Text>
              <Text style={styles.copy}>Used when worker mode is active but the remote call fails.</Text>
            </View>
            <Switch value={runtime.allowDemoFallback} onValueChange={(allowDemoFallback) => runtime.update({ allowDemoFallback })} />
          </View>
          <View style={styles.switchRow}>
            <View style={styles.switchCopy}>
              <Text style={styles.switchTitle}>Debug network</Text>
              <Text style={styles.copy}>Persists extra runtime flags for future connector diagnostics.</Text>
            </View>
            <Switch value={runtime.debugNetwork} onValueChange={(debugNetwork) => runtime.update({ debugNetwork })} />
          </View>
          <Button label="Reset runtime config" variant="ghost" onPress={() => runtime.reset()} fullWidth />
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  rowWrap: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  input: { backgroundColor: '#fff', borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 12, paddingHorizontal: 14, paddingVertical: 12 },
  label: { fontWeight: '700', color: tokens.colors.text },
  meta: { color: tokens.colors.textMuted, lineHeight: 20 },
  copy: { color: tokens.colors.textMuted, lineHeight: 21 },
  actions: { gap: 10, marginTop: 12 },
  success: { color: tokens.colors.success, fontWeight: '700' },
  switchRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 12 },
  switchCopy: { flex: 1, gap: 4 },
  switchTitle: { color: tokens.colors.text, fontWeight: '800' },
});
