import { SOURCE_PROFILES, getSourceProfile } from './profiles';

function assert(condition: unknown, message: string) {
  if (!condition) throw new Error(message);
}

assert(SOURCE_PROFILES.length >= 23, 'expected broad Berlin source coverage');
assert(!!getSourceProfile('immoscout-de'), 'missing immoscout profile');
assert(getSourceProfile('local-agency-rss-de')?.supportsRss === true, 'rss profile must support RSS');
console.log('profiles ok');
