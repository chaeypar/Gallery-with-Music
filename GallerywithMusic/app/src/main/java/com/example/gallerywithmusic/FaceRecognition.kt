package com.example.gallerywithmusic

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object FaceRecognition {
    private var segmentationDNN: Interpreter ?= null
    private lateinit var dnnInput: Array<Array<Array<FloatArray>>>
    private lateinit var dnnOutput: Array<FloatArray>

    private fun modelSetting(context: Context) {
        val model_name="reduced_fer_model.tflite"
        val tfliteModel: MappedByteBuffer
        try {
            tfliteModel = loadModelFile(context, model_name)
            val options = Interpreter.Options()
            options.setNumThreads(8)
            segmentationDNN = Interpreter(tfliteModel, options)
            Log.d("chaeypar", "tflite model loaded")
        } catch (e:Exception){
            Log.d("chaeypar", "tflite model not loaded")
        }

        val num_inputs = segmentationDNN!!.getInputTensorCount();
        for (i in 0 until num_inputs) {
            val inputTensor: Tensor = segmentationDNN!!.getInputTensor(i)
            val shape = inputTensor.shape()
            dnnInput = Array(shape[0]) {
                Array(shape[1]) {
                    Array(shape[2]) {
                        FloatArray(shape[3])
                    }
                }
            }
        }

        val num_outputs = segmentationDNN!!.getOutputTensorCount();
        for (i in 0 until num_outputs) {
            val outputTensor: Tensor = segmentationDNN!!.getOutputTensor(i)
            val shape = outputTensor.shape()
            dnnOutput = Array(shape[0]) {
                FloatArray(shape[1])
            }
        }
    }

    public fun doInference(context: Context, cropped: Bitmap) {
        if (segmentationDNN == null){
            modelSetting(context)
        }
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
        segmentationDNN!!.run(dnnInput, dnnOutput)

        var maxi: Float = 0f
        var idx = 0
        for (i in 0 until 4) {
            if (dnnOutput[0][i] > maxi) {
                maxi = dnnOutput[0][i]
                idx = i
            }
            Log.d("chaeypar", i.toString())
            Log.d("chaeypar", dnnOutput[0][i].toString())
        }
    }

    private fun loadModelFile(context: Context, model_name: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(model_name)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    public fun destroy(){
        segmentationDNN?.close()
    }
}