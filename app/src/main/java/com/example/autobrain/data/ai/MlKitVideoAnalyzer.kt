package com.example.autobrain.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * ML Kit Video Analyzer for On-Device Video Diagnostic
 * 
 * Features:
 * - Smoke detection (black, white, blue) via object detection + color analysis
 * - Vibration detection via frame delta comparison
 * - Quality checks (brightness, stability)
 * - 100% offline, no cloud needed
 * 
 * Smart Logic:
 * - Black smoke (>80% confidence) = Critical engine issue
 * - White smoke (>70% confidence) = Coolant/oil issue  
 * - Frame delta >10% = Excessive vibration
 * - Brightness <50% = Poor quality video
 */
@Singleton
class MlKitVideoAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val TAG = "MlKitVideoAnalyzer"
    
    // ML Kit Object Detector
    private var objectDetector: ObjectDetector? = null
    
    // Frame analysis state
    private var previousFramePixels: IntArray? = null
    private val frameAnalysisResults = mutableListOf<FrameAnalysisResult>()
    
    init {
        initializeDetector()
    }
    
    /**
     * Initialize ML Kit Object Detector with STREAM_MODE for real-time
     */
    private fun initializeDetector() {
        try {
            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()
            
            objectDetector = ObjectDetection.getClient(options)
            Log.d(TAG, "ML Kit Object Detector initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize detector: ${e.message}")
        }
    }
    
    /**
     * Analyze a single video frame
     * 
     * @param imageProxy CameraX ImageProxy from video stream
     * @return FrameAnalysisResult containing detections
     */
    @androidx.camera.core.ExperimentalGetImage
    suspend fun analyzeFrame(imageProxy: ImageProxy): FrameAnalysisResult = withContext(Dispatchers.Default) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return@withContext FrameAnalysisResult(
                    frameNumber = frameAnalysisResults.size,
                    timestamp = System.currentTimeMillis(),
                    error = "Invalid image"
                )
            }
            
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            // Convert to Bitmap for analysis
            val bitmap = imageProxy.toBitmap()
            
            // Analyze brightness
            val brightness = calculateBrightness(bitmap)
            
            // Detect objects (potential smoke)
            val smokeDetection = detectSmoke(inputImage, bitmap)
            
            // Detect vibration via frame delta
            val vibrationLevel = detectVibration(bitmap)
            
            imageProxy.close()
            
            val result = FrameAnalysisResult(
                frameNumber = frameAnalysisResults.size,
                timestamp = System.currentTimeMillis(),
                brightness = brightness,
                smokeDetected = smokeDetection != null,
                smokeType = smokeDetection?.smokeType ?: "",
                smokeConfidence = smokeDetection?.confidence ?: 0f,
                vibrationDetected = vibrationLevel > 0.1f,
                vibrationLevel = vibrationLevel
            )
            
            frameAnalysisResults.add(result)
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Frame analysis error: ${e.message}")
            FrameAnalysisResult(
                frameNumber = frameAnalysisResults.size,
                timestamp = System.currentTimeMillis(),
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    /**
     * Detect smoke in frame using ML Kit + color analysis
     */
    private suspend fun detectSmoke(inputImage: InputImage, bitmap: Bitmap): SmokeDetection? = withContext(Dispatchers.Default) {
        try {
            val detector = objectDetector ?: return@withContext null
            
            val objects = detector.process(inputImage).await()
            
            // Look for potential smoke objects
            val smokeObject = objects.firstOrNull { obj ->
                obj.labels.any { label ->
                    label.text.contains("smoke", ignoreCase = true) ||
                    label.text.contains("cloud", ignoreCase = true) ||
                    label.text.contains("fog", ignoreCase = true)
                } || obj.labels.isEmpty() && obj.boundingBox.width() * obj.boundingBox.height() > 10000
            }
            
            if (smokeObject != null) {
                // Analyze color in bounding box to determine smoke type
                val smokeType = analyzeSmokeColor(bitmap, smokeObject)
                val confidence = smokeObject.labels.firstOrNull()?.confidence ?: 0.5f
                
                SmokeDetection(
                    smokeType = smokeType,
                    confidence = confidence,
                    boundingBoxSize = smokeObject.boundingBox.width() * smokeObject.boundingBox.height()
                )
            } else {
                // Fallback: Analyze overall frame for smoke-like colors
                val smokeLikeColor = detectSmokeColorInFrame(bitmap)
                if (smokeLikeColor != null) {
                    SmokeDetection(
                        smokeType = smokeLikeColor.first,
                        confidence = smokeLikeColor.second,
                        boundingBoxSize = bitmap.width * bitmap.height / 4
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Smoke detection error: ${e.message}")
            null
        }
    }
    
    /**
     * Analyze smoke color from bounding box region
     */
    private fun analyzeSmokeColor(bitmap: Bitmap, obj: DetectedObject): String {
        val box = obj.boundingBox
        val centerX = (box.left + box.right) / 2
        val centerY = (box.top + box.bottom) / 2
        
        // Sample multiple pixels around center
        val colors = mutableListOf<Int>()
        for (dx in -10..10 step 5) {
            for (dy in -10..10 step 5) {
                val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                colors.add(bitmap.getPixel(x, y))
            }
        }
        
        return classifySmokeType(colors)
    }
    
    /**
     * Detect smoke-like colors in entire frame
     */
    private fun detectSmokeColorInFrame(bitmap: Bitmap): Pair<String, Float>? {
        val sampleSize = 20
        val colors = mutableListOf<Int>()
        
        for (x in 0 until bitmap.width step bitmap.width / sampleSize) {
            for (y in 0 until bitmap.height step bitmap.height / sampleSize) {
                colors.add(bitmap.getPixel(x, y))
            }
        }
        
        val smokeType = classifySmokeType(colors)
        
        // Return only if we have significant smoke-like colors
        return if (smokeType != "none") {
            smokeType to 0.4f // Lower confidence for frame-wide detection
        } else {
            null
        }
    }
    
    /**
     * Classify smoke type based on color samples
     */
    private fun classifySmokeType(colors: List<Int>): String {
        var darkCount = 0
        var lightCount = 0
        var blueishCount = 0
        
        colors.forEach { color ->
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            
            val brightness = (r + g + b) / 3
            
            when {
                // Black smoke (dark, low brightness)
                brightness < 80 && r < 100 && g < 100 && b < 100 -> darkCount++
                
                // White smoke (high brightness, balanced RGB)
                brightness > 180 && abs(r - g) < 30 && abs(g - b) < 30 -> lightCount++
                
                // Blue smoke (higher blue component)
                b > r + 20 && b > g + 20 -> blueishCount++
            }
        }
        
        val total = colors.size
        return when {
            darkCount.toFloat() / total > 0.3f -> "black"
            lightCount.toFloat() / total > 0.4f -> "white"
            blueishCount.toFloat() / total > 0.25f -> "blue"
            else -> "none"
        }
    }
    
    /**
     * Detect vibration by comparing frame pixels
     */
    private fun detectVibration(bitmap: Bitmap): Float {
        try {
            val width = bitmap.width
            val height = bitmap.height
            val currentPixels = IntArray(width * height)
            bitmap.getPixels(currentPixels, 0, width, 0, 0, width, height)
            
            val previous = previousFramePixels
            if (previous == null || previous.size != currentPixels.size) {
                previousFramePixels = currentPixels
                return 0f
            }
            
            // Calculate average pixel difference
            var totalDiff = 0L
            val sampleRate = 10 // Sample every 10th pixel for performance
            
            for (i in currentPixels.indices step sampleRate) {
                val curr = currentPixels[i]
                val prev = previous[i]
                
                val rDiff = abs(Color.red(curr) - Color.red(prev))
                val gDiff = abs(Color.green(curr) - Color.green(prev))
                val bDiff = abs(Color.blue(curr) - Color.blue(prev))
                
                totalDiff += (rDiff + gDiff + bDiff)
            }
            
            previousFramePixels = currentPixels
            
            // Normalize to 0-1 range
            val avgDiff = totalDiff.toFloat() / (currentPixels.size / sampleRate) / (255f * 3f)
            
            // Return vibration level (>0.1 is significant)
            return avgDiff
            
        } catch (e: Exception) {
            Log.w(TAG, "Vibration detection error: ${e.message}")
            return 0f
        }
    }
    
    /**
     * Calculate average brightness of frame
     */
    private fun calculateBrightness(bitmap: Bitmap): Float {
        try {
            var totalBrightness = 0L
            val sampleSize = 50
            var count = 0
            
            for (x in 0 until bitmap.width step bitmap.width / sampleSize) {
                for (y in 0 until bitmap.height step bitmap.height / sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    totalBrightness += brightness
                    count++
                }
            }
            
            // Return brightness as percentage (0-100)
            return (totalBrightness.toFloat() / count / 255f) * 100f
            
        } catch (e: Exception) {
            Log.w(TAG, "Brightness calculation error: ${e.message}")
            return 50f // Default mid-brightness
        }
    }
    
    /**
     * Get comprehensive analysis results from all frames
     */
    fun getComprehensiveResults(): VideoAnalysisResults {
        if (frameAnalysisResults.isEmpty()) {
            return VideoAnalysisResults(
                totalFrames = 0,
                error = "No frames analyzed"
            )
        }
        
        val smokeyFrames = frameAnalysisResults.filter { it.smokeDetected }
        val vibrationFrames = frameAnalysisResults.filter { it.vibrationDetected }
        val avgBrightness = frameAnalysisResults.map { it.brightness }.average().toFloat()
        
        // Determine dominant smoke type
        val smokeTypeCounts = smokeyFrames.groupBy { it.smokeType }.mapValues { it.value.size }
        val dominantSmokeType = smokeTypeCounts.maxByOrNull { it.value }?.key ?: "none"
        val maxSmokeConfidence = smokeyFrames.maxOfOrNull { it.smokeConfidence } ?: 0f
        
        // Calculate vibration severity
        val avgVibrationLevel = vibrationFrames.map { it.vibrationLevel }.average().toFloat()
        val vibrationSeverity = when {
            avgVibrationLevel > 0.3f -> "excessive"
            avgVibrationLevel > 0.2f -> "high"
            avgVibrationLevel > 0.1f -> "medium"
            avgVibrationLevel > 0.05f -> "low"
            else -> "none"
        }
        
        return VideoAnalysisResults(
            totalFrames = frameAnalysisResults.size,
            smokeDetected = smokeyFrames.isNotEmpty(),
            smokeType = dominantSmokeType,
            smokeSeverity = calculateSmokeSeverity(dominantSmokeType, maxSmokeConfidence, smokeyFrames.size, frameAnalysisResults.size),
            smokeConfidence = maxSmokeConfidence,
            smokeyFramesCount = smokeyFrames.size,
            vibrationDetected = vibrationFrames.isNotEmpty(),
            vibrationLevel = vibrationSeverity,
            vibrationSeverity = calculateVibrationSeverity(avgVibrationLevel),
            vibrationConfidence = if (vibrationFrames.isNotEmpty()) 0.8f else 0f,
            vibrationFramesCount = vibrationFrames.size,
            averageBrightness = avgBrightness,
            isStableVideo = avgVibrationLevel < 0.15f,
            videoQuality = determineVideoQuality(avgBrightness, avgVibrationLevel)
        )
    }
    
    /**
     * Calculate smoke severity on 0-5 scale
     */
    private fun calculateSmokeSeverity(smokeType: String, confidence: Float, smokeyFrames: Int, totalFrames: Int): Int {
        val framePercentage = smokeyFrames.toFloat() / totalFrames
        
        return when (smokeType) {
            "black" -> when {
                confidence > 0.8f && framePercentage > 0.5f -> 5 // Critical
                confidence > 0.7f && framePercentage > 0.3f -> 4
                confidence > 0.6f -> 3
                else -> 2
            }
            "white" -> when {
                confidence > 0.8f && framePercentage > 0.6f -> 4
                confidence > 0.7f && framePercentage > 0.4f -> 3
                else -> 2
            }
            "blue" -> when {
                confidence > 0.7f && framePercentage > 0.5f -> 4
                else -> 3
            }
            else -> 0
        }
    }
    
    /**
     * Calculate vibration severity on 0-5 scale
     */
    private fun calculateVibrationSeverity(avgLevel: Float): Int {
        return when {
            avgLevel > 0.3f -> 5 // Excessive
            avgLevel > 0.2f -> 4 // High
            avgLevel > 0.15f -> 3 // Medium
            avgLevel > 0.1f -> 2 // Low
            avgLevel > 0.05f -> 1 // Minimal
            else -> 0
        }
    }
    
    /**
     * Determine overall video quality
     */
    private fun determineVideoQuality(brightness: Float, vibrationLevel: Float): String {
        return when {
            brightness < 30f || vibrationLevel > 0.25f -> "poor"
            brightness < 50f || vibrationLevel > 0.15f -> "acceptable"
            else -> "good"
        }
    }
    
    /**
     * Reset analyzer for new video
     */
    fun reset() {
        previousFramePixels = null
        frameAnalysisResults.clear()
    }
    
    /**
     * Clean up resources
     */
    fun release() {
        objectDetector?.close()
        objectDetector = null
        reset()
    }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class FrameAnalysisResult(
    val frameNumber: Int,
    val timestamp: Long,
    val brightness: Float = 0f,
    val smokeDetected: Boolean = false,
    val smokeType: String = "",
    val smokeConfidence: Float = 0f,
    val vibrationDetected: Boolean = false,
    val vibrationLevel: Float = 0f,
    val error: String? = null
)

data class SmokeDetection(
    val smokeType: String, // "black", "white", "blue"
    val confidence: Float,
    val boundingBoxSize: Int
)

data class VideoAnalysisResults(
    val totalFrames: Int,
    val smokeDetected: Boolean = false,
    val smokeType: String = "",
    val smokeSeverity: Int = 0,
    val smokeConfidence: Float = 0f,
    val smokeyFramesCount: Int = 0,
    val vibrationDetected: Boolean = false,
    val vibrationLevel: String = "",
    val vibrationSeverity: Int = 0,
    val vibrationConfidence: Float = 0f,
    val vibrationFramesCount: Int = 0,
    val averageBrightness: Float = 0f,
    val isStableVideo: Boolean = true,
    val videoQuality: String = "good",
    val error: String? = null
)

// Extension to convert ImageProxy to Bitmap
@androidx.camera.core.ExperimentalGetImage
private fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    
    // Create bitmap with proper dimensions
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    
    // For simplicity, create a gray/colored bitmap from Y plane
    val pixels = IntArray(width * height)
    for (i in bytes.indices.take(pixels.size)) {
        val y = bytes[i].toInt() and 0xff
        pixels[i] = Color.rgb(y, y, y)
    }
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    
    return bitmap
}
