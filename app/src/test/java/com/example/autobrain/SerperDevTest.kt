package com.example.autobrain.data.remote

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test

/**
 * Simple test to verify Serper.dev API is working
 * Run this to test the API independently
 */
class SerperDevTest {
    
    @Test
    fun testSerperDevAPI() = runBlocking {
        val okHttpClient = OkHttpClient.Builder().build()
        val service = SerperDevImageService(okHttpClient)
        
        // Test with Audi RS6 2024
        val result = service.fetchCarImage("Audi", "RS6", 2024, 5)
        
        println("Result: ${result.isSuccess}")
        println("URL: ${result.getOrNull()}")
        
        assert(result.isSuccess) { "API call should succeed" }
        assert(result.getOrNull()?.isNotBlank() == true) { "Should return a valid URL" }
    }
    
    @Test
    fun testSerperDevWithFallbacks() = runBlocking {
        val okHttpClient = OkHttpClient.Builder().build()
        val service = SerperDevImageService(okHttpClient)
        
        // Test with fallback logic
        val result = service.fetchCarImageWithFallbacks("Audi", "RS6", 2024)
        
        println("Result with fallbacks: ${result.isSuccess}")
        println("URL: ${result.getOrNull()}")
        
        assert(result.isSuccess) { "API call with fallbacks should succeed" }
    }
}
