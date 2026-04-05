import { SafeAreaView, StyleSheet, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { tokens } from '@/design/tokens';
import { usePreferencesStore } from '@/stores/preferencesStore';

export default function LanguageSelectScreen() {
  const locale = usePreferencesStore((state) => state.locale);
  const setLocale = usePreferencesStore((state) => state.setLocale);

  return (
    <SafeAreaView style={styles.container}>
      <ScreenHeader title="Choose language" subtitle="Pick the app language before the first run." />
      <View style={styles.content}>
        {[
          ['en', 'English'],
          ['de', 'Deutsch'],
          ['ar', 'العربية'],
        ].map(([value, label]) => (
          <Button key={value} label={locale === value ? `${label} ✓` : label} variant={locale === value ? 'primary' : 'secondary'} onPress={() => setLocale(value as 'en' | 'de' | 'ar')} fullWidth />
        ))}
        <Button label="Continue" onPress={() => router.replace('/onboarding')} fullWidth />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background, padding: tokens.spacing.lg, gap: tokens.spacing.lg },
  content: { gap: tokens.spacing.md },
});
