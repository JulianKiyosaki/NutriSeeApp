package com.capstone.nutrisee.helper

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

data class FoodNutritionData(
    val className: String,
    val calories: Float,
    val protein: Float,
    val carbohydrates: Float,
    val fat: Float,
    val fiber: Float
)

val foodNutritionDatabase = listOf(
    FoodNutritionData("Ayam Bakar", 167f, 25.01f, 0.0f, 6.63f, 0.0f),
    FoodNutritionData("Ayam Geprek", 263f, 17.61f, 7.6f, 17.99f, 0.8f),
    FoodNutritionData("Ayam Goreng", 260f, 21.93f, 10.76f, 14.55f, 1.4f),
    FoodNutritionData("Bakso", 444f, 42.39f, 0.61f, 28.86f, 0.0f),
    FoodNutritionData("Ikan Bakar", 200f, 30.0f, 0.0f, 8.0f, 0.0f),
    FoodNutritionData("Mie Goreng", 350f, 8.0f, 50.0f, 15.0f, 2.0f),
    FoodNutritionData("Nasi Goreng", 400f, 8.0f, 55.0f, 13.0f, 2.0f),
    FoodNutritionData("Nasi Kuning", 150f, 2.99f, 32.96f, 0.27f, 0.6f),
    FoodNutritionData("Nasi Putih", 204f, 4.2f, 44.08f, 0.44f, 0.6f),
    FoodNutritionData("Pecel", 250f, 10.0f, 30.0f, 10.0f, 4.0f),
    FoodNutritionData("Rendang", 468f, 19.0f, 5.0f, 40.0f, 2.0f),
    FoodNutritionData("Sambal", 30f, 0.5f, 3.0f, 1.5f, 0.5f),
    FoodNutritionData("Sate Ayam", 34f, 2.93f, 0.73f, 2.22f, 0.3f),
    FoodNutritionData("Soto Ayam", 312f, 24.01f, 19.55f, 14.92f, 1.7f),
    FoodNutritionData("Tahu Goreng", 35f, 2.23f, 1.36f, 2.62f, 0.5f),
    FoodNutritionData("Telur Goreng", 92f, 6.3f, 0.6f, 7.0f, 0.0f),
    FoodNutritionData("Telur Rebus", 77f, 6.3f, 0.6f, 5.3f, 0.0f),
    FoodNutritionData("Tempe Goreng", 34f, 2.0f, 1.79f, 2.28f, 0.0f)
)

val foodNutritionMap = foodNutritionDatabase.associateBy { it.className }

val foodClasses = listOf(
    "Ayam Bakar", "Ayam Geprek", "Ayam Goreng", "Bakso", "Ikan Bakar",
    "Mie Goreng", "Nasi Goreng", "Nasi Kuning", "Nasi Putih", "Pecel",
    "Rendang", "Sambal", "Sate Ayam", "Soto Ayam", "Tahu Goreng",
    "Telur Goreng", "Telur Rebus", "Tempe Goreng"
)

data class NutritionResult(
    val foodName: String,
    val protein: Float?,
    val carbohydrate: Float?,
    val fat: Float?,
    val fiber: Float?,
    val calories: Float?,
    val boundingBox: RectF? = null,
    val confidence: Float?
)

interface FoodAnalysisListener {
    fun onError(error: String)
    fun onResults(results: List<NutritionResult>?, inferenceTime: Long)
}

