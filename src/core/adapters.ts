import type { NormalizedListing, ListingType, PropertyType } from '@/types/listing';
import type { Source } from '@/types/source';
import { demoRaw } from './demo';
import { useRuntimeStore } from '@/stores/runtimeStore';
import { requiresWorker } from './runtime';
import { RuntimeSecretsService } from '@/services/RuntimeSecretsService';
import { RemoteSourceService } from '@/services/RemoteSourceService';
import { adapterCatalog as adapterCatalogData, getSourceCatalogEntry } from './sourceCatalog';

export const adapterCatalog = adapterCatalogData;

const genericDemoMap = {
  'inberlinwohnen-de': demoRaw.inberlinwohnen,
  'degewo-de': demoRaw.degewo,
  'gesobau-de': demoRaw.gesobau,
  'howoge-de': demoRaw.howoge,
  'stadtundland-de': demoRaw.stadtundland,
  'wbm-de': demoRaw.wbm,
  'gewobag-de': demoRaw.gewobag,
  'berlinovo-de': demoRaw.berlinovo,
  'bgg-berlin-student-de': demoRaw.bggBerlinStudent,
  'studierendenwerk-berlin-de': demoRaw.studierendenwerkBerlin,
  'studentendorf-berlin-de': demoRaw.studentendorfBerlin,
  'wunderflats-berlin-de': demoRaw.wunderflatsBerlin,
  'housinganywhere-berlin-de': demoRaw.housingAnywhereBerlin,
  'spotahome-berlin-de': demoRaw.spotahomeBerlin,
  'coming-home-berlin-de': demoRaw.comingHomeBerlin,
  'urbanbnb-berlin-de': demoRaw.urbanbnbBerlin,
} as const;

type GenericDemoKey = keyof typeof genericDemoMap;

function simpleHash(input: string): string {
  let hash = 0;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return `h${Math.abs(hash)}`;
}

function buildListing(fields: {
  sourceId: string;
  sourceListingId: string;
  sourceUrl: string;
  title: string;
  description: string | null;
  city: string;
  district: string | null;
  street: string | null;
  houseNumber: string | null;
  zip: string | null;
  listingType: ListingType;
  propertyType: PropertyType;
  price: number | null;
  sizeSqm: number | null;
  rooms: number | null;
  lat: number | null;
  lng: number | null;
  furnished: boolean | null;
  balcony: boolean | null;
  elevator: boolean | null;
  parking: boolean | null;
  petsAllowed?: boolean | null;
  listedAt: string | null;
  fetchedAt: string;
  images?: string[];
  contactName?: string | null;
  country?: string;
}): NormalizedListing {
  const dedupeKey = `${fields.city.toLowerCase()}|${(fields.street ?? '').toLowerCase()}|${Math.round((fields.price ?? 0) / 10) * 10}`;
  const canonicalId = simpleHash(dedupeKey);
  const id = simpleHash(`${fields.sourceId}:${fields.sourceListingId}`);
  return {
    id,
    canonicalId,
    dedupeKey,
    sourceId: fields.sourceId,
    sourceListingId: fields.sourceListingId,
    sourceUrl: fields.sourceUrl,
    alternateSourceUrls: [],
    listingType: fields.listingType,
    propertyType: fields.propertyType,
    title: fields.title,
    description: fields.description,
    price: fields.price,
    priceCurrency: 'EUR',
    priceType: fields.listingType === 'rent' ? 'per_month' : 'total',
    pricePerSqm: fields.price && fields.sizeSqm ? Math.round(fields.price / fields.sizeSqm) : null,
    country: fields.country ?? 'DE',
    city: fields.city,
    district: fields.district,
    street: fields.street,
    houseNumber: fields.houseNumber,
    zip: fields.zip,
    lat: fields.lat,
    lng: fields.lng,
    sizeSqm: fields.sizeSqm,
    rooms: fields.rooms,
    furnished: fields.furnished,
    petsAllowed: fields.petsAllowed ?? null,
    parking: fields.parking,
    balcony: fields.balcony,
    elevator: fields.elevator,
    images: fields.images ?? [],
    thumbnailUrl: fields.images?.[0] ?? null,
    contactName: fields.contactName ?? null,
    freshnessScore: 0,
    completenessScore: 0,
    trustScore: 0,
    isSuspicious: false,
    suspiciousReasons: [],
    listedAt: fields.listedAt,
    fetchedAt: fields.fetchedAt,
    lastSeenAt: fields.fetchedAt,
    isActive: true,
  };
}

