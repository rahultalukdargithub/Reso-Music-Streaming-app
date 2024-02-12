package com.example.musicstreamapp

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicService : Service() {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int) {
        FirebaseFirestore.getInstance().collection("songs")
            .document(PlayerActivity.SongList[PlayerActivity.songPosition]).get()
            .addOnSuccessListener {
                val song = it.toObject(SongModel::class.java)
                song?.apply {
                    val intent = Intent(baseContext, MainActivity::class.java)
                    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                    val contentIntent = PendingIntent.getActivity(this@MusicService, 0, intent, flag)
                    val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
                    val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)
                    val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
                    val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)
                    val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
                    val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)
                    val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
                    val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val futureTarget = Glide.with(baseContext)
                                .asBitmap()
                                .load(coverUrl.toUri())
                                .submit()

                            val bitmap = futureTarget.get()


                            withContext(Dispatchers.Main) {
                                val notification =
                                    androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                                        .setContentIntent(contentIntent)
                                        .setContentTitle(title)
                                        .setContentText(subtitle)
                                        .setSmallIcon(R.drawable.music_icon)
                                        .setStyle(
                                            androidx.media.app.NotificationCompat.MediaStyle()
                                                .setMediaSession(mediaSession.sessionToken)

                                        )
                                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                                        .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                                        .setOnlyAlertOnce(true)
                                        .addAction(R.drawable.previous_icon, "Previous", prevPendingIntent)
                                        .addAction(playPauseBtn, "Play", playPendingIntent)
                                        .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
                                        .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
                                        .setLargeIcon(bitmap)
                                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                        .setTimeoutAfter(-1)
                                        .setOngoing(true)
                                        .build()

                                startForeground(13, notification)

//                                Glide.with(baseContext).clear(futureTarget)
                            }
                        } catch (e: Exception) {
                            Log.d("TAG", e.toString())
                        }
                    }
                }
            }
    }
}
