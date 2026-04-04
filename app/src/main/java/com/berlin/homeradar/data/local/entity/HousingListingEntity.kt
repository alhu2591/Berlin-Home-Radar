package com.berlin.homeradar.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان قاعدة البيانات لإعلان السكن في جدول Room.
 *
 * ## الفهارس (Indices):
 * - `(source, externalId)` **UNIQUE**: يمنع تكرار نفس الإعلان من نفس المصدر.
 * - `fingerprint`: يُسرّع البحث عن الإعلانات المتشابهة من مصادر مختلفة.
 * - `isActive`: يُسرّع استعلامات الفلترة على الإعلانات النشطة فقط.
 * - `lifecycleStatus`: يُسرّع الاستعلامات بحسب مرحلة دورة حياة الإعلان.
 *
 * ## الفرق عن [com.berlin.homeradar.domain.model.HousingListing]:
 * - يحتوي على حقول مُطبَّعة ([titleNormalized], [districtNormalized]) للبحث في SQL.
 * - [lifecycleStatus] مُخزَّن كـ String بدلاً من Enum للتوافق مع Room.
 * - [fingerprint] لإزالة التكرار بين المصادر المختلفة.
 *
 * @property id المعرّف الأساسي التلقائي. يبدأ من 0 قبل الإدراج.
 * @property source معرّف المصدر (مثل "wbm", "degewo").
 * @property externalId المعرّف الخارجي في موقع المصدر، فريد ضمن نفس المصدر.
 * @property titleNormalized عنوان مُطبَّع (lowercase, مسافات منظّفة) للبحث النصي السريع.
 * @property districtNormalized حي مُطبَّع بنفس الطريقة للفلترة الدقيقة.
 * @property fingerprint بصمة المحتوى لكشف التكرار عبر المصادر المختلفة.
 * @property lifecycleStatus حالة دورة الحياة كنص ("active", "inactive", ...).
 */
@Entity(
    tableName = "housing_listing",
    indices = [
        Index(value = ["source", "externalId"], unique = true),
        Index(value = ["fingerprint"], unique = false),
        Index(value = ["isActive"]),
        Index(value = ["lifecycleStatus"]),
    ],
)
data class HousingListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val source: String,
    val externalId: String,
    val title: String,
    val titleNormalized: String,
    val priceEuro: Int,
    val district: String,
    val districtNormalized: String,
    val location: String,
    val rooms: Double,
    val areaSqm: Double,
    val imageUrl: String?,
    val listingUrl: String,
    val isJobcenterSuitable: Boolean,
    val isWohngeldEligible: Boolean,
    val isWbsRequired: Boolean,
    val isFavorite: Boolean = false,
    val fingerprint: String,
    val updatedAtEpochMillis: Long,
    val lastSeenAtEpochMillis: Long = updatedAtEpochMillis,
    val isActive: Boolean = true,
    val lifecycleStatus: String = "active",
)
