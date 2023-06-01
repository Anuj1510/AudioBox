package com.example.audiobox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

// play pause aur jo bhi btn hai notification mai uske liye hai

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action){

            ApplicationClass.PLAY -> {
                if(PlayerActivity.isPlaying){
                    pauseMusicNR()
                }else{
                    playMusicNR()
                }
            }

            ApplicationClass.PREVIOUS ->{
                prevNextSong(Increment = false,context = context!!)
            }

            ApplicationClass.NEXT ->{
                prevNextSong(Increment = true,context = context!!)
            }

            ApplicationClass.EXIT -> {
                PlayerActivity.musicService!!.stopForeground(true)
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService = null
                exitProcess(1)
            }
        }

    }
    private fun playMusicNR(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause)
        PlayerActivity.binding.PlayPauseBtnPA.setIconResource(R.drawable.pause)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause)
    }

    private fun pauseMusicNR(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play)
        PlayerActivity.binding.PlayPauseBtnPA.setIconResource(R.drawable.play)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play)
    }

    private fun prevNextSong(Increment:Boolean,context: Context){
        setSongPosition(Increment = Increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
            .into(PlayerActivity.binding.songImagePA)

        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].tittle
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
            .into(NowPlaying.binding.songImgNP)

        NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].tittle
        playMusicNR()
        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if(PlayerActivity.isFavourite) PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favorite)
        else PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.playerfavorite)

    }
}