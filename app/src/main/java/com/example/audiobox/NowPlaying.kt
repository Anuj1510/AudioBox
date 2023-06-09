package com.example.audiobox

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.audiobox.databinding.FragmentNowPlayingBinding


class NowPlaying : Fragment() {

    companion object{
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)


        binding.root.visibility = View.INVISIBLE // agr song nahi chalega to fragment invisible hona chahiye
        // Inflate the layout for this fragment

        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic()
            else playMusic()
        }

        binding.nextBtnNP.setOnClickListener {

            setSongPosition(Increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()

            PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].tittle
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
                .into(binding.songImgNP)

            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].tittle
            PlayerActivity.musicService!!.showNotification(R.drawable.pause)
            playMusic()

        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(),PlayerActivity::class.java)

            intent.putExtra("index",PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")

            ContextCompat.startActivity(requireContext(),intent,null)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE

            binding.songNameNP.isSelected = true

            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
                .into(binding.songImgNP)

            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].tittle

            if(PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.pause)
            else binding.playPauseBtnNP.setIconResource(R.drawable.play)


        }

    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.play)
        PlayerActivity.musicService!!.showNotification(R.drawable.play)
    }
}