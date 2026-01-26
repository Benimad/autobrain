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
    
    suspend fun fetchCarImageUrl(make: String, model: String, year: Int, attemptNumber: Int = 0): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎯 Gemini searching for: $year $make $model (Attempt ${attemptNumber + 1})")
            
            val prompt = """
Find a high-quality car image URL for: $year $make $model

PRIORITY SOURCES (in order):
1. Wikimedia Commons: https://upload.wikimedia.org/wikipedia/commons/
2. Unsplash: https://images.unsplash.com/photo-
3. Pexels: https://images.pexels.com/photos/

RULES:
- Return ONLY URLs from these 3 sources
- URL must be a direct image link (.jpg, .jpeg, .png)
- Must be high resolution (1920x1080+)
- Professional quality, 3/4 front angle preferred
- REAL URLs only - verify they exist

RETURN FORMAT (JSON only, no markdown):
{
  "imageUrl": "https://direct-image-url.jpg",
  "source": "wikimedia|unsplash|pexels",
  "verified": true
}

If no URL found:
{"imageUrl": "", "source": "none", "verified": false}
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
                    // Retry with different attempt if under max retries
                    if (attemptNumber < 2) {
                        kotlinx.coroutines.delay(500)
                        return@withContext fetchCarImageUrl(make, model, year, attemptNumber + 1)
                    }
                }
            }
            
            Result.failure(Exception("No working URL found after ${attemptNumber + 1} attempts"))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun testUrl(url: String): Boolean {
        return try {
            // First try HEAD request
            val headRequest = Request.Builder()
                .url(url)
                .head()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                .addHeader("Accept", "image/*")
                .build()
            
            val client = okHttpClient.newBuilder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .followRedirects(true)
                .build()
            
            val headResponse = client.newCall(headRequest).execute()
            val headCode = headResponse.code
            val contentType = headResponse.header("Content-Type", "")
            headResponse.close()
            
            // Check if it's a valid image response
            if (headCode in 200..299 && contentType?.contains("image", ignoreCase = true) == true) {
                return true
            }
            
            // If HEAD fails, try GET with range to verify it's an actual image
            if (headCode == 405 || headCode == 403) {
                val getRequest = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                    .addHeader("Range", "bytes=0-1024")
                    .build()
                
                val getResponse = client.newCall(getRequest).execute()
                val getCode = getResponse.code
                val getContentType = getResponse.header("Content-Type", "")
                getResponse.close()
                
                return (getCode in 200..299 || getCode == 206) && getContentType?.contains("image", ignoreCase = true) == true
            }
            
            false
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