class ImageClassifierHelper(
    private val context: Context,
    private val listener: FoodAnalysisListener?,
    private val confidenceThreshold: Float = 0.5f,
    private val nmsThreshold: Float = 0.5f,
    private val modelPath: String = "best_float32.tflite"
) {

    private var interpreter: Interpreter? = null
    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0

    init {
        try {
            val interpreterOptions = Interpreter.Options()
            interpreter = Interpreter(loadModelFile(context.assets, modelPath), interpreterOptions)

            val inputTensor = interpreter?.getInputTensor(0)
            inputImageWidth = inputTensor?.shape()?.get(2) ?: 0
            inputImageHeight = inputTensor?.shape()?.get(1) ?: 0

            Log.i(TAG, "TFLite model loaded successfully.")
        } catch (e: Exception) {
            listener?.onError("Error initializing: ${e.message}")
            Log.e(TAG, "Error initializing", e)
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun analyzeFood(bitmap: Bitmap) {
        if (interpreter == null) {
            listener?.onError("Interpreter not initialized.")
            return
        }

        val startTime = SystemClock.uptimeMillis()

        try {
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0.0f, 255.0f))
                .build()

            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

            val outputTensor = interpreter!!.getOutputTensor(0)
            val outputBuffer = ByteBuffer.allocateDirect(outputTensor.numBytes())
            outputBuffer.order(ByteOrder.nativeOrder())

            interpreter?.run(tensorImage.buffer, outputBuffer)

            val nutritionResults = parseOutput(outputBuffer, bitmap.width, bitmap.height)
            val inferenceTime = SystemClock.uptimeMillis() - startTime
            listener?.onResults(nutritionResults, inferenceTime)

        } catch (e: Exception) {
            listener?.onError("Error during food analysis: ${e.message}")
            Log.e(TAG, "Error during food analysis", e)
        }
    }

    private fun parseOutput(outputBuffer: ByteBuffer, originalImageWidth: Int, originalImageHeight: Int): List<NutritionResult> {
        outputBuffer.rewind()
        val outputArray = FloatArray(interpreter!!.getOutputTensor(0).numElements())
        outputBuffer.asFloatBuffer().get(outputArray)

        val outputShape = interpreter!!.getOutputTensor(0).shape()
        val numElementsPerDetection = outputShape[1]
        val numDetections = outputShape[2]

        if (foodClasses.size != (numElementsPerDetection - 4)) {
            Log.e(TAG, "Class count mismatch. From Code: ${foodClasses.size}, From Model: ${numElementsPerDetection - 4}")
            return emptyList()
        }

        val detectedObjects = mutableListOf<DetectionCandidate>()
        val transposedOutput = FloatArray(outputArray.size)
        for (i in 0 until numDetections) {
            for (j in 0 until numElementsPerDetection) {
                transposedOutput[i * numElementsPerDetection + j] = outputArray[j * numDetections + i]
            }
        }

        for (i in 0 until numDetections) {
            val offset = i * numElementsPerDetection
            val xCenter = transposedOutput[offset]
            val yCenter = transposedOutput[offset + 1]
            val width = transposedOutput[offset + 2]
            val height = transposedOutput[offset + 3]

            var maxClassScore = 0f
            var detectedClassIndex = -1
            for (j in 0 until foodClasses.size) {
                val score = transposedOutput[offset + 4 + j]
                if (score > maxClassScore) {
                    maxClassScore = score
                    detectedClassIndex = j
                }
            }

            if (maxClassScore > confidenceThreshold) {
                val scaleX = originalImageWidth.toFloat() / inputImageWidth.toFloat()
                val scaleY = originalImageHeight.toFloat() / inputImageHeight.toFloat()
                val left = (xCenter - width / 2) * scaleX
                val top = (yCenter - height / 2) * scaleY
                val right = (xCenter + width / 2) * scaleX
                val bottom = (yCenter + height / 2) * scaleY

                detectedObjects.add(
                    DetectionCandidate(
                        classIndex = detectedClassIndex,
                        confidence = maxClassScore,
                        boundingBox = RectF(left, top, right, bottom)
                    )
                )
            }
        }

        val nmsResults = applyNMS(detectedObjects, nmsThreshold)

        return nmsResults.map { candidate ->
            val foodClassName = foodClasses[candidate.classIndex]
            val nutritionData = foodNutritionMap[foodClassName]
            NutritionResult(
                foodName = foodClassName,
                protein = nutritionData?.protein,
                carbohydrate = nutritionData?.carbohydrates,
                fat = nutritionData?.fat,
                fiber = nutritionData?.fiber,
                calories = nutritionData?.calories,
                boundingBox = candidate.boundingBox,
                confidence = candidate.confidence
            )
        }
    }

    private data class DetectionCandidate(val classIndex: Int, val confidence: Float, val boundingBox: RectF)

    private fun applyNMS(detections: List<DetectionCandidate>, threshold: Float): List<DetectionCandidate> {
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val result = mutableListOf<DetectionCandidate>()
        for (detection in sortedDetections) {
            var shouldKeep = true
            for (kept in result) {
                if (detection.classIndex == kept.classIndex) {
                    val iou = calculateIoU(detection.boundingBox, kept.boundingBox)
                    if (iou > threshold) {
                        shouldKeep = false
                        break
                    }
                }
            }
            if (shouldKeep) {
                result.add(detection)
            }
        }
        return result
    }

    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionLeft = maxOf(box1.left, box2.left)
        val intersectionTop = maxOf(box1.top, box2.top)
        val intersectionRight = minOf(box1.right, box2.right)
        val intersectionBottom = minOf(box1.bottom, box2.bottom)

        if (intersectionLeft >= intersectionRight || intersectionTop >= intersectionBottom) {
            return 0f
        }

        val intersectionArea = (intersectionRight - intersectionLeft) * (intersectionBottom - intersectionTop)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        val unionArea = box1Area + box2Area - intersectionArea
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }

    fun close() {
        interpreter?.close()
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}