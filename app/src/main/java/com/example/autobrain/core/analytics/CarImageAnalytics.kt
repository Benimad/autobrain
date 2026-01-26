package com.example.autobrain.core.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarImageAnalytics @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    private val TAG = "CarImageAnalytics"
    
    fun trackImageFetch(
        make: String,
        model: String,
        strategy: String,
        durationMs: Long,
        success: Boolean,
        cacheHit: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("car_make", make)
            putString("car_model", model)
            putString("fetch_strategy", strategy)
            putLong("duration_ms", durationMs)
            putBoolean("success", success)
            putBoolean("cache_hit", cacheHit)
        }
        
        firebaseAnalytics.logEvent("car_image_fetch", bundle)
        
        Log.d(TAG, "📊 Fetch: $make $model | $strategy | ${durationMs}ms | success=$success | cache=$cacheHit")
    }
    
    fun trackStrategyFailure(strategy: String, reason: String) {
        val bundle = Bundle().apply {
            putString("strategy", strategy)
            putString("failure_reason", reason)
        }
        
        firebaseAnalytics.logEvent("image_strategy_failure", bundle)
        Log.w(TAG, "⚠️ Strategy failed: $strategy - $reason")
    }
}
