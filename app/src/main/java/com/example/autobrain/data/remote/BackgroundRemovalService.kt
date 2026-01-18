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
class BackgroundRemovalService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "BackgroundRemovalService"
    
    // Free API alternative: remove.bg (50 free API calls/month)
    // For production, consider: PhotoRoom API, Slazzer, or Cloudinary
    
    suspend fun removeBackground(imageUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎨 Removing background from: $imageUrl")
            
            // Use remove.bg API (requires API key in local.properties)
            val apiKey = com.example.autobrain.BuildConfig.REMOVE_BG_API_KEY
            
            if (apiKey.isBlank()) {
                Log.w(TAG, "⚠️ No remove.bg API key, returning original URL")
                return@withContext Result.success(imageUrl)
            }
            
            val json = JSONObject().apply {
                put("image_url", imageUrl)
                put("size", "auto")
                put("format", "png")
            }
            
            val request = Request.Builder()
                .url("https://api.remove.bg/v1.0/removebg")
                .addHeader("X-Api-Key", apiKey)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                // Save the processed image bytes and return a data URL or upload to Firebase Storage
                val imageBytes = response.body?.bytes()
                if (imageBytes != null) {
                    // For now, return original URL (in production, upload to Firebase Storage)
                    Log.d(TAG, "✅ Background removed successfully (${imageBytes.size} bytes)")
                    // TODO: Upload to Firebase Storage and return the URL
                    Result.success(imageUrl)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Log.e(TAG, "❌ API error: ${response.code} - ${response.message}")
                Result.success(imageUrl) // Fallback to original
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Background removal error: ${e.message}", e)
            Result.success(imageUrl) // Fallback to original
        }
    }
}
