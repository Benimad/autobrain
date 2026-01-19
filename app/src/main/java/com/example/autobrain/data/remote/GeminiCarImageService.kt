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
    private val MAX_RETRY_ATTEMPTS = 3
    
    suspend fun fetchCarImageUrl(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        for (attempt in 1..MAX_RETRY_ATTEMPTS) {
            try {
                Log.d(TAG, "🤖 Attempt $attempt/$MAX_RETRY_ATTEMPTS: Using Gemini 2.5 Flash to find CONSISTENT professional car image for: $year $make $model")
            
                val searchFocus = when (attempt) {
                    1 -> "PRIORITIZE: Unsplash.com and Pexels.com - MOST STABLE, never expire"
                    2 -> "PRIORITIZE: Wikimedia Commons - reliable, often has PNGs"
                    else -> "PRIORITIZE: Any accessible high-quality source"
                }
            
            val prompt = """
                MISSION: Find ONE PERFECT professional car image for $year $make $model
                
                ATTEMPT $attempt/$MAX_RETRY_ATTEMPTS - $searchFocus
                
                🎯 MANDATORY CONSISTENCY RULES (NON-NEGOTIABLE - ALL CARS MUST MATCH):
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ✓ EXACT ANGLE: 3/4 front-left view (40-45° angle, car facing 2 o'clock)
                ✓ EXACT POSITION: Car centered horizontally, slightly above center vertically
                ✓ EXACT LIGHTING: Studio lighting with soft ground shadow
                ✓ EXACT BACKGROUND: Solid white/gray gradient OR transparent PNG
                ✓ EXACT FRAMING: Full car visible, wheels on ground, no cropping
                ✓ NO people, NO outdoor scenes, NO showroom clutter
                ✓ NO watermarks, NO text overlays, NO license plates
                ✓ PROFESSIONAL QUALITY: Press photo / Official manufacturer standard
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                📐 TECHNICAL SPECS:
                • Resolution: Minimum 1920x1080 (prefer 4K for quality)
                • Format: PNG preferred (transparent background) or JPG with solid background
                • Aspect ratio: 16:9 or 3:2
                • Quality: Professional press photo quality
                
                🔍 SEARCH PRIORITY (USE IN THIS EXACT ORDER):
                
                1. UNSPLASH.COM (HIGHEST PRIORITY - ALWAYS ACCESSIBLE):
                   - Direct URLs: https://images.unsplash.com/photo-{id}?w=1920&h=1080
                   - Search for: "$year $make $model car studio"
                   - MOST STABLE - URLs never expire
                   - Example: https://images.unsplash.com/photo-1617531653332-bd46c24f2068?w=1920&h=1080
                   
                2. PEXELS.COM (VERY STABLE):
                   - Direct URLs: https://images.pexels.com/photos/{id}/pexels-photo-{id}.jpeg
                   - Search for: "$year $make $model"
                   - Free, professional, always accessible
                   
                3. WIKIMEDIA COMMONS (RELIABLE):
                   - commons.wikimedia.org
                   - Search: "$year $make $model"
                   - Often has transparent PNGs
                   
                4. AUTOMOTIVE DATABASES (BACKUP):
                   - netcarshow.com (if accessible)
                   - conceptcarz.com (if accessible)
                
                🎨 IDEAL IMAGE DESCRIPTION:
                "$year $make $model professional press photo 3/4 front angle studio lighting transparent background"
                
                ⚡ RETURN FORMAT (JSON ONLY, no markdown, no extra text):
                {
                  "imageUrl": "https://exact-direct-image-url.jpg",
                  "source": "manufacturer/database/stock",
                  "angle": "3/4-front",
                  "background": "white/gray/transparent",
                  "resolution": "1920x1080",
                  "quality": "excellent",
                  "consistencyScore": "high",
                  "manufacturer": "official/third-party",
                  "meetsStandards": true
                }
                
                If NO suitable image found:
                {
                  "imageUrl": "",
                  "source": "none",
                  "angle": "none",
                  "background": "none",
                  "resolution": "0x0",
                  "quality": "none",
                  "consistencyScore": "none"
                }
                
                🚨 CRITICAL VALIDATION CHECKLIST:
                - [ ] URL ends with .jpg, .png, or .webp (direct image link)
                - [ ] Image is publicly accessible (no authentication required)
                - [ ] Shows EXACT car model and year (verify carefully)
                - [ ] Matches 3/4 front-left angle requirement (40-45°)
                - [ ] Has clean, professional background (no clutter)
                - [ ] No watermarks, text overlays, or visible license plates
                - [ ] Professional quality (press photo standard)
                - [ ] URL is currently active and accessible
                
                ⚠️ IMPORTANT: Previous attempt may have found a broken URL.
                For attempt $attempt, try a DIFFERENT source to avoid the same issue.
                
                SEARCH NOW for: $year $make $model
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: ""
            
            Log.d(TAG, "📝 Gemini 2.5 response: $responseText")
            
            val jsonResponse = extractJson(responseText)
            val imageUrl = jsonResponse.optString("imageUrl", "")
            val quality = jsonResponse.optString("quality", "unknown")
            val source = jsonResponse.optString("source", "unknown")
            val angle = jsonResponse.optString("angle", "unknown")
            val consistencyScore = jsonResponse.optString("consistencyScore", "unknown")
            
                if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                    Log.d(TAG, "🔍 Found potential image:")
                    Log.d(TAG, "   URL: $imageUrl")
                    Log.d(TAG, "   Source: $source | Angle: $angle")
                    Log.d(TAG, "   Quality: $quality | Consistency: $consistencyScore")
                    
                    // Validate URL accessibility
                    if (validateImageUrl(imageUrl)) {
                        Log.d(TAG, "✅ URL validation passed! Image is accessible")
                        
                        // Additional AI validation for consistency (optional but recommended)
                        val meetsStandards = jsonResponse.optBoolean("meetsStandards", true)
                        if (meetsStandards && consistencyScore == "high") {
                            Log.d(TAG, "✨ Image meets all professional standards!")
                            return@withContext Result.success(imageUrl)
                        } else if (consistencyScore != "low") {
                            Log.d(TAG, "✅ Image acceptable, proceeding...")
                            return@withContext Result.success(imageUrl)
                        } else {
                            Log.w(TAG, "⚠️ Low consistency score, retrying...")
                            if (attempt < MAX_RETRY_ATTEMPTS) {
                                kotlinx.coroutines.delay(1000)
                                continue
                            }
                        }
                    } else {
                        Log.w(TAG, "❌ URL validation failed (404/403). Retrying with different query...")
                        if (attempt < MAX_RETRY_ATTEMPTS) {
                            kotlinx.coroutines.delay(1000)
                            continue
                        }
                    }
                } else {
                    Log.w(TAG, "⚠️ Gemini returned invalid URL format")
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        kotlinx.coroutines.delay(1000)
                        continue
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Gemini error on attempt $attempt: ${e.message}", e)
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    kotlinx.coroutines.delay(1000)
                    continue
                }
            }
        }
        
        Log.e(TAG, "❌ All $MAX_RETRY_ATTEMPTS attempts failed to find accessible image")
        Result.failure(Exception("No accessible professional image found after $MAX_RETRY_ATTEMPTS attempts"))
    }
    
    private fun validateImageUrl(url: String): Boolean {
        return try {
            Log.d(TAG, "🔍 Validating URL accessibility: $url")
            
            val request = Request.Builder()
                .url(url)
                .head()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val statusCode = response.code
            response.close()
            
            when (statusCode) {
                in 200..299 -> {
                    Log.d(TAG, "✅ URL is accessible (HTTP $statusCode)")
                    true
                }
                else -> {
                    Log.w(TAG, "❌ URL returned HTTP $statusCode")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ URL validation error: ${e.message}")
            false
        }
    }
    
    private fun extractJson(text: String): JSONObject {
        return try {
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
    
    data class ImageConsistencyMetadata(
        val url: String,
        val source: String,
        val angle: String,
        val background: String,
        val quality: String,
        val consistencyScore: String
    )
    
    fun validateImageConsistency(metadata: ImageConsistencyMetadata): Boolean {
        val validAngles = listOf("3/4-front", "3/4", "front-angle", "studio-angle")
        val validBackgrounds = listOf("transparent", "white", "gray", "gradient", "studio")
        val validQuality = listOf("excellent", "good")
        val validConsistency = listOf("high", "medium")
        
        val isAngleValid = validAngles.any { metadata.angle.contains(it, ignoreCase = true) }
        val isBackgroundValid = validBackgrounds.any { metadata.background.contains(it, ignoreCase = true) }
        val isQualityValid = validQuality.any { metadata.quality.contains(it, ignoreCase = true) }
        val isConsistencyValid = validConsistency.any { metadata.consistencyScore.contains(it, ignoreCase = true) }
        
        val validationScore = listOf(isAngleValid, isBackgroundValid, isQualityValid, isConsistencyValid).count { it }
        
        Log.d(TAG, "🔍 Image validation score: $validationScore/4")
        Log.d(TAG, "   Angle: ${if (isAngleValid) "✅" else "❌"} ($metadata.angle)")
        Log.d(TAG, "   Background: ${if (isBackgroundValid) "✅" else "❌"} ($metadata.background)")
        Log.d(TAG, "   Quality: ${if (isQualityValid) "✅" else "❌"} ($metadata.quality)")
        Log.d(TAG, "   Consistency: ${if (isConsistencyValid) "✅" else "❌"} ($metadata.consistencyScore)")
        
        return validationScore >= 3
    }
}
