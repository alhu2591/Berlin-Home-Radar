import { SafeAreaView, ScrollView, StyleSheet, View } from 'react-native';
import { router } from 'expo-router';
import { Button } from '@/components/shared/Button';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { SourceCard } from '@/components/sources/SourceCard';
import { tokens } from '@/design/tokens';
import { useSourceStore } from '@/stores/sourceStore';

export default function SourcesScreen() {
  const sources = useSourceStore((state) => state.sources);
  const restoreCatalogDefaults = useSourceStore((state) => state.restoreCatalogDefaults);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ScreenHeader
          title="Berlin source manager"
          subtitle="Enable, disable, inspect, and edit Berlin listing sources only."
          rightAction={
            <View style={styles.headerActions}>
              <Button size="sm" label="Catalog" variant="secondary" onPress={() => router.push('/sources/catalog')} />
              <Button size="sm" label="Add" onPress={() => router.push('/sources/add')} />
            </View>
          }
        />
        <Button label="Restore Berlin defaults" variant="ghost" onPress={() => restoreCatalogDefaults()} fullWidth />
        {sources.map((source) => <SourceCard key={source.id} source={source} />)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.background },
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  headerActions: { flexDirection: 'row', gap: 8 },
});
