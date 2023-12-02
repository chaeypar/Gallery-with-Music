package com.example.gallerywithmusic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private var selected : String ?= null
    private lateinit var emotions: Array<String>
    private var youtubeplayer : YouTubePlayer?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_main)

        System.loadLibrary("opencv_java4")

        emotions = arrayOf("angry", "disgust", "fear", "happy", "neutral", "sad", "surprise")

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
            var idx = -1;
            val builder = AlertDialog.Builder(this).run {
                setTitle("Select how you feel")
                setSingleChoiceItems(emotions, 0) { dialog, which ->
                    idx = which
                }
                setPositiveButton("Ok") { dialog, which ->
                    if (idx != -1){
                        selected = emotions[idx]
                        setStatus(1)
                        setVideo()
                    }
                    else{
                        Toast.makeText(this@DetailActivity, "Select how you feel", Toast.LENGTH_SHORT).show()
                    }
                }
                setNegativeButton("Cancel") { dialog, which ->

                }
                show()
            }
        }

        val youtube: YouTubePlayerView = findViewById(R.id.youtube_screen)

        lifecycle.addObserver(youtube)

        youtube.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                var item : Music
                if (selected == null) {
                    val status = findViewById<TextView>(R.id.status_text)
                    status.text = getString(R.string.noface)
                    item = VideoSerial.setMusic("neutral")
                }
                else {
                    item = VideoSerial.setMusic(selected!!)
                    setStatus(0)
                }
                youTubePlayer.loadVideo(item.id, 0f)
                youtubeplayer = youTubePlayer
            }
        })
    }
    private fun setVideo(){
        val item = VideoSerial.setMusic(selected!!)
        youtubeplayer?.loadVideo(item.id, 0f)
    }

    private fun setStatus(stat: Int){
        val status = findViewById<TextView>(R.id.status_text)
        val msg = when (stat){
            0-> "You look"
            else -> "You may feel"
        }
        status.text = when(selected){
            "angry" -> "$msg angry"
            "disgust" -> "$msg disgusted"
            "fear" -> "$msg frightened"
            "happy" -> "$msg happy"
            "neutral" -> "$msg neutral"
            "sad" -> "$msg sad"
            else -> "$msg surprised"
        }
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
                if (!faces.isEmpty()){
                    val face = faces[0]
                    val bounds = face.boundingBox
                    if (bounds.left >= 0 && bounds.top >= 0
                        && bounds.width() >= 0 && bounds.height() >= 0
                        && bounds.right <= bitmap.width && bounds.bottom <= bitmap.height) {
                        val chooseButton = findViewById<Button>(R.id.choose_button)
                        chooseButton.text = getString(R.string.change_status)
                        cropped = Bitmap.createBitmap(
                            bitmap,
                            bounds.left,
                            bounds.top,
                            bounds.width(),
                            bounds.height()
                        )
                        val idx = FaceRecognition.doInference(this, cropped)
                        selected = emotions[idx]
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