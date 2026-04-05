import type { Source } from '@/types/source';
import { buildDraftFromCatalog, getSourceCatalogEntry, sourceCatalog } from './sourceCatalog';

function makeGenericListings(prefix: string, label: string, contactName: string, districtA: string, districtB: string, urlBase: string, furnished = false, propertyType: 'apartment' | 'studio' | 'room' = 'apartment') {
  return [
    {
      id: `${prefix}-1`,
      title: `${label}: Bright option in ${districtA}`,
      description: `Sample Berlin listing for ${label}.`,
      city: 'Berlin',
      district: districtA,
      street: districtA === 'Mitte' ? 'Torstrasse' : districtA === 'Treptow' ? 'Elsenstrasse' : 'Pappelallee',
      houseNumber: '12',
      zip: districtA === 'Mitte' ? '10119' : districtA === 'Treptow' ? '12435' : '10437',
      type: 'rent',
      propertyType,
      price: furnished ? 1590 : 1090,
      sizeSqm: propertyType === 'room' ? 21 : propertyType === 'studio' ? 34 : 57,
      rooms: propertyType === 'room' ? 1 : 2,
      lat: 52.52,
      lng: 13.41,
      furnished,
      balcony: propertyType !== 'room',
      elevator: propertyType !== 'room',
      parking: false,
      listedAt: '2026-04-04T09:30:00.000Z',
      url: `${urlBase}/sample-1`,
      images: ['https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200'],
      contactName,
    },
    {
      id: `${prefix}-2`,
      title: `${label}: Berlin listing in ${districtB}`,
      description: `Second Berlin sample listing for ${label}.`,
      city: 'Berlin',
      district: districtB,
      street: districtB === 'Charlottenburg' ? 'Kantstrasse' : districtB === 'Lichtenberg' ? 'Sewanstrasse' : 'Warschauer Strasse',
      houseNumber: '27',
      zip: districtB === 'Charlottenburg' ? '10623' : districtB === 'Lichtenberg' ? '10319' : '10243',
      type: 'rent',
      propertyType,
      price: furnished ? 1740 : 1240,
      sizeSqm: propertyType === 'room' ? 18 : propertyType === 'studio' ? 29 : 49,
      rooms: propertyType === 'room' ? 1 : 2,
      lat: 52.505,
      lng: 13.33,
      furnished,
      balcony: true,
      elevator: propertyType !== 'room',
      parking: false,
      listedAt: '2026-04-03T11:00:00.000Z',
      url: `${urlBase}/sample-2`,
      images: ['https://images.unsplash.com/photo-1494526585095-c41746248156?w=1200'],
      contactName,
    },
  ] as const;
}

const municipalDemo = makeGenericListings('muni', 'Municipal Berlin', 'Berlin Public Housing', 'Treptow', 'Lichtenberg', 'https://example.com/berlin-public');
const furnishedDemo = makeGenericListings('furn', 'Temporary Berlin', 'Berlin Furnished Team', 'Prenzlauer Berg', 'Charlottenburg', 'https://example.com/berlin-furnished', true, 'studio');
const studentDemo = makeGenericListings('student', 'Student Berlin', 'Student Housing Berlin', 'Adlershof', 'Zehlendorf', 'https://example.com/berlin-student', true, 'room');

