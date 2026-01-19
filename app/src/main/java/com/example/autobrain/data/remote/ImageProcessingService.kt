package com.example.autobrain.data.remote

import android.graphics.*
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Professional Image Processing Service
 * Applies consistent studio-quality styling to all car images
 */
@Singleton
class ImageProcessingService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val firebaseStorage: FirebaseStorage,
    private val backgroundRemovalService: BackgroundRemovalService
) {
    private val TAG = "ImageProcessingService"
    
    // Professional styling constants
    private val CANVAS_WIDTH = 1920
    private val CANVAS_HEIGHT = 1080
    private val CAR_WIDTH_RATIO = 0.75f
    private val CAR_VERTICAL_OFFSET = -50f // Slightly above center
    private val SHADOW_INTENSITY = 40 // 0-255
    private val SHADOW_BLUR = 25f
    
    /**
     * Main processing pipeline: Download → Remove BG → Apply Studio Style → Upload
     */
    suspend fun applyConsistentStyling(imageUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎨 Starting professional image processing pipeline")
            Log.d(TAG, "📥 Source: $imageUrl")
            
            // Step 1: Download original image
            val originalBitmap = downloadImage(imageUrl)
            if (originalBitmap == null) {
                Log.e(TAG, "❌ Failed to download image")
                return@withContext Result.failure(Exception("Failed to download image"))
            }
            Log.d(TAG, "✅ Downloaded: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Step 2: Remove background using remove.bg
            val transparentBitmap = removeBackgroundFromBitmap(originalBitmap)
            if (transparentBitmap == null) {
                Log.w(TAG, "⚠️ Background removal failed, using original")
                // Continue with original if background removal fails
                return@withContext applyStudioBackgroundAndUpload(originalBitmap)
            }
            Log.d(TAG, "✅ Background removed successfully")
            
            // Step 3: Apply professional studio background
            val styledBitmap = applyStudioBackground(transparentBitmap)
            Log.d(TAG, "✅ Studio styling applied: ${styledBitmap.width}x${styledBitmap.height}")
            
            // Step 4: Upload to Firebase Storage
            val firebaseUrl = uploadToFirebase(styledBitmap)
            Log.d(TAG, "☁️ Uploaded to Firebase: $firebaseUrl")
            
            Result.success(firebaseUrl)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Processing error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download image from URL
     */
    private suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Download failed: HTTP ${response.code}")
                return@withContext null
            }
            
            val bytes = response.body?.bytes()
            if (bytes == null || bytes.isEmpty()) {
                Log.e(TAG, "Empty response body")
                return@withContext null
            }
            
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}")
            null
        }
    }
    
    /**
     * Remove background using remove.bg service
     */
    private suspend fun removeBackgroundFromBitmap(bitmap: Bitmap): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Convert bitmap to bytes
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageBytes = stream.toByteArray()
            
            // Upload to temporary location
            val tempRef = firebaseStorage.reference.child("temp/${UUID.randomUUID()}.png")
            tempRef.putBytes(imageBytes).await()
            val tempUrl = tempRef.downloadUrl.await().toString()
            
            // Remove background
            val result = backgroundRemovalService.removeBackground(tempUrl)
            
            // Clean up temp file
            tempRef.delete()
            
            if (result.isSuccess) {
                val processedUrl = result.getOrNull()
                if (processedUrl != null) {
                    return@withContext downloadImage(processedUrl)
                }
            }
            
            null
        } catch (e: Exception) {
            Log.e(TAG, "Background removal error: ${e.message}")
            null
        }
    }
    
    /**
     * Apply professional studio background with gradient and shadow
     */
    private fun applyStudioBackground(carBitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        // 1. Draw professional gradient background
        drawGradientBackground(canvas)
        
        // 2. Draw soft shadow under car
        drawSoftShadow(canvas)
        
        // 3. Draw car centered and properly sized
        drawCenteredCar(canvas, carBitmap)
        
        return result
    }
    
    /**
     * Draw professional gradient background (white to light gray)
     */
    private fun drawGradientBackground(canvas: Canvas) {
        val gradient = LinearGradient(
            0f, 0f, 
            0f, CANVAS_HEIGHT.toFloat(),
            intArrayOf(
                Color.parseColor("#FFFFFF"), // Pure white top
                Color.parseColor("#F5F5F5"), // Very light gray middle
                Color.parseColor("#E8E8E8")  // Light gray bottom
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }
        
        canvas.drawRect(0f, 0f, CANVAS_WIDTH.toFloat(), CANVAS_HEIGHT.toFloat(), paint)
    }
    
    /**
     * Draw soft shadow under car for depth
     */
    private fun drawSoftShadow(canvas: Canvas) {
        val shadowPaint = Paint().apply {
            color = Color.BLACK
            alpha = SHADOW_INTENSITY
            maskFilter = BlurMaskFilter(SHADOW_BLUR, BlurMaskFilter.Blur.NORMAL)
            isAntiAlias = true
        }
        
        // Elliptical shadow at bottom
        val shadowLeft = CANVAS_WIDTH * 0.25f
        val shadowTop = CANVAS_HEIGHT * 0.75f
        val shadowRight = CANVAS_WIDTH * 0.75f
        val shadowBottom = CANVAS_HEIGHT * 0.88f
        
        canvas.drawOval(shadowLeft, shadowTop, shadowRight, shadowBottom, shadowPaint)
    }
    
    /**
     * Draw car centered and properly sized
     */
    private fun drawCenteredCar(canvas: Canvas, carBitmap: Bitmap) {
        // Calculate dimensions maintaining aspect ratio
        val carWidth = CANVAS_WIDTH * CAR_WIDTH_RATIO
        val carHeight = carWidth * (carBitmap.height.toFloat() / carBitmap.width)
        
        // Center horizontally, slightly above center vertically
        val left = (CANVAS_WIDTH - carWidth) / 2
        val top = (CANVAS_HEIGHT - carHeight) / 2 + CAR_VERTICAL_OFFSET
        
        val destRect = RectF(left, top, left + carWidth, top + carHeight)
        
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }
        
        canvas.drawBitmap(carBitmap, null, destRect, paint)
    }
    
    /**
     * Helper method for cases where background removal fails
     */
    private suspend fun applyStudioBackgroundAndUpload(originalBitmap: Bitmap): Result<String> {
        return try {
            val styledBitmap = applyStudioBackground(originalBitmap)
            val firebaseUrl = uploadToFirebase(styledBitmap)
            Result.success(firebaseUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload processed image to Firebase Storage
     */
    private suspend fun uploadToFirebase(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val fileName = "car_images/professional_${UUID.randomUUID()}.png"
        val storageRef = firebaseStorage.reference.child(fileName)
        
        // Convert to PNG bytes
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageBytes = stream.toByteArray()
        
        Log.d(TAG, "📤 Uploading ${imageBytes.size / 1024}KB to Firebase Storage...")
        
        // Upload
        storageRef.putBytes(imageBytes).await()
        val downloadUrl = storageRef.downloadUrl.await()
        
        downloadUrl.toString()
    }
    
    /**
     * Quick validation that image meets professional standards
     */
    fun validateImageQuality(bitmap: Bitmap): Boolean {
        return bitmap.width >= 1280 && 
               bitmap.height >= 720 && 
               bitmap.config != null
    }
}
