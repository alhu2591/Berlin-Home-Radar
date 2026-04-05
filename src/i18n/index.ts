import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import enCommon from './en/common.json';
import deCommon from './de/common.json';
import arCommon from './ar/common.json';

void i18n.use(initReactI18next).init({
  compatibilityJSON: 'v4',
  lng: 'en',
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
  resources: {
    en: { common: enCommon },
    de: { common: deCommon },
    ar: { common: arCommon }
  },
  defaultNS: 'common'
});

export { i18n };
