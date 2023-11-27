package com.example.gallerywithmusic

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GalleryAdapter(private val images: List<Uri?>) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return images.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = images[position]

        val bitmap = MediaStore.Images.Media.getBitmap(holder.itemView.context.contentResolver, imageUri)
        holder.galleryImg.setImageBitmap(bitmap)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("uri", imageUri.toString())
            holder.itemView.context.startActivity(intent)
        }

    }

    inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view){
        val galleryImg: ImageView = itemView.findViewById(R.id.gallery_item)
    }

}

