package com.example.gallerywithmusic

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class GalleryActivity : AppCompatActivity() {
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri?>
    private lateinit var sharedPreferences: SharedPreferences
    private var images = mutableListOf<Uri?>()
    private var pictureUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_recycler)

        val permissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.all { permission -> permission.value }) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED){
        } else {
            permissions.launch(arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"))
        }

        attachImages()

        val galleryToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(galleryToolbar)
        supportActionBar?.title = "Gallery with Music"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val galleryRecycler: RecyclerView = findViewById(R.id.gallery_recycler)
        galleryRecycler.layoutManager= GridLayoutManager(this, 3)
        galleryRecycler.adapter = GalleryAdapter(images)

        sharedPreferences = getSharedPreferences("GallerywithMusic", Context.MODE_PRIVATE)
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) {
            if (it){
                val picCount = sharedPreferences.getInt("picCount", 0)
                sharedPreferences.edit().putInt("picCount", picCount + 1).apply()
                images.add(pictureUri)
                galleryRecycler.adapter?.notifyItemInserted(images.size-1)
            }
        }

    }

    private fun createImage() : Uri? {
        val picCount = sharedPreferences.getInt("picCount", 0)
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "picture_$picCount")
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GallerywithMusic")
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun attachImages(){
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = 'Pictures/GallerywithMusic/'"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            selection,
            null,
            null
        )?.use { cursor ->
            val mediaId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(mediaId)
                val photoUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images.add(photoUri)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.toolbar_camera -> {
                pictureUri = createImage()
                cameraLauncher.launch(pictureUri)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}