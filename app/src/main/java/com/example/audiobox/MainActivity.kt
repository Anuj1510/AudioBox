package com.example.audiobox

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiobox.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.system.exitProcess


/* iss app mai ekk bug hai aur wo ye hai ki jub mai apne songs ko favourite mai add krta hu uske baad agr mai uss song ko apne phone se delete kr deta
 hu tub jo song hai wo main activity se to delete ho jata hai kyuki wo direct songs ko access kr raha hai phone ki storage se but kyuki wo song shared
 preferences mai stored ho gaya hai isliye delete hone ke baad bhi favourites mai wo song show hoga aur ye bug video no. 77 mai fix kiya hai */


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter:MusicAdapter

    companion object{
        lateinit var MusicListMA:ArrayList<Music>
        lateinit var musicListSearch:ArrayList<Music>
        var search:Boolean = false
        var themeIndex:Int = 0
        var sortOrder:Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex",0)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestRuntimePermission()

        //for retrieving favourites data using shared preferences
        FavoriteActivity.favouriteSongs = ArrayList()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
        val jsonString = editor.getString("FavouriteSongs", null)
        val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
        if(jsonString != null) {
            val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
            FavoriteActivity.favouriteSongs.addAll(data)
        }

        //for retrieving Playlist data using shared preferences
        PlaylistActivity.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
        if(jsonStringPlaylist != null) {
            val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
            PlaylistActivity.musicPlaylist = dataPlaylist
        }



        // isko true krne ke baad hamara recycler view utne hi items banata hai jitne ki need hoti hai
        // isse memory save hoti hai
        binding.MusicRv.setHasFixedSize(true)
        binding.MusicRv.layoutManager = LinearLayoutManager(this@MainActivity)


        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)

        MusicListMA = getAllAudio()

        // Adapter
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.MusicRv.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : " + musicAdapter.itemCount



        binding.shuffle.setOnClickListener {
            val intent = Intent(this,PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }

        binding.favorites.setOnClickListener {
            val intent = Intent(this,FavoriteActivity::class.java)
            startActivity(intent)
        }

        binding.playlist.setOnClickListener {
            val intent = Intent(this,PlaylistActivity::class.java)
            startActivity(intent)
        }

        // for navigation drawer
        toggle = ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navMenu.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setMessage("Do you want to close the app?")
                        .setPositiveButton("Yes"){_,_->
                            if(PlayerActivity.musicService != null){
                                PlayerActivity.musicService!!.stopForeground(true)
                                PlayerActivity.musicService!!.mediaPlayer!!.release()
                                PlayerActivity.musicService = null

                            }
                            exitProcess(1)
                        }
                        .setNegativeButton("No"){dialog,_->
                            dialog.dismiss()
                        }
                    val customdialog = builder.create()
                    customdialog.show()
                    customdialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customdialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }

                R.id.navFeedback -> startActivity(Intent(this@MainActivity, feedbackActivity::class.java))
                R.id.navSettings -> startActivity(Intent(this@MainActivity, settingsActivity::class.java))
                R.id.navAbout -> startActivity(Intent(this@MainActivity, aboutActivity::class.java))
            }

            true
        }

    }

    // for runtime permission request
    private fun requestRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),15)  // request code apni marzi se jo bhi ho daal lo
        } // arrayof isliye if we have to ask for multiple permission
    }

    // function to get all audio files from storage
    @SuppressLint("Range")
    private fun getAllAudio():ArrayList<Music>{

        // we are going to define a cursor in this function and cursor is an object which basically helps us to
        // get any things from storage, we just have to tell it that what we want



        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0" // !=0 means wo null nahi hona chahiye
        val projection = arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,selection,null,
            sortingList[sortOrder]
            , null)
        if(cursor != null){
            if(cursor.moveToFirst())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))?:"Unknown"
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))?:"Unknown"
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))?:"Unknown"
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))?:"Unknown"
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart") // uri mai ye string hamesha constant rahegi bilkul bhi change nahi
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(id = idC, tittle = titleC, album = albumC, artist = artistC, path = pathC, duration = durationC,
                    artUri = artUriC)
                    val file = File(music.path)
                    if(file.exists())
                        tempList.add(music)
                }while (cursor.moveToNext())
            cursor.close()
        }



        return tempList
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 15){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //grantResults[0] isliye liya hai kyuki hamne wha pe arrayof declare kra tha jiske 0 indexed pe storage ki permission tha
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show()

                MusicListMA = getAllAudio()

                // Adapter
                musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
                binding.MusicRv.adapter = musicAdapter
                binding.totalSongs.text = "Total Songs : " + musicAdapter.itemCount

            }
            else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),15)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
            }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
            PlayerActivity.musicService!!.stopForeground(true)
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)
        }


    }

    override fun onResume() {
        super.onResume()
        // for storing favourites data using shared preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavoriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs",jsonString)

        // for storing playlist data using shared preferences
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()

        //for sorting
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if(sortOrder != sortValue){
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)
        }
//        if(PlayerActivity.musicService != null) binding.nowPlaying.visibility = View.VISIBLE
    }


    // ------> ye music search feature ka function hai jo ki incomplete hai
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.search_view_menu, menu)
//        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean = true
//            override fun onQueryTextChange(newText: String?): Boolean {
//                musicListSearch = ArrayList()
//                if(newText != null){
//                    val userInput = newText.lowercase()
//                    for (song in MusicListMA)
//                        if(song.tittle.lowercase().contains(userInput))
//                            musicListSearch.add(song)
//                    search = true
//                    musicAdapter.updateMusicList(searchList = musicListSearch)
//                }
//                return true
//            }
//        })
//        return super.onCreateOptionsMenu(menu)
//    }

}