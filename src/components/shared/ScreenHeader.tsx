import type { ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { tokens } from '@/design/tokens';

export function ScreenHeader({ title, subtitle, rightAction }: { title: string; subtitle?: string; rightAction?: ReactNode }) {
  return (
    <View style={styles.row}>
      <View style={styles.copy}>
        <Text style={styles.title}>{title}</Text>
        {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
      </View>
      {rightAction ? <View>{rightAction}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 },
  copy: { flex: 1, gap: 6 },
  title: { fontSize: 28, fontWeight: '800', color: tokens.colors.text },
  subtitle: { color: tokens.colors.textMuted, lineHeight: 21 },
});
