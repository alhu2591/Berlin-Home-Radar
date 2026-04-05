export interface WorkerProfile {
  adapterId: string;
  label: string;
  inventoryFamily: string;
  parserFamily: string;
  supportsDistrictFilter: boolean;
  supportsOfficialApi: boolean;
  supportsRss: boolean;
  liveSearchUrls: string[];
  detailPatterns: string[];
  docsUrl?: string;
  notes: string;
}

export interface WorkerSourceDiagnosis {
  plan: {
    adapterId: string;
    strategyRequested: string;
    strategyResolved: 'rss' | 'official_api' | 'public_html';
    district: string | null;
    districtAliases: string[];
    publicUrls: string[];
    officialApiConfigured: boolean;
    rssConfigured: boolean;
    maxResults: number;
    notes: string[];
  };
  profile: WorkerProfile | null;
}


export interface WorkerSourceProbeUrl {
  url: string;
  candidateDetailUrls: string[];
  parsedListCount: number;
  jsonLdCount: number;
  snippetCount: number;
  htmlBytes: number;
  detailFetchedCount: number;
  detailKnownCount: number;
  detailJsonLdCount: number;
  detailHeuristicCount: number;
  sampleTitles: string[];
}

export interface WorkerSourceProbe {
  adapterId: string;
  strategyResolved: 'rss' | 'official_api' | 'public_html';
  urls: WorkerSourceProbeUrl[];
}
