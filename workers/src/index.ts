import { getSourceProfile, SOURCE_PROFILES } from './profiles';
import { diagnoseSource, fetchListingsForSource, probeSource } from './sources';
import type { SourcePayload, WorkerEnv } from './types';

const SUPPORTED_ADAPTERS = SOURCE_PROFILES.map((item) => item.adapterId) as string[];

function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data, null, 2), {
    status,
    headers: {
      'content-type': 'application/json; charset=utf-8',
      'cache-control': 'no-store',
    },
  });
}

function unauthorized() {
  return json({ error: 'Unauthorized' }, 401);
}

function parseBearerToken(request: Request) {
  const header = request.headers.get('authorization') ?? '';
  const match = header.match(/^Bearer\s+(.+)$/i);
  return match?.[1]?.trim() ?? null;
}

function isAuthorized(request: Request, env: WorkerEnv) {
  const required = env.WORKER_BEARER_TOKEN?.trim();
  if (!required) return true;
  return parseBearerToken(request) === required;
}

async function handleFetch(request: Request, env: WorkerEnv) {
  if (!isAuthorized(request, env)) return unauthorized();
  const payload = (await request.json()) as SourcePayload;
  if (!payload?.adapterId || !SUPPORTED_ADAPTERS.includes(payload.adapterId)) {
    return json({ error: 'Unsupported adapterId' }, 400);
  }
  try {
    const listings = await fetchListingsForSource(payload, env);
    return json({
      listings,
      source: {
        adapterId: payload.adapterId,
        count: listings.length,
        profile: getSourceProfile(payload.adapterId),
      },
    });
  } catch (error) {
    return json({
      error: error instanceof Error ? error.message : 'Unknown worker error',
      adapterId: payload.adapterId,
    }, 500);
  }
}

export default {
  async fetch(request: Request, env: WorkerEnv): Promise<Response> {
    const url = new URL(request.url);
    if (request.method === 'GET' && url.pathname === '/health') {
      return json({
        ok: true,
        worker: 'berlin-home-radar-connectors',
        supportedAdapters: SUPPORTED_ADAPTERS,
      });
    }

    if (request.method === 'GET' && url.pathname === '/sources/meta') {
      if (!isAuthorized(request, env)) return unauthorized();
      return json({
        count: SOURCE_PROFILES.length,
        profiles: SOURCE_PROFILES,
      });
    }

    if (request.method === 'POST' && url.pathname === '/sources/diagnose') {
      if (!isAuthorized(request, env)) return unauthorized();
      const payload = (await request.json()) as SourcePayload;
      if (!payload?.adapterId || !SUPPORTED_ADAPTERS.includes(payload.adapterId)) {
        return json({ error: 'Unsupported adapterId' }, 400);
      }
      return json(diagnoseSource(payload, env));
    }

    if (request.method === 'POST' && url.pathname === '/sources/probe') {
      if (!isAuthorized(request, env)) return unauthorized();
      const payload = (await request.json()) as SourcePayload;
      if (!payload?.adapterId || !SUPPORTED_ADAPTERS.includes(payload.adapterId)) {
        return json({ error: 'Unsupported adapterId' }, 400);
      }
      try {
        return json(await probeSource(payload, env));
      } catch (error) {
        return json({ error: error instanceof Error ? error.message : 'Unknown probe error', adapterId: payload.adapterId }, 500);
      }
    }

    if (request.method === 'POST' && url.pathname === '/sources/fetch') {
      return handleFetch(request, env);
    }

    return json({ error: 'Not found' }, 404);
  },
};
