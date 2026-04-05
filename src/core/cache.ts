import * as SQLite from 'expo-sqlite';
import type { CacheStats, SyncHistoryItem, SyncSummary } from '@/types/sync';
import type { NormalizedListing } from '@/types/listing';

let databasePromise: Promise<SQLite.SQLiteDatabase> | null = null;

async function getDb() {
  if (!databasePromise) databasePromise = SQLite.openDatabaseAsync('homesync.db');
  return databasePromise;
}

export const CacheService = {
  async init() {
    const db = await getDb();
    await db.execAsync(`
      CREATE TABLE IF NOT EXISTS listings (
        id TEXT PRIMARY KEY NOT NULL,
        canonical_id TEXT NOT NULL,
        source_id TEXT NOT NULL,
        city TEXT NOT NULL,
        fetched_at TEXT NOT NULL,
        full_data TEXT NOT NULL
      );
      CREATE TABLE IF NOT EXISTS meta (
        key TEXT PRIMARY KEY NOT NULL,
        value TEXT NOT NULL
      );
      CREATE TABLE IF NOT EXISTS sync_history (
        id TEXT PRIMARY KEY NOT NULL,
        finished_at TEXT NOT NULL,
        summary_json TEXT NOT NULL
      );
    `);
  },
  async upsertListings(listings: NormalizedListing[]) {
    await this.init();
    const db = await getDb();
    for (const listing of listings) {
      await db.runAsync(
        `INSERT OR REPLACE INTO listings (id, canonical_id, source_id, city, fetched_at, full_data) VALUES (?, ?, ?, ?, ?, ?)`,
        listing.id, listing.canonicalId, listing.sourceId, listing.city, listing.fetchedAt, JSON.stringify(listing)
      );
    }
  },
  async getListings(): Promise<NormalizedListing[]> {
    await this.init();
    const db = await getDb();
    const rows = await db.getAllAsync<{ full_data: string }>('SELECT full_data FROM listings ORDER BY fetched_at DESC');
    return rows.map((row) => JSON.parse(row.full_data) as NormalizedListing);
  },
  async setSyncSummary(summary: SyncSummary) {
    await this.setMeta('last_sync_summary', summary);
  },
  async getSyncSummary(): Promise<SyncSummary | null> {
    return this.getMeta<SyncSummary>('last_sync_summary');
  },
  async addSyncHistory(summary: SyncSummary) {
    await this.init();
    const db = await getDb();
    const item: SyncHistoryItem = { id: `sync-${Date.now()}`, ...summary };
    await db.runAsync('INSERT OR REPLACE INTO sync_history (id, finished_at, summary_json) VALUES (?, ?, ?)', item.id, item.finishedAt, JSON.stringify(item));
    await db.runAsync(`DELETE FROM sync_history WHERE id NOT IN (
      SELECT id FROM sync_history ORDER BY finished_at DESC LIMIT 20
    )`);
  },
  async getSyncHistory(limit = 10): Promise<SyncHistoryItem[]> {
    await this.init();
    const db = await getDb();
    const rows = await db.getAllAsync<{ summary_json: string }>('SELECT summary_json FROM sync_history ORDER BY finished_at DESC LIMIT ?', limit);
    return rows.map((row) => JSON.parse(row.summary_json) as SyncHistoryItem);
  },
  async setMeta<T>(key: string, value: T) {
    await this.init();
    const db = await getDb();
    await db.runAsync('INSERT OR REPLACE INTO meta (key, value) VALUES (?, ?)', key, JSON.stringify(value));
  },
  async getMeta<T>(key: string): Promise<T | null> {
    await this.init();
    const db = await getDb();
    const row = await db.getFirstAsync<{ value: string }>('SELECT value FROM meta WHERE key = ?', key);
    return row ? JSON.parse(row.value) as T : null;
  },
  async getStats(): Promise<CacheStats> {
    const listings = await this.getListings();
    const summary = await this.getSyncSummary();
    const approximateSizeKb = Math.round(JSON.stringify(listings).length / 1024);
    return {
      listingCount: listings.length,
      approximateSizeKb,
      lastSyncAt: summary?.finishedAt ?? null,
    };
  },
  async clearListings() {
    await this.init();
    const db = await getDb();
    await db.runAsync('DELETE FROM listings');
  },
  async clearAll() {
    await this.init();
    const db = await getDb();
    await db.runAsync('DELETE FROM listings');
    await db.runAsync('DELETE FROM meta');
    await db.runAsync('DELETE FROM sync_history');
  }
};
