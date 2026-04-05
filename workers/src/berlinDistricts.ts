const DISTRICT_ALIASES_RAW: Record<string, string[]> = {
  mitte: ['berlin mitte', 'mitte', 'tiergarten', 'moabit', 'wedding', 'gesundbrunnen'],
  friedrichshain: ['friedrichshain', 'berlin friedrichshain'],
  kreuzberg: ['kreuzberg', 'berlin kreuzberg'],
  'friedrichshain-kreuzberg': ['friedrichshain-kreuzberg', 'friedrichshain kreuzberg', 'kreuzberg/friedrichshain'],
  pankow: ['pankow', 'berlin pankow', 'prenzlauer berg', 'weissensee', 'weißensee', 'karow', 'buch'],
  charlottenburg: ['charlottenburg', 'berlin charlottenburg'],
  wilmersdorf: ['wilmersdorf', 'berlin wilmersdorf'],
  'charlottenburg-wilmersdorf': ['charlottenburg-wilmersdorf', 'charlottenburg wilmersdorf'],
  spandau: ['spandau', 'berlin spandau'],
  steglitz: ['steglitz', 'berlin steglitz'],
  zehlendorf: ['zehlendorf', 'berlin zehlendorf'],
  'steglitz-zehlendorf': ['steglitz-zehlendorf', 'steglitz zehlendorf', 'schlachtensee', 'dahlem', 'lichterfelde'],
  tempelhof: ['tempelhof', 'berlin tempelhof'],
  schoeneberg: ['schoneberg', 'schöneberg', 'berlin schoeneberg', 'berlin schöneberg'],
  'tempelhof-schoeneberg': ['tempelhof-schoeneberg', 'tempelhof-schoneberg', 'tempelhof schoeneberg', 'tempelhof schöneberg'],
  neukoelln: ['neukolln', 'neukölln', 'berlin neukolln', 'berlin neukölln', 'britz', 'rudow', 'buckow'],
  treptow: ['treptow', 'berlin treptow'],
  koepenick: ['koepenick', 'köpenick', 'berlin koepenick', 'berlin köpenick'],
  'treptow-koepenick': ['treptow-koepenick', 'treptow-kopenick', 'treptow köpenick', 'treptow koepenick', 'oberschoneweide', 'oberschöneweide', 'adlershof'],
  marzahn: ['marzahn', 'berlin marzahn'],
  hellersdorf: ['hellersdorf', 'berlin hellersdorf'],
  'marzahn-hellersdorf': ['marzahn-hellersdorf', 'marzahn hellersdorf'],
  lichtenberg: ['lichtenberg', 'berlin lichtenberg', 'friedrichsfelde', 'hohenschoenhausen', 'hohenschönhausen'],
  reinickendorf: ['reinickendorf', 'berlin reinickendorf', 'tegel', 'wittenau'],
};

export function normalizeBerlinToken(value: string | null | undefined) {
  return (value ?? '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/ß/g, 'ss')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

export function districtAliasesFor(district: string | null | undefined) {
  const normalized = normalizeBerlinToken(district);
  if (!normalized) return [];
  return DISTRICT_ALIASES[normalized] ?? [normalized];
}

export function berlinDistrictMatches(text: string | null | undefined, district: string | null | undefined) {
  const haystack = normalizeBerlinToken(text);
  if (!district) return true;
  const aliases = districtAliasesFor(district);
  if (!aliases.length) return true;
  return aliases.some((alias) => haystack.includes(normalizeBerlinToken(alias)));
}

const DISTRICT_ALIASES: Record<string, string[]> = Object.fromEntries(
  Object.entries(DISTRICT_ALIASES_RAW).map(([key, values]) => [normalizeBerlinToken(key), values.map((item) => normalizeBerlinToken(item))])
);
