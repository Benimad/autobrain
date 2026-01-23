package com.example.autobrain.data.repository

import android.util.Log
import com.example.autobrain.data.local.dao.CarImageDao
import com.example.autobrain.data.local.entity.CarImageEntity
import com.example.autobrain.data.remote.BackgroundRemovalService
import com.example.autobrain.data.remote.GeminiCarImageService
import kotlin.math.absoluteValue
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
    private val CURRENT_CACHE_VERSION = 4 // Incremented - only working URLs
    
    suspend fun fetchCarImageUrl(
        userId: String,
        make: String,
        model: String,
        year: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank()) {
                return@withContext Result.failure(Exception("User ID is required for personalized car images"))
            }
            
            if (make.isBlank() || model.isBlank() || year == 0) {
                return@withContext Result.failure(Exception("Car details are incomplete"))
            }
            
            Log.d(TAG, "🎯 Fetching car image for USER $userId: $year $make $model")
            
            // Check for user-specific cached image
            val carKey = CarImageDao.generateCarKey(userId, make, model, year)
            val cachedImage = carImageDao.getCarImage(carKey)
            
            if (cachedImage != null && !isCacheExpired(cachedImage) && isCacheVersionValid(cachedImage)) {
                if (cachedImage.imageUrl.contains("firebasestorage.googleapis.com")) {
                    Log.d(TAG, "✅ Using cached image (v${cachedImage.cacheVersion}): ${cachedImage.imageUrl}")
                    carImageDao.updateLastAccessed(carKey)
                    return@withContext Result.success(cachedImage.imageUrl)
                }
            }
            
            // Cache miss, expired, or outdated version - delete old cache if exists
            if (cachedImage != null) {
                Log.d(TAG, "🗑️ Deleting outdated cache (version: ${cachedImage.cacheVersion}, current: $CURRENT_CACHE_VERSION)")
                carImageDao.deleteCarImage(carKey)
            }
            
            // Delete any legacy cache entries (without userId) to clean up database
            @Suppress("DEPRECATION")
            val legacyCarKey = CarImageDao.generateCarKey(make, model, year)
            carImageDao.deleteCarImage(legacyCarKey)
            
            Log.d(TAG, "🔍 Cache miss or outdated, fetching NEW professional image...")
            
            // Fetch from network with enhanced Gemini search
            val imageUrl = fetchFromNetwork(make, model, year)
            
            // Cache the result with USER-SPECIFIC key
            cacheImage(userId, make, model, year, imageUrl)
            
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching car image: ${e.message}", e)
            
            // CRITICAL: DO NOT cache fallback images - they might be wrong
            // Return a temporary placeholder that won't be cached
            val placeholderUrl = "https://via.placeholder.com/1920x1080/2C2C2C/FFFFFF?text=$make+$model+$year"
            Log.w(TAG, "⚠️ Returning placeholder (not cached): $placeholderUrl")
            Result.success(placeholderUrl)
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
        
        // Use fallback with background removal
        Log.w(TAG, "📦 Using fallback image with background removal")
        val fallbackUrl = generateFallbackImageUrl(make, model, year)
        return applyBackgroundRemoval(fallbackUrl)
    }
    
    private suspend fun applyBackgroundRemoval(imageUrl: String): String {
        Log.d(TAG, "🎨 Processing image for professional quality...")
        try {
            val bgRemovalResult = backgroundRemovalService.removeBackground(imageUrl)
            if (bgRemovalResult.isSuccess && bgRemovalResult.getOrNull()?.isNotBlank() == true) {
                val processedUrl = bgRemovalResult.getOrNull()!!
                Log.d(TAG, "✨ Professional image ready (Firebase Storage)")
                return processedUrl
            } else {
                Log.w(TAG, "⚠️ Background removal failed, using original")
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Background removal error: ${e.message}")
        }
        return imageUrl
    }
    
    private suspend fun cacheImage(userId: String, make: String, model: String, year: Int, imageUrl: String) {
        try {
            val carKey = CarImageDao.generateCarKey(userId, make, model, year)
            val entity = CarImageEntity(
                carKey = carKey,
                userId = userId,
                make = make,
                model = model,
                year = year,
                imageUrl = imageUrl,
                isTransparent = imageUrl.endsWith(".png") || imageUrl.contains("transparent"),
                source = when {
                    imageUrl.contains("firebasestorage.googleapis.com") -> "processed+firebase"
                    imageUrl.contains("wikimedia") -> "wikimedia"
                    imageUrl.contains("pexels") -> "pexels"
                    imageUrl.contains("unsplash") -> "unsplash"
                    else -> "fallback"
                },
                cacheVersion = CURRENT_CACHE_VERSION
            )
            carImageDao.insertCarImage(entity)
            Log.d(TAG, "💾 Cached image (v$CURRENT_CACHE_VERSION) for user: $userId, source: ${entity.source}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Cache error: ${e.message}")
        }
    }
    
    private fun isCacheExpired(cachedImage: CarImageEntity): Boolean {
        val expiryTime = cachedImage.cachedAt + (CACHE_EXPIRY_DAYS * 24 * 60 * 60 * 1000)
        return System.currentTimeMillis() > expiryTime
    }
    
    private fun isCacheVersionValid(cachedImage: CarImageEntity): Boolean {
        return cachedImage.cacheVersion == CURRENT_CACHE_VERSION
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
    
    suspend fun clearUserCache(userId: String) {
        try {
            carImageDao.deleteUserCarImages(userId)
            Log.d(TAG, "🗑️ Cleared all cached images for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Clear user cache error: ${e.message}")
        }
    }
    
    suspend fun clearCarImageCache(userId: String, make: String, model: String, year: Int) {
        try {
            val carKey = CarImageDao.generateCarKey(userId, make, model, year)
            carImageDao.deleteCarImage(carKey)
            Log.d(TAG, "🗑️ Cleared cache for USER $userId: $year $make $model")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Clear specific cache error: ${e.message}")
        }
    }
    
    fun generateFallbackImageUrl(make: String, model: String, year: Int, attemptIndex: Int = 0): String {
        val cleanMake = make.trim().lowercase()
        
        // Working Unsplash URLs - verified to load
        val brandFallbacks = mapOf(
            "bmw" to listOf(
                "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=1920&q=80",
                "https://images.unsplash.com/photo-1617531653332-bd46c24f2068?w=1920&q=80"
            ),
            "audi" to listOf(
                "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=1920&q=80",
                "https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?w=1920&q=80"
            ),
            "mercedes" to listOf(
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=1920&q=80",
                "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=1920&q=80"
            ),
            "toyota" to listOf(
                "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=1920&q=80",
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=1920&q=80"
            ),
            "honda" to listOf(
                "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=1920&q=80",
                "https://images.unsplash.com/photo-1590362891991-f776e747a588?w=1920&q=80"
            ),
            "ford" to listOf(
                "https://images.unsplash.com/photo-1580414068984-6bc2c0eedeeb?w=1920&q=80",
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=1920&q=80"
            ),
            "tesla" to listOf(
                "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=1920&q=80",
                "https://images.unsplash.com/photo-1536700503339-1e4b06520771?w=1920&q=80"
            ),
            "porsche" to listOf(
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1920&q=80",
                "https://images.unsplash.com/photo-1611859266238-4b98091d9d9b?w=1920&q=80"
            )
        )
        
        val genericFallbacks = listOf(
            "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=1920&q=80",
            "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=1920&q=80",
            "https://images.unsplash.com/photo-1580414068984-6bc2c0eedeeb?w=1920&q=80",
            "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=1920&q=80"
        )
        
        val brandUrls = brandFallbacks[cleanMake] ?: brandFallbacks.entries.find { 
            cleanMake.contains(it.key) || it.key.contains(cleanMake)
        }?.value
        
        return if (brandUrls != null) {
            brandUrls.getOrElse(attemptIndex % brandUrls.size) { brandUrls[0] }
        } else {
            val carHash = "$cleanMake$model$year".hashCode().absoluteValue
            genericFallbacks[carHash % genericFallbacks.size]
        }
    }
}