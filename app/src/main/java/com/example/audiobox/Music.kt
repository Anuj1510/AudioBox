package com.example.audiobox

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import java.util.concurrent.TimeUnit

data class Music(

    val id:String,
    val tittle:String,
    val album:String,
    val artist:String,
    val duration:Long = 0,
    val path:String,
    val artUri:String

)

class Playlist{
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}
class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes*TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

// hum log notification mai direct image nahi dal sakte usse hume bitmap ki help se dalna hoga
fun getImgArt(path:String): ByteArray? {
    val retriver = MediaMetadataRetriever()
    retriver.setDataSource(path)
    return retriver.embeddedPicture

}

// fun to check the song position
fun setSongPosition(Increment: Boolean){

    if(!PlayerActivity.repeat){
        if(Increment){

            if(PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition){
                PlayerActivity.songPosition = 0
            }else{
                ++PlayerActivity.songPosition
            }

        }else{
            if(PlayerActivity.songPosition == 0){
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            }else{
                --PlayerActivity.songPosition
            }
        }
    }

}

fun favouriteChecker(id:String) : Int{

    PlayerActivity.isFavourite = false

    FavoriteActivity.favouriteSongs.forEachIndexed { index, music ->

        if(id == music.id){
            PlayerActivity.isFavourite = true
            return index
        }

    }

    return -1

}


