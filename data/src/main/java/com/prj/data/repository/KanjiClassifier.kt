package com.prj.data.repository

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.prj.domain.model.dictionaryscreen.KanjiImage
import com.prj.domain.repository.IKanjiClassifier
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import timber.log.Timber
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.PriorityQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.min

class Recognition(val id: Int, val title: String, val confidence: Float) {
    override fun toString(): String {
        return String.format("[%d] %s - (%.1f)", id, title, confidence)
    }
}

class KanjiClassifier @Inject constructor(@ApplicationContext private val context: Context) :
    IKanjiClassifier {
    private var interpreter: Interpreter? = null
    private var modelInputSize: Int = 0
    private var isInitialized = false
    /** Executor to run inference task in the background. */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    private lateinit var labels: List<String>
    private var numLabels: Int = 0

    private fun initializeInterpreter() {
        //Load TF lite model from file and init interpreter
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "etlcb_9b_model.tflite")
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(Runtime.getRuntime().availableProcessors())
        tfliteOptions.setUseNNAPI(true)
        val interpreter = Interpreter(model, tfliteOptions)
        labels = loadLabelList(context)
        numLabels = labels.size
        modelInputSize = ((DIM_BATCH_SIZE
                * IMAGE_HEIGHT
                * IMAGE_WIDTH
                * DIM_PIXEL_SIZE
                * NUM_BYTES_PER_PIXEL))
        this.interpreter = interpreter
        isInitialized = true
    }

    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class)
    fun loadLabelList(context: Context): List<String> {
        val labels = ArrayList<String>()
        val reader =
            BufferedReader(
                InputStreamReader(context.assets.open(LABEL_FILE_PATH))
            )
        var line = reader.readLine()
        while (line != null) {
            labels.add(line)
            line = reader.readLine()
        }
        return labels
    }

    private fun normalizePixelValue(pixelValue: Int) : Float{
        val r  = (pixelValue shr 16) and 0xFF
        val g = (pixelValue shr 8) and 0xFF
        val b = pixelValue and 0xFF
        val grey = ((0.299f * r + 0.597f * g + 0.114f * b) / 255f)
        return grey
    }

    private lateinit var byteBuffer: ByteBuffer

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        if ((bitmap.width != IMAGE_WIDTH) && (bitmap.height != IMAGE_HEIGHT)) {
            throw Exception(
                String.format(
                    "The image with shape (%d, %d) is not equals (%d, %d)!!!",
                    bitmap.width, bitmap.height, IMAGE_WIDTH, IMAGE_HEIGHT
                )
            )
        }
        byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.rewind()

        val intValues = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT )
        var id = 0
        for(i in 0 until IMAGE_HEIGHT){
            for(j in 0 until IMAGE_WIDTH) {
                Timber.i("Pixel value before: ${intValues[id++]}")
            }
        }

        bitmap.getPixels(intValues, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        var index = 0
        for(i in 0 until IMAGE_HEIGHT){
            for(j in 0 until IMAGE_WIDTH) {
                val pixelValue = intValues[index++]
                Timber.i("Pixel value after: $pixelValue")
                byteBuffer.putFloat(normalizePixelValue(pixelValue))
            }
        }
    }

    override fun classifyAsync(image: KanjiImage): Result<List<String>> {
        try {
            // Ensure thread safety that initializeInterpreter() called only once at a time
            if (!isInitialized) {
                synchronized(this) {
                    if (!isInitialized) {
                        initializeInterpreter()
                    }
                }
            }
            val bitmap = decodeImage(image)
            val result = classify(bitmap)
            return Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Classification failed")
            return Result.failure(e)
        }
    }

    private fun classify(bitmap: Bitmap): List<String> {
        check(isInitialized) {"TF lite not init yet"}

        convertBitmapToByteBuffer(bitmap)
        Timber.i("Bytebuffer: $byteBuffer")
        //array to store model output
        val output = Array(1){FloatArray(OUTPUT_CLASSES_COUNT)}

        //run interpreter with input
        interpreter?.run(byteBuffer ,output)

        // Post-processing: find the character with the highest probability
        // and return it a human-readable string.
        val results = java.util.ArrayList<Recognition>()

        // sort the result by confidence
        val pq: PriorityQueue<Recognition> =
            PriorityQueue<Recognition>(
                numLabels,
                Comparator { a, b -> // we want to sort descending
                    b.confidence.compareTo(a.confidence)
                })

        for (i in 0 until numLabels) {
            val labelStr = labels[i]
            for (j in labelStr.indices) {
                val labelChar = labelStr.substring(j, j + 1)
                pq.add(Recognition(i, labelChar, output[0][i]))
            }
        }

        val returnSize: Int = min(pq.size, MAX_RESULTS)
        for (i in 0 until returnSize) {
            pq.poll()?.let { results.add(it) }
            Timber.i(" Classify all results: " + results[i].title + results[i].confidence)
        }
        // return list of 10 most identical kanji
        return results.map { it.title }
    }


    private fun decodeImage(image: KanjiImage): Bitmap {
        return BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
    }

    fun close() {
        executorService.execute {
            interpreter?.close()
            Timber.d("Closed TFLite interpreter.")
        }
    }

    companion object {
        private const val DIM_BATCH_SIZE = 1;
        private const val DIM_PIXEL_SIZE = 1;
        private const val IMAGE_WIDTH = 64; //Input array shape of 64 x 64
        private const val IMAGE_HEIGHT = 64;
        private const val NUM_BYTES_PER_PIXEL = 4;
        private const val MAX_RESULTS = 10

        private const val LABEL_FILE_PATH = "etlcb_9b_labels.txt"
        private const val OUTPUT_CLASSES_COUNT = 3036
    }


}