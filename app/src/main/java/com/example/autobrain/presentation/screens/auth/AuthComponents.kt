package com.example.autobrain.presentation.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.autobrain.presentation.components.GoogleIcon
import com.example.autobrain.presentation.components.XIcon
import com.example.autobrain.presentation.components.AnimatedBackground
import com.example.autobrain.presentation.components.AnimatedEntrance

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isActive = step <= currentStep
            val isCurrent = step == currentStep
            
            val width by animateDpAsState(
                targetValue = if (isCurrent) 32.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "width"
            )
            
            val color by animateColorAsState(
                targetValue = if (isActive) Color(0xFF00D9D9) else Color(0xFF374151),
                animationSpec = tween(500),
                label = "color"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isCurrent) {
                            Modifier.shadow(4.dp, CircleShape, spotColor = Color(0xFF00D9D9))
                        } else Modifier
                    )
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.15f else 0f,
        animationSpec = tween(300),
        label = "glow"
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .then(
                    if (isFocused) {
                        Modifier.shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = if (isError) Color(0xFFFF6B6B) else Color(0xFF00D9D9)
                        )
                    } else Modifier
                )
        ) {
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(10.dp)
                        .background(
                            (if (isError) Color(0xFFFF6B6B) else Color(0xFF00D9D9)).copy(alpha = glowAlpha), 
                            RoundedCornerShape(14.dp)
                        )
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color(0xFF6B7280),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { isFocused = it.isFocused },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = (if (isError) Color(0xFFFF6B6B) else Color(0xFF00D9D9)).copy(alpha = 0.5f),
                    unfocusedBorderColor = if (isError) Color(0xFFFF6B6B).copy(alpha = 0.5f) else Color(0xFF374151),
                    focusedContainerColor = Color(0xFF161E29),
                    unfocusedContainerColor = Color(0xFF1F2937).copy(alpha = 0.6f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00D9D9)
                ),
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = onPasswordToggle) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (isFocused) Color(0xFF00D9D9) else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else null
            )
        }
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun MainButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )

    val buttonGradient = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF00D9D9), Color(0xFF00B4D8))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF374151), Color(0xFF374151))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = if (enabled && !loading) 12.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF00D9D9).copy(alpha = 0.5f)
            )
            .background(buttonGradient, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF0A1117),
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color(0xFF0A1117) else Color(0xFF9CA3AF),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun OrDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
        Text(
            text = "OR CONTINUE WITH",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color(0xFF6B7280),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.1f)))
    }
}

@Composable
fun SocialSection(onGoogleClick: () -> Unit = {}, onXClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialLoginButton(onClick = onGoogleClick) { GoogleIcon(modifier = Modifier.size(24.dp)) }
        SocialLoginButton(onClick = onXClick) { XIcon(modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun SocialLoginButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF1F2937))
            .border(1.dp, Color(0xFF374151), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

