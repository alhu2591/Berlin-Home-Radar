export type RuntimeMode = 'demo' | 'worker_hybrid' | 'worker_only';

export interface RuntimeConfig {
  mode: RuntimeMode;
  workerBaseUrl: string;
  requestTimeoutMs: number;
  allowDemoFallback: boolean;
  debugNetwork: boolean;
  lastValidatedAt: string | null;
}
