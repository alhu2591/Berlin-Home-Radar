package com.berlin.homeradar.domain.model

/**
 * إعدادات التطبيق الكاملة كما يريدها المستخدم.
 *
 * يُستخدم هذا الكلاس كـ snapshot موحّد يجمع كل تفضيلات المستخدم في مكان واحد.
 * يُقرأ من [UserPreferencesRepository] ويُطبَّق عبر طبقة الـ Domain مباشرةً.
 *
 * @property language لغة واجهة التطبيق المختارة (عربي / ألماني / تلقائي).
 * @property themeMode الثيم المختار (فاتح / داكن / تلقائي حسب النظام).
 * @property syncInterval فترة التكرار للمزامنة التلقائية في الخلفية.
 * @property backgroundSyncEnabled هل المزامنة التلقائية في الخلفية مفعّلة.
 * @property remoteSourceEnabled هل مصدر الإعلانات البعيد (Remote API) مفعّل.
 * @property enabledSourceIds مجموعة معرّفات المصادر التي اختار المستخدم تفعيلها.
 * @property customSources قائمة المصادر المخصصة التي أضافها المستخدم يدوياً.
 * @property sourceOrder ترتيب المصادر كما رتّبها المستخدم (من الأعلى للأسفل).
 * @property savedSearches قائمة عمليات البحث المحفوظة مع إعداداتها وتنبيهاتها.
 */
data class AppSettings(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val syncInterval: SyncIntervalOption = SyncIntervalOption.MINUTES_15,
    val backgroundSyncEnabled: Boolean = true,
    val remoteSourceEnabled: Boolean = false,
    val enabledSourceIds: Set<String> = emptySet(),
    val customSources: List<SourceDefinition> = emptyList(),
    val sourceOrder: List<String> = emptyList(),
    val savedSearches: List<SavedSearch> = emptyList(),
)
