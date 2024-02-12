package com.example.musicstreamapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class SongListActivity : AppCompatActivity() {
    companion object{

        lateinit var category : CategoryModel
    }

    private lateinit var songListAdapter: SongListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_song_list)

        val text1 =findViewById<TextView>(R.id.name_text_view)
        val image =findViewById<ImageView>(R.id.cover_image_view)

        text1.text=category.name
        Glide.with(image).load(category.coverUrl)
            .apply(

                RequestOptions().transform(RoundedCorners(32))
            )
            .into(image)

        val recyclerView2 =findViewById<RecyclerView>(R.id.songs_list_recycler_view)


        songListAdapter = SongListAdapter(this,category.songs)
        recyclerView2.layoutManager = LinearLayoutManager(this)
        recyclerView2.adapter = songListAdapter
    }
}