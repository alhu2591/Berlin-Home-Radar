package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.source.model.RawListing
import com.berlin.homeradar.domain.model.ListingLifecycleStatus
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * سياسة إزالة التكرار (Deduplication) بين الإعلانات المجلوبة والمحفوظة.
 *
 * ## مشكلة التكرار:
 * نفس الشقة قد تظهر في مصادر متعددة أو تتغير بيانات إعلانها (سعر، عنوان) بين عمليات الجلب.
 * بدون هذا الكلاس، سينتهي الأمر بتكرارات أو إعلانات قديمة لا تزال ظاهرة.
 *
 * ## آلية Fingerprint:
 * بدلاً من الاعتماد فقط على (source + externalId)، يُنشئ [fingerprint] بصمة من:
 * **العنوان المُنظَّم + السعر + المساحة المقرَّبة + الغرف المقرَّبة + الحي المُنظَّم**
 *
 * يساعد ذلك على كشف نفس الشقة المعلَن عنها بمعرّفات خارجية مختلفة.
 *
 * ## التطبيع (Normalize):
 * تُحوَّل جميع النصوص إلى lowercase بـ Locale.GERMANY للتعامل الصحيح مع الأحرف الألمانية
 * (ä→ä, ö→ö, ü→ü) وإزالة المسافات المتكررة.
 */
@Singleton
class DeduplicationPolicy @Inject constructor() {

    /**
     * يُنشئ بصمة فريدة للإعلان تعتمد على محتواه لا على معرّفه الخارجي.
     *
     * صيغة البصمة: `normalizedTitle|price|roundedArea|roundedRooms×10|normalizedDistrict`
     *
     * - المساحة تُقرَّب لأقرب متر صحيح.
     * - الغرف تُضرب في 10 وتُقرَّب للتعامل مع الكسور (مثل 1.5 غرفة → 15).
     *
     * @param listing الإعلان الخام المجلوب من المصدر.
     * @return سلسلة نصية تمثل البصمة الفريدة للإعلان.
     */
    fun fingerprint(listing: RawListing): String {
        val title = normalize(listing.title)
        val district = normalize(listing.district)
        val area = listing.areaSqm.roundToInt()
        val rooms = (listing.rooms * 10.0).roundToInt()
        return "$title|${listing.priceEuro}|$area|$rooms|$district"
    }

    /**
     * يُطبّع نصاً للمقارنة المتسقة: trim + lowercase بألمانية + تقليل المسافات.
     *
     * @param value النص المراد تطبيعه.
     * @return النص بعد التطبيع.
     */
    fun normalize(value: String): String = value
        .trim()
        .lowercase(Locale.GERMANY)
        .replace(Regex("\\s+"), " ")

    /**
     * يدمج بيانات إعلان موجود مع بيانات إعلان قادم جديد من المصدر.
     *
     * يحتفظ بـ:
     * - [HousingListingEntity.id] الأصلي للحفاظ على الاستمرارية في قاعدة البيانات.
     * - [HousingListingEntity.isFavorite] الأصلي حتى لا يفقد المستخدم مفضلاته.
     * - الصورة القديمة إذا لم يوفّر المصدر الجديد صورة.
     *
     * @param existing الكيان المحفوظ حالياً في قاعدة البيانات.
     * @param incoming الإعلان الجديد القادم من عملية الجلب.
     * @param now وقت المزامنة الحالية بالميلي ثانية.
     * @return الكيان المحدَّث الجاهز للحفظ في قاعدة البيانات.
     */
    fun merge(existing: HousingListingEntity, incoming: RawListing, now: Long): HousingListingEntity {
        return existing.copy(
            title = incoming.title,
            titleNormalized = normalize(incoming.title),
            priceEuro = incoming.priceEuro,
            district = incoming.district,
            districtNormalized = normalize(incoming.district),
            location = incoming.location,
            rooms = incoming.rooms,
            areaSqm = incoming.areaSqm,
            imageUrl = incoming.imageUrl ?: existing.imageUrl,
            listingUrl = incoming.listingUrl,
            isJobcenterSuitable = incoming.isJobcenterSuitable,
            isWohngeldEligible = incoming.isWohngeldEligible,
            isWbsRequired = incoming.isWbsRequired,
            fingerprint = fingerprint(incoming),
            updatedAtEpochMillis = now,
            lastSeenAtEpochMillis = now,
            isActive = true,
            lifecycleStatus = ListingLifecycleStatus.ACTIVE.storageValue,
        )
    }

    /**
     * يُحوّل إعلاناً خاماً [RawListing] إلى كيان قاعدة بيانات [HousingListingEntity].
     *
     * يُستخدم عند إدراج إعلان **جديد** لم يُوجد مسبقاً في قاعدة البيانات.
     *
     * @param raw الإعلان الخام من المصدر.
     * @param existingId معرّف الكيان إن وُجد مسبقاً (للتحديث)، أو null للإدراج كجديد.
     * @param isFavorite حالة المفضلة المحفوظة مسبقاً للإعلان (يُحفظ دائماً).
     * @param now وقت عملية الجلب الحالية بالميلي ثانية.
     * @return كيان قاعدة البيانات الجاهز للإدراج أو التحديث.
     */
    fun toEntity(raw: RawListing, existingId: Long?, isFavorite: Boolean, now: Long): HousingListingEntity {
        return HousingListingEntity(
            id = existingId ?: 0L,
            source = raw.source,
            externalId = raw.externalId,
            title = raw.title,
            titleNormalized = normalize(raw.title),
            priceEuro = raw.priceEuro,
            district = raw.district,
            districtNormalized = normalize(raw.district),
            location = raw.location,
            rooms = raw.rooms,
            areaSqm = raw.areaSqm,
            imageUrl = raw.imageUrl,
            listingUrl = raw.listingUrl,
            isJobcenterSuitable = raw.isJobcenterSuitable,
            isWohngeldEligible = raw.isWohngeldEligible,
            isWbsRequired = raw.isWbsRequired,
            isFavorite = isFavorite,
            fingerprint = fingerprint(raw),
            updatedAtEpochMillis = now,
            lastSeenAtEpochMillis = now,
            isActive = true,
            lifecycleStatus = ListingLifecycleStatus.ACTIVE.storageValue,
        )
    }
}
