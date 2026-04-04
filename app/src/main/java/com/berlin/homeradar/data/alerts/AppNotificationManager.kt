package com.berlin.homeradar.data.alerts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.berlin.homeradar.MainActivity
import com.berlin.homeradar.R
import com.berlin.homeradar.domain.model.HousingListing
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يُدير إنشاء قنوات الإشعارات وعرض إشعارات تنبيه البحوث المحفوظة.
 *
 * ## قناة الإشعارات:
 * النظام يتطلب إنشاء قناة (Channel) واحدة أو أكثر في Android O (API 26)+.
 * [ensureChannel] آمن للاستدعاء المتكرر لأنه يتجاهل إعادة الإنشاء إن كانت القناة موجودة.
 *
 * ## صلاحيات الإشعارات:
 * - Android < 13 (TIRAMISU): لا تحتاج صلاحية صريحة، يكفي أن تكون الإشعارات مفعّلة في الإعدادات.
 * - Android 13+: تتطلب صلاحية [Manifest.permission.POST_NOTIFICATIONS] الممنوحة من المستخدم.
 *
 * ## Deep Link:
 * كل إشعار يحمل رابطاً عميقاً بصيغة `berlinhomeradar://listing/{id}`
 * يفتح صفحة تفاصيل الإعلان مباشرةً عند الضغط على الإشعار.
 *
 * @constructor يُحقن بواسطة Hilt مع [ApplicationContext] كـ Singleton.
 */
@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * يتأكد من وجود قناة الإشعارات ويُنشئها إن لم تكن موجودة.
     *
     * يُستدعى مبكراً من [com.berlin.homeradar.BerlinHomeRadarApp.onCreate]
     * وأيضاً قبل كل إشعار كضمان إضافي.
     * لا يفعل شيئاً على Android < O (API 26).
     */
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * يعرض إشعار تنبيه بوجود إعلانات جديدة مطابقة لبحث محفوظ.
     *
     * - إذا كان هناك مطابقة واحدة: يعرض عنوان الإعلان مباشرةً.
     * - إذا كانت هناك مطابقات متعددة: يعرض العدد الإجمالي مع عنوان أول إعلان.
     * - يحمل الإشعار Deep Link لفتح صفحة أول إعلان مطابق عند الضغط.
     *
     * @param searchName اسم البحث المحفوظ (يظهر كعنوان للإشعار).
     * @param matchCount عدد الإعلانات المطابقة الجديدة.
     * @param listing أول إعلان مطابق (يُستخدم في نص الإشعار والـ Deep Link).
     */
    fun showSavedSearchAlert(
        searchName: String,
        matchCount: Int,
        listing: HousingListing,
    ) {
        ensureChannel()
        if (!canPostNotifications()) return

        // بناء Deep Link Intent يفتح تفاصيل الإعلان الأول مباشرةً
        val deepLinkUri = Uri.parse("berlinhomeradar://listing/${listing.id}")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            listing.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val contentText = if (matchCount == 1) {
            listing.title
        } else {
            context.getString(R.string.notification_matches_latest, matchCount, listing.title)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(searchName)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(
                        R.string.notification_big_text,
                        listing.title,
                        listing.location,
                        listing.priceEuro,
                    )
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // يُخفى الإشعار تلقائياً عند الضغط عليه
            .build()

        // استخدام hashCode للاسم كـ ID يضمن إشعاراً واحداً لكل بحث محفوظ
        NotificationManagerCompat.from(context).notify(searchName.hashCode(), notification)
    }

    /**
     * يتحقق من إمكانية إرسال الإشعارات في اللحظة الحالية.
     *
     * يتحقق من:
     * 1. أن الإشعارات مفعّلة على مستوى التطبيق في إعدادات النظام.
     * 2. على Android 13+: أن صلاحية [Manifest.permission.POST_NOTIFICATIONS] ممنوحة.
     *
     * @return true إذا كان بإمكان التطبيق إرسال إشعارات الآن.
     */
    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        /** معرّف القناة المستخدم في [NotificationChannel] وكل [NotificationCompat.Builder]. */
        const val CHANNEL_ID = "saved_search_alerts"
    }
}
