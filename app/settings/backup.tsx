import { useState } from 'react';
import { Alert, SafeAreaView, ScrollView, Share, StyleSheet, Text, TextInput, View } from 'react-native';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { tokens } from '@/design/tokens';
import { BackupService } from '@/services/BackupService';

export default function BackupScreen() {
  const [json, setJson] = useState('');
  const [loading, setLoading] = useState(false);

  async function exportBackup() {
    try {
      setLoading(true);
      const payload = await BackupService.exportJson();
      setJson(payload);
    } catch (error) {
      Alert.alert('Export failed', error instanceof Error ? error.message : 'Unknown backup error');
    } finally {
      setLoading(false);
    }
  }

  async function shareBackup() {
    try {
      if (!json.trim()) {
        const payload = await BackupService.exportJson();
        setJson(payload);
        await Share.share({ message: payload });
        return;
      }
      await Share.share({ message: json });
    } catch (error) {
      Alert.alert('Share failed', error instanceof Error ? error.message : 'Unknown share error');
    }
  }

  async function restoreBackup() {
    try {
      setLoading(true);
      await BackupService.restoreFromJson(json);
      Alert.alert('Restore complete', 'Local stores, sources, and runtime config were restored from this JSON backup.');
    } catch (error) {
      Alert.alert('Restore failed', error instanceof Error ? error.message : 'Unknown restore error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Backup & restore" subtitle="Copy or share local app state as JSON, then restore it on another device or build." />
        <Section title="Export">
          <Text style={styles.copy}>This includes preferences, saved alerts, favorites, sources, runtime config, and sync metadata. Cached listings themselves are not exported.</Text>
          <View style={styles.actions}>
            <Button label="Generate backup JSON" loading={loading} onPress={() => void exportBackup()} fullWidth />
            <Button label="Share backup JSON" variant="secondary" onPress={() => void shareBackup()} fullWidth />
          </View>
        </Section>
        <Section title="Backup JSON">
          <TextInput value={json} onChangeText={setJson} multiline textAlignVertical="top" autoCapitalize="none" autoCorrect={false} style={styles.editor} placeholder="Backup JSON will appear here or paste one to restore." />
        </Section>
        <Section title="Restore">
          <Text style={styles.warning}>Restoring overwrites local preferences, alerts, favorites, compare queue, analytics counters, source definitions, and runtime config.</Text>
          <Button label="Restore from pasted JSON" variant="ghost" loading={loading} onPress={() => void restoreBackup()} fullWidth />
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  copy: { color: tokens.colors.textMuted, lineHeight: 22 },
  warning: { color: tokens.colors.warning, lineHeight: 22 },
  actions: { gap: 10 },
  editor: { minHeight: 280, backgroundColor: '#fff', borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 12, paddingHorizontal: 14, paddingVertical: 12, fontFamily: 'monospace', color: tokens.colors.text },
});
