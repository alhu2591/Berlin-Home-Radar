package com.berlin.homeradar.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.berlin.homeradar.data.local.AppDatabase
import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SourceMetricDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.remote.api.RemoteListingsService
import com.berlin.homeradar.data.telemetry.AnalyticsLogger
import com.berlin.homeradar.data.telemetry.CrashReporter
import com.berlin.homeradar.data.telemetry.NetworkAnalyticsLogger
import com.berlin.homeradar.data.telemetry.NetworkCrashReporter
import com.berlin.homeradar.data.repository.HousingRepositoryImpl
import com.berlin.homeradar.domain.repository.HousingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * وحدة Hilt الرئيسية لتوفير (Provide) التبعيات المشتركة على مستوى التطبيق.
 *
 * مُثبَّتة في [SingletonComponent] أي أن جميع ما توفّره حياته مرتبطة بحياة التطبيق.
 *
 * ## ما توفّره:
 * - **قاعدة البيانات** ([AppDatabase]) وجميع الـ DAOs.
 * - **WorkManager** مُهيَّأ مع ApplicationContext.
 * - **Json** (Kotlinx Serialization) مع `ignoreUnknownKeys`.
 * - **OkHttpClient** مع timeouts مناسبة وـ User-Agent ثابت.
 * - **Retrofit** مُهيَّأ لجلب الإعلانات البعيدة.
 *
 * @see AppBindsModule للـ bindings التجريدية (interface → implementation).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    /**
     * يُوفّر قاعدة البيانات الرئيسية للتطبيق.
     *
     * تشمل [MIGRATION_1_3] ترقية الجدول لإضافة:
     * - `lastSeenAtEpochMillis`, `isActive`, `lifecycleStatus` لإدارة دورة حياة الإعلانات.
     * - جدول `source_metric` جديد لتتبع أداء كل مصدر.
     *
     * **ملاحظة**: الهجرة تنتقل مباشرة من الإصدار 1 إلى 3 (تجاوز 2) لدمج هجرتين سابقتين.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "berlin_home_radar.db",
        )
            .addMigrations(MIGRATION_1_3)
            .build()
    }

    /** يُوفّر [HousingListingDao] من قاعدة البيانات المُحقنة. */
    @Provides
    fun provideHousingListingDao(database: AppDatabase): HousingListingDao = database.housingListingDao()

    /** يُوفّر [SyncStatusDao] من قاعدة البيانات المُحقنة. */
    @Provides
    fun provideSyncStatusDao(database: AppDatabase): SyncStatusDao = database.syncStatusDao()

    /** يُوفّر [SourceMetricDao] من قاعدة البيانات المُحقنة. */
    @Provides
    fun provideSourceMetricDao(database: AppDatabase): SourceMetricDao = database.sourceMetricDao()

    /** يُوفّر WorkManager Singleton مُهيَّأ بالـ ApplicationContext. */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    /**
     * يُوفّر مُشغّل JSON مُهيَّأ مع [ignoreUnknownKeys] = true
     * للتسامح مع حقول جديدة قد يُضيفها API في المستقبل بدون كسر التطبيق.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    /**
     * يُوفّر [OkHttpClient] مُهيَّأ لطلبات الشبكة.
     *
     * الإعدادات:
     * - `retryOnConnectionFailure`: إعادة المحاولة تلقائياً عند انقطاع الاتصال.
     * - Timeouts: 20 ثانية للاتصال، 30 ثانية للقراءة والإرسال.
     * - User-Agent ثابت لتعريف التطبيق للمواقع.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .callTimeout(java.time.Duration.ofSeconds(30))
        .connectTimeout(java.time.Duration.ofSeconds(20))
        .readTimeout(java.time.Duration.ofSeconds(30))
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "BerlinHomeRadar/1.2 (Android)")
                .build()
            chain.proceed(request)
        }
        .build()

    /**
     * يُوفّر [Retrofit] مُهيَّأ للتواصل مع Remote Listings API.
     *
     * **ملاحظة**: [baseUrl] placeholder فقط، العنوان الفعلي يُحدَّد عبر Remote Config.
     */
    @Provides
    @Singleton
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /** يُوفّر [RemoteListingsService] من Retrofit المُحقن. */
    @Provides
    @Singleton
    fun provideRemoteListingsService(retrofit: Retrofit): RemoteListingsService {
        return retrofit.create(RemoteListingsService::class.java)
    }

    /**
     * هجرة قاعدة البيانات من الإصدار 1 إلى 3.
     *
     * ## التغييرات:
     * - إضافة `lastSeenAtEpochMillis` لتتبع آخر ظهور للإعلان في المزامنة.
     * - إضافة `isActive` و `lifecycleStatus` لإدارة الإعلانات المنتهية.
     * - إنشاء فهارس لـ `isActive` و `lifecycleStatus` لأداء استعلامات أفضل.
     * - إنشاء جدول `source_metric` جديد لتتبع أداء كل مصدر عبر الزمن.
     */
    private val MIGRATION_1_3 = object : Migration(1, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE housing_listing ADD COLUMN lastSeenAtEpochMillis INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE housing_listing SET lastSeenAtEpochMillis = updatedAtEpochMillis")
            db.execSQL("ALTER TABLE housing_listing ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE housing_listing ADD COLUMN lifecycleStatus TEXT NOT NULL DEFAULT 'active'")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_housing_listing_isActive ON housing_listing(isActive)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_housing_listing_lifecycleStatus ON housing_listing(lifecycleStatus)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS source_metric (
                    sourceId TEXT NOT NULL PRIMARY KEY,
                    successCount INTEGER NOT NULL DEFAULT 0,
                    failureCount INTEGER NOT NULL DEFAULT 0,
                    lastAttemptMillis INTEGER,
                    lastSuccessfulPullMillis INTEGER,
                    lastItemCount INTEGER NOT NULL DEFAULT 0,
                    averageItemCount REAL NOT NULL DEFAULT 0.0,
                    averageDurationMillis REAL NOT NULL DEFAULT 0.0,
                    consecutiveZeroItemPulls INTEGER NOT NULL DEFAULT 0,
                    lastErrorMessage TEXT
                )
                """
            )
        }
    }
}

/**
 * وحدة Hilt لربط الـ interfaces بتنفيذاتها عبر [@Binds].
 *
 * يُفضَّل [@Binds] على [@Provides] للـ bindings لأنه لا ينشئ كائناً جديداً،
 * بل يُخبر Hilt فقط أن [HousingRepositoryImpl] هو التنفيذ لـ [HousingRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    /** يربط [HousingRepositoryImpl] كتنفيذ لـ [HousingRepository]. */
    @Binds
    abstract fun bindHousingRepository(impl: HousingRepositoryImpl): HousingRepository

    /** يربط [NetworkAnalyticsLogger] كتنفيذ لـ [AnalyticsLogger]. */
    @Binds
    abstract fun bindAnalyticsLogger(impl: NetworkAnalyticsLogger): AnalyticsLogger

    /** يربط [NetworkCrashReporter] كتنفيذ لـ [CrashReporter]. */
    @Binds
    abstract fun bindCrashReporter(impl: NetworkCrashReporter): CrashReporter
}