export const demoRaw = {
  immoscout: [
    { id:'is24-100', title:'Bright apartment near Alexanderplatz', description:'Modern 2-room apartment with balcony and elevator.', city:'Berlin', district:'Mitte', street:'Torstrasse', houseNumber:'88', zip:'10119', type:'rent', propertyType:'apartment', price:1450, sizeSqm:60, rooms:2, lat:52.529, lng:13.401, furnished:true, balcony:true, elevator:true, parking:false, listedAt:'2026-04-03T09:00:00.000Z', url:'https://example.com/immoscout/100', images:['https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200'], contactName:'Anna Keller' },
    { id:'is24-101', title:'Family apartment in Charlottenburg', description:'Quiet street, good schools, renovated kitchen.', city:'Berlin', district:'Charlottenburg', street:'Kantstrasse', houseNumber:'18', zip:'10623', type:'rent', propertyType:'apartment', price:2300, sizeSqm:95, rooms:4, lat:52.506, lng:13.331, furnished:false, balcony:true, elevator:false, parking:true, listedAt:'2026-04-01T09:00:00.000Z', url:'https://example.com/immoscout/101', images:['https://images.unsplash.com/photo-1494526585095-c41746248156?w=1200'], contactName:'Scout Broker' },
  ],
  immowelt: [
    { uid:'iw-301', headline:'Renovated flat with U-Bahn access', body:'Popular expat-friendly unit with updated bathroom.', municipality:'Berlin', neighborhood:'Prenzlauer Berg', street:'Danziger Strasse', houseNumber:'12', postalCode:'10435', mode:'rent', category:'apartment', totalPrice:1710, livingArea:67, roomCount:2, geo:{ lat:52.54, lng:13.424 }, amenities:{ furnished:false, balcony:true, elevator:true, parking:false }, postedAt:'2026-04-02T10:10:00.000Z', sourceUrl:'https://example.com/immowelt/301', imageUrls:['https://images.unsplash.com/photo-1460317442991-0ec209397118?w=1200'], agent:'Immowelt Partner' },
    { uid:'iw-302', headline:'Sunny Altbau in Friedrichshain', body:'Classic Berlin apartment with high ceilings and upgraded heating.', municipality:'Berlin', neighborhood:'Friedrichshain', street:'Marchlewskistrasse', houseNumber:'53', postalCode:'10243', mode:'rent', category:'apartment', totalPrice:1890, livingArea:72, roomCount:3, geo:{ lat:52.514, lng:13.447 }, amenities:{ furnished:false, balcony:true, elevator:false, parking:false }, postedAt:'2026-03-31T12:00:00.000Z', sourceUrl:'https://example.com/immowelt/302', imageUrls:['https://images.unsplash.com/photo-1502005229762-cf1b2da7c5d6?w=1200'], agent:'Berlin Altbau Living' },
  ],
  immonet: [
    { listingId:'imn-88', headline:'Bright apartment near Alexanderplatz', descriptionText:'Duplicate source entry for the same Mitte apartment.', municipality:'Berlin', area:'Mitte', addressLine:'Torstrasse 88', postalCode:'10119', mode:'rent', category:'apartment', totalPrice:1460, livingArea:61, roomCount:2, geo:{ lat:52.529, lng:13.401 }, amenities:{ furnished:true, balcony:true, elevator:true, parking:false }, postedAt:'2026-04-02T11:00:00.000Z', sourceUrl:'https://example.com/immonet/88', imageUrls:['https://images.unsplash.com/photo-1484154218962-a197022b5858?w=1200'], agent:'Immonet Agency' },
    { listingId:'imn-89', headline:'Loft-style apartment in Neukolln', descriptionText:'Open-plan Berlin loft with balcony and elevator.', municipality:'Berlin', area:'Neukolln', addressLine:'Sonnenallee 125', postalCode:'12059', mode:'rent', category:'apartment', totalPrice:1590, livingArea:58, roomCount:2, geo:{ lat:52.481, lng:13.448 }, amenities:{ furnished:false, balcony:true, elevator:true, parking:false }, postedAt:'2026-04-01T11:00:00.000Z', sourceUrl:'https://example.com/immonet/89', imageUrls:['https://images.unsplash.com/photo-1460317442991-0ec209397118?w=1200'], agent:'City Living Berlin' },
  ],
  wgGesucht: [
    { offerId:'wg-501', title:'Furnished WG room in Neukolln', body:'Student-friendly room, shared kitchen, bills included.', cityName:'Berlin', districtName:'Neukolln', streetName:'Pannierstrasse', zipCode:'12047', listingMode:'rent', kind:'room', rent:980, areaSqm:28, rooms:1, coordinates:[52.486,13.434], extras:['furnished'], petsAllowed:false, datePublished:'2026-04-04T10:00:00.000Z', href:'https://example.com/wg/501', photos:['https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=1200'], contact:'WG Team' },
    { offerId:'wg-502', title:'Room near TU Berlin', body:'Shared flat close to transport and campus.', cityName:'Berlin', districtName:'Charlottenburg', streetName:'Wilmersdorfer Strasse', zipCode:'10627', listingMode:'rent', kind:'room', rent:870, areaSqm:22, rooms:1, coordinates:[52.508,13.305], extras:['furnished','balcony'], petsAllowed:true, datePublished:'2026-04-03T10:00:00.000Z', href:'https://example.com/wg/502', photos:['https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200'], contact:'WG Berlin' },
  ],
  kleinanzeigen: [
    { adId:'ek-19', name:'Compact studio in Kreuzberg', text:'Studio flat with fitted kitchen and balcony.', place:'Berlin', quarter:'Kreuzberg', street:'Gneisenaustrasse', postcode:'10961', purpose:'rent', unitType:'studio', amount:1200, sqm:33, rooms:1, coords:{ lat:52.491, lng:13.39 }, balcony:true, furnished:false, parking:false, createdAt:'2026-03-30T08:00:00.000Z', link:'https://example.com/kleinanzeigen/19', media:[], seller:'Private Owner' },
    { adId:'ek-20', name:'Private apartment in Wedding', text:'Direct-from-owner rental with no broker fee.', place:'Berlin', quarter:'Wedding', street:'Mullerstrasse', postcode:'13353', purpose:'rent', unitType:'apartment', amount:1490, sqm:57, rooms:2, coords:{ lat:52.547, lng:13.359 }, balcony:false, furnished:false, parking:false, createdAt:'2026-04-02T08:00:00.000Z', link:'https://example.com/kleinanzeigen/20', media:['https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=1200'], seller:'Owner Direct Berlin' },
  ],
  wohnungsboerse: [
    { uid:'wb-11', heading:'Pet-friendly apartment in Moabit', summary:'Lift, storage, and sunny living room.', city:'Berlin', district:'Moabit', streetAddress:'Turmstrasse 90', postal:'10559', offerType:'rent', estateType:'apartment', monthlyPrice:1650, area:78, roomAmount:3, latitude:52.526, longitude:13.342, furnished:false, balcony:true, parking:false, pets:true, listedOn:'2026-04-03T06:00:00.000Z', url:'https://example.com/wohnungsboerse/11', images:['https://images.unsplash.com/photo-1494526585095-c41746248156?w=1200'], contactName:'Wohnungsboerse Team' },
    { uid:'wb-12', heading:'Berlin studio in Schoneberg', summary:'Compact city studio with balcony and lift.', city:'Berlin', district:'Schoneberg', streetAddress:'Hauptstrasse 51', postal:'10827', offerType:'rent', estateType:'studio', monthlyPrice:1190, area:36, roomAmount:1, latitude:52.489, longitude:13.355, furnished:true, balcony:true, parking:false, pets:false, listedOn:'2026-04-01T06:00:00.000Z', url:'https://example.com/wohnungsboerse/12', images:['https://images.unsplash.com/photo-1460317442991-0ec209397118?w=1200'], contactName:'Wohnungsboerse Berlin' },
  ],
  inberlinwohnen: municipalDemo,
  degewo: municipalDemo,
  gesobau: municipalDemo,
  howoge: municipalDemo,
  stadtundland: municipalDemo,
  wbm: municipalDemo,
  gewobag: municipalDemo,
  berlinovo: furnishedDemo,
  bggBerlinStudent: studentDemo,
  studierendenwerkBerlin: studentDemo,
  studentendorfBerlin: studentDemo,
  wunderflatsBerlin: furnishedDemo,
  housingAnywhereBerlin: furnishedDemo,
  spotahomeBerlin: furnishedDemo,
  comingHomeBerlin: furnishedDemo,
  urbanbnbBerlin: furnishedDemo,
  localAgencyRss: [
    { id:'rss-de-1', title:'Loft apartment from local agency feed', description:'Imported from a neighborhood broker RSS feed.', city:'Berlin', district:'Friedrichshain', street:'Boxhagener Strasse', houseNumber:'22', zip:'10245', type:'rent', propertyType:'apartment', price:1840, sizeSqm:72, rooms:2, lat:52.511, lng:13.463, furnished:false, balcony:true, elevator:false, parking:false, listedAt:'2026-04-04T07:30:00.000Z', url:'https://example.com/local-rss/1', images:['https://images.unsplash.com/photo-1494526585095-c41746248156?w=1200'], contactName:'Kiez Makler' },
    { id:'rss-de-2', title:'New-build apartment near Tempelhofer Feld', description:'Broker feed entry from a Berlin local agency.', city:'Berlin', district:'Tempelhof', street:'Columbiadamm', houseNumber:'94', zip:'10965', type:'rent', propertyType:'apartment', price:1760, sizeSqm:69, rooms:2, lat:52.474, lng:13.403, furnished:false, balcony:true, elevator:true, parking:false, listedAt:'2026-04-02T07:30:00.000Z', url:'https://example.com/local-rss/2', images:['https://images.unsplash.com/photo-1502005229762-cf1b2da7c5d6?w=1200'], contactName:'Tempelhof Local Agency' },
  ],
} as const;

