package com.example.audiobox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiobox.databinding.ActivityPlaylistBinding
import com.example.audiobox.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playlistAdapter:PlaylistAdapter

    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity,2)
        // Adapter
        playlistAdapter = PlaylistAdapter(this, MusicList = musicPlaylist.ref)
        binding.playlistRV.adapter = playlistAdapter

        binding.backBtnPL.setOnClickListener{finish()}
        binding.addPlaylistBtn.setOnClickListener {
            customAlertDialog()
        }
    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist_dialog,binding.root,false)
        val builder = MaterialAlertDialogBuilder(this)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val dialog = builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD"){ dialog, _ ->
                val playlistName = binder.playlistName.text
                val createdBy = binder.yourName.text
                if(playlistName != null && createdBy != null)
                    if(playlistName.isNotEmpty() && createdBy.isNotEmpty())
                    {
                        addPlaylist(playlistName.toString(), createdBy.toString())
                    }
                dialog.dismiss()
            }.create()
        dialog.show()
    }

    private fun addPlaylist(name: String, createdBy: String) {

        var playlistExists = false
        for(i in musicPlaylist.ref) {
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if(playlistExists) Toast.makeText(this, "Playlist Exist!!", Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.refreshPlaylist()
        }

    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }

}