package com.example.gallerywithmusic

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
class DetailActivity: AppCompatActivity() {
    private lateinit var cropped : Bitmap
    private lateinit var segmentationDNN: Interpreter
    private lateinit var dnnInput : Array<Array<Array<FloatArray>>>
    private lateinit var dnnOutput: Array<FloatArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.picture_main)

        System.loadLibrary("opencv_java4")

        val galleryToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(galleryToolbar)
        supportActionBar?.title = "Gallery with Music"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val detailView = findViewById<ImageView>(R.id.detail_image)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(intent.getStringExtra("uri")))
        detailView.setImageBitmap(bitmap)

        modelSetting()
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

    }

    private fun modelSetting(){
        val model_name="fer_model.tflite"
        val tfliteModel: MappedByteBuffer
        try {
            tfliteModel = loadModelFile(this, model_name)
            val options = Interpreter.Options()
            options.setNumThreads(8)
            segmentationDNN = Interpreter(tfliteModel, options)
        } catch (e:Exception){
            Log.d("chaeypar", "tflite model not loaded")
        }

        val num_inputs = segmentationDNN.getInputTensorCount();
        for (i in 0 until num_inputs) {
            val inputTensor: Tensor = segmentationDNN.getInputTensor(i)
            val shape = inputTensor.shape()
            dnnInput = Array(shape[0]) {
                Array(shape[1]) {
                    Array(shape[2]) {
                        FloatArray(shape[3])
                    }
                }
            }
        }

        val num_outputs = segmentationDNN.getOutputTensorCount();
        for (i in 0 until num_outputs) {
            val outputTensor: Tensor = segmentationDNN.getOutputTensor(i)
            val shape = outputTensor.shape()
            dnnOutput = Array(shape[0]) {
                FloatArray(shape[1])
            }
        }
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
                    cropped = Bitmap.createBitmap(
                        bitmap,
                        bounds.left,
                        bounds.top,
                        bounds.width(),
                        bounds.height()
                    )
                    doInference()
                }
            }
            .addOnFailureListener { e ->
                Log.d("chaeypar", e.toString());
            }
    }

    private fun doInference(){
        val dnnInputSize = 48
        val mat = Mat()
        Utils.bitmapToMat(cropped, mat)

        val resized = Mat()
        val size = Size(dnnInputSize / 1.0, dnnInputSize / 1.0)
        Imgproc.resize(mat, resized, size)

        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY)

        for (i in 0 until dnnInputSize) {
            for (j in 0 until dnnInputSize) {
                val cur = grayMat.get(i, j)
                dnnInput[0][i][j][0] = cur[0].toFloat()
            }
        }
        segmentationDNN.run(dnnInput, dnnOutput)

        var maxi:Float = 0f
        var idx = 0
        for (i in 0 until 7) {
            if (dnnOutput[0][i] > maxi) {
                maxi = dnnOutput[0][i]
                idx = i
            }
        }
    }

    private fun loadModelFile(activity: Activity, model_name: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(model_name)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        onBackPressed()
        return true
    }

}