package com.example.audiobox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.audiobox.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var favoriteAdapter: FavoriteAdapter



    companion object{
        var favouriteSongs:ArrayList<Music> = ArrayList()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.hide()

        binding.backBtnFA.setOnClickListener{finish()}

        binding.favouriteRV.setHasFixedSize(true)
        binding.favouriteRV.setItemViewCacheSize(15)
        binding.favouriteRV.layoutManager = GridLayoutManager(this@FavoriteActivity,4)

        // Adapter
        favoriteAdapter = FavoriteAdapter(this@FavoriteActivity, favouriteSongs)
        binding.favouriteRV.adapter = favoriteAdapter

        // shuffle button in favorites
        if(favouriteSongs.size<1) binding.shuffleBtnFA.visibility = View.INVISIBLE
        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","FavoriteShuffle")
            startActivity(intent)
        }
    }
}