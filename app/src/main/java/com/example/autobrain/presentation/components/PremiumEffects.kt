package com.example.autobrain.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.autobrain.R

@Composable
fun LogoSection() {
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logowitoutbg),
            contentDescription = "AutoBrain Logo",
            modifier = Modifier.size(130.dp)
        )
    }
}

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    
    val xOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x1"
    )
    
    val yOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y1"
    )

    val xOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -150f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00D9D9).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.2f + xOffset1, size.height * 0.3f + yOffset1),
                radius = 800f
            ),
            center = Offset(size.width * 0.2f + xOffset1, size.height * 0.3f + yOffset1),
            radius = 800f
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.8f + xOffset2, size.height * 0.6f - yOffset1),
                radius = 1000f
            ),
            center = Offset(size.width * 0.8f + xOffset2, size.height * 0.6f - yOffset1),
            radius = 1000f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.05f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.9f + xOffset2 / 2),
                radius = 700f
            ),
            center = Offset(size.width * 0.5f, size.height * 0.9f + xOffset2 / 2),
            radius = 700f
        )
    }
}

@Composable
fun AnimatedEntrance(
    visible: Boolean,
    delay: Int,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(durationMillis = 800, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "translateY"
    )

    Box(
        modifier = Modifier
            .alpha(alpha)
            .graphicsLayer(translationY = translateY)
    ) {
        content()
    }
}
