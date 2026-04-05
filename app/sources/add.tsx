import { SafeAreaView, StyleSheet } from 'react-native';
import { router } from 'expo-router';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { SourceForm } from '@/components/sources/SourceForm';
import { tokens } from '@/design/tokens';
import { useSourceStore } from '@/stores/sourceStore';

export default function AddSourceScreen() {
  const addSource = useSourceStore((state) => state.addSource);
  return (
    <SafeAreaView style={styles.container}>
      <ScreenHeader title="Add source" subtitle="Create a new adapter configuration." />
      <SourceForm onSubmit={(payload) => { addSource(payload); router.replace('/sources'); }} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({ container: { flex: 1, backgroundColor: tokens.colors.background } });
