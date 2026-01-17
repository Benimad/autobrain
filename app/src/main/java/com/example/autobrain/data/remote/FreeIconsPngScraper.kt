package com.example.autobrain.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreeIconsPngScraper @Inject constructor() {
    private val TAG = "FreeIconsPngScraper"
    private val BASE_URL = "https://www.freeiconspng.com"
    
    suspend fun fetchCarImage(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val query = "$make $model car png"
            val searchUrl = "$BASE_URL/search.html?q=${query.replace(" ", "+")}"
            
            Log.d(TAG, "🔍 Searching: $searchUrl")
            
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            
            val imageElements = doc.select("div.search-result-box img[src*=freeiconspng]")
            
            if (imageElements.isEmpty()) {
                Log.w(TAG, "⚠️ No images found")
                return@withContext Result.failure(Exception("No images found"))
            }
            
            val imageUrl = imageElements.first()?.attr("src")
                ?.replace("/thumbs/", "/uploads/")
                ?.replace("-thumb.png", ".png")
            
            if (imageUrl.isNullOrBlank()) {
                Log.w(TAG, "⚠️ Image URL is blank")
                return@withContext Result.failure(Exception("Image URL is blank"))
            }
            
            val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl else "$BASE_URL$imageUrl"
            Log.d(TAG, "✅ Found image: $fullImageUrl")
            
            Result.success(fullImageUrl)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
