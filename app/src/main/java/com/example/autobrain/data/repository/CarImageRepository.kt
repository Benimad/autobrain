package com.example.autobrain.data.repository

import android.util.Log
import com.example.autobrain.data.local.dao.CarImageDao
import com.example.autobrain.data.local.entity.CarImageEntity
import com.example.autobrain.data.remote.BackgroundRemovalService
import com.example.autobrain.data.remote.GeminiCarImageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarImageRepository @Inject constructor(
    private val carImageDao: CarImageDao,
    private val geminiCarImageService: GeminiCarImageService,
    private val backgroundRemovalService: BackgroundRemovalService
) {
    private val TAG = "CarImageRepository"
    private val CACHE_EXPIRY_DAYS = 30L
    
    suspend fun fetchCarImageUrl(
        make: String,
        model: String,
        year: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (make.isBlank() || model.isBlank() || year == 0) {
                return@withContext Result.failure(Exception("Car details are incomplete"))
            }
            
            Log.d(TAG, "🎯 Fetching car image for: $year $make $model")
            
            // Check Room cache first - skip cache if image doesn't have transparent background
            val carKey = CarImageDao.generateCarKey(make, model, year)
            val cachedImage = carImageDao.getCarImage(carKey)
            
            if (cachedImage != null && !isCacheExpired(cachedImage)) {
                // Only use cache if it's a Firebase Storage URL (already processed)
                if (cachedImage.imageUrl.contains("firebasestorage.googleapis.com")) {
                    Log.d(TAG, "✅ Using cached processed image from Room: ${cachedImage.imageUrl}")
                    carImageDao.updateLastAccessed(carKey)
                    return@withContext Result.success(cachedImage.imageUrl)
                } else {
                    Log.d(TAG, "🔄 Cache found but not processed, fetching with background removal...")
                    carImageDao.deleteCarImage(carKey)
                }
            }
            
            // Fetch from network (always returns a URL, either from Gemini or fallback with BG removal)
            val imageUrl = fetchFromNetwork(make, model, year)
            
            // Cache the result
            cacheImage(make, model, year, imageUrl)
            
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching car image: ${e.message}", e)
            val fallbackUrl = generateFallbackImageUrl(make, model, year, 0)
            Result.success(fallbackUrl)
        }
    }
    
    private suspend fun fetchFromNetwork(make: String, model: String, year: Int): String {
        try {
            Log.d(TAG, "🚀 Fetching professional car image using Gemini 2.5 Flash...")
            val geminiResult = geminiCarImageService.fetchCarImageUrl(make, model, year)
            
            if (geminiResult.isSuccess && geminiResult.getOrNull()?.isNotBlank() == true) {
                var imageUrl = geminiResult.getOrNull()!!
                Log.d(TAG, "✅ Gemini found professional image: $imageUrl")
                
                // Apply background removal
                return applyBackgroundRemoval(imageUrl)
            } else {
                Log.w(TAG, "⚠️ Gemini couldn't find suitable professional image")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network fetch error: ${e.message}", e)
        }
        
        Log.w(TAG, "📦 Using fallback image with background removal")
        // Apply background removal to fallback image too
        val fallbackUrl = generateFallbackImageUrl(make, model, year, 0)
        return applyBackgroundRemoval(fallbackUrl)
    }
    
    private suspend fun applyBackgroundRemoval(imageUrl: String): String {
        if (backgroundRemovalService.shouldRemoveBackground(imageUrl)) {
            Log.d(TAG, "🎨 Applying background removal for studio quality...")
            try {
                val bgRemovalResult = backgroundRemovalService.removeBackground(imageUrl)
                if (bgRemovalResult.isSuccess && bgRemovalResult.getOrNull()?.isNotBlank() == true) {
                    val processedUrl = bgRemovalResult.getOrNull()!!
                    Log.d(TAG, "✨ Professional image ready with transparent background")
                    return processedUrl
                } else {
                    Log.w(TAG, "⚠️ Background removal failed, using original image")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Background removal error: ${e.message}, using original")
            }
        } else {
            Log.d(TAG, "✓ Image already has clean background")
        }
        return imageUrl
    }
    
    private suspend fun cacheImage(make: String, model: String, year: Int, imageUrl: String) {
        try {
            val carKey = CarImageDao.generateCarKey(make, model, year)
            val entity = CarImageEntity(
                carKey = carKey,
                make = make,
                model = model,
                year = year,
                imageUrl = imageUrl,
                isTransparent = imageUrl.endsWith(".png") || imageUrl.contains("transparent"),
                source = when {
                    imageUrl.contains("firebase") -> "gemini+firebase"
                    imageUrl.contains("wikimedia") -> "gemini+wikimedia"
                    imageUrl.contains("audi-mediacenter") -> "gemini+manufacturer"
                    imageUrl.contains("media.bmw") -> "gemini+manufacturer"
                    imageUrl.contains("motortrend") -> "gemini+press"
                    imageUrl.contains("caranddriver") -> "gemini+press"
                    imageUrl.contains("unsplash") -> "gemini+stock"
                    imageUrl.contains("pexels") -> "gemini+stock"
                    else -> "gemini"
                }
            )
            carImageDao.insertCarImage(entity)
            Log.d(TAG, "💾 Cached image in Room database (source: ${entity.source})")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Cache error: ${e.message}")
        }
    }
    
    private fun isCacheExpired(cachedImage: CarImageEntity): Boolean {
        val expiryTime = cachedImage.cachedAt + (CACHE_EXPIRY_DAYS * 24 * 60 * 60 * 1000)
        return System.currentTimeMillis() > expiryTime
    }
    
    suspend fun clearExpiredCache() {
        try {
            val expiryTime = System.currentTimeMillis() - (CACHE_EXPIRY_DAYS * 24 * 60 * 60 * 1000)
            carImageDao.deleteExpiredImages(expiryTime)
            Log.d(TAG, "🧹 Cleared expired cache")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Clear cache error: ${e.message}")
        }
    }
    
    suspend fun clearCarImageCache(make: String, model: String, year: Int) {
        try {
            val carKey = CarImageDao.generateCarKey(make, model, year)
            carImageDao.deleteCarImage(carKey)
            Log.d(TAG, "🗑️ Cleared cache for: $year $make $model")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Clear specific cache error: ${e.message}")
        }
    }
    
    fun generateFallbackImageUrl(make: String, model: String, year: Int, attemptIndex: Int = 0): String {
        val cleanMake = make.trim().lowercase().replace(" ", "-")
        val cleanModel = model.trim().lowercase().replace(" ", "-")
        
        val fallbackSources = listOf(
            "https://images.unsplash.com/photo-1617531653332-bd46c24f2068?w=1920&h=1080&fit=crop&q=80",
            "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=1920&h=1080&fit=crop&q=80",
            "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=1920&h=1080&fit=crop&q=80",
            "https://images.unsplash.com/photo-1580414068984-6bc2c0eedeeb?w=1920&h=1080&fit=crop&q=80",
            "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=1920&h=1080&fit=crop&q=80"
        )
        
        return fallbackSources.getOrElse(attemptIndex % fallbackSources.size) { fallbackSources[0] }
    }
}