const now = new Date().toISOString();

function buildSourceConfig(template: ReturnType<typeof buildDraftFromCatalog>) {
  const entry = getSourceCatalogEntry(template.adapterId);
  return {
    ...(entry?.defaultConfig ?? {}),
    city: template.city,
    listingType: template.listingType,
    maxResults: template.maxResults,
    executionMode: template.executionMode,
    workerPath: template.workerPath,
    ...(template.fetchStrategy ? { fetchStrategy: template.fetchStrategy } : {}),
    ...(template.district ? { district: template.district } : {}),
    ...(template.customSearchUrl ? { customSearchUrl: template.customSearchUrl } : {}),
    ...(template.rssUrl ? { rssUrl: template.rssUrl } : {}),
    ...(template.includeHouses ? { includeHouses: template.includeHouses } : {}),
    ...(template.wgMode ? { wgMode: template.wgMode } : {}),
  };
}

function toDefaultSource(template: ReturnType<typeof buildDraftFromCatalog>, index: number, enabled: boolean): Source {
  return {
    id: `source-${template.adapterId}`,
    name: template.name,
    adapterId: template.adapterId,
    country: template.country,
    enabled,
    priority: index + 1,
    config: buildSourceConfig(template),
    createdAt: now,
    updatedAt: now,
    health: { status: 'idle' },
  };
}

export const defaultSources: Source[] = sourceCatalog.map((entry, index) =>
  toDefaultSource(
    {
      ...buildDraftFromCatalog(entry.id),
      name: `${entry.label} ${entry.defaultCity}`,
    },
    index,
    entry.enabledByDefault,
  ),
);
