import { MMKV } from 'react-native-mmkv';
const storage = new MMKV({ id: 'berlin-home-radar' });

export const kv = {
  getString: (key: string) => storage.getString(key) ?? null,
  setString: (key: string, value: string) => storage.set(key, value),
  getObject<T>(key: string): T | null {
    const raw = storage.getString(key);
    return raw ? (JSON.parse(raw) as T) : null;
  },
  setObject(key: string, value: unknown) {
    storage.set(key, JSON.stringify(value));
  }
};
