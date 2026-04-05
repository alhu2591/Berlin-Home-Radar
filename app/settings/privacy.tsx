import { SafeAreaView, ScrollView, StyleSheet, Text } from 'react-native';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { Section } from '@/components/shared/Section';
import { tokens } from '@/design/tokens';

export default function PrivacyScreen() {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader title="Privacy & source attribution" subtitle="Local-first behavior and attribution guidance for aggregated listing data." />
        <Section title="Local storage only">
          <Text style={styles.copy}>Favorites, filters, dismissed items, alerts, and analytics counters stay on the device in local storage. This scaffold does not send personal data to a backend.</Text>
        </Section>
        <Section title="Source attribution">
          <Text style={styles.copy}>Every listing keeps its origin adapter and direct source link. Duplicate groups preserve alternate source URLs so attribution remains transparent.</Text>
        </Section>
        <Section title="Legal caution">
          <Text style={styles.copy}>Before connecting real third-party data sources, review platform terms, robots policies, attribution requirements, and any market-specific restrictions. Prefer officially allowed feeds or consented edge proxies.</Text>
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
