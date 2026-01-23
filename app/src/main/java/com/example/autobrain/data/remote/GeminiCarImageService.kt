package com.example.autobrain.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiCarImageService @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "GeminiCarImageService"
    
    suspend fun fetchCarImageUrl(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎯 Gemini searching for: $year $make $model")
            
            val prompt = """
You are an expert car image finder. Find ONE high-quality car image URL.

CAR: $year $make $model

REQUIREMENTS:
1. Image must show EXACTLY this car model and year
2. Professional studio quality (3/4 front angle, clean background)
3. URL must be from RELIABLE sources that work on mobile:
   ✅ Wikimedia Commons (upload.wikimedia.org)
   ✅ Pexels (images.pexels.com) 
   ✅ Unsplash (images.unsplash.com)
   ❌ NO manufacturer sites (they return 404)
   ❌ NO netcarshow.com (timeouts)

4. High resolution (1920x1080 minimum)
5. Real photo, not render

SEARCH STRATEGY:
- First try Wikimedia Commons for "$make $model $year"
- Then try Pexels for "$make $model"
- Finally try Unsplash for "$make $model"
- Use REAL photo IDs that exist, don't make them up

RETURN ONLY THIS JSON (no markdown, no explanation):
{
  "imageUrl": "https://actual-working-url.jpg",
  "source": "wikimedia",
  "verified": true,
  "quality": "excellent"
}

If you cannot find a WORKING URL, return:
{
  "imageUrl": "",
  "source": "none",
  "verified": false,
  "quality": "none"
}
""".trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val text = response.text?.trim() ?: ""
            
            Log.d(TAG, "📝 Gemini response: $text")
            
            val json = extractJson(text)
            val url = json.optString("imageUrl", "")
            val verified = json.optBoolean("verified", false)
            
            if (url.isNotBlank() && url.startsWith("http") && verified) {
                Log.d(TAG, "🔍 Testing URL: $url")
                
                if (testUrl(url)) {
                    Log.d(TAG, "✅ URL works!")
                    return@withContext Result.success(url)
                } else {
                    Log.w(TAG, "❌ URL failed validation")
                }
            }
            
            Result.failure(Exception("No working URL found"))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun testUrl(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                .build()
            
            val response = okHttpClient.newBuilder()
                .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                .build()
                .newCall(request)
                .execute()
            
            val code = response.code
            response.close()
            
            code in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "URL test failed: ${e.message}")
            false
        }
    }
    
    private fun extractJson(text: String): JSONObject {
        return try {
            val start = text.indexOf("{")
            val end = text.lastIndexOf("}") + 1
            if (start >= 0 && end > start) {
                JSONObject(text.substring(start, end))
            } else {
                JSONObject()
            }
        } catch (e: Exception) {
            JSONObject()
        }
    }
}
