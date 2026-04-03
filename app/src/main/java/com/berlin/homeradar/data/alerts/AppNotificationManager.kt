package com.berlin.homeradar.data.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.berlin.homeradar.MainActivity
import com.berlin.homeradar.R
import com.berlin.homeradar.domain.model.HousingListing
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Saved search alerts",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Local alerts when saved searches match new Berlin listings."
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showSavedSearchAlert(
        searchName: String,
        matchCount: Int,
        listing: HousingListing,
    ) {
        ensureChannel()
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
            "$matchCount new matches. Latest: ${listing.title}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(searchName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${listing.title}\n${listing.location} • €${listing.priceEuro}"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(searchName.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "saved_search_alerts"
    }
}
