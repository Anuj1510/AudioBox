package com.example.audiobox

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.audiobox.databinding.FavoriteViewBinding
import com.example.audiobox.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistAdapter(private val context: Context, private var MusicList: ArrayList<Playlist>):
    RecyclerView.Adapter<PlaylistAdapter.MyHolder>() {
    class MyHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {

        val image = binding.playlistImg
        val name = binding.playlistName
        val root = binding.root
        val delete = binding.playlistDeleteBtn

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context),parent,false))



    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.name.text = MusicList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(MusicList[position].name)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes"){ dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }

        if(PlaylistActivity.musicPlaylist.ref[position].playlist.size>0){
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_foreground).centerCrop())
                .into(holder.image)
        }

    }

    override fun getItemCount(): Int {
        return MusicList.size
    }



    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    fun refreshPlaylist(){
        MusicList = ArrayList()
        MusicList.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }

}