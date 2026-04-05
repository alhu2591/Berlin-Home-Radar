import { useEffect, useState } from 'react';
import { Alert, SafeAreaView, ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { CacheService } from '@/core/cache';
import { formatDateTime } from '@/core/format';
import { tokens } from '@/design/tokens';
import { refreshAllListings } from '@/hooks/useBootstrap';
import { useAlertStore } from '@/stores/alertStore';
import { useAnalyticsStore } from '@/stores/analyticsStore';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoritesStore } from '@/stores/favoritesStore';
import { useFilterStore } from '@/stores/filterStore';
import { usePreferencesStore } from '@/stores/preferencesStore';
import { useRuntimeStore } from '@/stores/runtimeStore';
import type { CacheStats } from '@/types/sync';

export default function SettingsScreen() {
  const locale = usePreferencesStore((state) => state.locale);
  const themeMode = usePreferencesStore((state) => state.themeMode);
  const analyticsEnabled = usePreferencesStore((state) => state.analyticsEnabled);
  const setLocale = usePreferencesStore((state) => state.setLocale);
  const setThemeMode = usePreferencesStore((state) => state.setThemeMode);
  const setAnalyticsEnabled = usePreferencesStore((state) => state.setAnalyticsEnabled);
  const resetOnboarding = usePreferencesStore((state) => state.resetOnboarding);
  const clearHistory = useAlertStore((state) => state.clearHistory);
  const resetAnalytics = useAnalyticsStore((state) => state.reset);
  const clearDismissed = useFavoritesStore((state) => state.clearDismissed);
  const resetCompare = useCompareStore((state) => state.clear);
  const resetFilter = useFilterStore((state) => state.resetFilter);
  const runtime = useRuntimeStore((state) => ({ mode: state.mode, workerBaseUrl: state.workerBaseUrl }));
  const [stats, setStats] = useState<CacheStats | null>(null);

  useEffect(() => {
    void CacheService.getStats().then(setStats);
  }, []);

  async function clearAppData() {
    await CacheService.clearAll();
    clearHistory();
    clearDismissed();
    resetCompare();
    resetFilter();
    resetAnalytics();
    setStats(await CacheService.getStats());
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Settings" subtitle="Preferences, storage, diagnostics, runtime, and legal pages." />
        <Section title="Language">
          <View style={styles.row}>
            {['en', 'de', 'ar'].map((value) => <Button key={value} size="sm" variant={locale === value ? 'primary' : 'secondary'} label={value.toUpperCase()} onPress={() => setLocale(value as 'en' | 'de' | 'ar')} />)}
          </View>
        </Section>
        <Section title="Theme">
          <View style={styles.row}>
            {['system', 'light', 'dark'].map((value) => <Button key={value} size="sm" variant={themeMode === value ? 'primary' : 'secondary'} label={value} onPress={() => setThemeMode(value as 'system' | 'light' | 'dark')} />)}
          </View>
        </Section>
        <Section title="Privacy-respecting analytics">
          <View style={styles.switchRow}>
            <View style={styles.switchCopy}>
              <Text style={styles.switchTitle}>Enable local analytics</Text>
              <Text style={styles.switchBody}>Tracks counts like refreshes and listing opens only on-device.</Text>
            </View>
            <Switch value={analyticsEnabled} onValueChange={setAnalyticsEnabled} />
          </View>
          <Button label="Open insights" variant="secondary" onPress={() => router.push('/settings/insights')} fullWidth />
          <Button label="Open diagnostics" variant="ghost" onPress={() => router.push('/settings/diagnostics')} fullWidth />
        </Section>
        <Section title="Runtime & connectors">
          <Text style={styles.copy}>Mode: {runtime.mode}</Text>
          <Text style={styles.copy}>Worker: {runtime.workerBaseUrl || 'Not configured'}</Text>
          <View style={styles.actions}>
            <Button label="Runtime & worker settings" variant="secondary" onPress={() => router.push('/settings/runtime')} fullWidth />
            <Button label="Backup & restore" variant="ghost" onPress={() => router.push('/settings/backup')} fullWidth />
          </View>
        </Section>
        <Section title="Data & cache">
          <Text style={styles.copy}>Listings cached: {stats?.listingCount ?? '—'}</Text>
          <Text style={styles.copy}>Approx size: {stats?.approximateSizeKb ?? '—'} KB</Text>
          <Text style={styles.copy}>Last sync: {formatDateTime(stats?.lastSyncAt ?? null)}</Text>
          <View style={styles.actions}>
            <Button label="Open source manager" variant="secondary" onPress={() => router.push('/sources')} fullWidth />
            <Button label="Source health" variant="ghost" onPress={() => router.push('/settings/source-health')} fullWidth />
            <Button label="Refresh adapters" variant="secondary" onPress={() => void refreshAllListings().then(async () => setStats(await CacheService.getStats()))} fullWidth />
            <Button label="Clear cached listings" variant="ghost" onPress={() => void CacheService.clearListings().then(async () => setStats(await CacheService.getStats()))} fullWidth />
            <Button label="Reset cache & diagnostics" variant="ghost" onPress={() => Alert.alert('Reset local data', 'This clears cached listings, analytics counters, compare queue, dismissed items, and alert history.', [{ text: 'Cancel', style: 'cancel' }, { text: 'Reset', style: 'destructive', onPress: () => { void clearAppData(); } }])} fullWidth />
          </View>
        </Section>
        <Section title="About & legal">
          <View style={styles.actions}>
            <Button label="About HomeSync" variant="secondary" onPress={() => router.push('/settings/about')} fullWidth />
            <Button label="Privacy & source attribution" variant="secondary" onPress={() => router.push('/settings/privacy')} fullWidth />
          </View>
        </Section>
        <Section title="App flow">
          <Button label="Re-run onboarding" variant="secondary" onPress={() => { resetOnboarding(); router.replace('/language-select'); }} fullWidth />
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  row: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  actions: { gap: 10 },
  copy: { color: tokens.colors.textMuted, lineHeight: 22 },
  switchRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 12 },
  switchCopy: { flex: 1, gap: 4 },
  switchTitle: { color: tokens.colors.text, fontWeight: '800' },
  switchBody: { color: tokens.colors.textMuted, lineHeight: 20 },
});
