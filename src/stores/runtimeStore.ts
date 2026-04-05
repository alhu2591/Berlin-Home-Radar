import { create } from 'zustand';
import { kv } from '@/core/kv';
import { defaultRuntimeConfig, sanitizeRuntimeConfig } from '@/core/runtime';
import type { RuntimeConfig } from '@/types/runtime';

interface RuntimeStore extends RuntimeConfig {
  hydrate: () => void;
  update: (patch: Partial<RuntimeConfig>) => void;
  reset: () => void;
}

function persist(config: RuntimeConfig) {
  kv.setObject('runtime-store', config);
}

export const useRuntimeStore = create<RuntimeStore>((set, get) => ({
  ...defaultRuntimeConfig,
  hydrate: () => {
    const saved = kv.getObject<RuntimeConfig>('runtime-store');
    if (saved) set(sanitizeRuntimeConfig(saved));
  },
  update: (patch) => {
    const next = sanitizeRuntimeConfig({ ...get(), ...patch });
    persist(next);
    set(next);
  },
  reset: () => {
    persist(defaultRuntimeConfig);
    set(defaultRuntimeConfig);
  },
}));
