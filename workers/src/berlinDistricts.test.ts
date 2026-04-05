import { berlinDistrictMatches } from './berlinDistricts';

function assert(condition: unknown, message: string) {
  if (!condition) throw new Error(message);
}

assert(berlinDistrictMatches('Neukölln, Berlin', 'Neukoelln'), 'umlaut alias should match');
assert(berlinDistrictMatches('Schlachtensee Berlin', 'Steglitz-Zehlendorf'), 'district alias should match');
assert(!berlinDistrictMatches('Pankow Berlin', 'Mitte'), 'different districts should not match');
console.log('district matching ok');
