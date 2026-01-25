package com.example.autobrain.presentation.screens.diagnostics

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.autobrain.data.ai.FrameAnalysisResultWithSnapshot
import com.example.autobrain.presentation.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlaybackScreen(
    navController: NavController,
    videoPath: String,
    criticalFrames: List<FrameAnalysisResultWithSnapshot>
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var selectedFrame by remember { mutableStateOf<FrameAnalysisResultWithSnapshot?>(null) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoFile = File(videoPath)
            if (videoFile.exists()) {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                prepare()
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            duration = this@apply.duration
                        }
                    }
                    
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                })
            }
        }
    }
    
    // Update current position
    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            kotlinx.coroutines.delay(100)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lecture Vidéo", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1628))
            )
        },
        containerColor = Color(0xFF0A1628)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Video Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay markers for critical frames
                criticalFrames.forEach { frame ->
                    val framePosition = (frame.timestamp - (criticalFrames.firstOrNull()?.timestamp ?: 0L)).toFloat() / 1000f
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = (framePosition / (duration.toFloat() / 1000f) * 300).dp.coerceIn(0.dp, 300.dp),
                                top = 16.dp
                            )
                            .size(12.dp)
                            .background(
                                if (frame.smokeDetected) ErrorRed else WarningAmber,
                                CircleShape
                            )
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Playback Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2838)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Timeline with markers
                    VideoTimeline(
                        currentPosition = currentPosition,
                        duration = duration,
                        criticalFrames = criticalFrames,
                        onSeek = { position ->
                            exoPlayer.seekTo(position)
                        }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Play/Pause controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer.seekTo((currentPosition - 5000).coerceAtLeast(0))
                            }
                        ) {
                            Icon(Icons.Default.Replay5, "Rewind", tint = ElectricTeal)
                        }
                        
                        Spacer(Modifier.width(24.dp))
                        
                        IconButton(
                            onClick = {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (isPlaying) "Pause" else "Play",
                                tint = ElectricTeal,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(24.dp))
                        
                        IconButton(
                            onClick = {
                                exoPlayer.seekTo((currentPosition + 5000).coerceAtMost(duration))
                            }
                        ) {
                            Icon(Icons.Default.Forward5, "Forward", tint = ElectricTeal)
                        }
                    }
                    
                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(currentPosition),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            formatTime(duration),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Critical Frames List
            Text(
                "Moments Critiques (${criticalFrames.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                criticalFrames.forEach { frame ->
                    CriticalFrameCard(
                        frame = frame,
                        onClick = {
                            selectedFrame = frame
                            // Seek to frame timestamp
                            val frameTime = frame.timestamp - (criticalFrames.firstOrNull()?.timestamp ?: 0)
                            exoPlayer.seekTo(frameTime)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun VideoTimeline(
    currentPosition: Long,
    duration: Long,
    criticalFrames: List<FrameAnalysisResultWithSnapshot>,
    onSeek: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .background(Color(0xFF2C3848), RoundedCornerShape(2.dp))
        )
        
        // Progress track
        if (duration > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentPosition.toFloat() / duration)
                    .height(4.dp)
                    .align(Alignment.CenterStart)
                    .background(ElectricTeal, RoundedCornerShape(2.dp))
            )
        }
        
        // Critical frame markers
        criticalFrames.forEach { frame ->
            val framePosition = ((frame.timestamp - (criticalFrames.firstOrNull()?.timestamp ?: 0L)).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (framePosition * 300).dp.coerceIn(0.dp, 300.dp))
                    .size(12.dp)
                    .background(
                        if (frame.smokeDetected) ErrorRed else WarningAmber,
                        CircleShape
                    )
                    .clickable {
                        onSeek(frame.timestamp - (criticalFrames.firstOrNull()?.timestamp ?: 0L))
                    }
            )
        }
    }
}

@Composable
private fun CriticalFrameCard(
    frame: FrameAnalysisResultWithSnapshot,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2838)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (frame.snapshotPath != null) {
                AsyncImage(
                    model = File(frame.snapshotPath),
                    contentDescription = "Frame ${frame.frameNumber}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF1C2838), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, null, tint = TextSecondary)
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Frame #${frame.frameNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                if (frame.smokeDetected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cloud,
                            null,
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Fumée ${frame.smokeType} (${(frame.smokeConfidence * 100).toInt()}%)",
                            fontSize = 12.sp,
                            color = ErrorRed
                        )
                    }
                }
                
                if (frame.vibrationDetected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Vibration,
                            null,
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Vibration élevée",
                            fontSize = 12.sp,
                            color = WarningAmber
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.PlayArrow,
                "Play",
                tint = ElectricTeal
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
