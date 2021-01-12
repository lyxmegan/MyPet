package edu.utap.mypet

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val notificationUtils = NotificationUtils(context)
        val notification = notificationUtils.getNotificationBuilder().build()
        val uniqueId = ((Date().time / 1000L) % Integer.MAX_VALUE).toInt()
        notificationUtils.getManager().notify(uniqueId, notification)
    }
}
