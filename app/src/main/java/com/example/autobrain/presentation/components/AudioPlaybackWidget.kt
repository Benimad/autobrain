package com.example.autobrain.presentation.components

import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AudioPlaybackWidget(
    audioFilePath: String,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    
    val mediaPlayer = remember(audioFilePath) {
        if (File(audioFilePath).exists()) {
            MediaPlayer().apply {
                try {
                    setDataSource(audioFilePath)
                    prepare()
                    duration = this.duration
                } catch (e: Exception) {
                    null
                }
            }
        } else null
    }
    
    LaunchedEffect(isPlaying) {
        while (isPlaying && mediaPlayer != null) {
            currentPosition = mediaPlayer.currentPosition
            delay(100)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
    
    if (mediaPlayer == null) {
        Card(modifier) {
            Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Audio file not available")
            }
        }
        return
    }
    
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text("Audio Recording", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { 
                    mediaPlayer.seekTo(it.toInt())
                    currentPosition = it.toInt()
                },
                valueRange = 0f..duration.toFloat()
            )
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatTime(currentPosition))
                
                Row {
                    IconButton(onClick = {
                        if (isPlaying) {
                            mediaPlayer.pause()
                        } else {
                            mediaPlayer.start()
                        }
                        isPlaying = !isPlaying
                    }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    
                    IconButton(onClick = {
                        mediaPlayer.seekTo(0)
                        currentPosition = 0
                        isPlaying = false
                        mediaPlayer.pause()
                    }) {
                        Icon(Icons.Default.Refresh, "Reset")
                    }
                }
                
                Text(formatTime(duration))
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}
