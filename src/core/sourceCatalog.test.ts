import { buildDraftFromCatalog, getCatalogByMarket, getSourceCatalogEntry, sourceCatalog } from './sourceCatalog';

function assert(condition: boolean, message: string) {
  if (!condition) throw new Error(message);
}

const germany = getCatalogByMarket('DE');
assert(germany.every((item) => item.defaultCity === 'Berlin'), 'All catalog entries should default to Berlin');
assert(germany.some((item) => item.id === 'immoscout-de'), 'Berlin market should include ImmobilienScout24');
assert(germany.some((item) => item.id === 'local-agency-rss-de'), 'Berlin market should include local agency RSS');
assert(germany.some((item) => item.id === 'inberlinwohnen-de'), 'Berlin market should include inberlinwohnen');
assert(germany.some((item) => item.id === 'wunderflats-berlin-de'), 'Berlin market should include Wunderflats');

const draft = buildDraftFromCatalog('wohnungsboerse-de');
assert(draft.city === 'Berlin', 'Wohnungsboerse should default to Berlin');
assert(draft.fetchStrategy === 'public_html', 'Wohnungsboerse should default to HTML fetching');
assert(sourceCatalog.length >= 20, 'Catalog should include a broad Berlin source superset');

const agency = getSourceCatalogEntry('local-agency-rss-de');
assert(agency?.workerStrategyHint === 'rss', 'Local agency feed should use RSS worker hint');
