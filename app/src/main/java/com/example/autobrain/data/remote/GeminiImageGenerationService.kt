package com.example.autobrain.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating 3D car images using Gemini's image generation capabilities
 * This provides an alternative when real car photos are not available
 */
@Singleton
class GeminiImageGenerationService @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    private val TAG = "GeminiImageGen"
    
    /**
     * Generate a 3D-style car image description that can be used with image generation
     * Note: Actual image generation requires Imagen API access
     */
    suspend fun generateCarImagePrompt(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎨 Generating image prompt for: $year $make $model")
            
            val prompt = """
Generate a detailed prompt for creating a professional 3D car image:

Car: $year $make $model

Create a prompt that describes:
1. Professional studio lighting (3/4 front angle)
2. Clean gradient background (dark to light)
3. Realistic car details and proportions
4. High-quality 3D render style
5. Metallic paint finish
6. Chrome details and realistic wheels
7. Professional automotive photography style

Return ONLY the image generation prompt, no explanations.
""".trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val imagePrompt = response.text?.trim() ?: ""
            
            if (imagePrompt.isNotBlank()) {
                Log.d(TAG, "✅ Generated prompt: ${imagePrompt.take(100)}...")
                Result.success(imagePrompt)
            } else {
                Result.failure(Exception("Failed to generate image prompt"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating prompt: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get a curated list of high-quality car image sources
     * This is a fallback strategy when Gemini search fails
     */
    suspend fun getCuratedCarImageSources(make: String, model: String, year: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Getting curated sources for: $year $make $model")
            
            val prompt = """
Find 3 high-quality image URLs for: $year $make $model

Requirements:
- ONLY use Unsplash (images.unsplash.com)
- Direct image URLs ending in ?w=1920&q=80
- Professional automotive photography
- 3/4 front angle preferred

Return as JSON array:
["url1", "url2", "url3"]

If not found, return empty array: []
""".trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val text = response.text?.trim() ?: "[]"
            
            // Extract URLs from response
            val urls = extractUrlsFromJson(text)
            
            if (urls.isNotEmpty()) {
                Log.d(TAG, "✅ Found ${urls.size} curated sources")
                Result.success(urls)
            } else {
                Result.failure(Exception("No curated sources found"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting curated sources: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun extractUrlsFromJson(text: String): List<String> {
        return try {
            val startIndex = text.indexOf("[")
            val endIndex = text.lastIndexOf("]") + 1
            
            if (startIndex >= 0 && endIndex > startIndex) {
                val jsonArray = text.substring(startIndex, endIndex)
                // Simple parsing - extract URLs between quotes
                val urlPattern = """"(https://[^"]+)"""".toRegex()
                urlPattern.findAll(jsonArray)
                    .map { it.groupValues[1] }
                    .filter { it.contains("unsplash") || it.contains("pexels") || it.contains("wikimedia") }
                    .toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract URLs: ${e.message}")
            emptyList()
        }
    }
}
