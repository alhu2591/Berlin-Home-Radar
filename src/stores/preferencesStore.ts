import { create } from 'zustand';
import { kv } from '@/core/kv';
import { i18n } from '@/i18n';
import type { AppLocale, PreferencesState, ThemeMode } from '@/types/preferences';

interface PreferencesStore extends PreferencesState {
  setLocale: (locale: AppLocale) => void;
  setThemeMode: (mode: ThemeMode) => void;
  setAnalyticsEnabled: (enabled: boolean) => void;
  completeOnboarding: () => void;
  resetOnboarding: () => void;
  hydrate: () => void;
}

const defaults: PreferencesState = {
  locale: 'en',
  themeMode: 'system',
  hasCompletedOnboarding: false,
  analyticsEnabled: true,
};

function persist(state: PreferencesState) {
  kv.setObject('preferences-store', state);
}

function snapshot(state: PreferencesStore): PreferencesState {
  return {
    locale: state.locale,
    themeMode: state.themeMode,
    hasCompletedOnboarding: state.hasCompletedOnboarding,
    analyticsEnabled: state.analyticsEnabled,
  };
}

export const usePreferencesStore = create<PreferencesStore>((set, get) => ({
  ...defaults,
  setLocale: (locale) => {
    void i18n.changeLanguage(locale);
    const next = { ...snapshot(get()), locale };
    persist(next);
    set({ locale });
  },
  setThemeMode: (mode) => {
    const next = { ...snapshot(get()), themeMode: mode };
    persist(next);
    set({ themeMode: mode });
  },
  setAnalyticsEnabled: (enabled) => {
    const next = { ...snapshot(get()), analyticsEnabled: enabled };
    persist(next);
    set({ analyticsEnabled: enabled });
  },
  completeOnboarding: () => {
    const next = { ...snapshot(get()), hasCompletedOnboarding: true };
    persist(next);
    set({ hasCompletedOnboarding: true });
  },
  resetOnboarding: () => {
    const next = { ...snapshot(get()), hasCompletedOnboarding: false };
    persist(next);
    set({ hasCompletedOnboarding: false });
  },
  hydrate: () => {
    const saved = kv.getObject<PreferencesState>('preferences-store');
    if (saved) {
      set({ ...defaults, ...saved });
      void i18n.changeLanguage(saved.locale);
    }
  },
}));
