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

class CategoryAdapter(private val context: Activity, private val categoryList:List<CategoryModel>)
    : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var title: TextView
        var image: ImageView

        init{
            title = itemView.findViewById(R.id.name_text_view)
            image = itemView.findViewById(R.id.cover_image_view)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.category_item_recycler_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem : CategoryModel = categoryList[position]
        holder.title.text = currentItem.name

        Glide.with(holder.image).load(currentItem.coverUrl)
            .apply(
                RequestOptions().transform(RoundedCorners(32))
            )
            .into(holder.image)

        holder.itemView.setOnClickListener {

            val intent = Intent(context, SongListActivity::class.java)

            SongListActivity.category = currentItem
            context.startActivity(intent)
        }


    }
}