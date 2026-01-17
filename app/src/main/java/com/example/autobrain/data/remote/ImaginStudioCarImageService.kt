package com.example.autobrain.data.remote

import android.util.Log
import com.example.autobrain.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImaginStudioCarImageService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "FreeCarImageService"
    private val GOOGLE_API_KEY = BuildConfig.IMAGIN_STUDIO_API_KEY
    private val GOOGLE_CSE_ID = "d4db6361611024e2d"
    
    companion object {
        private val TRIM_SUFFIXES = setOf(
            "apt", "performance", "plus", "premium", "sport", "luxury", 
            "touring", "competition", "package", "edition", "avant"
        )
        
        private val BRAND_SPECIFIC_MODELS = mapOf(
            "audi" to mapOf(
                "rs6" to "RS 6 Avant",
                "rs7" to "RS 7 Sportback",
                "a4" to "A4",
                "a6" to "A6",
                "a3" to "A3",
                "q5" to "Q5",
                "q7" to "Q7"
            ),
            "bmw" to mapOf(
                "m3" to "M3",
                "m5" to "M5",
                "m4" to "M4",
                "x5" to "X5",
                "x3" to "X3"
            ),
            "mercedes-benz" to mapOf(
                "amg" to "AMG",
                "c-class" to "C-Class",
                "e-class" to "E-Class",
                "s-class" to "S-Class"
            ),
            "mercedes" to mapOf(
                "c-class" to "C-Class",
                "e-class" to "E-Class",
                "s-class" to "S-Class"
            )
        )
        
        fun cleanModelName(model: String): String {
            val modelLower = model.lowercase().trim()
            val parts = modelLower.split(" ", "-", "_")
            
            val cleanedParts = parts.filter { part ->
                part.isNotBlank() && !TRIM_SUFFIXES.contains(part)
            }
            
            return cleanedParts.joinToString(" ")
        }
        
        fun enhanceSearchQuery(make: String, model: String, year: Int): String {
            val makeLower = make.lowercase().trim()
            val cleanedModel = cleanModelName(model)
            val modelKey = cleanedModel.lowercase().replace(" ", "")
            
            val enhancedModel = BRAND_SPECIFIC_MODELS[makeLower]?.get(modelKey) ?: cleanedModel
            return "$year $make $enhancedModel official press photo"
        }
    }
    
    data class CarImageOptions(
        val angle: String = "01",
        val width: Int = 1920,
        val height: Int = 1280,
        val background: String = "transparent",
        val quality: Int = 100
    )
    
    suspend fun fetchExactCarImageUrl(
        make: String,
        model: String,
        year: Int,
        options: CarImageOptions = CarImageOptions()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Searching for exact car image: $year $make $model")
            
            val wikimediaResult = searchWikimediaCommons(make, model, year)
            if (wikimediaResult.isSuccess) {
                Log.d(TAG, "✅ Found exact car on Wikimedia Commons")
                return@withContext wikimediaResult
            }
            
            if (GOOGLE_API_KEY.isNotBlank() && GOOGLE_API_KEY != "your_imagin_studio_api_key_here") {
                val googleResult = searchGoogleImages(make, model, year)
                if (googleResult.isSuccess) {
                    Log.d(TAG, "✅ Found exact car via Google Custom Search")
                    return@withContext googleResult
                }
            }
            
            Log.w(TAG, "⚠️ Free search failed, using fallback")
            Result.failure(Exception("No free images found"))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error searching for car image: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private suspend fun searchWikimediaCommons(
        make: String,
        model: String,
        year: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val enhancedQuery = enhanceSearchQuery(make, model, year)
            val query = enhancedQuery.replace(" ", "%20")
            val url = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrnamespace=6&gsrsearch=$query&gsrlimit=10&prop=imageinfo&iiprop=url&iiurlwidth=1920"
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AutoBrain/1.0")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val jsonResponse = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            
            val jsonObject = JSONObject(jsonResponse)
            val pages = jsonObject.optJSONObject("query")?.optJSONObject("pages")
            
            if (pages != null && pages.length() > 0) {
                val firstPage = pages.keys().next()
                val imageInfo = pages.getJSONObject(firstPage)
                    .optJSONArray("imageinfo")
                    ?.optJSONObject(0)
                
                val imageUrl = imageInfo?.optString("thumburl") ?: imageInfo?.optString("url")
                if (!imageUrl.isNullOrBlank()) {
                    Log.d(TAG, "📷 Wikimedia image: $imageUrl")
                    return@withContext Result.success(imageUrl)
                }
            }
            
            Result.failure(Exception("No Wikimedia images found"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Wikimedia search error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun searchGoogleImages(
        make: String,
        model: String,
        year: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val enhancedQuery = enhanceSearchQuery(make, model, year)
            val query = enhancedQuery.replace(" ", "+")
            val url = "https://www.googleapis.com/customsearch/v1?key=$GOOGLE_API_KEY&cx=$GOOGLE_CSE_ID&q=$query&searchType=image&num=1&imgSize=xlarge&imgType=photo"
            
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val jsonResponse = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            
            val jsonObject = JSONObject(jsonResponse)
            val items = jsonObject.optJSONArray("items")
            
            if (items != null && items.length() > 0) {
                val firstItem = items.getJSONObject(0)
                val imageUrl = firstItem.optString("link")
                if (imageUrl.isNotBlank()) {
                    Log.d(TAG, "🔍 Google image: $imageUrl")
                    return@withContext Result.success(imageUrl)
                }
            }
            
            Result.failure(Exception("No Google images found"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Google search error: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun fetchCarImageWithMultipleAngles(
        make: String,
        model: String,
        year: Int
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val angles = listOf("01", "29", "13", "33")
            val imageUrls = angles.map { angle ->
                val options = CarImageOptions(
                    angle = angle,
                    width = 1920,
                    height = 1280,
                    background = "transparent",
                    quality = 100
                )
                
                val result = fetchExactCarImageUrl(make, model, year, options)
                result.getOrNull() ?: ""
            }.filter { it.isNotBlank() }
            
            if (imageUrls.isEmpty()) {
                Result.failure(Exception("No images generated"))
            } else {
                Log.d(TAG, "✅ Generated ${imageUrls.size} angle variations")
                Result.success(imageUrls)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching multiple angles: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun getAvailableAngles(): List<String> = listOf(
        "01",
        "04",
        "06",
        "08",
        "09",
        "13",
        "29",
        "33"
    )
}
