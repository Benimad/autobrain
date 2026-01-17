package com.example.autobrain.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SerperDevImageService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "SerperDevImageService"
    private val SERPER_API_KEY = "43e476974247717d170410e99e6f5587f2564352"
    private val SERPER_API_URL = "https://google.serper.dev/images"
    
    companion object {
        fun buildProfessionalSearchQuery(make: String, model: String, year: Int, attemptIndex: Int = 0): String {
            val cleanMake = make.trim()
            val cleanModel = model.trim()
            
            return when (attemptIndex) {
                0 -> "$cleanMake $cleanModel $year 3D render white background"
                1 -> "$cleanMake $cleanModel $year studio shot white background shadow"
                2 -> "$cleanMake $cleanModel $year isolated clean background"
                3 -> "$cleanMake $cleanModel $year product photography white"
                4 -> "$cleanMake $cleanModel $year press photo clean"
                else -> "$cleanMake $cleanModel $year"
            }
        }
    }
    
    /**
     * Fetch car image from Serper.dev Google Image Search API
     * Returns the best matching image URL
     */
    suspend fun fetchCarImage(
        make: String,
        model: String,
        year: Int,
        numResults: Int = 5
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val query = buildProfessionalSearchQuery(make, model, year, 0)
            Log.d(TAG, "🔍 Searching Serper.dev for: $query")
            
            val jsonBody = JSONObject().apply {
                put("q", query)
                put("num", numResults)
                put("gl", "us")
                put("hl", "en")
            }
            
            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(SERPER_API_URL)
                .header("X-API-KEY", SERPER_API_KEY)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e(TAG, "❌ Serper API error: ${response.code} - ${response.message}")
                return@withContext Result.failure(Exception("Serper API returned ${response.code}"))
            }
            
            val jsonResponse = JSONObject(responseBody)
            val images = jsonResponse.optJSONArray("images")
            
            if (images == null || images.length() == 0) {
                Log.w(TAG, "⚠️ No images found in Serper response")
                return@withContext Result.failure(Exception("No images found"))
            }
            
            // Get the first (best match) image URL
            val firstImage = images.getJSONObject(0)
            val imageUrl = firstImage.optString("imageUrl")
            
            if (imageUrl.isBlank()) {
                Log.w(TAG, "⚠️ Image URL is blank")
                return@withContext Result.failure(Exception("Image URL is blank"))
            }
            
            Log.d(TAG, "✅ Found car image via Serper.dev: $imageUrl")
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching from Serper.dev: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch multiple car images with different search queries
     * Useful for fallback scenarios
     */
    suspend fun fetchCarImageWithFallbacks(
        make: String,
        model: String,
        year: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        for (attemptIndex in 0..4) {
            try {
                val query = buildProfessionalSearchQuery(make, model, year, attemptIndex)
                Log.d(TAG, "🔄 Attempt #${attemptIndex + 1}: $query")
                
                val jsonBody = JSONObject().apply {
                    put("q", query)
                    put("num", 3)
                    put("gl", "us")
                    put("hl", "en")
                }
                
                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url(SERPER_API_URL)
                    .header("X-API-KEY", SERPER_API_KEY)
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val jsonResponse = JSONObject(responseBody)
                    val images = jsonResponse.optJSONArray("images")
                    
                    if (images != null && images.length() > 0) {
                        val firstImage = images.getJSONObject(0)
                        val imageUrl = firstImage.optString("imageUrl")
                        
                        if (imageUrl.isNotBlank()) {
                            Log.d(TAG, "✅ Success on attempt #${attemptIndex + 1}: $imageUrl")
                            return@withContext Result.success(imageUrl)
                        }
                    }
                }
                
                // Small delay between attempts
                kotlinx.coroutines.delay(500)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Attempt #${attemptIndex + 1} failed: ${e.message}")
            }
        }
        
        Log.e(TAG, "❌ All Serper.dev attempts failed")
        Result.failure(Exception("All Serper.dev attempts failed"))
    }
    
    /**
     * Fetch multiple car images for gallery/carousel
     */
    suspend fun fetchMultipleCarImages(
        make: String,
        model: String,
        year: Int,
        numResults: Int = 10
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val query = buildProfessionalSearchQuery(make, model, year, 0)
            Log.d(TAG, "🔍 Fetching multiple images for: $query")
            
            val jsonBody = JSONObject().apply {
                put("q", query)
                put("num", numResults)
                put("gl", "us")
                put("hl", "en")
            }
            
            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url(SERPER_API_URL)
                .header("X-API-KEY", SERPER_API_KEY)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Serper API error"))
            }
            
            val jsonResponse = JSONObject(responseBody)
            val images = jsonResponse.optJSONArray("images")
            
            if (images == null || images.length() == 0) {
                return@withContext Result.failure(Exception("No images found"))
            }
            
            val imageUrls = mutableListOf<String>()
            for (i in 0 until images.length()) {
                val image = images.getJSONObject(i)
                val imageUrl = image.optString("imageUrl")
                if (imageUrl.isNotBlank()) {
                    imageUrls.add(imageUrl)
                }
            }
            
            Log.d(TAG, "✅ Found ${imageUrls.size} car images")
            Result.success(imageUrls)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching multiple images: ${e.message}", e)
            Result.failure(e)
        }
    }
}
