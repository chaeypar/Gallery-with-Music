package com.example.gallerywithmusic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class DetailActivity: AppCompatActivity() {
    private lateinit var cropped : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_main)

        System.loadLibrary("opencv_java4")

        val galleryToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(galleryToolbar)
        supportActionBar?.title = "Gallery with Music"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val detailView = findViewById<ImageView>(R.id.detail_image)
        var bitmap = rotate(Uri.parse(intent.getStringExtra("uri")), this)
        detailView.setImageBitmap(bitmap)

        detectFace(bitmap)

        val chooseButton = findViewById<Button>(R.id.choose_button)
        chooseButton.setOnClickListener {
            val emotions = arrayOf("neutral", "sad", "happy", "surprise", "disgust", "fear", "angry")
            var selected = "neutral"
            val builder = AlertDialog.Builder(this).run {
                setTitle("Select how you feel")
                setSingleChoiceItems(emotions, 0) { dialog, which ->
                    selected = emotions[which]
                }
                setPositiveButton("Ok") { dialog, which ->

                }
                setNegativeButton("Cancel") { dialog, which ->

                }
                show()
            }
        }
        chooseButton.visibility = View.GONE

        val youtube: YouTubePlayerView = findViewById(R.id.youtube_screen)

        lifecycle.addObserver(youtube)

        youtube.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "TBjie1MP5e0"
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })
    }

    private fun rotate(uri: Uri, context: Context): Bitmap {
        val inputStream1 = context.contentResolver.openInputStream(uri)
        val inputStream2 = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream1)
        inputStream1?.close()

        val rotateInterface = ExifInterface(inputStream2!!)
        val orientation = rotateInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val mat = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> mat.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> mat.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> mat.postRotate(270f)
        }
        inputStream2.close()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mat, true)
    }

    private fun detectFace(bitmap: Bitmap){
        val option = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient(option)
        val result = detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()){
                    val chooseButton = findViewById<Button>(R.id.choose_button)
                    chooseButton.visibility = View.VISIBLE
                }
                else {
                    val face = faces[0]
                    val bounds = face.boundingBox
                    if (bounds.left >= 0 && bounds.top >= 0
                        && bounds.width() > 0 && bounds.height() >= 0
                        && bounds.right <= bitmap.width && bounds.bottom <= bitmap.height) {
                        cropped = Bitmap.createBitmap(
                            bitmap,
                            bounds.left,
                            bounds.top,
                            bounds.width(),
                            bounds.height()
                        )
                        FaceRecognition.doInference(this, cropped)
                    }
                    else{
                        val chooseButton = findViewById<Button>(R.id.choose_button)
                        chooseButton.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d("chaeypar", e.toString());
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        onBackPressed()
        return true
    }
}