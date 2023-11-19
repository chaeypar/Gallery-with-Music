package com.example.gallerywithmusic

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GalleryAdapter(private val images: List<Int>) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        Log.d("chaeypar", images.size.toString())
        return images.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = images[position]

        val bitmap = BitmapFactory.decodeResource(holder.itemView.context.resources, R.drawable.pencil)
        holder.galleryImg.setImageBitmap(bitmap)
    }

    inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view){
        val galleryImg: ImageView = itemView.findViewById(R.id.gallery_item)
    }

}

