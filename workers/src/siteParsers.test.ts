import { listingFromKnownDetailPage, listingsFromKnownListPage, previewKnownListPageLinks } from './siteParsers';
import type { SourcePayload } from './types';

const baseSource = (adapterId: string): SourcePayload => ({
  sourceId: `source-${adapterId}`,
  adapterId,
  country: 'DE',
  config: { city: 'Berlin', listingType: 'rent', maxResults: '20' },
});

function assert(condition: unknown, message: string) {
  if (!condition) throw new Error(message);
}

const howogeHtml = `
<html><body>
<h1>Neue Wohnung mit WBS 100!</h1>
<div>Adresse: Plonzstraße 34, 10365 Berlin, Lichtenberg</div>
<div>Warmmiete 300,00 €</div>
<div>Wohnfläche 30,0 m²</div>
<div>Zimmer 1</div>
<div>Bezugsfrei 01.05.2026</div>
<div>Merkmale Balkon/Loggia Fahrstuhl</div>
</body></html>`;

const gewobagHtml = `
<html><body>
<h1>Schöne Altbauwohnung sucht Nachmieter*in zum 1. März 2026</h1>
<div>Mietpreis</div>
<div>Gesamtmiete | 741,00 Euro</div>
<div>Allgemeine Angebotsdaten</div>
<div>Anschrift | Arndtstr. 35, 10965 Berlin</div>
<div>Bezirk/Ortsteil | Friedrichshain-Kreuzberg / Kreuzberg</div>
<div>Anzahl Zimmer | 2</div>
<div>Fläche in m² | 50,34 m²</div>
<div>Frei ab | 01.03.2026</div>
<div>Merkmale Haustiere erlaubt Balkon/Terasse Fahrstuhl</div>
</body></html>`;

const howoge = listingFromKnownDetailPage(baseSource('howoge-de'), howogeHtml, 'https://example.com/howoge');
assert(howoge?.price === 300, 'HOWOGE should parse warm rent');
assert(howoge?.district === 'Lichtenberg', 'HOWOGE should parse district');
assert(howoge?.balcony === true, 'HOWOGE should parse balcony');

const gewobag = listingFromKnownDetailPage(baseSource('gewobag-de'), gewobagHtml, 'https://example.com/gewobag');
assert(gewobag?.price === 741, 'Gewobag should parse total rent');
assert(gewobag?.rooms === 2, 'Gewobag should parse rooms');
assert(gewobag?.petsAllowed === true, 'Gewobag should parse pets flag');


const wbmListHtml = `
<html><body>
<a href="/wohnungen-berlin/angebote/details/2-zimmer-wohnung-in-mitte/">2-Zimmer-Wohnung in Mitte</a>
<div>Seydelstrasse 31, 10117 Berlin</div>
<div>1.327,20 € Warmmiete</div>
<div>52,88 m² Größe</div>
<div>2 Zimmer</div>
<div>Aufzug Balkon</div>
<a href="/wohnungen-berlin/angebote/details/3-zimmer-wohnung-in-spandau/">3 Zimmerdachgeschoßwohnung in Spandau</a>
<div>Mertensstrasse 24, 13587 Berlin</div>
<div>1.695,60 € Warmmiete</div>
<div>109,55 m² Größe</div>
<div>3 Zimmer</div>
<div>Aufzug Balkon</div>
</body></html>`;

const wbmList = listingsFromKnownListPage(baseSource('wbm-de'), wbmListHtml, 'https://www.wbm.de/wohnungen-berlin/angebote/');
assert(wbmList.length >= 2, 'WBM list parser should extract multiple cards');
assert(wbmList[0]?.price === 1327.2, 'WBM list parser should parse warm rent');
assert(wbmList[0]?.rooms === 2, 'WBM list parser should parse rooms');
assert(previewKnownListPageLinks(baseSource('wbm-de'), wbmListHtml, 'https://www.wbm.de/wohnungen-berlin/angebote/').length === 2, 'WBM preview should return detail links');


const studentendorfListHtml = `
<html><body>
<a href="/de/wg-zimmer/schlachtensee-kategorie-b">WG-Zimmer Schlachtensee Kategorie B</a>
<div>möbliert</div>
<div>4 – 8 Mitbewohner</div>
<div>Ab 475,22 €</div>
<a href="/de/wg-zimmer/adlershof-student-room">Adlershof student room</a>
<div>furnished</div>
<div>private bathroom</div>
<div>From 546,67 €</div>
</body></html>`;

const studentendorfList = listingsFromKnownListPage(baseSource('studentendorf-berlin-de'), studentendorfListHtml, 'https://www.studentendorf.berlin/de/wg-zimmer/');
assert(studentendorfList.length >= 2, 'Studentendorf list parser should extract student room cards');
assert(studentendorfList[0]?.propertyType === 'room', 'Studentendorf cards should be typed as rooms');
assert(studentendorfList[0]?.furnished === true, 'Studentendorf cards should default to furnished');
assert(studentendorfList[0]?.district === 'Schlachtensee', 'Studentendorf cards should infer Schlachtensee district');
assert(previewKnownListPageLinks(baseSource('studentendorf-berlin-de'), studentendorfListHtml, 'https://www.studentendorf.berlin/de/wg-zimmer/').length === 2, 'Studentendorf preview should return detail links');

const inBerlinWohnenHtml = `
<html><body>
<a href="/wohnungsfinder/angebot/familienfreundlich-wohnen-mit-wbs-berechtigung">Familienfreundlich wohnen mit WBS-Berechtigung</a>
<div>Bühringstraße 7, 13086 Berlin</div>
<div>3 Zi.</div>
<div>56,43 m²</div>
<div>760,12 € Warmmiete</div>
<div>WBS erforderlich Balkon</div>
</body></html>`;

const inBerlin = listingsFromKnownListPage(baseSource('inberlinwohnen-de'), inBerlinWohnenHtml, 'https://inberlinwohnen.de/wohnungsfinder/');
assert(inBerlin.length === 1, 'inberlinwohnen list parser should extract one card');
assert(inBerlin[0]?.price === 760.12, 'inberlinwohnen list parser should parse warm rent');
assert(inBerlin[0]?.description?.includes('WBS required') === true, 'inberlinwohnen parser should carry WBS description');
