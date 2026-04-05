import type { RuntimeConfig } from '@/types/runtime';

export const defaultRuntimeConfig: RuntimeConfig = {
  mode: 'demo',
  workerBaseUrl: '',
  requestTimeoutMs: 8000,
  allowDemoFallback: true,
  debugNetwork: false,
  lastValidatedAt: null,
};

export function sanitizeRuntimeConfig(input?: Partial<RuntimeConfig> | null): RuntimeConfig {
  return {
    mode: input?.mode === 'worker_hybrid' || input?.mode === 'worker_only' || input?.mode === 'demo' ? input.mode : defaultRuntimeConfig.mode,
    workerBaseUrl: typeof input?.workerBaseUrl === 'string' ? input.workerBaseUrl.trim().replace(/\/$/, '') : defaultRuntimeConfig.workerBaseUrl,
    requestTimeoutMs: Number.isFinite(input?.requestTimeoutMs) ? Math.max(1500, Math.min(30000, Number(input?.requestTimeoutMs))) : defaultRuntimeConfig.requestTimeoutMs,
    allowDemoFallback: typeof input?.allowDemoFallback === 'boolean' ? input.allowDemoFallback : defaultRuntimeConfig.allowDemoFallback,
    debugNetwork: typeof input?.debugNetwork === 'boolean' ? input.debugNetwork : defaultRuntimeConfig.debugNetwork,
    lastValidatedAt: typeof input?.lastValidatedAt === 'string' || input?.lastValidatedAt === null ? input.lastValidatedAt ?? null : null,
  };
}

export function requiresWorker(config: RuntimeConfig) {
  return config.mode === 'worker_hybrid' || config.mode === 'worker_only';
}
