package com.example.autobrain.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiCarImageGenerator @Inject constructor(
    private val imageGenModel: GenerativeModel,
    private val okHttpClient: OkHttpClient,
    private val firebaseStorage: FirebaseStorage
) {
    private val TAG = "GeminiCarImageGen"
    
    suspend fun generateCarImage(make: String, model: String, year: Int, color: String = "metallic silver"): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎨 Generating realistic image for: $year $make $model")
            
            // Use Gemini to create a detailed prompt for image generation
            val promptCreationRequest = """
Create a detailed, professional prompt for generating a photorealistic car image.

Car: $year $make $model
Color: $color

Generate a prompt that describes:
- Professional automotive photography
- 3/4 front angle (45 degrees)
- Studio lighting with soft shadows
- Clean gradient background
- Realistic reflections and details
- High quality, 4K resolution
- Commercial/showroom presentation

Return ONLY the image generation prompt, no explanations.
""".trimIndent()
            
            val response = imageGenModel.generateContent(promptCreationRequest)
            val imagePrompt = response.text?.trim() ?: ""
            
            if (imagePrompt.isBlank()) {
                return@withContext Result.failure(Exception("Failed to create image prompt"))
            }
            
            Log.d(TAG, "📝 Generated prompt: ${imagePrompt.take(100)}...")
            
            // Use the prompt to search for the best matching image
            val searchPrompt = """
Find the BEST high-quality image URL that matches this description:

$imagePrompt

Car: $year $make $model

Search ONLY these sources:
1. Unsplash: https://images.unsplash.com/
2. Pexels: https://images.pexels.com/
3. Wikimedia: https://upload.wikimedia.org/

Return JSON:
{"imageUrl": "https://...", "source": "unsplash|pexels|wikimedia"}

If not found: {"imageUrl": "", "source": "none"}
""".trimIndent()
            
            val searchResponse = imageGenModel.generateContent(searchPrompt)
            val searchText = searchResponse.text?.trim() ?: ""
            
            val json = extractJson(searchText)
            val imageUrl = json.optString("imageUrl", "")
            
            if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                // Validate URL
                if (validateImageUrl(imageUrl)) {
                    Log.d(TAG, "✅ Found matching image: $imageUrl")
                    return@withContext Result.success(imageUrl)
                }
            }
            
            Log.w(TAG, "⚠️ No suitable image found")
            Result.failure(Exception("No matching image found"))
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Generation error: ${e.message}", e)
            Result.failure(e)
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
    
    private fun validateImageUrl(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val isValid = response.isSuccessful
            response.close()
            isValid
        } catch (e: Exception) {
            false
        }
    }
}
