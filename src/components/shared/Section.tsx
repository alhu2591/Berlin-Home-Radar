import type { ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { tokens } from '@/design/tokens';

export function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>{title}</Text>
      <View>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 10 },
  title: { fontWeight: '800', color: tokens.colors.text, fontSize: 16 },
});
