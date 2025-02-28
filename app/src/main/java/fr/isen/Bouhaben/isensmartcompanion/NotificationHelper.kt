package fr.isen.Bouhaben.isensmartcompanion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra("event_title") ?: "Event Reminder"

        // ✅ Ensure the notification channel is created before sending the notification
        createNotificationChannel(context)

        // ✅ Check if the app has notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return // Exit if permission is not granted
            }
        }

        // ✅ Unique Notification ID based on event title hash
        val notificationId = eventTitle.hashCode()

        // ✅ Intent to open the app when clicking the notification
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Build the notification
        val notification = NotificationCompat.Builder(context, "event_channel")
            .setSmallIcon(R.drawable.notifon)
            .setContentTitle("Upcoming Event!")
            .setContentText("Reminder: $eventTitle is happening soon.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss when clicked
            .build()

        // ✅ Send the notification
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }


    companion object {
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "event_channel",
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Event reminders"
                }

                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        }
    }
}
