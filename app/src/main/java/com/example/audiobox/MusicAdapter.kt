package com.example.audiobox

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.audiobox.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var MusicList: ArrayList<Music>, private var playlistDetails:Boolean = false
, private val selectionActivity: Boolean = false)
    :RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding: MusicViewBinding):RecyclerView.ViewHolder(binding.root) {

        val tittle = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    private fun addSong(song: Music): Boolean {
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPos].playlist.forEachIndexed{ index,music ->

            if(song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPos].playlist.removeAt(index)
                return false
            }

        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPos].playlist.add(song)
        return true
    }

    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.tittle.text = MusicList[position].tittle
        holder.album.text = MusicList[position].album
        holder.duration.text = formatDuration(MusicList[position].duration)

        // to load image
        Glide.with(context)
            .load(MusicList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
            .into(holder.image)
        when {
            playlistDetails ->{

                holder.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }

            }
            selectionActivity ->{
                holder.root.setOnClickListener {
                    if(addSong(MusicList[position]))
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.pink))
                    else
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                }
            }
            else -> {
                holder.root.setOnClickListener {
                when{
                    MusicList[position].id == PlayerActivity.nowPlayingId ->
                        sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                    else->sendIntent(ref="MusicAdapter", pos = position) }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return MusicList.size
    }

    fun updateMusicList(searchList:ArrayList<Music>){
        MusicList = ArrayList()
        MusicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    fun refreshPlaylist(){
        MusicList = ArrayList()
        MusicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPos].playlist
        notifyDataSetChanged()
    }

}