export type AppLocale = 'en' | 'de' | 'ar';
export type ThemeMode = 'system' | 'light' | 'dark';

export interface PreferencesState {
  locale: AppLocale;
  themeMode: ThemeMode;
  analyticsEnabled: boolean;
}
