package com.example.autobrain.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dedicated scraper for freeiconspng.com to fetch transparent PNG car images.
 * Legal Note: Ensure compliance with the website's terms of service and robots.txt.
 * For production, consider using official APIs or licensed image sources.
 */
@Singleton
class FreeIconsPngScraper @Inject constructor() {
    private val TAG = "FreeIconsPngScraper"
    private val BASE_URL = "https://www.freeiconspng.com"
    
    suspend fun fetchCarImageUrl(make: String, model: String, year: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Try multiple search strategies
            val searchStrategies = listOf(
                "$make $model $year png",
                "$make $model car png",
                "$year $make $model transparent",
                "$make $model vehicle png"
            )
            
            for (query in searchStrategies) {
                val imageUrl = searchWithQuery(query)
                if (imageUrl != null) {
                    Log.d(TAG, "✅ Found image with query: $query")
                    return@withContext Result.success(imageUrl)
                }
            }
            
            Log.w(TAG, "⚠️ No images found for $make $model $year")
            Result.failure(Exception("No images found"))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun searchWithQuery(query: String): String? {
        return try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val searchUrl = "$BASE_URL/search.html?q=$encodedQuery"
            
            Log.d(TAG, "🔍 Searching: $searchUrl")
            
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://www.google.com")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .timeout(15000)
                .followRedirects(true)
                .get()
            
            // Extract image from search results
            extractImageUrl(doc)
        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}")
            null
        }
    }
    
    private fun extractImageUrl(doc: org.jsoup.nodes.Document): String? {
        // Try multiple extraction methods
        val extractors = listOf(
            { extractFromSearchResults(doc) },
            { extractFromImageTags(doc) },
            { extractFromDataAttributes(doc) }
        )
        
        for (extractor in extractors) {
            extractor()?.let { return it }
        }
        
        return null
    }
    
    private fun extractFromSearchResults(doc: org.jsoup.nodes.Document): String? {
        val selectors = listOf(
            "div.search-result-item img",
            "div.search-result-box img",
            "div.icon-preview img",
            "a.search-result-link img"
        )
        
        for (selector in selectors) {
            doc.select(selector).firstOrNull()?.let { img ->
                val src = img.attr("src").ifEmpty { img.attr("data-src") }
                if (src.isNotBlank()) {
                    return convertToHighResUrl(src)
                }
            }
        }
        return null
    }
    
    private fun extractFromImageTags(doc: org.jsoup.nodes.Document): String? {
        doc.select("img[src*=freeiconspng], img[src*=uploads]").firstOrNull()?.let { img ->
            val src = img.attr("src")
            if (src.isNotBlank()) {
                return convertToHighResUrl(src)
            }
        }
        return null
    }
    
    private fun extractFromDataAttributes(doc: org.jsoup.nodes.Document): String? {
        doc.select("img[data-src], img[data-original]").firstOrNull()?.let { img ->
            val src = img.attr("data-src").ifEmpty { img.attr("data-original") }
            if (src.isNotBlank()) {
                return convertToHighResUrl(src)
            }
        }
        return null
    }
    
    private fun convertToHighResUrl(url: String): String {
        var fullUrl = when {
            url.startsWith("http") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "$BASE_URL$url"
            else -> "$BASE_URL/$url"
        }
        
        // Convert thumbnail to full-size PNG
        fullUrl = fullUrl
            .replace("/thumbs/", "/uploads/")
            .replace("-thumb.png", ".png")
            .replace("-thumb.jpg", ".png")
            .replace("_thumb", "")
            .replace(".jpg", ".png")
        
        return fullUrl
    }
}
