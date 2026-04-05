import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';
import { tokens } from '@/design/tokens';

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger';
type Size = 'sm' | 'md' | 'lg';

const variantStyles = {
  primary: 'primary',
  secondary: 'secondary',
  ghost: 'ghost',
  danger: 'danger',
} as const;

const labelStyles = {
  primary: 'label_primary',
  secondary: 'label_secondary',
  ghost: 'label_ghost',
  danger: 'label_danger',
} as const;

const sizeStyles = {
  sm: 'size_sm',
  md: 'size_md',
  lg: 'size_lg',
} as const;

export function Button({
  label,
  onPress,
  variant = 'primary',
  size = 'md',
  loading,
  disabled,
  fullWidth,
}: {
  label: string;
  onPress: () => void;
  variant?: Variant;
  size?: Size;
  loading?: boolean;
  disabled?: boolean;
  fullWidth?: boolean;
}) {
  const isDisabled = disabled || loading;
  return (
    <Pressable
      onPress={onPress}
      disabled={isDisabled}
      style={[styles.base, styles[variantStyles[variant]], styles[sizeStyles[size]], fullWidth && styles.fullWidth, isDisabled && styles.disabled]}
    >
      {loading ? <ActivityIndicator color={variant === 'primary' ? '#fff' : tokens.colors.primary} /> : <Text style={[styles.label, styles[labelStyles[variant]]]}>{label}</Text>}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: { alignItems: 'center', justifyContent: 'center', borderRadius: tokens.radius.lg, minHeight: 44, paddingHorizontal: 14 },
  primary: { backgroundColor: tokens.colors.primary },
  secondary: { backgroundColor: tokens.colors.primarySoft, borderWidth: 1, borderColor: '#A8C7FF' },
  ghost: { backgroundColor: 'transparent', borderWidth: 1, borderColor: tokens.colors.border },
  danger: { backgroundColor: tokens.colors.danger },
  size_sm: { paddingVertical: 8 },
  size_md: { paddingVertical: 12 },
  size_lg: { paddingVertical: 16 },
  label: { fontWeight: '700' },
  label_primary: { color: '#fff' },
  label_secondary: { color: tokens.colors.primary },
  label_ghost: { color: tokens.colors.text },
  label_danger: { color: '#fff' },
  disabled: { opacity: 0.6 },
  fullWidth: { width: '100%' },
});
