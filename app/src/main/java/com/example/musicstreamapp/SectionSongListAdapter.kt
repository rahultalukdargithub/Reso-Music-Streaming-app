package com.example.musicstreamapp

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class SectionSongListAdapter(private val context: Activity, private  val songIdList : List<String>)
    : RecyclerView.Adapter<SectionSongListAdapter.MyViewHolder>(){
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var subtitle: TextView
        var image: ImageView

        init{
            title = itemView.findViewById(R.id.song_title_text_view)
            subtitle = itemView.findViewById(R.id.song_subtitle_text_view)
            image = itemView.findViewById(R.id.song_cover_image_view)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.section_song_list_recycler_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return songIdList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem : String = songIdList[position]
        val musicListPA = ArrayList<String>()
        for(element in songIdList)
        {
            val currentItem2 : String = element
            println(currentItem2)
            FirebaseFirestore.getInstance().collection("songs")
                .document(currentItem2).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {
                        musicListPA.add(url)
                    }
                }

        }


        FirebaseFirestore.getInstance().collection("songs")
            .document(currentItem).get()
            .addOnSuccessListener {
                val song = it.toObject(SongModel::class.java)
                song?.apply {
                    holder.title.text = title
                    holder.subtitle.text = subtitle
                    Glide.with(holder.image).load(coverUrl)
                        .apply(
                            RequestOptions().transform(RoundedCorners(32))
                        )
                        .into(holder.image)
                    holder.itemView.setOnClickListener {



                        if(PlayerActivity.nowPlayingID==id)
                        {
                            val intent= Intent(context, PlayerActivity::class.java)
                            intent.putExtra("index",PlayerActivity.songPosition)
                            intent.putExtra("class","NowPlaying")
                            PlayerActivity.SongList=songIdList
                            PlayerActivity.musicListPA=musicListPA
                            context.startActivity(intent)
                        }else{
                            val intent= Intent(context, PlayerActivity::class.java)
                            intent.putExtra("index",position)
                            intent.putExtra("class","SectionSongListAdapter")
                            PlayerActivity.SongList=songIdList
                            PlayerActivity.musicListPA=musicListPA
                            context.startActivity(intent)
                        }

                    }
                }
            }
    }
}