package com.example.autobrain.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreeCarImageService @Inject constructor() {
    private val TAG = "FreeCarImageService"
    
    suspend fun fetchCarImage(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        val query = "$year $make $model car"
        
        // Priority 1: FreeIconsPNG (transparent PNG, no background)
        fetchFromFreeIconsPng(make, model)?.let {
            Log.d(TAG, "✅ FreeIconsPNG: $it")
            return@withContext Result.success(it)
        }
        
        // Priority 2: Wikimedia (transparent PNG, best quality)
        fetchFromWikimedia(make, model, year)?.let {
            Log.d(TAG, "✅ Wikimedia: $it")
            return@withContext Result.success(it)
        }
        
        // Priority 3: Unsplash (high quality)
        fetchFromUnsplash(query)?.let {
            Log.d(TAG, "✅ Unsplash: $it")
            return@withContext Result.success(it)
        }
        
        // Priority 4: Pexels
        fetchFromPexels(query)?.let {
            Log.d(TAG, "✅ Pexels: $it")
            return@withContext Result.success(it)
        }
        
        // Priority 5: Pixabay
        fetchFromPixabay(query)?.let {
            Log.d(TAG, "✅ Pixabay: $it")
            return@withContext Result.success(it)
        }
        
        Log.w(TAG, "⚠️ No images found")
        Result.failure(Exception("No images found"))
    }
    
    private fun fetchFromFreeIconsPng(make: String, model: String): String? {
        return try {
            val query = "$make $model car png"
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://www.freeiconspng.com/search.html?q=$encodedQuery"
            
            Log.d(TAG, "🔍 Searching FreeIconsPNG: $url")
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(15000)
                .followRedirects(true)
                .get()
            
            // Try multiple selectors for better results
            val imageSelectors = listOf(
                "div.search-result-item img",
                "div.search-result-box img",
                "img[src*=freeiconspng]",
                "a.search-result-link img",
                "div.icon-preview img"
            )
            
            for (selector in imageSelectors) {
                val imgElement = doc.select(selector).firstOrNull()
                if (imgElement != null) {
                    val imgSrc = imgElement.attr("src").ifEmpty { imgElement.attr("data-src") }
                    if (imgSrc.isNotBlank()) {
                        val fullUrl = when {
                            imgSrc.startsWith("http") -> imgSrc
                            imgSrc.startsWith("//") -> "https:$imgSrc"
                            else -> "https://www.freeiconspng.com$imgSrc"
                        }
                        
                        // Convert thumbnail to full-size image
                        val highResUrl = fullUrl
                            .replace("/thumbs/", "/uploads/")
                            .replace("-thumb.png", ".png")
                            .replace("-thumb.jpg", ".png")
                            .replace("_thumb", "")
                        
                        Log.d(TAG, "✅ Found image: $highResUrl")
                        return highResUrl
                    }
                }
            }
            
            Log.w(TAG, "⚠️ No images found with any selector")
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ FreeIconsPNG error: ${e.message}", e)
            null
        }
    }
    
    private fun fetchFromWikimedia(make: String, model: String, year: Int): String? {
        return try {
            val searchQuery = "${make}_${model}_${year}".replace(" ", "_")
            val url = "https://commons.wikimedia.org/w/index.php?search=$searchQuery&title=Special:MediaSearch&type=image"
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            doc.select("img.sdms-image-result__thumbnail").firstOrNull()?.attr("src")
                ?.replace("/thumb/", "/")
                ?.substringBeforeLast("/")
                ?.takeIf { it.contains("upload.wikimedia.org") }
        } catch (e: Exception) {
            Log.e(TAG, "Wikimedia error: ${e.message}")
            null
        }
    }
    
    private fun fetchFromUnsplash(query: String): String? {
        return try {
            val searchQuery = "$query 3D white background".replace(" ", "+")
            val url = "https://unsplash.com/s/photos/$searchQuery"
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            doc.select("img[srcset]").firstOrNull()?.attr("src")
                ?.takeIf { it.startsWith("https://images.unsplash.com") }
        } catch (e: Exception) {
            Log.e(TAG, "Unsplash error: ${e.message}")
            null
        }
    }
    
    private fun fetchFromPexels(query: String): String? {
        return try {
            val searchQuery = query.replace(" ", "-")
            val url = "https://www.pexels.com/search/$searchQuery/"
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            doc.select("img[src*=images.pexels.com]").firstOrNull()?.attr("src")
                ?.takeIf { it.contains("images.pexels.com") }
        } catch (e: Exception) {
            Log.e(TAG, "Pexels error: ${e.message}")
            null
        }
    }
    
    private fun fetchFromPixabay(query: String): String? {
        return try {
            val searchQuery = query.replace(" ", "+")
            val url = "https://pixabay.com/images/search/$searchQuery/"
            
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()
            
            doc.select("img[srcset]").firstOrNull()?.attr("src")
                ?.takeIf { it.contains("pixabay.com") }
        } catch (e: Exception) {
            Log.e(TAG, "Pixabay error: ${e.message}")
            null
        }
    }
}
