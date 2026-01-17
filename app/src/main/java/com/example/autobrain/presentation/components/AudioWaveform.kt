package com.example.autobrain.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlin.math.sin

/**
 * Premium Audio Waveform Component
 * 
 * Features:
 * - Gradient colored bars with glow effect
 * - Smooth animated transitions
 * - Center-aligned mirrored visualization
 * - Premium glass-like appearance
 */
@Composable
fun AudioWaveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00FFFF),
    barWidth: Float = 4f,
    gapWidth: Float = 2f
) {
    // Glow animation for premium effect
    val infiniteTransition = rememberInfiniteTransition(label = "waveformGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // Secondary glow color
    val secondaryColor = Color(0xFF00E5CC)
    val glowColor = color.copy(alpha = glowAlpha * 0.5f)
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val totalBars = (width / (barWidth + gapWidth)).toInt()
        
        // Visible amplitudes calculation
        val visibleAmplitudes = if (amplitudes.size > totalBars) {
            amplitudes.takeLast(totalBars)
        } else {
            amplitudes
        }

        val startX = (width - (visibleAmplitudes.size * (barWidth + gapWidth))) / 2

        // Draw glow layer first (behind bars)
        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val x = startX + index * (barWidth + gapWidth) + barWidth / 2
            val normalizedAmp = amplitude.coerceIn(0.05f, 1f)
            val barHeight = (normalizedAmp * height * 0.85f).coerceAtLeast(6f)
            
            val startY = centerY - barHeight / 2
            val endY = centerY + barHeight / 2
            
            // Outer glow
            drawLine(
                color = glowColor,
                start = Offset(x, startY - 4f),
                end = Offset(x, endY + 4f),
                strokeWidth = barWidth + 8f,
                cap = StrokeCap.Round
            )
        }

        // Draw main bars with gradient effect
        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val x = startX + index * (barWidth + gapWidth) + barWidth / 2
            val normalizedAmp = amplitude.coerceIn(0.05f, 1f)
            val barHeight = (normalizedAmp * height * 0.85f).coerceAtLeast(6f)
            
            val startY = centerY - barHeight / 2
            val endY = centerY + barHeight / 2
            
            // Calculate color intensity based on position (center bars brighter)
            val centerFactor = 1f - (kotlin.math.abs(index - visibleAmplitudes.size / 2f) / (visibleAmplitudes.size / 2f)).coerceIn(0f, 1f) * 0.3f
            val barColor = color.copy(alpha = (0.7f + normalizedAmp * 0.3f) * centerFactor)
            
            // Main bar
            drawLine(
                color = barColor,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
            
            // Inner bright core for high amplitudes
            if (normalizedAmp > 0.4f) {
                val coreHeight = barHeight * 0.6f
                val coreStartY = centerY - coreHeight / 2
                val coreEndY = centerY + coreHeight / 2
                
                drawLine(
                    color = Color.White.copy(alpha = normalizedAmp * 0.4f * centerFactor),
                    start = Offset(x, coreStartY),
                    end = Offset(x, coreEndY),
                    strokeWidth = barWidth * 0.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        
        // Draw center line (reference line)
        drawLine(
            color = color.copy(alpha = 0.15f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1f
        )
    }
}

/**
 * Premium Animated Waveform for idle/waiting states
 * Shows a beautiful sine wave animation
 */
@Composable
fun AnimatedIdleWaveform(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00FFFF),
    barCount: Int = 40
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idleWave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val barWidth = 3f
        val gapWidth = 4f
        
        val startX = (width - (barCount * (barWidth + gapWidth))) / 2
        
        for (i in 0 until barCount) {
            val x = startX + i * (barWidth + gapWidth) + barWidth / 2
            
            // Sine wave calculation with multiple harmonics for organic feel
            val normalizedX = i.toFloat() / barCount
            val wave1 = sin(normalizedX * 4 * Math.PI + phase).toFloat()
            val wave2 = sin(normalizedX * 2 * Math.PI - phase * 0.5f).toFloat() * 0.5f
            val combinedWave = (wave1 + wave2) / 1.5f
            
            val amplitude = (0.2f + kotlin.math.abs(combinedWave) * 0.6f)
            val barHeight = (amplitude * height * 0.7f).coerceAtLeast(8f)
            
            val startY = centerY - barHeight / 2
            val endY = centerY + barHeight / 2
            
            // Glow effect
            drawLine(
                color = color.copy(alpha = glowIntensity * 0.3f),
                start = Offset(x, startY - 3f),
                end = Offset(x, endY + 3f),
                strokeWidth = barWidth + 6f,
                cap = StrokeCap.Round
            )
            
            // Main bar
            val barAlpha = 0.6f + amplitude * 0.4f
            drawLine(
                color = color.copy(alpha = barAlpha),
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
