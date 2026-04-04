package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.config.FeatureFlags
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.model.SourceDefinition
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient

/**
 * سجلّ مركزي لجميع مصادر الإعلانات المتاحة في التطبيق.
 *
 * يُوفّر نقطة وصول موحّدة لاكتشاف المصادر وبناء adapters لها.
 * يُدير ثلاثة أنواع من المصادر:
 *
 * 1. **مصادر مدمجة** (Built-in): مبرمجة مباشرة في التطبيق، متاحة بـ [lazy] لأداء أفضل.
 * 2. **مصادر مخصصة** (Custom): أضافها المستخدم عبر الإعدادات بـ URL.
 * 3. **مصادر بعيدة** (Remote): تُفعَّل عبر Remote Config.
 *
 * ## آلية اختيار المصادر المفعّلة ([getEnabledSources]):
 * - يُطبّق ترتيب المستخدم (sourceOrder) من DataStore.
 * - يُصفّي المصادر غير المفعّلة (enabledSourceIds).
 * - يُصفّي المصادر المعطّلة عبر [FeatureFlags].
 *
 * @constructor يُحقن بواسطة Hilt كـ Singleton.
 */
@Singleton
class ListingSourceRegistry @Inject constructor(
    private val bundledJsonListingSource: BundledJsonListingSource,
    private val bundledHtmlListingSource: BundledHtmlListingSource,
    private val remoteJsonListingSource: RemoteJsonListingSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * المصادر المدمجة مُحمَّلة بشكل كسول (lazy) وتُبنى مرة واحدة فقط.
     * المفتاح هو [ListingSource.sourceId].
     */
    private val builtInSources by lazy {
        listOf(
            bundledJsonListingSource,
            bundledHtmlListingSource,
            remoteJsonListingSource,
        ).associateBy { it.sourceId }
    }

    /**
     * يُرجع قائمة المصادر المفعّلة بحسب إعدادات المستخدم وترتيبه وـ FeatureFlags.
     *
     * **ترتيب الأولوية**:
     * 1. ترتيب [UserPreferencesRepository.appSettings] → sourceOrder (كما رتّبه المستخدم).
     * 2. المصادر غير المفعّلة تُحذف (enabledSourceIds).
     * 3. المصادر المعطّلة بـ FeatureFlags تُحذف.
     *
     * @return قائمة [ListingSource] جاهزة للجلب بالترتيب الصحيح.
     */
    suspend fun getEnabledSources(): List<ListingSource> {
        val settings = userPreferencesRepository.appSettings.first()
        val allDefinitions = (SourceCatalog.all + settings.customSources).associateBy { it.id }
        return settings.sourceOrder
            .mapNotNull { sourceId ->
                val definition = allDefinitions[sourceId]
                adapterFor(definition)
            }
            .filter { source -> source.sourceId in settings.enabledSourceIds }
            .filter { source -> FeatureFlags.isSourceEnabled(source.sourceId) }
    }

    /**
     * يبني adapter مناسب لتعريف مصدر معيّن.
     *
     * منطق الاختيار:
     * - مصدر مدمج موجود في [builtInSources] → يُرجعه مباشرةً.
     * - مصدر مخصص من المستخدم أو يدعم المزامنة → ينشئ [CustomConfiguredListingSource].
     * - مصدر لا يدعم المزامنة التلقائية → يُرجع null (للاستعراض اليدوي فقط).
     * - المصدر معطّل بـ FeatureFlags → يُرجع null.
     *
     * @param definition تعريف المصدر المراد بناء adapter له.
     * @return [ListingSource] جاهز، أو null إذا لا يمكن بناء adapter مناسب.
     */
    fun adapterFor(definition: SourceDefinition?): ListingSource? {
        definition ?: return null
        if (!FeatureFlags.isSourceEnabled(definition.id)) return null
        return builtInSources[definition.id]
            ?: if (definition.isUserAdded || definition.supportsAutomatedSync) {
                CustomConfiguredListingSource(definition, okHttpClient)
            } else {
                null
            }
    }

    /**
     * يبحث عن مصدر مدمج بمعرّفه.
     *
     * @param sourceId معرّف المصدر المطلوب.
     * @return [ListingSource] إذا وُجد في المصادر المدمجة، وإلا null.
     */
    fun findSource(sourceId: String): ListingSource? {
        val builtIn = builtInSources[sourceId]
        return builtIn
    }
}