function score(listing: NormalizedListing): NormalizedListing {
  const completenessBase = [listing.title, listing.price, listing.city, listing.sizeSqm, listing.rooms, listing.thumbnailUrl, listing.lat, listing.lng]
    .filter((value) => value !== null && value !== undefined && value !== '').length;
  const completenessScore = Math.round((completenessBase / 8) * 100);
  const freshnessScore = listing.listedAt ? Math.max(20, 100 - Math.floor((Date.now() - new Date(listing.listedAt).getTime()) / 86400000) * 8) : 55;
  const trustScore = Math.min(100, Math.round(completenessScore * 0.55 + freshnessScore * 0.35 + (listing.images.length > 0 ? 10 : 0)));
  const suspiciousReasons = [];
  if (listing.images.length === 0) suspiciousReasons.push('No images attached');
  if (!listing.street && !listing.district) suspiciousReasons.push('Weak location details');
  if ((listing.price ?? 0) < 500 && (listing.sizeSqm ?? 0) > 45) suspiciousReasons.push('Price looks unusually low');
  return { ...listing, completenessScore, freshnessScore, trustScore, suspiciousReasons, isSuspicious: suspiciousReasons.length > 0 };
}

function simulateFetch<T>(items: T[], config: Record<string, string>, cityGetter: (item: T) => string, typeGetter: (item: T) => string) {
  const city = config.city?.toLowerCase();
  const listingType = config.listingType;
  return items.filter((item) => {
    const matchesCity = city && city !== 'any city' ? cityGetter(item).toLowerCase().includes(city) : true;
    const matchesType = listingType && listingType !== 'both' ? typeGetter(item) === listingType : true;
    return matchesCity && matchesType;
  }).slice(0, Number(config.maxResults ?? '40'));
}

function mapGenericDemo(
  adapterId: string,
  items: readonly {
    id: string;
    title: string;
    description: string | null;
    city: string;
    district: string | null;
    street: string | null;
    houseNumber: string | null;
    zip: string | null;
    type: ListingType;
    propertyType: PropertyType;
    price: number | null;
    sizeSqm: number | null;
    rooms: number | null;
    lat: number | null;
    lng: number | null;
    furnished: boolean | null;
    balcony: boolean | null;
    elevator: boolean | null;
    parking: boolean | null;
    listedAt: string | null;
    url: string;
    images: string[];
    contactName: string | null;
  }[],
  config: Record<string, string>,
) {
  const fetchedAt = new Date().toISOString();
  const entry = getSourceCatalogEntry(adapterId);
  return simulateFetch(items as typeof items[number][], config, (item) => item.city, (item) => item.type).map((raw) =>
    score(
      buildListing({
        sourceId: adapterId,
        sourceListingId: raw.id,
        sourceUrl: raw.url,
        title: raw.title,
        description: raw.description,
        city: raw.city,
        district: raw.district,
        street: raw.street,
        houseNumber: raw.houseNumber,
        zip: raw.zip,
        listingType: raw.type,
        propertyType: raw.propertyType,
        price: raw.price,
        sizeSqm: raw.sizeSqm,
        rooms: raw.rooms,
        lat: raw.lat,
        lng: raw.lng,
        furnished: raw.furnished,
        balcony: raw.balcony,
        elevator: raw.elevator,
        parking: raw.parking,
        listedAt: raw.listedAt,
        fetchedAt,
        images: raw.images,
        contactName: raw.contactName,
        country: entry?.country,
      }),
    ),
  );
}

