package com.example.autobrain.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundRemovalService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val firebaseStorage: FirebaseStorage
) {
    private val TAG = "BackgroundRemovalService"
    
    suspend fun removeBackground(imageUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎨 Processing professional car image with background removal")
            Log.d(TAG, "📥 Source URL: $imageUrl")
            
            val apiKey = com.example.autobrain.BuildConfig.REMOVE_BG_API_KEY
            
            if (apiKey.isBlank() || apiKey == "your_remove_bg_api_key_here") {
                Log.w(TAG, "⚠️ No remove.bg API key configured, skipping background removal")
                return@withContext Result.success(imageUrl)
            }
            
            val json = JSONObject().apply {
                put("image_url", imageUrl)
                put("size", "auto")
                put("format", "png")
                put("type", "auto")
                put("type_level", "2")
                put("crop", false)
                put("scale", "original")
            }
            
            val request = Request.Builder()
                .url("https://api.remove.bg/v1.0/removebg")
                .addHeader("X-Api-Key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            Log.d(TAG, "🚀 Sending request to remove.bg API...")
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val imageBytes = response.body?.bytes()
                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    Log.d(TAG, "✅ Background removed successfully (${imageBytes.size} bytes)")
                    
                    val processedUrl = uploadToFirebaseStorage(imageBytes)
                    if (processedUrl != null) {
                        Log.d(TAG, "☁️ Uploaded to Firebase Storage: $processedUrl")
                        return@withContext Result.success(processedUrl)
                    } else {
                        Log.w(TAG, "⚠️ Firebase upload failed, using original URL")
                        return@withContext Result.success(imageUrl)
                    }
                } else {
                    Log.e(TAG, "❌ Empty response from remove.bg")
                    return@withContext Result.success(imageUrl)
                }
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                Log.e(TAG, "❌ remove.bg API error (${response.code}): ${response.message}")
                Log.e(TAG, "Error details: $errorBody")
                return@withContext Result.success(imageUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Background removal error: ${e.message}", e)
            return@withContext Result.success(imageUrl)
        }
    }
    
    private suspend fun uploadToFirebaseStorage(imageBytes: ByteArray): String? {
        return try {
            val fileName = "car_images/processed_${UUID.randomUUID()}.png"
            val storageRef = firebaseStorage.reference.child(fileName)
            
            Log.d(TAG, "📤 Uploading ${imageBytes.size} bytes to Firebase Storage...")
            val uploadTask = storageRef.putBytes(imageBytes).await()
            val downloadUrl = storageRef.downloadUrl.await()
            
            Log.d(TAG, "✅ Upload successful: ${downloadUrl}")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase Storage upload error: ${e.message}", e)
            null
        }
    }
    
    fun shouldRemoveBackground(imageUrl: String): Boolean {
        // Only skip if already processed (Firebase Storage URL)
        val alreadyProcessed = imageUrl.contains("firebasestorage.googleapis.com", ignoreCase = true)
        return !alreadyProcessed
    }
}
