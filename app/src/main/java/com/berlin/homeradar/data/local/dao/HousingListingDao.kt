package com.berlin.homeradar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import kotlinx.coroutines.flow.Flow

/**
 * واجهة الوصول لبيانات إعلانات السكن في قاعدة بيانات Room.
 *
 * ## ملاحظات على [observeListings]:
 * - الفلترة تحدث كلياً على مستوى SQL لأداء أمثل.
 * - [sourceFilteringDisabled] تُستخدم لتجاوز شرط `IN (:selectedSourceIds)` عند عدم
 *   تحديد مصادر، إذ أن SQL لا يدعم قائمة فارغة في `IN ()`.
 * - [queryNormalized] و [districtNormalized] مُطبَّعة مسبقاً في [DeduplicationPolicy]
 *   للتوافق مع الحقول [titleNormalized] و [districtNormalized] في الجدول.
 *
 * ## استراتيجية UPSERT:
 * يستخدم [@Upsert] بدلاً من [@Insert] أو [@Update] للتعامل الذكي مع الإدراج والتحديث معاً،
 * بناءً على قيد UNIQUE (source, externalId).
 */
@Dao
interface HousingListingDao {

    /**
     * يراقب الإعلانات النشطة مع تطبيق فلاتر متعددة في استعلام SQL واحد.
     *
     * ترتيب النتائج: الأحدث أولاً، ثم الأرخص عند تساوي الوقت.
     *
     * @param onlyFavorites إذا true، يعرض المفضلة فقط.
     * @param minRooms الحد الأدنى للغرف، أو null لعدم التقييد.
     * @param minArea الحد الأدنى للمساحة بالمتر المربع، أو null.
     * @param maxPrice الحد الأقصى للسعر باليورو، أو null.
     * @param districtNormalized اسم الحي مُطبَّع، أو null.
     * @param queryNormalized نص البحث الحر المُطبَّع، أو "" لعدم التصفية.
     * @param onlyJobcenter إذا true، يعرض فقط ما يناسب Jobcenter.
     * @param onlyWohngeld إذا true، يعرض فقط ما يقبل Wohngeld.
     * @param onlyWbs إذا true، يعرض فقط ما يستلزم WBS.
     * @param sourceFilteringDisabled إذا true، يتجاهل شرط [selectedSourceIds].
     * @param selectedSourceIds قائمة معرّفات المصادر المطلوب تضمينها.
     * @return [Flow] يُبعث بقائمة محدَّثة عند أي تغيير في قاعدة البيانات.
     */
    @Query(
        """
        SELECT * FROM housing_listing
        WHERE isActive = 1
          AND (:onlyFavorites = 0 OR isFavorite = 1)
          AND (:minRooms IS NULL OR rooms >= :minRooms)
          AND (:minArea IS NULL OR areaSqm >= :minArea)
          AND (:maxPrice IS NULL OR priceEuro <= :maxPrice)
          AND (:districtNormalized IS NULL OR districtNormalized = :districtNormalized)
          AND (:onlyJobcenter = 0 OR isJobcenterSuitable = 1)
          AND (:onlyWohngeld = 0 OR isWohngeldEligible = 1)
          AND (:onlyWbs = 0 OR isWbsRequired = 1)
          AND (:sourceFilteringDisabled = 1 OR source IN (:selectedSourceIds))
          AND (
            :queryNormalized = '' OR
            titleNormalized LIKE '%' || :queryNormalized || '%' OR
            districtNormalized LIKE '%' || :queryNormalized || '%' OR
            lower(location) LIKE '%' || :queryNormalized || '%' OR
            lower(source) LIKE '%' || :queryNormalized || '%'
          )
        ORDER BY updatedAtEpochMillis DESC, priceEuro ASC
        """
    )
    fun observeListings(
        onlyFavorites: Boolean,
        minRooms: Double?,
        minArea: Double?,
        maxPrice: Int?,
        districtNormalized: String?,
        queryNormalized: String,
        onlyJobcenter: Boolean,
        onlyWohngeld: Boolean,
        onlyWbs: Boolean,
        sourceFilteringDisabled: Boolean,
        selectedSourceIds: List<String>,
    ): Flow<List<HousingListingEntity>>

    /** يراقب جميع الإعلانات النشطة بلا فلاتر. يُستخدم لاشتقاق قوائم الأحياء والمصادر المتاحة. */
    @Query("SELECT * FROM housing_listing WHERE isActive = 1 ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    fun observeActiveListings(): Flow<List<HousingListingEntity>>

    /** يُرجع جميع الإعلانات (نشطة وغير نشطة). يُستخدم في المزامنة لمقارنة الحالات. */
    @Query("SELECT * FROM housing_listing ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    suspend fun getAll(): List<HousingListingEntity>

    /** يُرجع الإعلانات النشطة فقط. يُستخدم في تقييم تنبيهات البحوث المحفوظة. */
    @Query("SELECT * FROM housing_listing WHERE isActive = 1 ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    suspend fun getAllActive(): List<HousingListingEntity>

    /** يُرجع إعلاناً بمعرّفه الداخلي، أو null إن لم يُوجد. */
    @Query("SELECT * FROM housing_listing WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HousingListingEntity?

    /** يُرجع إعلاناً بمعرّف المصدر والمعرّف الخارجي معاً (زوج فريد). */
    @Query("SELECT * FROM housing_listing WHERE source = :source AND externalId = :externalId LIMIT 1")
    suspend fun getBySourceAndExternalId(source: String, externalId: String): HousingListingEntity?

    /** يُرجع أحدث إعلان يحمل بصمة [fingerprint] معينة. يُستخدم لكشف التكرار عبر المصادر. */
    @Query("SELECT * FROM housing_listing WHERE fingerprint = :fingerprint ORDER BY updatedAtEpochMillis DESC LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): HousingListingEntity?

    /**
     * يُدرج إعلاناً جديداً أو يُحدّث موجوداً بناءً على قيد UNIQUE.
     * @return معرّف الصف المُدرَج أو المُحدَّث.
     */
    @Upsert
    suspend fun upsert(entity: HousingListingEntity): Long

    /** يُدرج أو يُحدّث قائمة إعلانات دفعةً واحدة. */
    @Upsert
    suspend fun upsertAll(entities: List<HousingListingEntity>)

    /**
     * يعكس حالة المفضلة للإعلان بمعرّفه.
     * إذا كان في المفضلة يُزيله، وإذا لم يكن يُضيفه.
     */
    @Query("UPDATE housing_listing SET isFavorite = NOT isFavorite WHERE id = :listingId")
    suspend fun toggleFavorite(listingId: Long)

    /** يراقب قائمة أسماء الأحياء الفريدة للإعلانات النشطة. يُستخدم لملء قائمة الفلترة بالحي. */
    @Query("SELECT DISTINCT district FROM housing_listing WHERE isActive = 1 ORDER BY district ASC")
    fun observeDistricts(): Flow<List<String>>
}
