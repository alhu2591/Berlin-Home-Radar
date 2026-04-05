import { useMemo, useState } from 'react';
import { ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { tokens } from '@/design/tokens';
import type { SourceDraft } from '@/types/source';
import { Button } from '@/components/shared/Button';
import { buildDraftFromCatalog, getCatalogByMarket, getSourceCatalogEntry } from '@/core/sourceCatalog';

const defaults: SourceDraft = buildDraftFromCatalog('immoscout-de');
const markets = ['DE'] as const;

export function SourceForm({ initialValue, onSubmit }: { initialValue?: SourceDraft; onSubmit: (payload: SourceDraft) => void }) {
  const [draft, setDraft] = useState<SourceDraft>(initialValue ?? defaults);
  const [market, setMarket] = useState<(typeof markets)[number]>('DE');
  const entries = useMemo(() => getCatalogByMarket(market), [market]);
  const selectedEntry = getSourceCatalogEntry(draft.adapterId);
  const isRss = draft.adapterId === 'local-agency-rss-de';
  const isWg = draft.adapterId === 'wg-gesucht-de';
  const supportsHouseToggle = selectedEntry?.category === 'portal' && !isRss && !isWg;

  function applyAdapter(adapterId: string) {
    const previousTemplate = buildDraftFromCatalog(draft.adapterId);
    const next = buildDraftFromCatalog(adapterId);
    setDraft((state) => ({
      ...state,
      adapterId: next.adapterId,
      city: 'Berlin',
      country: 'DE',
      listingType: next.listingType,
      maxResults: next.maxResults,
      executionMode: next.executionMode,
      workerPath: next.workerPath,
      fetchStrategy: next.fetchStrategy,
      district: next.district,
      customSearchUrl: next.customSearchUrl,
      rssUrl: next.rssUrl,
      includeHouses: next.includeHouses,
      wgMode: next.wgMode,
      name: state.name === previousTemplate.name || state.name === defaults.name ? next.name : state.name,
    }));
  }

  return (
    <ScrollView contentContainerStyle={styles.content}>
      <View style={styles.card}>
        <Text style={styles.label}>Coverage</Text>
        <View style={styles.row}>
          {markets.map((value) => (
            <Button key={value} size="sm" label="Berlin" variant={market === value ? 'primary' : 'secondary'} onPress={() => setMarket(value)} />
          ))}
        </View>
        <Text style={styles.label}>Name</Text>
        <TextInput value={draft.name} onChangeText={(value) => setDraft((state) => ({ ...state, name: value }))} style={styles.input} />
        <Text style={styles.label}>Adapter</Text>
        <View style={styles.row}>
          {entries.map((adapter) => (
            <Button
              key={adapter.id}
              size="sm"
              label={adapter.label}
              variant={draft.adapterId === adapter.id ? 'primary' : 'secondary'}
              onPress={() => applyAdapter(adapter.id)}
            />
          ))}
        </View>
        {selectedEntry ? <Text style={styles.helper}>{selectedEntry.description}</Text> : null}
        <Text style={styles.label}>City</Text>
        <TextInput value={draft.city} editable={false} style={styles.input} />
        <Text style={styles.label}>District filter</Text>
        <TextInput value={draft.district ?? ''} onChangeText={(value) => setDraft((state) => ({ ...state, district: value }))} style={styles.input} placeholder="Optional: Mitte, Neukolln, Prenzlauer Berg..." />
        <Text style={styles.label}>Listing type</Text>
        <View style={styles.row}>
          {['rent', 'buy', 'both'].map((value) => (
            <Button key={value} size="sm" label={value} variant={draft.listingType === value ? 'primary' : 'secondary'} onPress={() => setDraft((state) => ({ ...state, listingType: value as SourceDraft['listingType'] }))} />
          ))}
        </View>
        <Text style={styles.label}>Execution mode</Text>
        <View style={styles.row}>
          {['auto', 'demo', 'worker'].map((value) => (
            <Button key={value} size="sm" label={value} variant={draft.executionMode === value ? 'primary' : 'secondary'} onPress={() => setDraft((state) => ({ ...state, executionMode: value as SourceDraft['executionMode'] }))} />
          ))}
        </View>
        <Text style={styles.label}>Worker fetch strategy</Text>
        <View style={styles.row}>
          {(['auto', 'official_api', 'public_html', 'rss'] as const).map((value) => (
            <Button key={value} size="sm" label={value} variant={(draft.fetchStrategy ?? 'auto') === value ? 'primary' : 'secondary'} onPress={() => setDraft((state) => ({ ...state, fetchStrategy: value }))} />
          ))}
        </View>
        {!isRss ? (
          <>
            <Text style={styles.label}>Custom search URL</Text>
            <TextInput value={draft.customSearchUrl ?? ''} onChangeText={(value) => setDraft((state) => ({ ...state, customSearchUrl: value }))} style={styles.input} placeholder="Optional override for the worker search URL" autoCapitalize="none" />
          </>
        ) : null}
        {isRss ? (
          <>
            <Text style={styles.label}>RSS / Atom feed URL(s)</Text>
            <TextInput value={draft.rssUrl ?? ''} onChangeText={(value) => setDraft((state) => ({ ...state, rssUrl: value }))} style={styles.input} placeholder="https://agency.example/feed.xml, https://agency.example/berlin.atom" autoCapitalize="none" />
          </>
        ) : null}
        {supportsHouseToggle ? (
          <>
            <Text style={styles.label}>Include houses</Text>
            <View style={styles.row}>
              {(['yes', 'no'] as const).map((value) => (
                <Button key={value} size="sm" label={value} variant={(draft.includeHouses ?? 'no') === value ? 'primary' : 'secondary'} onPress={() => setDraft((state) => ({ ...state, includeHouses: value }))} />
              ))}
            </View>
          </>
        ) : null}
        {isWg ? (
          <>
            <Text style={styles.label}>WG-Gesucht mode</Text>
            <View style={styles.row}>
              {(['rooms', 'apartments', 'both'] as const).map((value) => (
                <Button key={value} size="sm" label={value} variant={(draft.wgMode ?? 'both') === value ? 'primary' : 'secondary'} onPress={() => setDraft((state) => ({ ...state, wgMode: value }))} />
              ))}
            </View>
          </>
        ) : null}
        <Text style={styles.label}>Worker path</Text>
        <TextInput value={draft.workerPath} onChangeText={(value) => setDraft((state) => ({ ...state, workerPath: value }))} style={styles.input} placeholder="/sources/fetch" autoCapitalize="none" />
        <Text style={styles.label}>Max results</Text>
        <TextInput value={draft.maxResults} onChangeText={(value) => setDraft((state) => ({ ...state, maxResults: value }))} keyboardType="numeric" style={styles.input} />
        <Text style={styles.label}>Country</Text>
        <TextInput value={draft.country} editable={false} style={styles.input} />
      </View>
      <Button label="Save source" onPress={() => onSubmit({ ...draft, city: 'Berlin', country: 'DE' })} fullWidth />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: tokens.spacing.lg, gap: tokens.spacing.md, paddingBottom: 120 },
  card: { backgroundColor: '#fff', borderRadius: 18, borderWidth: 1, borderColor: tokens.colors.border, padding: 16, gap: 10 },
  label: { fontWeight: '700', color: tokens.colors.text },
  helper: { color: tokens.colors.textMuted, lineHeight: 20 },
  input: { backgroundColor: '#F7FAFC', borderWidth: 1, borderColor: tokens.colors.border, borderRadius: 12, paddingHorizontal: 14, paddingVertical: 12 },
  row: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
});
