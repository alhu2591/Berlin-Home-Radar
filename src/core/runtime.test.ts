import { defaultRuntimeConfig, sanitizeRuntimeConfig } from '@/core/runtime';

test('sanitizes runtime config values', () => {
  const result = sanitizeRuntimeConfig({
    mode: 'worker_only',
    workerBaseUrl: 'https://demo.workers.dev/',
    requestTimeoutMs: 999999,
    allowDemoFallback: false,
    debugNetwork: true,
  });

  expect(result.mode).toBe('worker_only');
  expect(result.workerBaseUrl).toBe('https://demo.workers.dev');
  expect(result.requestTimeoutMs).toBe(30000);
  expect(result.allowDemoFallback).toBe(false);
  expect(result.debugNetwork).toBe(true);
});

test('falls back to defaults for invalid values', () => {
  const result = sanitizeRuntimeConfig({
    mode: undefined,
    workerBaseUrl: '  ',
    requestTimeoutMs: 10,
  });

  expect(result.mode).toBe(defaultRuntimeConfig.mode);
  expect(result.workerBaseUrl).toBe('');
  expect(result.requestTimeoutMs).toBe(1500);
});
