package com.example.autobrain.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@HiltWorker
class FallbackUrlHealthCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, params) {
    
    private val TAG = "UrlHealthCheck"
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "🏥 Starting fallback URL health check")
        
        val fallbackUrls = listOf(
            "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=1920&q=80",
            "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=1920&q=80",
            "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=1920&q=80",
            "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=1920&q=80",
            "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=1920&q=80",
            "https://images.unsplash.com/photo-1580414068984-6bc2c0eedeeb?w=1920&q=80",
            "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=1920&q=80",
            "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1920&q=80"
        )
        
        var healthyCount = 0
        var brokenCount = 0
        
        fallbackUrls.forEach { url ->
            if (testUrl(url)) {
                healthyCount++
                Log.d(TAG, "✅ Healthy: $url")
            } else {
                brokenCount++
                Log.e(TAG, "❌ Broken: $url")
            }
        }
        
        Log.d(TAG, "🏥 Health check complete: $healthyCount healthy, $brokenCount broken")
        
        return Result.success()
    }
    
    private fun testUrl(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val isHealthy = response.isSuccessful
            response.close()
            isHealthy
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val request = PeriodicWorkRequestBuilder<FallbackUrlHealthCheckWorker>(
                30, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                "fallback_url_health_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
