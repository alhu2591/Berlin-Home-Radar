import type { NormalizedListing } from '@/types/listing';
import type { RuntimeConfig } from '@/types/runtime';
import type { Source } from '@/types/source';
import type { WorkerProfile, WorkerSourceDiagnosis, WorkerSourceProbe } from '@/types/worker';

interface WorkerResponse {
  listings: NormalizedListing[];
  source?: {
    adapterId?: string;
    count?: number;
    profile?: WorkerProfile;
  };
}

interface WorkerMetaResponse {
  count: number;
  profiles: WorkerProfile[];
}

function normalizeBaseUrl(url: string) {
  return url.trim().replace(/\/$/, '');
}

async function fetchWithTimeout(url: string, init: RequestInit, timeoutMs: number) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await fetch(url, { ...init, signal: controller.signal });
  } finally {
    clearTimeout(timer);
  }
}

export const RemoteSourceService = {
  async fetchListings(source: Source, runtime: RuntimeConfig, workerToken?: string | null): Promise<NormalizedListing[]> {
    const baseUrl = normalizeBaseUrl(runtime.workerBaseUrl);
    if (!baseUrl) throw new Error('Worker base URL is not configured');

    const endpoint = `${baseUrl}${source.config.workerPath ?? '/sources/fetch'}`;
    const response = await fetchWithTimeout(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(workerToken ? { Authorization: `Bearer ${workerToken}` } : {}),
      },
      body: JSON.stringify({
        sourceId: source.id,
        adapterId: source.adapterId,
        country: source.country,
        config: source.config,
      }),
    }, runtime.requestTimeoutMs);

    if (!response.ok) {
      const body = await response.text();
      throw new Error(`Worker responded ${response.status}: ${body.slice(0, 120)}`);
    }

    const payload = (await response.json()) as WorkerResponse;
    if (!payload || !Array.isArray(payload.listings)) {
      throw new Error('Worker response is missing listings[]');
    }

    return payload.listings;
  },

  async diagnoseSource(source: Source, runtime: RuntimeConfig, workerToken?: string | null): Promise<WorkerSourceDiagnosis> {
    const baseUrl = normalizeBaseUrl(runtime.workerBaseUrl);
    if (!baseUrl) throw new Error('Worker base URL is not configured');
    const response = await fetchWithTimeout(`${baseUrl}/sources/diagnose`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(workerToken ? { Authorization: `Bearer ${workerToken}` } : {}),
      },
      body: JSON.stringify({
        sourceId: source.id,
        adapterId: source.adapterId,
        country: source.country,
        config: source.config,
      }),
    }, runtime.requestTimeoutMs);
    if (!response.ok) {
      const body = await response.text();
      throw new Error(`Worker diagnose failed ${response.status}: ${body.slice(0, 120)}`);
    }
    return (await response.json()) as WorkerSourceDiagnosis;
  },



  async probeSource(source: Source, runtime: RuntimeConfig, workerToken?: string | null): Promise<WorkerSourceProbe> {
    const baseUrl = normalizeBaseUrl(runtime.workerBaseUrl);
    if (!baseUrl) throw new Error('Worker base URL is not configured');
    const response = await fetchWithTimeout(`${baseUrl}/sources/probe`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(workerToken ? { Authorization: `Bearer ${workerToken}` } : {}),
      },
      body: JSON.stringify({
        sourceId: source.id,
        adapterId: source.adapterId,
        country: source.country,
        config: source.config,
      }),
    }, runtime.requestTimeoutMs);
    if (!response.ok) {
      const body = await response.text();
      throw new Error(`Worker probe failed ${response.status}: ${body.slice(0, 120)}`);
    }
    return (await response.json()) as WorkerSourceProbe;
  },
  async getMeta(runtime: RuntimeConfig, workerToken?: string | null): Promise<WorkerMetaResponse> {
    const baseUrl = normalizeBaseUrl(runtime.workerBaseUrl);
    if (!baseUrl) throw new Error('Worker base URL is not configured');
    const response = await fetchWithTimeout(`${baseUrl}/sources/meta`, {
      method: 'GET',
      headers: workerToken ? { Authorization: `Bearer ${workerToken}` } : {},
    }, runtime.requestTimeoutMs);
    if (!response.ok) {
      const body = await response.text();
      throw new Error(`Worker meta failed ${response.status}: ${body.slice(0, 120)}`);
    }
    return (await response.json()) as WorkerMetaResponse;
  },
  async ping(runtime: RuntimeConfig, workerToken?: string | null) {
    const baseUrl = normalizeBaseUrl(runtime.workerBaseUrl);
    if (!baseUrl) throw new Error('Worker base URL is not configured');
    const startedAt = Date.now();
    const response = await fetchWithTimeout(`${baseUrl}/health`, {
      method: 'GET',
      headers: workerToken ? { Authorization: `Bearer ${workerToken}` } : {},
    }, runtime.requestTimeoutMs);
    if (!response.ok) throw new Error(`Worker health check failed (${response.status})`);
    return { ok: true, latencyMs: Date.now() - startedAt };
  },
};
