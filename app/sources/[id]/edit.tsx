import { SafeAreaView, StyleSheet } from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { EmptyState } from '@/components/shared/EmptyState';
import { ScreenHeader } from '@/components/shared/ScreenHeader';
import { SourceForm } from '@/components/sources/SourceForm';
import { tokens } from '@/design/tokens';
import { useSourceStore } from '@/stores/sourceStore';

export default function EditSourceScreen() {
  const params = useLocalSearchParams<{ id: string }>();
  const source = useSourceStore((state) => state.sources.find((item) => item.id === params.id));
  const updateSource = useSourceStore((state) => state.updateSource);

  if (!source) {
    return <SafeAreaView style={styles.container}><EmptyState title="Source not found" description="Cannot edit a source that does not exist." /></SafeAreaView>;
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScreenHeader title="Edit source" subtitle="Adjust adapter settings and save." />
      <SourceForm
        initialValue={{
          name: source.name,
          adapterId: source.adapterId,
          city: source.config.city ?? '',
          listingType: (source.config.listingType as 'rent' | 'buy' | 'both') ?? 'rent',
          maxResults: source.config.maxResults ?? '40',
          country: source.country,
          executionMode: (source.config.executionMode as 'auto' | 'demo' | 'worker') ?? 'auto',
          workerPath: source.config.workerPath ?? '/sources/fetch',
          fetchStrategy: (source.config.fetchStrategy as 'auto' | 'official_api' | 'public_html' | 'rss') ?? 'auto',
          district: source.config.district ?? '',
          customSearchUrl: source.config.customSearchUrl ?? '',
          rssUrl: source.config.rssUrl ?? '',
          includeHouses: (source.config.includeHouses as 'yes' | 'no') ?? 'no',
          wgMode: (source.config.wgMode as 'rooms' | 'apartments' | 'both') ?? 'both',
        }}
        onSubmit={(payload) => { updateSource(source.id, payload); router.replace(`/sources/${source.id}`); }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({ container: { flex: 1, backgroundColor: tokens.colors.background } });
