package com.example.autobrain.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

/**
 * Professional Car Image Compositor
 * 
 * Creates consistent, professional-looking car images by compositing transparent
 * car PNGs onto standardized backgrounds. This ensures ALL user cars are displayed
 * with the same visual style, lighting, and presentation.
 * 
 * Consistency Features:
 * - Same dark gradient background for all cars
 * - Consistent car positioning and scaling
 * - Professional studio-quality presentation
 * - Matches reference design exactly
 */
object CarImageCompositor {
    private const val TAG = "CarImageCompositor"
    
    /**
     * Downloads and composites a transparent car image onto a professional background.
     * 
     * This is the CORE method for achieving consistent car presentations across
     * the entire app. All car images should use DARK_PROFESSIONAL background
     * to match the reference design.
     * 
     * @param context Android context
     * @param carImageUrl URL of the transparent car PNG (from Gemini + background removal)
     * @param backgroundType Type of background (default: DARK_PROFESSIONAL for consistency)
     * @return Result containing the composited professional car image bitmap
     */
    suspend fun compositeCarImage(
        context: Context,
        carImageUrl: String,
        backgroundType: BackgroundType = BackgroundType.DARK_PROFESSIONAL
    ): kotlin.Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎨 Compositing car image: $carImageUrl")
            
            // Download car image
            val carBitmap = downloadImage(carImageUrl)
                ?: return@withContext kotlin.Result.failure(Exception("Failed to download car image"))
            
            // Create background
            val background = createBackground(context, backgroundType, carBitmap.width, carBitmap.height)
            
            // Composite images
            val result = compositeBitmaps(background, carBitmap)
            
            Log.d(TAG, "✅ Image composited successfully")
            kotlin.Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Composition error: ${e.message}", e)
            kotlin.Result.failure(e)
        }
    }
    
    private fun downloadImage(url: String): Bitmap? {
        return try {
            if (url.startsWith("android.resource://")) {
                // Handle local resources
                null
            } else {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()
                BitmapFactory.decodeStream(connection.getInputStream())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}")
            null
        }
    }
    
    private fun createBackground(
        context: Context,
        type: BackgroundType,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        when (type) {
            BackgroundType.GRADIENT -> {
                val paint = Paint().apply {
                    shader = android.graphics.LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        intArrayOf(0xFFF5F5F5.toInt(), 0xFFE8E8E8.toInt()),
                        null,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            BackgroundType.DARK_PROFESSIONAL -> {
                val paint = Paint().apply {
                    shader = android.graphics.LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        intArrayOf(
                            0xFF1E2330.toInt(),
                            0xFF14161E.toInt(),
                            0xFF0D0E14.toInt()
                        ),
                        floatArrayOf(0f, 0.5f, 1f),
                        android.graphics.Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            BackgroundType.ROAD -> {
                canvas.drawColor(0xFFE8E8E8.toInt())
            }
            BackgroundType.STUDIO -> {
                canvas.drawColor(0xFFFFFFFF.toInt())
            }
        }
        
        return bitmap
    }
    
    private fun compositeBitmaps(background: Bitmap, foreground: Bitmap): Bitmap {
        val result = background.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        
        // Scale foreground to fit background while maintaining aspect ratio
        val scale = minOf(
            background.width.toFloat() / foreground.width,
            background.height.toFloat() / foreground.height
        ) * 0.8f // 80% of background size
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(
                (background.width - foreground.width * scale) / 2,
                (background.height - foreground.height * scale) / 2
            )
        }
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(foreground, matrix, paint)
        
        return result
    }
    
    enum class BackgroundType {
        GRADIENT,
        DARK_PROFESSIONAL,
        ROAD,
        STUDIO
    }
}
