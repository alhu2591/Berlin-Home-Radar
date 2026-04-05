import { SafeAreaView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { Section } from '@/components/shared/Section';
import { tokens } from '@/design/tokens';
import { usePreferencesStore } from '@/stores/preferencesStore';

const steps = [
  { title: 'One Berlin search, many sources', body: 'HomeSync Berlin collects apartment, room, and local agency listings for Berlin into one deduplicated feed.' },
  { title: 'Local-first by default', body: 'Listings, filters, favorites, and source settings live on the device and work offline.' },
  { title: 'Built for multilingual Berlin renters', body: 'The project stays prepared for Arabic, German, and English while staying tightly scoped to Berlin.' },
];

export default function OnboardingScreen() {
  const complete = usePreferencesStore((state) => state.completeOnboarding);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>Welcome to HomeSync Berlin</Text>
        <Text style={styles.subtitle}>A mobile-first Berlin housing aggregation workspace.</Text>
        {steps.map((step, index) => (
          <Section key={step.title} title={`${index + 1}. ${step.title}`}>
            <Text style={styles.stepText}>{step.body}</Text>
          </Section>
        ))}
        <Button label="Start exploring" onPress={() => { complete(); router.replace('/(tabs)'); }} fullWidth />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md },
  title: { fontSize: 30, fontWeight: '800', color: tokens.colors.text },
  subtitle: { color: tokens.colors.textMuted, lineHeight: 22 },
  stepText: { color: tokens.colors.text, lineHeight: 22 },
});
