package com.example.gallerywithmusic

import android.content.ContentUris
import android.content.ContentValues
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
import java.text.SimpleDateFormat
import java.util.Date

class GalleryActivity : AppCompatActivity() {
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri?>
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
            && ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, "android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED){
        } else {
            permissions.launch(arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.INTERNET"))
        }

        attachImages()

        val galleryToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(galleryToolbar)
        supportActionBar?.title = "Gallery with Music"

        val galleryRecycler: RecyclerView = findViewById(R.id.gallery_recycler)
        galleryRecycler.layoutManager= GridLayoutManager(this, 3)
        galleryRecycler.adapter = GalleryAdapter(images)

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) {
            if (it){
                images.add(pictureUri)
                galleryRecycler.adapter?.notifyItemInserted(images.size-1)
            }
        }

    }
    private fun createImage() : Uri? {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()))
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

    override fun onDestroy() {
        super.onDestroy()
        FaceRecognition.destroy()
    }
}