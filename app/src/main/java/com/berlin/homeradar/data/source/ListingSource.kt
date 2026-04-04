package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.source.model.RawListing

/**
 * العقد الأساسي لجميع مصادر الإعلانات.
 *
 * كل مصدر سكن (موقع ويب، API، ملف محلي) يُنفّذ هذا الـ interface
 * ليُدار بشكل موحّد عبر [ListingSourceRegistry].
 *
 * ## التنفيذات المتاحة:
 * - [BundledJsonListingSource]: يقرأ من ملف JSON مُدمج في assets التطبيق.
 * - [BundledHtmlListingSource]: يحلّل ملف HTML مُدمج في assets.
 * - [RemoteJsonListingSource]: يجلب من Remote API عبر Retrofit.
 * - [CustomConfiguredListingSource]: مصدر مخصص أضافه المستخدم بعنوان URL.
 */
interface ListingSource {

    /** معرّف فريد للمصدر، يُستخدم في التسجيل والفلترة وحفظ الإعلانات. */
    val sourceId: String

    /**
     * يجلب قائمة الإعلانات من المصدر.
     *
     * @return قائمة [RawListing] الخام قبل معالجة إزالة التكرار.
     * @throws Exception عند فشل الجلب (شبكة، parsing، إلخ).
     *   الاستثناء يُعالَج في [com.berlin.homeradar.data.repository.HousingRepositoryImpl]
     *   ولا يُوقف جلب المصادر الأخرى.
     */
    suspend fun fetch(): List<RawListing>

    /**
     * يُنفّذ فحص صحة المصدر للتحقق من إمكانية الوصول إليه.
     *
     * التنفيذ الافتراضي يُجري [fetch] كاملاً ويتجاهل النتيجة.
     * يمكن تجاوزه بفحص أخف (HEAD request مثلاً) في التنفيذات المتقدمة.
     *
     * @return [Result.success] إذا كان المصدر يعمل، [Result.failure] مع الاستثناء عند الفشل.
     */
    suspend fun healthCheck(): Result<Unit> = runCatching {
        fetch()
        Unit
    }
}
