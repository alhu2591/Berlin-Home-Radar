import { SafeAreaView, ScrollView, StyleSheet, Text } from 'react-native';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { tokens } from '@/design/tokens';

export default function AboutScreen() {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="About HomeSync" subtitle="Versioned scaffold for a multilingual local-first real estate aggregator." />
        <Section title="Version">
          <Text style={styles.copy}>v1.2.0 scaffold · Expo Router · Zustand · SQLite · local analytics · alerts · runtime connectors · backup/restore</Text>
        </Section>
        <Section title="What this build includes">
          <Text style={styles.copy}>Unified listing feed, filters, compare, source manager, local cache, saved searches, alert history, privacy page, analytics insights, worker-ready runtime config, and JSON backup/restore.</Text>
        </Section>
        <Section title="Delivery note">
          <Text style={styles.copy}>This package is designed as a strong scaffold that can be connected to Cloudflare Workers or first-party APIs next. Demo adapters remain in place so the product flow works immediately, while each source can now be switched toward worker-backed execution.</Text>
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  copy: { color: tokens.colors.text, lineHeight: 22 },
});
