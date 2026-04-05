import { StyleSheet, Text, View } from 'react-native';
import { tokens } from '@/design/tokens';

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.description}>{description}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 18, gap: 8, alignItems: 'center' },
  title: { fontWeight: '800', color: tokens.colors.text, fontSize: 16 },
  description: { color: tokens.colors.textMuted, textAlign: 'center', lineHeight: 22 },
});
