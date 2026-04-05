import { create } from 'zustand';
import { kv } from '@/core/kv';
import { defaultSources } from '@/core/demo';
import { buildDraftFromCatalog, getSourceCatalogEntry, sourceCatalog } from '@/core/sourceCatalog';
import type { Source, SourceDraft, SourceHealth } from '@/types/source';

interface SourceStore {
  sources: Source[];
  hydrate: () => void;
  addSource: (draft: SourceDraft) => void;
  addCatalogSource: (adapterId: string) => void;
  restoreCatalogDefaults: () => void;
  updateSource: (id: string, draft: SourceDraft) => void;
  toggleSource: (id: string) => void;
  removeSource: (id: string) => void;
  setHealthStatus: (id: string, patch: SourceHealth) => void;
}

function persist(sources: Source[]) {
  kv.setObject('source-store', sources);
}

function buildConfigFromDraft(draft: SourceDraft, adapterId: string) {
  const entry = getSourceCatalogEntry(adapterId);
  return {
    ...(entry?.defaultConfig ?? {}),
    city: entry?.defaultCity ?? 'Berlin',
    listingType: draft.listingType,
    maxResults: draft.maxResults,
    executionMode: draft.executionMode,
    workerPath: draft.workerPath || '/sources/fetch',
    ...(draft.fetchStrategy ? { fetchStrategy: draft.fetchStrategy } : {}),
    ...(draft.district ? { district: draft.district.trim() } : {}),
    ...(draft.customSearchUrl ? { customSearchUrl: draft.customSearchUrl.trim() } : {}),
    ...(draft.rssUrl ? { rssUrl: draft.rssUrl.trim() } : {}),
    ...(draft.includeHouses ? { includeHouses: draft.includeHouses } : {}),
    ...(draft.wgMode ? { wgMode: draft.wgMode } : {}),
  };
}

function clampToBerlin(source: Source): Source {
  const entry = getSourceCatalogEntry(source.adapterId);
  const defaultCity = entry?.defaultCity ?? 'Berlin';
  return {
    ...source,
    country: entry?.country ?? 'DE',
    config: {
      ...(entry?.defaultConfig ?? {}),
      ...source.config,
      city: defaultCity,
      workerPath: source.config.workerPath ?? '/sources/fetch',
      executionMode: source.config.executionMode ?? 'auto',
      fetchStrategy: source.config.fetchStrategy ?? entry?.workerStrategyHint ?? 'auto',
      includeHouses: source.config.includeHouses ?? entry?.defaultConfig.includeHouses ?? 'no',
      wgMode: source.config.wgMode ?? entry?.defaultConfig.wgMode ?? 'rooms',
      district: source.config.district ?? '',
      customSearchUrl: source.config.customSearchUrl ?? entry?.defaultConfig.customSearchUrl ?? '',
      rssUrl: source.config.rssUrl ?? entry?.defaultConfig.rssUrl ?? '',
    },
  };
}

function buildSource(draft: SourceDraft, priority: number): Source {
  return clampToBerlin({
    id: `source-${Date.now()}-${Math.round(Math.random() * 1000)}`,
    name: draft.name,
    adapterId: draft.adapterId,
    country: draft.country,
    enabled: true,
    priority,
    config: buildConfigFromDraft(draft, draft.adapterId),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    health: { status: 'idle' },
  });
}

function normalizeSources(sources: Source[]) {
  return sources
    .filter((item) => !!getSourceCatalogEntry(item.adapterId))
    .map((item, index) => clampToBerlin({
      ...item,
      priority: item.priority ?? index + 1,
    }));
}

function makeUniqueName(baseName: string, sources: Source[]) {
  const used = new Set(sources.map((item) => item.name));
  if (!used.has(baseName)) return baseName;
  let counter = 2;
  while (used.has(`${baseName} ${counter}`)) counter += 1;
  return `${baseName} ${counter}`;
}

export const useSourceStore = create<SourceStore>((set) => ({
  sources: defaultSources,
  hydrate: () => {
    const saved = kv.getObject<Source[]>('source-store');
    if (saved && saved.length) {
      const normalized = normalizeSources(saved);
      set({ sources: normalized.length ? normalized : defaultSources });
      persist(normalized.length ? normalized : defaultSources);
    } else persist(defaultSources);
  },
  addSource: (draft) =>
    set((state) => {
      const sources = [...state.sources, buildSource({ ...draft, city: 'Berlin', country: 'DE', name: makeUniqueName(draft.name, state.sources) }, state.sources.length + 1)];
      persist(sources);
      return { sources };
    }),
  addCatalogSource: (adapterId) =>
    set((state) => {
      const template = sourceCatalog.find((item) => item.id === adapterId);
      if (!template) return state;
      const existing = state.sources.find((item) => item.adapterId === adapterId && item.config.city === template.defaultCity);
      if (existing) {
        const sources = state.sources.map((item) => item.id === existing.id ? { ...item, enabled: true, updatedAt: new Date().toISOString() } : item);
        persist(sources);
        return { sources };
      }
      const draft = buildDraftFromCatalog(adapterId);
      const name = makeUniqueName(draft.name, state.sources);
      const sources = [...state.sources, buildSource({ ...draft, name }, state.sources.length + 1)];
      persist(sources);
      return { sources };
    }),
  restoreCatalogDefaults: () => set(() => {
    persist(defaultSources);
    return { sources: defaultSources };
  }),
  updateSource: (id, draft) =>
    set((state) => {
      const sources = state.sources.map((item) => item.id === id ? clampToBerlin({
        ...item,
        name: draft.name,
        adapterId: draft.adapterId,
        country: 'DE',
        config: buildConfigFromDraft({ ...draft, city: 'Berlin', country: 'DE' }, draft.adapterId),
        updatedAt: new Date().toISOString(),
      }) : item);
      persist(sources);
      return { sources };
    }),
  toggleSource: (id) =>
    set((state) => {
      const sources = state.sources.map((item) => item.id === id ? { ...item, enabled: !item.enabled, updatedAt: new Date().toISOString() } : item);
      persist(sources);
      return { sources };
    }),
  removeSource: (id) =>
    set((state) => {
      const sources = state.sources.filter((item) => item.id !== id).map((item, index) => ({ ...item, priority: index + 1 }));
      persist(sources);
      return { sources };
    }),
  setHealthStatus: (id, patch) =>
    set((state) => {
      const sources = state.sources.map((item) => item.id === id ? { ...item, health: { ...item.health, ...patch } } : item);
      persist(sources);
      return { sources };
    }),
}));
