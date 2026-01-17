package com.example.autobrain.data.repository

import android.util.Log
import com.example.autobrain.data.remote.FreeCarImageService
import com.example.autobrain.data.remote.ImaginStudioCarImageService
import com.example.autobrain.data.remote.SerperDevImageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarImageRepository @Inject constructor(
    private val serperDevService: SerperDevImageService,
    private val freeCarImageService: FreeCarImageService,
    private val imaginStudioService: ImaginStudioCarImageService
) {
    private val TAG = "CarImageRepository"
    
    private val exactCarAngles = listOf("01", "29", "13", "33")
    
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
            
            // PRIORITY 1: FREE Jsoup (Unsplash/Pexels)
            try {
                Log.d(TAG, "📡 Calling FREE image service...")
                val freeResult = freeCarImageService.fetchCarImage(make, model, year)
                if (freeResult.isSuccess && freeResult.getOrNull()?.isNotBlank() == true) {
                    val url = freeResult.getOrNull()!!
                    Log.d(TAG, "✅ FREE car image: $url")
                    return@withContext Result.success(url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Free service error: ${e.message}")
            }
            
            // PRIORITY 2: Serper.dev (Paid backup)
            try {
                Log.d(TAG, "📡 Calling Serper.dev API...")
                val serperResult = serperDevService.fetchCarImageWithFallbacks(make, model, year)
                if (serperResult.isSuccess && serperResult.getOrNull()?.isNotBlank() == true) {
                    val url = serperResult.getOrNull()!!
                    Log.d(TAG, "✅ REALISTIC car image from Serper.dev: $url")
                    Log.d(TAG, "🏎️ This is the REAL $year $make $model from Google Images!")
                    return@withContext Result.success(url)
                } else {
                    Log.w(TAG, "⚠️ Serper.dev returned no results")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Serper.dev API error: ${e.message}", e)
            }
            
            // PRIORITY 2: Wikimedia/Google Custom Search (Free APIs)
            Log.d(TAG, "🔄 Serper.dev failed, trying free APIs...")
            val cleanedModel = ImaginStudioCarImageService.cleanModelName(model)
            val exactCarResult = imaginStudioService.fetchExactCarImageUrl(
                make = make,
                model = model,
                year = year,
                options = ImaginStudioCarImageService.CarImageOptions(
                    angle = "01",
                    width = 1920,
                    height = 1280,
                    background = "transparent",
                    quality = 100
                )
            )
            
            if (exactCarResult.isSuccess && exactCarResult.getOrNull()?.isNotBlank() == true) {
                val url = exactCarResult.getOrNull()!!
                Log.d(TAG, "✅ Car image from free API: $url")
                return@withContext Result.success(url)
            }
            
            // PRIORITY 3: Static fallback URLs
            Log.w(TAG, "⚠️ All APIs failed, using fallback sources")
            val fallbackUrl = generateFallbackImageUrl(make, model, year, 0)
            Log.d(TAG, "🔄 Using fallback: $fallbackUrl")
            Result.success(fallbackUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching car image: ${e.message}", e)
            val fallbackUrl = generateFallbackImageUrl(make, model, year, 0)
            Log.d(TAG, "🔄 Using fallback due to error: $fallbackUrl")
            Result.success(fallbackUrl)
        }
    }
    
    fun generateFallbackImageUrl(make: String, model: String, year: Int, attemptIndex: Int = 0): String {
        val cleanedModelName = ImaginStudioCarImageService.cleanModelName(model)
        val cleanMake = make.trim().lowercase().replace(" ", "-")
        val cleanModel = cleanedModelName.trim().lowercase().replace(" ", "-")
        val makeUpper = make.trim().uppercase().replace(" ", "+")
        val modelUpper = cleanedModelName.trim().uppercase().replace(" ", "+")
        
        val fallbackSources = listOf(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/${year}_${makeUpper}_${modelUpper}.jpg/1920px-${year}_${makeUpper}_${modelUpper}.jpg",
            "https://cdn.wheel-size.com/automobile/body/audi-rs6-2024-1700830821.7616775.jpg",
            "https://www.cstatic-images.com/car-pictures/xl/$cleanMake-$cleanModel-${year}_main.png",
            "https://platform.cstatic-images.com/xlarge/in/v2/stock_photos/$cleanMake/$cleanModel/$year/${year}-$cleanMake-$cleanModel-frontview.png",
            "https://media.ed.edmunds-media.com/$cleanMake/$cleanModel/$year/oem/${year}_${cleanMake}_${cleanModel}_sedan_fq_oem_1_1600.jpg",
            "android.resource://com.example.autobrain/" + com.example.autobrain.R.drawable.car_placeholder_gradient
        )
        
        val selectedUrl = fallbackSources.getOrElse(attemptIndex % fallbackSources.size) {
            fallbackSources[0]
        }
        
        Log.d(TAG, "🔄 Fallback URL #$attemptIndex: $selectedUrl")
        return selectedUrl
    }
    
    suspend fun fetchCarImageWithMultipleAngles(
        make: String,
        model: String,
        year: Int
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val result = imaginStudioService.fetchCarImageWithMultipleAngles(make, model, year)
            
            if (result.isSuccess) {
                Log.d(TAG, "✅ Fetched multiple angle images for exact car")
                result
            } else {
                val fallbackUrls = exactCarAngles.mapIndexed { index, _ ->
                    generateFallbackImageUrl(make, model, year, index)
                }
                Result.success(fallbackUrls)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching multiple angles: ${e.message}", e)
            Result.failure(e)
        }
    }
}
