import { SafeAreaView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { tokens } from '@/design/tokens';

export default function NotFoundScreen() {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.title}>Page not found</Text>
        <Text style={styles.subtitle}>The route does not exist yet. Go back to the Berlin Home Radar dashboard.</Text>
        <Button label="Go home" onPress={() => router.replace('/(tabs)')} fullWidth />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: tokens.colors.background, padding: tokens.spacing.lg },
  card: { width: '100%', backgroundColor: tokens.colors.surface, borderRadius: tokens.radius.xl, padding: tokens.spacing.xl, gap: tokens.spacing.md, borderWidth: 1, borderColor: tokens.colors.border },
  title: { fontSize: 24, fontWeight: '800', color: tokens.colors.text },
  subtitle: { color: tokens.colors.textMuted, lineHeight: 22 },
});
