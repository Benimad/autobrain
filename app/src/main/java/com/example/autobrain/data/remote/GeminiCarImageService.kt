package com.example.autobrain.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiCarImageService @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    private val TAG = "GeminiCarImageService"
    
    suspend fun fetchCarImageUrl(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🤖 Using Gemini AI to find car image for: $year $make $model")
            
            val prompt = """
                Find a high-quality, professional car image URL for: $year $make $model
                
                Requirements:
                - Must be a real, high-resolution image (at least 1920x1080)
                - Preferably from official sources (manufacturer websites, automotive press)
                - Side or 3/4 view angle preferred
                - Clean background or studio shot
                - PNG or JPG format
                
                Search these sources in order:
                1. Official manufacturer websites (audi.com, bmw.com, etc.)
                2. Automotive press (motortrend.com, caranddriver.com, autoweek.com)
                3. Wikimedia Commons
                4. High-quality stock photo sites
                
                Return ONLY a valid JSON object with this exact format:
                {
                  "imageUrl": "https://...",
                  "source": "source name",
                  "quality": "high/medium",
                  "hasTransparentBackground": true/false
                }
                
                If no image found, return:
                {
                  "imageUrl": "",
                  "source": "none",
                  "quality": "none",
                  "hasTransparentBackground": false
                }
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: ""
            
            Log.d(TAG, "📝 Gemini response: $responseText")
            
            // Parse JSON response
            val jsonResponse = extractJson(responseText)
            val imageUrl = jsonResponse.optString("imageUrl", "")
            
            if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                Log.d(TAG, "✅ Gemini found image: $imageUrl")
                Result.success(imageUrl)
            } else {
                Log.w(TAG, "⚠️ Gemini couldn't find a valid image URL")
                Result.failure(Exception("No valid image URL found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gemini error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun extractJson(text: String): JSONObject {
        return try {
            // Try to find JSON in the response
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = text.substring(jsonStart, jsonEnd)
                JSONObject(jsonString)
            } else {
                JSONObject()
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            JSONObject()
        }
    }
}
