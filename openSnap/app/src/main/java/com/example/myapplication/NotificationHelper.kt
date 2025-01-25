package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.app.NotificationCompat
import kotlin.random.Random

// https://medium.com/@kathankraithatha/notifications-with-jetpack-compose-3302f27e1348
class NotificationHelper(val context: Context) {

    private val notificationHelper = context.getSystemService(NotificationManager::class.java)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "openSnap_Messages",
                "openSnap messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for openSnap messages"
            }

            notificationHelper.createNotificationChannel(channel)
        } else {
            Log.d("HELLO NOT DONE", "SD")
        }
    }

    fun showBasicNotification(){
        Log.d("HERE", "NOTIFC")
        val notification=NotificationCompat.Builder(context,"openSnap_Messages")
            .setContentTitle("Message!")
            .setContentText("Test sad")
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .build()

        notificationHelper.notify(
            Random.nextInt(),
            notification
        )
    }

    fun showStopNotification() {
        // https://developer.android.com/develop/background-work/background-tasks/broadcasts
        // https://developer.android.com/develop/ui/views/notifications/navigation
        val stopIntent = Intent("com.example.ACTION_STOP").apply {}

        val actionPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "openSnap_Messages")
            .setContentTitle("Message!")
            .setContentText("Test sad")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .addAction(
                android.R.drawable.ic_menu_view,
                "STOP",
                actionPendingIntent
            )
            .build()

        notificationHelper.notify(Random.nextInt(), notification)
    }



    // Interactable notification
    // Sensor (temperature) extended device control
}

