package com.example.audiobox

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.audiobox.databinding.ActivityPlayerBinding


class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener {

    companion object{
        lateinit var musicListPA : ArrayList<Music>
        var songPosition : Int = 0
        var isPlaying : Boolean = false
        var musicService:MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding

        var repeat:Boolean = false
        var nowPlayingId:String = ""
        var isFavourite : Boolean = false
        var fIndex : Int = -1
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)



        supportActionBar?.hide()

        initializeLayout()

        binding.PlayPauseBtnPA.setOnClickListener{
            if(isPlaying){
                pauseMusic()
            }
            else{
                playMusic()
            }
        }

        binding.nextbtnPA.setOnClickListener {
            PrevNextSong(Increment = true)
        }

        binding.previousbtnPA.setOnClickListener {
            PrevNextSong(Increment = false)
        }

        binding.repeatBtnPA.setOnClickListener{
            if(!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            }else{
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.pink))
            }
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            }catch (e: Exception){
                Toast.makeText(this,  "Equalizer Feature not Supported!!", Toast.LENGTH_SHORT).show()}
        }


        binding.backBtnPA.setOnClickListener{finish()}

        binding.youtubePA.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/"))
            startActivity(intent)
        }

        binding.shareBtnPA.setOnClickListener{
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File!!"))
        }


        // humne seek bar ke liye listener add liya hai...listner wo object hota hai jo seek bar ke touch krne pe respond krega

        binding.seekBarPA.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit // unit matlab ki iss function ka koi bhi use nahi hai to phir ye function call nahi hona chahiye
        })

        // favourite button ke liye

        binding.favouriteBtnPA.setOnClickListener{
            if(isFavourite){
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.playerfavorite)
                FavoriteActivity.favouriteSongs.removeAt(fIndex)
            }else{
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.favorite)
                FavoriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }


    }

    // to put image and song name in player activity
    private fun setLayout(){
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
            .into(binding.songImagePA)

        binding.songNamePA.text = musicListPA[songPosition].tittle
        if(repeat){
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
        }
        if(isFavourite) binding.favouriteBtnPA.setImageResource(R.drawable.favorite)
        else binding.favouriteBtnPA.setImageResource(R.drawable.playerfavorite)
    }


    // to create media player which plays the songs
    private fun createMediaPlayer(){
        try {
            if(musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.PlayPauseBtnPA.setIconResource(R.drawable.pause)
            musicService!!.showNotification(R.drawable.pause)
            binding.tvseekbarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvseekbarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id

        }catch (e:Exception){return}
    }

    private fun initializeLayout(){
        // single song ko play krne ke liye
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "FavoriteAdapter" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavoriteActivity.favouriteSongs)
                setLayout()
                createMediaPlayer()
            }
            "MusicAdapter" ->{
                //to start service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
                createMediaPlayer()

            }
            "MainActivity" ->{
                //to start service
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
                createMediaPlayer()
            }

            "NowPlaying" ->{
                setLayout()
                binding.tvseekbarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvseekbarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying){
                    binding.PlayPauseBtnPA.setIconResource(R.drawable.pause)
                }else{
                    binding.PlayPauseBtnPA.setIconResource(R.drawable.play)
                }
            }

            "FavoriteShuffle" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavoriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
                createMediaPlayer()
            }

            "PlaylistDetailsAdapter" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPos].playlist)
                setLayout()
                createMediaPlayer()
            }

            "PlaylistDetailsShuffle" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPos].playlist)
                musicListPA.shuffle()
                setLayout()
                createMediaPlayer()
            }


        }
    }

    // fun for playpausebtn...it play the music
    private fun playMusic(){
        binding.PlayPauseBtnPA.setIconResource(R.drawable.pause)
        musicService!!.showNotification(R.drawable.pause)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    // fun for playpausebtn...it stops the music
    private fun pauseMusic(){
        binding.PlayPauseBtnPA.setIconResource(R.drawable.play)
        musicService!!.showNotification(R.drawable.play)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }


    // fun for prev and next button
    private fun PrevNextSong(Increment:Boolean){
        if(Increment){
           setSongPosition(Increment = true)
            setLayout()
            createMediaPlayer()

        }else{

            setSongPosition(Increment = false)
            setLayout()
            createMediaPlayer()


        }
    }



    // to connect service
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()

    }

    // to disconnect service
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }



    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try{setLayout()}catch (e:Exception){return}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK){
            return
        }
    }

}