export async function fetchAdapterListings(adapterId: string, config: Record<string, string>): Promise<NormalizedListing[]> {
  const fetchedAt = new Date().toISOString();
  await new Promise((resolve) => setTimeout(resolve, 120));
  switch (adapterId) {
    case 'immoscout-de':
      return simulateFetch(demoRaw.immoscout, config, (item) => item.city, (item) => item.type).map((raw) => score(buildListing({
        sourceId: adapterId, sourceListingId: raw.id, sourceUrl: raw.url, title: raw.title, description: raw.description, city: raw.city, district: raw.district, street: raw.street, houseNumber: raw.houseNumber, zip: raw.zip, listingType: raw.type, propertyType: raw.propertyType, price: raw.price, sizeSqm: raw.sizeSqm, rooms: raw.rooms, lat: raw.lat, lng: raw.lng, furnished: raw.furnished, balcony: raw.balcony, elevator: raw.elevator, parking: raw.parking, listedAt: raw.listedAt, fetchedAt, images: raw.images, contactName: raw.contactName, country: 'DE',
      })));
    case 'immowelt-de':
      return simulateFetch(demoRaw.immowelt, config, (item) => item.municipality, (item) => item.mode).map((raw) => score(buildListing({
        sourceId: adapterId, sourceListingId: raw.uid, sourceUrl: raw.sourceUrl, title: raw.headline, description: raw.body, city: raw.municipality, district: raw.neighborhood, street: raw.street, houseNumber: raw.houseNumber, zip: raw.postalCode, listingType: raw.mode, propertyType: raw.category, price: raw.totalPrice, sizeSqm: raw.livingArea, rooms: raw.roomCount, lat: raw.geo.lat, lng: raw.geo.lng, furnished: raw.amenities.furnished, balcony: raw.amenities.balcony, elevator: raw.amenities.elevator, parking: raw.amenities.parking, listedAt: raw.postedAt, fetchedAt, images: raw.imageUrls, contactName: raw.agent, country: 'DE',
      })));
    case 'immonet-de':
      return simulateFetch(demoRaw.immonet, config, (item) => item.municipality, (item) => item.mode).map((raw) => score(buildListing({
        sourceId: adapterId, sourceListingId: raw.listingId, sourceUrl: raw.sourceUrl, title: raw.headline, description: raw.descriptionText, city: raw.municipality, district: raw.area, street: raw.addressLine.split(' ')[0], houseNumber: raw.addressLine.split(' ')[1] ?? null, zip: raw.postalCode, listingType: raw.mode, propertyType: raw.category, price: raw.totalPrice, sizeSqm: raw.livingArea, rooms: raw.roomCount, lat: raw.geo.lat, lng: raw.geo.lng, furnished: raw.amenities.furnished, balcony: raw.amenities.balcony, elevator: raw.amenities.elevator, parking: raw.amenities.parking, listedAt: raw.postedAt, fetchedAt, images: raw.imageUrls, contactName: raw.agent, country: 'DE',
      })));
    case 'wg-gesucht-de':
      return simulateFetch(demoRaw.wgGesucht, config, (item) => item.cityName, (item) => item.listingMode).map((raw) => score(buildListing({
        sourceId: adapterId, sourceListingId: raw.offerId, sourceUrl: raw.href, title: raw.title, description: raw.body, city: raw.cityName, district: raw.districtName, street: raw.streetName, houseNumber: null, zip: raw.zipCode, listingType: raw.listingMode, propertyType: raw.kind, price: raw.rent, sizeSqm: raw.areaSqm, rooms: raw.rooms, lat: raw.coordinates[0], lng: raw.coordinates[1], furnished: raw.extras.includes('furnished'), balcony: raw.extras.includes('balcony'), elevator: false, parking: false, petsAllowed: raw.petsAllowed, listedAt: raw.datePublished, fetchedAt, images: raw.photos, contactName: raw.contact, country: 'DE',
      })));
    case 'ebay-kleinanzeigen-de':
      return simulateFetch(demoRaw.kleinanzeigen, config, (item) => item.place, (item) => item.purpose).map((raw) => score(buildListing({
        sourceId: adapterId, sourceListingId: raw.adId, sourceUrl: raw.link, title: raw.name, description: raw.text, city: raw.place, district: raw.quarter, street: raw.street, houseNumber: null, zip: raw.postcode, listingType: raw.purpose, propertyType: raw.unitType, price: raw.amount, sizeSqm: raw.sqm, rooms: raw.rooms, lat: raw.coords.lat, lng: raw.coords.lng, furnished: raw.furnished, balcony: raw.balcony, elevator: false, parking: raw.parking, listedAt: raw.createdAt, fetchedAt, images: raw.media, contactName: raw.seller, country: 'DE',
      })));
    case 'wohnungsboerse-de':
      return simulateFetch(demoRaw.wohnungsboerse, config, (item) => item.city, (item) => item.offerType).map((raw) => score(buildListing({
        sourceId: adapterId, sourceListingId: raw.uid, sourceUrl: raw.url, title: raw.heading, description: raw.summary, city: raw.city, district: raw.district, street: raw.streetAddress.split(' ')[0], houseNumber: raw.streetAddress.split(' ')[1] ?? null, zip: raw.postal, listingType: raw.offerType, propertyType: raw.estateType, price: raw.monthlyPrice, sizeSqm: raw.area, rooms: raw.roomAmount, lat: raw.latitude, lng: raw.longitude, furnished: raw.furnished, balcony: raw.balcony, elevator: true, parking: raw.parking, petsAllowed: raw.pets, listedAt: raw.listedOn, fetchedAt, images: raw.images, contactName: raw.contactName, country: 'DE',
      })));
    case 'local-agency-rss-de':
      return mapGenericDemo(adapterId, demoRaw.localAgencyRss, config);
    default:
      if (adapterId in genericDemoMap) {
        return mapGenericDemo(adapterId, genericDemoMap[adapterId as GenericDemoKey], config);
      }
      return [];
  }
}

