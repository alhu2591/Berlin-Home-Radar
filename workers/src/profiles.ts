export type ParserFamily = 'official_api' | 'json_ld' | 'detail_pages' | 'rss' | 'mixed';
export type InventoryFamily = 'portal' | 'marketplace' | 'room' | 'public_housing' | 'student' | 'furnished' | 'agency';

export interface SourceProfile {
  adapterId: string;
  label: string;
  inventoryFamily: InventoryFamily;
  parserFamily: ParserFamily;
  supportsDistrictFilter: boolean;
  supportsOfficialApi: boolean;
  supportsRss: boolean;
  liveSearchUrls: string[];
  detailPatterns: string[];
  docsUrl?: string;
  notes: string;
}

export const SOURCE_PROFILES: SourceProfile[] = [
  {
    adapterId: 'immoscout-de',
    label: 'ImmobilienScout24',
    inventoryFamily: 'portal',
    parserFamily: 'mixed',
    supportsDistrictFilter: true,
    supportsOfficialApi: true,
    supportsRss: false,
    liveSearchUrls: [
      'https://www.immobilienscout24.de/Suche/de/berlin/berlin/wohnung-mieten',
      'https://www.immobilienscout24.de/Suche/de/berlin/berlin/wohnung-kaufen'
    ],
    detailPatterns: ['\\/expose\\/\\d+'],
    docsUrl: 'https://api.immobilienscout24.de/api-docs/search/introduction/',
    notes: 'Official API when partner credentials exist; otherwise public Berlin result pages and expose pages.'
  },
  {
    adapterId: 'immowelt-de',
    label: 'Immowelt',
    inventoryFamily: 'portal',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.immowelt.de/suche/mieten/wohnung/berlin/berlin-10115/ad08de8634'],
    detailPatterns: ['\\/expose\\/[^"\']+', '\\/immobilien\\/[^"\']+'],
    docsUrl: 'https://www.immowelt.de/',
    notes: 'Public search and expose pages; often needs detail-page fallback when list cards are sparse.'
  },
  {
    adapterId: 'immonet-de',
    label: 'Immonet',
    inventoryFamily: 'portal',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.immonet.de/berlin/wohnung-mieten.html'],
    detailPatterns: ['\\/[0-9]+\\.html', '\\/expose\\/[^"\']+'],
    docsUrl: 'https://www.immonet.de/',
    notes: 'Berlin list pages with detail pages and mirrored supply from other portals.'
  },
  {
    adapterId: 'wg-gesucht-de',
    label: 'WG-Gesucht',
    inventoryFamily: 'room',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.wg-gesucht.de/wg-zimmer-in-Berlin.8.0.1.0.html'],
    detailPatterns: ['\\/\\d+\\.html'],
    docsUrl: 'https://www.wg-gesucht.de/',
    notes: 'Primary Berlin room-share source for WGs, sublets, and some apartments.'
  },
  {
    adapterId: 'ebay-kleinanzeigen-de',
    label: 'Kleinanzeigen',
    inventoryFamily: 'marketplace',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.kleinanzeigen.de/s-wohnungmieten/berlin/c203l3331'],
    detailPatterns: ['\\/s-anzeige\\/[^"\']+'],
    docsUrl: 'https://www.kleinanzeigen.de/',
    notes: 'Public listing pages from private landlords and marketplace inventory.'
  },
  {
    adapterId: 'wohnungsboerse-de',
    label: 'Wohnungsboerse',
    inventoryFamily: 'portal',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.wohnungsboerse.net/Berlin/mieten/wohnungen'],
    detailPatterns: ['wohnung[^"\']+', 'haus[^"\']+'],
    docsUrl: 'https://www.wohnungsboerse.net/',
    notes: 'Long-tail Berlin supply; detail pages often carry more information than overview tiles.'
  },
  {
    adapterId: 'inberlinwohnen-de',
    label: 'inberlinwohnen',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://inberlinwohnen.de/wohnungsfinder/'],
    detailPatterns: ['wohnungsfinder[^"\']+', 'angebot[^"\']+'],
    docsUrl: 'https://inberlinwohnen.de/wohnungsfinder/',
    notes: 'Key Berlin municipal portal for the six state-owned companies.'
  },
  {
    adapterId: 'degewo-de',
    label: 'degewo',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.degewo.de/immosuche/'],
    detailPatterns: ['\\/immosuche\\/details\\/[^"\']+'],
    docsUrl: 'https://www.degewo.de/immosuche/',
    notes: 'Direct municipal inventory and applications.'
  },
  {
    adapterId: 'gesobau-de',
    label: 'GESOBAU',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.gesobau.de/mieten/wohnungssuche/'],
    detailPatterns: ['wohnungssuche[^"\']+', 'mietangebote[^"\']+'],
    docsUrl: 'https://www.gesobau.de/',
    notes: 'Municipal inventory with direct GESOBAU offers and project pages.'
  },
  {
    adapterId: 'howoge-de',
    label: 'HOWOGE',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.howoge.de/immobiliensuche/wohnungssuche.html'],
    detailPatterns: ['\\/wohnungssuche\\/detail\\/[^"\']+'],
    docsUrl: 'https://www.howoge.de/immobiliensuche/wohnungssuche.html',
    notes: 'HOWOGE apartment search plus special swap and project pages.'
  },
  {
    adapterId: 'stadtundland-de',
    label: 'STADT UND LAND',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://stadtundland.de/wohnungssuche'],
    detailPatterns: ['wohnungssuche[^"\']+', 'vermietung[^"\']+'],
    docsUrl: 'https://stadtundland.de/wohnungssuche',
    notes: 'Public housing projects and direct rental starts.'
  },
  {
    adapterId: 'wbm-de',
    label: 'WBM',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.wbm.de/wohnungen-berlin/angebote/'],
    detailPatterns: ['\\/wohnungen-berlin\\/angebote\\/details\\/[^"\']+'],
    docsUrl: 'https://www.wbm.de/wohnungen-berlin/angebote/',
    notes: 'Direct WBM offers and project details.'
  },
  {
    adapterId: 'gewobag-de',
    label: 'Gewobag',
    inventoryFamily: 'public_housing',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.gewobag.de/fuer-mietinteressentinnen/wohnungssuche/'],
    detailPatterns: ['\\/mietangebote\\/[^"\']+'],
    docsUrl: 'https://www.gewobag.de/fuer-mietinteressentinnen/wohnungssuche/',
    notes: 'Direct municipal offer pages and project inventory.'
  },
  {
    adapterId: 'berlinovo-de',
    label: 'berlinovo',
    inventoryFamily: 'student',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.backend.berlinovo.de/de/apartments/apartmentanlagen'],
    detailPatterns: ['\\/apartments\\/[^"\']+'],
    docsUrl: 'https://www.backend.berlinovo.de/de/apartments/apartmentanlagen',
    notes: 'Student and temporary apartment pages on berlinovo backends.'
  },
  {
    adapterId: 'bgg-berlin-student-de',
    label: 'BGG / berlinovo student',
    inventoryFamily: 'student',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.bgg-berlin.com/de/unternehmen/presse/berlinovo-errichtet-bis-20252026-rund-6000-wohnplaetze-fuer-studierende'],
    detailPatterns: ['\\/apartments\\/[^"\']+'],
    docsUrl: 'https://www.bgg-berlin.com/',
    notes: 'Legacy placeholder for berlinovo-related student inventory references.'
  },
  {
    adapterId: 'studierendenwerk-berlin-de',
    label: 'studierendenWERK BERLIN',
    inventoryFamily: 'student',
    parserFamily: 'json_ld',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.stw.berlin/en/housing/'],
    detailPatterns: ['\\/housing\\/[^"\']+'],
    docsUrl: 'https://www.stw.berlin/en/housing/',
    notes: 'Official student housing overview and individual residency pages.'
  },
  {
    adapterId: 'studentendorf-berlin-de',
    label: 'Studentendorf Berlin',
    inventoryFamily: 'student',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.studentendorf.berlin/student-rooms'],
    detailPatterns: ['student-rooms[^"\']+', 'wohnungen[^"\']+', 'room[^"\']+'],
    docsUrl: 'https://www.studentendorf.berlin/student-rooms',
    notes: 'Student villages in Schlachtensee and Adlershof.'
  },
  {
    adapterId: 'wunderflats-berlin-de',
    label: 'Wunderflats',
    inventoryFamily: 'furnished',
    parserFamily: 'json_ld',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://wunderflats.com/en/furnished-apartments/berlin'],
    detailPatterns: ['furnished-apartments[^"\']+'],
    docsUrl: 'https://wunderflats.com/en/furnished-apartments/berlin',
    notes: 'Temporary and furnished Berlin apartments.'
  },
  {
    adapterId: 'housinganywhere-berlin-de',
    label: 'HousingAnywhere',
    inventoryFamily: 'furnished',
    parserFamily: 'json_ld',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://housinganywhere.com/s/Berlin--Germany'],
    detailPatterns: ['room[^"\']+', 'apartment[^"\']+'],
    docsUrl: 'https://housinganywhere.com/s/Berlin--Germany',
    notes: 'International housing marketplace for temporary stays and student moves.'
  },
  {
    adapterId: 'spotahome-berlin-de',
    label: 'Spotahome',
    inventoryFamily: 'furnished',
    parserFamily: 'json_ld',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.spotahome.com/de/s/berlin'],
    detailPatterns: ['\\/de\\/berlin\\/for-rent:[^"\']+'],
    docsUrl: 'https://www.spotahome.com/de/s/berlin',
    notes: 'Verified-booking style furnished inventory.'
  },
  {
    adapterId: 'coming-home-berlin-de',
    label: 'coming home',
    inventoryFamily: 'furnished',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.coming-home.com/mieten'],
    detailPatterns: ['mieten[^"\']+', 'apartments[^"\']+'],
    docsUrl: 'https://www.coming-home.com/mieten',
    notes: 'Corporate and temporary stays in Berlin.'
  },
  {
    adapterId: 'urbanbnb-berlin-de',
    label: 'Urbanbnb',
    inventoryFamily: 'furnished',
    parserFamily: 'detail_pages',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: false,
    liveSearchUrls: ['https://www.urbanbnb.de/page/de/wohnen-auf-zeit-berlin?lang=de'],
    detailPatterns: ['wohnen-auf-zeit[^"\']+', 'apartment[^"\']+'],
    docsUrl: 'https://www.urbanbnb.de/page/de/wohnen-auf-zeit-berlin?lang=de',
    notes: 'Temporary furnished housing pages in Berlin.'
  },
  {
    adapterId: 'local-agency-rss-de',
    label: 'Local agency RSS',
    inventoryFamily: 'agency',
    parserFamily: 'rss',
    supportsDistrictFilter: true,
    supportsOfficialApi: false,
    supportsRss: true,
    liveSearchUrls: [],
    detailPatterns: [],
    docsUrl: undefined,
    notes: 'Custom RSS or Atom feeds from Berlin agencies and project sites.'
  }
];

const BY_ID = new Map(SOURCE_PROFILES.map((item) => [item.adapterId, item] as const));

export function getSourceProfile(adapterId: string) {
  return BY_ID.get(adapterId) ?? null;
}
