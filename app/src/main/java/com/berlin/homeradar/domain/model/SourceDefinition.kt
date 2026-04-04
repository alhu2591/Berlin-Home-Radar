package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

/**
 * تعريف مصدر الإعلانات (مواقع السكن في برلين).
 *
 * يمثّل هذا الكلاس موقعاً واحداً يمكن جلب الإعلانات منه، سواء كان مدمجاً في التطبيق
 * أو أضافه المستخدم بنفسه.
 *
 * @property id معرّف فريد للمصدر يُستخدم داخلياً (مثل "wbm", "degewo").
 * @property displayName الاسم الظاهر للمستخدم في الواجهة.
 * @property websiteUrl رابط الموقع الرئيسي للمصدر (للعرض أو الزيارة اليدوية).
 * @property supportsAutomatedSync هل يدعم هذا المصدر المزامنة التلقائية (scraping/API).
 *   إذا كانت false، فالمصدر للاستعراض اليدوي فقط.
 * @property description وصف مختصر للمصدر يظهر في إعدادات إدارة المصادر.
 * @property isUserAdded هل أضاف المستخدم هذا المصدر يدوياً (false = مدمج في التطبيق).
 * @property sourceType نوع المصدر التقني (JSON, HTML, CATALOG)، يُحدَّد تلقائياً
 *   بناءً على [supportsAutomatedSync] إن لم يُحدَّد صراحةً.
 */
@Serializable
data class SourceDefinition(
    val id: String,
    val displayName: String,
    val websiteUrl: String,
    val supportsAutomatedSync: Boolean,
    val description: String,
    val isUserAdded: Boolean = false,
    val sourceType: SourceType = if (supportsAutomatedSync) SourceType.HTML else SourceType.CATALOG,
)
