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
            
            // Check Room cache first
            val carKey = CarImageDao.generateCarKey(make, model, year)
            val cachedImage = carImageDao.getCarImage(carKey)
            
            if (cachedImage != null && !isCacheExpired(cachedImage)) {
                Log.d(TAG, "✅ Using cached image from Room: ${cachedImage.imageUrl}")
                carImageDao.updateLastAccessed(carKey)
                return@withContext Result.success(cachedImage.imageUrl)
            }
            
            // Fetch from network
            val imageUrl = fetchFromNetwork(make, model, year)
            
            // Cache the result
            if (imageUrl != null) {
                cacheImage(make, model, year, imageUrl)
            }
            
            Result.success(imageUrl ?: generateFallbackImageUrl(make, model, year, 0))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching car image: ${e.message}", e)
            val fallbackUrl = generateFallbackImageUrl(make, model, year, 0)
            Result.success(fallbackUrl)
        }
    }
    
    private suspend fun fetchFromNetwork(make: String, model: String, year: Int): String? {
        // Use Gemini AI to find the best car image
        try {
            Log.d(TAG, "🤖 Using Gemini AI to find car image...")
            val geminiResult = geminiCarImageService.fetchCarImageUrl(make, model, year)
            if (geminiResult.isSuccess && geminiResult.getOrNull()?.isNotBlank() == true) {
                var imageUrl = geminiResult.getOrNull()!!
                Log.d(TAG, "✅ Gemini found: $imageUrl")
                
                // Apply background removal for professional look
                try {
                    val bgRemovalResult = backgroundRemovalService.removeBackground(imageUrl)
                    if (bgRemovalResult.isSuccess) {
                        imageUrl = bgRemovalResult.getOrNull() ?: imageUrl
                        Log.d(TAG, "🎨 Background removed")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Background removal failed: ${e.message}")
                }
                
                return imageUrl
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gemini error: ${e.message}")
        }
        
        Log.w(TAG, "⚠️ Gemini failed, using fallback")
        return null
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
                isTransparent = imageUrl.contains("freeiconspng") || imageUrl.contains(".png"),
                source = when {
                    imageUrl.contains("freeiconspng") -> "freeiconspng"
                    imageUrl.contains("unsplash") -> "unsplash"
                    imageUrl.contains("pexels") -> "pexels"
                    imageUrl.contains("serper") -> "serper"
                    else -> "other"
                }
            )
            carImageDao.insertCarImage(entity)
            Log.d(TAG, "💾 Cached image in Room database")
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
    
    fun generateFallbackImageUrl(make: String, model: String, year: Int, attemptIndex: Int = 0): String {
        val cleanMake = make.trim().lowercase().replace(" ", "-")
        val cleanModel = model.trim().lowercase().replace(" ", "-")
        
        val fallbackSources = listOf(
            "https://cdn.wheel-size.com/automobile/body/audi-rs6-2024-1700830821.7616775.jpg",
            "https://www.cstatic-images.com/car-pictures/xl/$cleanMake-$cleanModel-${year}_main.png",
            "android.resource://com.example.autobrain/" + com.example.autobrain.R.drawable.car_placeholder_gradient
        )
        
        return fallbackSources.getOrElse(attemptIndex % fallbackSources.size) { fallbackSources[0] }
    }
}