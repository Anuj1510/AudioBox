package com.example.audiobox

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.audiobox.databinding.FavoriteViewBinding

class FavoriteAdapter(private val context: Context, private var MusicList: ArrayList<Music>):RecyclerView.Adapter<FavoriteAdapter.MyHolder>() {
    class MyHolder(binding: FavoriteViewBinding):RecyclerView.ViewHolder(binding.root) {
        val image = binding.songImgFV
        val name = binding.songNameFV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavoriteViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = MusicList[position].tittle
        Glide.with(context)
            .load(MusicList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_round  /* isme hamne naya icon isliye dala hai ki if any case agr image load nahi ho paayi to konsa image dalna haoi*/).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavoriteAdapter")
            ContextCompat.startActivity(context, intent, null)
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
}