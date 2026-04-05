import { Pressable, SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { EmptyState } from '@/components/shared/EmptyState';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { formatDateTime } from '@/core/format';
import { tokens } from '@/design/tokens';
import { useAlertStore } from '@/stores/alertStore';
import { useFilterStore } from '@/stores/filterStore';

export default function AlertsScreen() {
  const alerts = useAlertStore((state) => state.alerts);
  const history = useAlertStore((state) => state.history);
  const toggleAlert = useAlertStore((state) => state.toggleAlert);
  const removeAlert = useAlertStore((state) => state.removeAlert);
  const addAlertFromFilter = useAlertStore((state) => state.addAlertFromFilter);
  const clearHistory = useAlertStore((state) => state.clearHistory);
  const filter = useFilterStore((state) => state.filter);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Alerts" subtitle="Saved searches, background-ready runs, and local history." rightAction={<Button size="sm" label="Save current filter" onPress={() => addAlertFromFilter(filter, 'daily')} />} />
        {alerts.length === 0 ? <EmptyState title="No alerts yet" description="Save your current filters to turn them into reusable alerts." /> : alerts.map((alert) => (
          <View key={alert.id} style={styles.card}>
            <View style={styles.row}>
              <View style={styles.copy}>
                <Text style={styles.title}>{alert.name}</Text>
                <Text style={styles.meta}>Frequency: {alert.frequency} · Matches: {alert.matchCount}</Text>
                <Text style={styles.meta}>Last run: {formatDateTime(alert.lastRunAt)}</Text>
              </View>
              <Text style={[styles.badge, alert.enabled ? styles.badgeOn : styles.badgeOff]}>{alert.enabled ? 'On' : 'Off'}</Text>
            </View>
            <View style={styles.row}>
              <Button size="sm" label={alert.enabled ? 'Disable' : 'Enable'} variant="secondary" onPress={() => toggleAlert(alert.id)} />
              <Button size="sm" label="Open results" variant="ghost" onPress={() => router.push(`/alerts/${alert.id}/results`)} />
              <Button size="sm" label="Delete" variant="ghost" onPress={() => removeAlert(alert.id)} />
            </View>
          </View>
        ))}

        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>History</Text>
          {history.length > 0 ? <Button size="sm" label="Clear" variant="ghost" onPress={clearHistory} /> : null}
        </View>
        {history.length === 0 ? <EmptyState title="No alert history" description="History entries appear here after sync finds matching listings." /> : history.slice(0, 10).map((item) => (
          <Pressable key={item.id} style={styles.historyCard} onPress={() => router.push(`/alerts/${item.alertId}/results`)}>
            <Text style={styles.historyTitle}>{item.alertName}</Text>
            <Text style={styles.historyMeta}>{item.matchCount} matches · {formatDateTime(item.timestamp)}</Text>
          </Pressable>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 10 },
  row: { flexDirection: 'row', justifyContent: 'space-between', gap: 8, flexWrap: 'wrap' },
  copy: { flex: 1, gap: 4 },
  title: { fontSize: 16, fontWeight: '800', color: tokens.colors.text },
  meta: { color: tokens.colors.textMuted, lineHeight: 20 },
  badge: { paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999, fontWeight: '700' },
  badgeOn: { backgroundColor: '#EEF7ED', color: '#2E7D32' },
  badgeOff: { backgroundColor: '#F3F5F8', color: tokens.colors.textMuted },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  sectionTitle: { fontSize: 18, fontWeight: '800', color: tokens.colors.text },
  historyCard: { backgroundColor: '#fff', borderRadius: 16, borderWidth: 1, borderColor: tokens.colors.border, padding: 14, gap: 4 },
  historyTitle: { fontWeight: '800', color: tokens.colors.text },
  historyMeta: { color: tokens.colors.textMuted },
});
