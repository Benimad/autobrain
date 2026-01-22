package com.example.autobrain.core.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

object MediaCompressionUtils {
    private const val TAG = "MediaCompression"
    
    suspend fun compressVideo(
        inputPath: String,
        outputPath: String,
        targetBitrate: Int = 1_000_000,
        maxWidth: Int = 1280,
        maxHeight: Int = 720
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                return@withContext Result.Error(Exception("Input file not found"))
            }
            
            val extractor = MediaExtractor()
            extractor.setDataSource(inputPath)
            
            var videoTrackIndex = -1
            var inputFormat: MediaFormat? = null
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/")) {
                    videoTrackIndex = i
                    inputFormat = format
                    break
                }
            }
            
            if (videoTrackIndex == -1 || inputFormat == null) {
                extractor.release()
                Log.w(TAG, "No video track found, copying original")
                inputFile.copyTo(File(outputPath), overwrite = true)
                return@withContext Result.Success(outputPath)
            }
            
            val width = inputFormat.getInteger(MediaFormat.KEY_WIDTH)
            val height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT)
            
            val scaledWidth = if (width > maxWidth) maxWidth else width
            val scaledHeight = (scaledWidth * height / width).coerceAtMost(maxHeight)
            
            val outputFormat = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                scaledWidth,
                scaledHeight
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, 24)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            
            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var muxerStarted = false
            var videoTrack = -1
            
            extractor.selectTrack(videoTrackIndex)
            
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            
            while (!outputDone) {
                if (!inputDone) {
                    val inputBufferIndex = codec.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                        val sampleSize = inputBuffer?.let { extractor.readSampleData(it, 0) } ?: -1
                        
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }
                
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                when {
                    outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = codec.outputFormat
                        videoTrack = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    outputBufferIndex >= 0 -> {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                        
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        
                        if (bufferInfo.size > 0 && muxerStarted) {
                            outputBuffer?.let {
                                it.position(bufferInfo.offset)
                                it.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(videoTrack, it, bufferInfo)
                            }
                        }
                        
                        codec.releaseOutputBuffer(outputBufferIndex, false)
                        
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }
            
            extractor.release()
            codec.stop()
            codec.release()
            muxer.stop()
            muxer.release()
            
            val originalSize = inputFile.length()
            val compressedSize = File(outputPath).length()
            val compressionRatio = ((1 - compressedSize.toFloat() / originalSize) * 100).toInt()
            
            Log.d(TAG, "Video compressed: ${originalSize / 1024}KB -> ${compressedSize / 1024}KB ($compressionRatio% reduction)")
            
            Result.Success(outputPath)
        } catch (e: Exception) {
            Log.e(TAG, "Video compression failed: ${e.message}", e)
            
            try {
                File(inputPath).copyTo(File(outputPath), overwrite = true)
                Log.w(TAG, "Using original video due to compression failure")
                Result.Success(outputPath)
            } catch (copyError: Exception) {
                Result.Error(copyError)
            }
        }
    }
    
    suspend fun compressAudio(
        inputPath: String,
        outputPath: String,
        targetBitrate: Int = 64_000,
        sampleRate: Int = 16000
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                return@withContext Result.Error(Exception("Input file not found"))
            }
            
            val extractor = MediaExtractor()
            extractor.setDataSource(inputPath)
            
            var audioTrackIndex = -1
            var inputFormat: MediaFormat? = null
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    inputFormat = format
                    break
                }
            }
            
            if (audioTrackIndex == -1 || inputFormat == null) {
                extractor.release()
                Log.w(TAG, "No audio track or raw PCM, copying original")
                inputFile.copyTo(File(outputPath), overwrite = true)
                return@withContext Result.Success(outputPath)
            }
            
            val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            
            val outputFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate,
                channelCount
            ).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            }
            
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            
            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var muxerStarted = false
            var audioTrack = -1
            
            extractor.selectTrack(audioTrackIndex)
            
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            
            while (!outputDone) {
                if (!inputDone) {
                    val inputBufferIndex = codec.dequeueInputBuffer(10000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                        val sampleSize = inputBuffer?.let { extractor.readSampleData(it, 0) } ?: -1
                        
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }
                
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                when {
                    outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = codec.outputFormat
                        audioTrack = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    outputBufferIndex >= 0 -> {
                        val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                        
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        
                        if (bufferInfo.size > 0 && muxerStarted) {
                            outputBuffer?.let {
                                it.position(bufferInfo.offset)
                                it.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(audioTrack, it, bufferInfo)
                            }
                        }
                        
                        codec.releaseOutputBuffer(outputBufferIndex, false)
                        
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }
            
            extractor.release()
            codec.stop()
            codec.release()
            muxer.stop()
            muxer.release()
            
            val originalSize = inputFile.length()
            val compressedSize = File(outputPath).length()
            val compressionRatio = ((1 - compressedSize.toFloat() / originalSize) * 100).toInt()
            
            Log.d(TAG, "Audio compressed: ${originalSize / 1024}KB -> ${compressedSize / 1024}KB ($compressionRatio% reduction)")
            
            Result.Success(outputPath)
        } catch (e: Exception) {
            Log.e(TAG, "Audio compression failed: ${e.message}", e)
            
            try {
                File(inputPath).copyTo(File(outputPath), overwrite = true)
                Log.w(TAG, "Using original audio due to compression failure")
                Result.Success(outputPath)
            } catch (copyError: Exception) {
                Result.Error(copyError)
            }
        }
    }
}
