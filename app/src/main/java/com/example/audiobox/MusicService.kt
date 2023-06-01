package com.example.audiobox

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat

// Service in android app means agr hum chahate hai ki hamara android app baand hone ke baad bhi chale like upr koi notification aa gaya to usse service khete hai

class MusicService: Service() {

    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder {
        mediaSessionCompat = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder() : Binder(){
        fun currentService() : MusicService{
            return this@MusicService
        }
    }

    fun showNotification(PlayPauseBtn:Int){

        val intent = Intent(baseContext, MainActivity::class.java)

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        val imageArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if(imageArt != null){
            BitmapFactory.decodeByteArray(imageArt,0,imageArt.size)
        }else{
            BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher_foreground)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].tittle)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.note)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.sessionToken))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.arrow, "Previous", prevPendingIntent)
            .addAction(PlayPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next, "Next", nextPendingIntent)
            .addAction(R.drawable.exit, "Exit", exitPendingIntent)
            .build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (PlayerActivity.isPlaying) 1F else 0F
            mediaSessionCompat.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSessionCompat.setPlaybackState(playBackState)
        }

        startForeground(15, notification)
    }

    fun createMediaPlayer(){
        try {
            if(PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.PlayPauseBtnPA.setIconResource(R.drawable.pause)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause)
            PlayerActivity.binding.tvseekbarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvseekbarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id

        }catch (e:Exception){return}
    }

    fun seekBarSetup(){

        runnable = Runnable {
            PlayerActivity.binding.tvseekbarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }

        Handler(Looper.getMainLooper()).postDelayed(runnable,0)

    }


}