export interface SourceHealth {
  status: 'idle' | 'ok' | 'error' | 'testing';
  latencyMs?: number;
  listingCount?: number;
  parserFamily?: string;
  notes?: string;
  lastSuccessAt?: string;
  lastErrorAt?: string;
  lastErrorMessage?: string;
}

export interface Source {
  id: string;
  name: string;
  adapterId: string;
  country: string;
  enabled: boolean;
  priority: number;
  config: Record<string, string>;
  createdAt: string;
  updatedAt: string;
  health: SourceHealth;
}

export interface SourceDraft {
  name: string;
  adapterId: string;
  city: string;
  listingType: 'rent' | 'buy' | 'both';
  maxResults: string;
  country: string;
  executionMode: 'auto' | 'demo' | 'worker';
  workerPath: string;
  fetchStrategy?: 'auto' | 'official_api' | 'public_html' | 'rss';
  district?: string;
  customSearchUrl?: string;
  rssUrl?: string;
  includeHouses?: 'yes' | 'no';
  wgMode?: 'rooms' | 'apartments' | 'both';
}
