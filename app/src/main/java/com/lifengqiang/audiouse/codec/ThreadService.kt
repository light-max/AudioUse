package com.lifengqiang.audiouse.codec

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lifengqiang.audiouse.R
import com.lifengqiang.audiouse.ui.play.PlayActivity


class ThreadService : Service(), OnPlayModeListener {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        val modeName = getSharedPreferences("AudioDecoder", 0)
            .getString("PlayMode", PlayMode.SINGLE.name)
        AudioPlayer.getInstance().setPlayMode(PlayMode.valueOf(modeName!!))
        AudioPlayer.getInstance().registerPlayModeListener(this, false)
    }

    override fun onDestroy() {
        AudioPlayer.getInstance().unregisterPlayModeListener(this)
    }

    private fun createNotificationChannel(
        channelID: String,
        channelNAME: String,
        level: Int
    ): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelID, channelNAME, level)
            manager.createNotificationChannel(channel)
            channelID
        } else {
            null
        }
    }

    override fun onStartCommand(intent_: Intent?, flags: Int, startId: Int): Int {
        val intent = Intent(this, PlayActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val channelId = createNotificationChannel(
            "com.lifengqiang.audiouse.audioPlay",
            "AudioDecoderThread",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notification = NotificationCompat.Builder(this, channelId!!)
            .setContentTitle("音频解码线程正在运行中")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
//        val notificationManager = NotificationManagerCompat.from(this)
//        notificationManager.notify(100, notification.build())
        startForeground(100, notification.build())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onPlayModeChange(player: AudioPlayer) {
        getSharedPreferences("AudioDecoder", 0)
            .edit()
            .putString("PlayMode", player.getPlayMode().name)
            .apply()
    }
}