function resolveExecutionMode(source: Source, runtimeMode: 'demo' | 'worker_hybrid' | 'worker_only') {
  const sourceMode = source.config.executionMode;
  if (sourceMode === 'demo' || sourceMode === 'worker') return sourceMode;
  if (runtimeMode === 'worker_only') return 'worker';
  if (runtimeMode === 'worker_hybrid') return 'worker';
  return 'demo';
}

export async function fetchSourceListings(source: Source): Promise<NormalizedListing[]> {
  const runtime = useRuntimeStore.getState();
  const sourceMode = resolveExecutionMode(source, runtime.mode);

  if (sourceMode === 'worker') {
    const token = await RuntimeSecretsService.getWorkerToken();
    try {
      return await RemoteSourceService.fetchListings(source, runtime, token);
    } catch (error) {
      if (!runtime.allowDemoFallback || runtime.mode === 'worker_only') throw error;
      return fetchAdapterListings(source.adapterId, source.config);
    }
  }

  if (requiresWorker(runtime) && runtime.allowDemoFallback === false) {
    throw new Error('Runtime requires worker-backed fetching but this source is set to demo');
  }

  return fetchAdapterListings(source.adapterId, source.config);
}

export async function testAdapterConnection(adapterId: string, config: Record<string, string>) {
  const listings = await fetchAdapterListings(adapterId, config);
  return {
    ok: listings.length > 0,
    listingCount: listings.length,
    sampleTitle: listings[0]?.title ?? null,
    mode: 'demo' as const,
  };
}

export async function testSourceConnection(source: Source) {
  const runtime = useRuntimeStore.getState();
  const token = await RuntimeSecretsService.getWorkerToken();
  const sourceMode = resolveExecutionMode(source, runtime.mode);
  if (sourceMode === 'worker') {
    const startedAt = Date.now();
    const listings = await RemoteSourceService.fetchListings(source, runtime, token);
    return {
      ok: listings.length > 0,
      listingCount: listings.length,
      sampleTitle: listings[0]?.title ?? null,
      latencyMs: Date.now() - startedAt,
      mode: 'worker' as const,
    };
  }
  const listings = await fetchAdapterListings(source.adapterId, source.config);
  return {
    ok: listings.length > 0,
    listingCount: listings.length,
    sampleTitle: listings[0]?.title ?? null,
    latencyMs: 120,
    mode: 'demo' as const,
  };
}

export function deduplicate(listings: NormalizedListing[]) {
  const map = new Map<string, NormalizedListing[]>();
  for (const listing of listings) {
    const bucket = map.get(listing.dedupeKey) ?? [];
    bucket.push(listing);
    map.set(listing.dedupeKey, bucket);
  }
  return Array.from(map.values()).map((items) => {
    const best = [...items].sort((a, b) => (b.trustScore + b.freshnessScore) - (a.trustScore + a.freshnessScore))[0];
    return {
      ...best,
      alternateSourceUrls: items
        .filter((item) => item.id !== best.id)
        .map((item) => ({ sourceId: item.sourceId, url: item.sourceUrl })),
    };
  });
}
