package com.example.gallerywithmusic

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class DetailActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_main)

        val detailView = findViewById<ImageView>(R.id.detail_image)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(intent.getStringExtra("uri")))
        detailView.setImageBitmap(bitmap)


    }

}