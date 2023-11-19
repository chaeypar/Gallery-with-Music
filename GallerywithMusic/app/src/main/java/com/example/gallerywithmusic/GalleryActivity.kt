package com.example.gallerywithmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_recycler)
        val images = listOf<Int>(R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground)

        val galleryToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(galleryToolbar)
        supportActionBar?.title = "Gallery with Music"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val galleryRecycler: RecyclerView = findViewById(R.id.gallery_recycler)
        galleryRecycler.layoutManager= GridLayoutManager(this, 3)
        galleryRecycler.adapter = GalleryAdapter(images)
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        onBackPressed()
        return true
    }
}