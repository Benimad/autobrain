package com.example.autobrain.data.ai

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * TFLite Audio Classifier - Smart On-Device Classification
 * 
 * Features:
 * - TensorFlow Lite model inference (YAMNet or custom car_engine.tflite)
 * - Real-time audio processing at 16kHz
 * - Advanced heuristic analysis fallback
 * - Quality validation (ambient noise detection)
 * - 100% offline - no internet required
 */
@Singleton
class TfliteAudioClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "TfliteAudioClassifier"
    
    // TFLite Model
    private var interpreter: Interpreter? = null
    private var modelLoaded = false
    
    // Audio Recording
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    
    // State Management
    private val _audioQuality = MutableStateFlow<AudioQuality>(AudioQuality.Unknown)
    val audioQuality: StateFlow<AudioQuality> = _audioQuality.asStateFlow()
    
    private val _waveformData = MutableStateFlow<List<Float>>(emptyList())
    val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()
    
    // Configuration
    companion object {
        private const val MODEL_FILE = "car_engine_sounds.tflite" // YAMNet adapted
        private const val SAMPLE_RATE = 16000
        private const val CHANNELS = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val RECORDING_DURATION_MS = 12000L // 12 seconds
        private const val CHUNK_DURATION_MS = 1000L // Analyze every 1 second
        
        // Quality thresholds
        private const val MIN_DB_LEVEL = -50.0
        private const val MAX_DB_LEVEL = -10.0
        private const val AMBIENT_NOISE_THRESHOLD = -45.0
        private const val CONFIDENCE_THRESHOLD = 0.7f
        
        // Waveform visualization
        private const val WAVEFORM_BARS = 60
    }
    
    // =============================================================================
    // INITIALIZATION
    // =============================================================================
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            modelLoaded = loadModel()
            Log.d(TAG, "TfliteAudioClassifier initialized. Model loaded: $modelLoaded")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}")
            false
        }
    }
    
    private fun loadModel(): Boolean {
        return try {
            val modelBuffer = loadModelFile()
            if (modelBuffer != null) {
                val options = Interpreter.Options().apply {
                    setNumThreads(4) // Multi-threaded inference
                    setUseNNAPI(true) // Use Android NNAPI for acceleration
                }
                interpreter = Interpreter(modelBuffer, options)
                Log.d(TAG, "TFLite model loaded successfully")
                true
            } else {
                Log.w(TAG, "Model file not found, will use heuristic analysis only")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Model loading failed: ${e.message}")
            false
        }
    }
    
    private fun loadModelFile(): MappedByteBuffer? {
        return try {
            val fileDescriptor = context.assets.openFd(MODEL_FILE)
            val inputStream = java.io.FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.d(TAG, "Model file not available: ${e.message}")
            null
        }
    }
    
    // =============================================================================
    // RECORDING & CLASSIFICATION
    // =============================================================================
    
    /**
     * Start recording and classify audio with real-time feedback
     */
    suspend fun recordAndClassify(
        durationMs: Long = RECORDING_DURATION_MS,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): ClassificationResult = withContext(Dispatchers.IO) {
        try {
            // Initialize audio recording
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNELS, ENCODING)
                .coerceAtLeast(SAMPLE_RATE * 2)
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNELS,
                ENCODING,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext ClassificationResult.Error("Échec initialisation microphone")
            }
            
            audioRecord?.startRecording()
            isRecording = true
            
            // Recording buffers
            val allAudioData = mutableListOf<Short>()
            val chunkClassifications = mutableListOf<List<AudioClassification>>()
            
            val startTime = SystemClock.elapsedRealtime()
            val audioBuffer = ShortArray(bufferSize / 2)
            
            var lastChunkTime = startTime
            val chunkBuffer = mutableListOf<Short>()
            
            // Recording loop
            while (isRecording && (SystemClock.elapsedRealtime() - startTime) < durationMs) {
                val readResult = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                
                if (readResult > 0) {
                    // Store all data
                    val samples = audioBuffer.take(readResult)
                    allAudioData.addAll(samples)
                    chunkBuffer.addAll(samples)
                    
                    // Update waveform visualization
                    updateWaveform(samples)
                    
                    // Analyze chunk every 1 second
                    val currentTime = SystemClock.elapsedRealtime()
                    if (currentTime - lastChunkTime >= CHUNK_DURATION_MS && chunkBuffer.isNotEmpty()) {
                        val chunkArray = chunkBuffer.toShortArray()
                        
                        // Validate audio quality
                        val quality = assessAudioQuality(chunkArray)
                        _audioQuality.value = quality
                        
                        if (quality is AudioQuality.Good) {
                            // Classify chunk
                            val classifications = classifyAudioChunk(chunkArray)
                            if (classifications.isNotEmpty()) {
                                chunkClassifications.add(classifications)
                            }
                        }
                        
                        chunkBuffer.clear()
                        lastChunkTime = currentTime
                    }
                }
                
                // Update progress
                val progress = (SystemClock.elapsedRealtime() - startTime).toFloat() / durationMs
                val statusMessage = when (_audioQuality.value) {
                    is AudioQuality.Good -> "Enregistrement en cours..."
                    is AudioQuality.TooQuiet -> "⚠️ Signal trop faible"
                    is AudioQuality.TooNoisy -> "⚠️ Trop de bruit ambiant"
                    is AudioQuality.Unknown -> "Analyse..."
                }
                onProgress(progress.coerceIn(0f, 1f), statusMessage)
                
                delay(50)
            }
            
            stopRecording()
            
            // Validate recording quality
            val overallQuality = assessAudioQuality(allAudioData.toShortArray())
            if (overallQuality !is AudioQuality.Good) {
                return@withContext ClassificationResult.PoorQuality(
                    "Qualité audio insuffisante. ${overallQuality.message}\n" +
                    "Recommandation: Réessayez dans un environnement plus calme."
                )
            }
            
            // Combine all chunk classifications
            val finalClassifications = if (chunkClassifications.isNotEmpty()) {
                aggregateClassifications(chunkClassifications)
            } else {
                // Fallback: analyze entire recording
                classifyAudioChunk(allAudioData.toShortArray())
            }
            
            // Validate confidence
            val topClassification = finalClassifications.maxByOrNull { it.confidence }
            if (topClassification == null || topClassification.confidence < 0.3f) {
                return@withContext ClassificationResult.Ambiguous(
                    "Son ambigu - impossible de classifier avec confiance.\n" +
                    "Recommandation: Recommencer avec moteur au ralenti stable."
                )
            }
            
            // Save audio file for history
            val audioFilePath = saveAudioToFile(allAudioData.toShortArray())
            
            ClassificationResult.Success(
                classifications = finalClassifications.take(5),
                audioFilePath = audioFilePath,
                durationMs = durationMs.toInt()
            )
            
        } catch (e: SecurityException) {
            ClassificationResult.Error("Permission microphone requise")
        } catch (e: Exception) {
            Log.e(TAG, "Classification error: ${e.message}", e)
            ClassificationResult.Error("Erreur: ${e.message}")
        }
    }
    
    private fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording: ${e.message}")
        }
        audioRecord = null
    }
    
    // =============================================================================
    // CLASSIFICATION LOGIC
    // =============================================================================
    
    private suspend fun classifyAudioChunk(audioData: ShortArray): List<AudioClassification> =
        withContext(Dispatchers.Default) {
            val classifications = mutableListOf<AudioClassification>()
            
            // Try TFLite model first
            if (modelLoaded && interpreter != null) {
                try {
                    val modelResults = runModelInference(audioData)
                    classifications.addAll(modelResults)
                } catch (e: Exception) {
                    Log.e(TAG, "Model inference failed: ${e.message}")
                }
            }
            
            // Always add heuristic analysis for robustness
            val heuristicResults = performHeuristicAnalysis(audioData)
            classifications.addAll(heuristicResults)
            
            // Deduplicate and combine
            combineAndDeduplicateResults(classifications)
        }
    
    /**
     * Run TFLite model inference
     */
    private fun runModelInference(audioData: ShortArray): List<AudioClassification> {
        return try {
            val interpreter = this.interpreter ?: return emptyList()
            
            // Prepare input: normalize to [-1, 1] and reshape
            val inputSize = minOf(audioData.size, SAMPLE_RATE * 2) // Max 2 seconds
            val inputBuffer = ByteBuffer.allocateDirect(inputSize * 4)
                .order(ByteOrder.nativeOrder())
            
            val floatBuffer = inputBuffer.asFloatBuffer()
            for (i in 0 until inputSize) {
                floatBuffer.put(audioData[i].toFloat() / Short.MAX_VALUE)
            }
            inputBuffer.rewind()
            
            // Output buffer (10 classes)
            val outputBuffer = Array(1) { FloatArray(10) }
            
            // Run inference
            interpreter.run(inputBuffer, outputBuffer)
            
            // Map to classifications
            val labels = listOf(
                EngineSoundTypes.NORMAL_ENGINE,
                EngineSoundTypes.KNOCKING,
                EngineSoundTypes.RATTLING,
                EngineSoundTypes.BELT_SQUEAL,
                EngineSoundTypes.GRINDING,
                EngineSoundTypes.HISSING,
                EngineSoundTypes.CLICKING,
                EngineSoundTypes.TAPPING,
                EngineSoundTypes.RUMBLING,
                EngineSoundTypes.WHINING
            )
            
            outputBuffer[0].mapIndexed { index, confidence ->
                AudioClassification(
                    label = labels.getOrElse(index) { "unknown" },
                    confidence = confidence,
                    description = EngineSoundTypes.descriptions[labels.getOrNull(index)] ?: ""
                )
            }.filter { it.confidence >= 0.3f }
            
        } catch (e: Exception) {
            Log.e(TAG, "Model inference error: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Heuristic analysis fallback
     */
    private fun performHeuristicAnalysis(audioData: ShortArray): List<AudioClassification> {
        if (audioData.isEmpty()) return emptyList()
        
        val results = mutableListOf<AudioClassification>()
        
        // Calculate audio features
        val rms = calculateRMS(audioData)
        val dbLevel = 20 * log10(rms.coerceAtLeast(1.0))
        val zeroCrossings = calculateZeroCrossings(audioData)
        val spectral = analyzeSpectralFeatures(audioData)
        val temporal = analyzeTemporalFeatures(audioData)
        
        // Smart classification rules
        when {
            // Very quiet - likely normal idle
            dbLevel < -40 -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.NORMAL_ENGINE,
                    confidence = 0.75f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.NORMAL_ENGINE] ?: ""
                ))
            }
            
            // High frequency + periodic = belt squeal
            spectral.hasHighFrequencyPeaks && temporal.isPeriodic && zeroCrossings > audioData.size * 0.35 -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.BELT_SQUEAL,
                    confidence = 0.72f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.BELT_SQUEAL] ?: ""
                ))
            }
            
            // Low frequency + impulsive + loud = knocking
            spectral.hasLowFrequencyPeaks && temporal.isImpulsive && dbLevel > -25 -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.KNOCKING,
                    confidence = 0.68f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.KNOCKING] ?: ""
                ))
            }
            
            // Irregular pattern + moderate loudness = rattling
            temporal.isIrregular && dbLevel > -30 -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.RATTLING,
                    confidence = 0.65f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.RATTLING] ?: ""
                ))
            }
            
            // Very high frequency = hissing
            zeroCrossings > audioData.size * 0.45 -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.HISSING,
                    confidence = 0.60f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.HISSING] ?: ""
                ))
            }
            
            // Low frequency + periodic = rumbling
            spectral.hasLowFrequencyPeaks && temporal.isPeriodic -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.RUMBLING,
                    confidence = 0.62f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.RUMBLING] ?: ""
                ))
            }
            
            // Regular pattern + moderate = likely normal
            temporal.isPeriodic && dbLevel > -35 -> {
                results.add(AudioClassification(
                    label = EngineSoundTypes.NORMAL_ENGINE,
                    confidence = 0.70f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.NORMAL_ENGINE] ?: ""
                ))
            }
            
            else -> {
                // Default to normal with lower confidence
                results.add(AudioClassification(
                    label = EngineSoundTypes.NORMAL_ENGINE,
                    confidence = 0.55f,
                    description = EngineSoundTypes.descriptions[EngineSoundTypes.NORMAL_ENGINE] ?: ""
                ))
            }
        }
        
        return results
    }
    
    // =============================================================================
    // AUDIO ANALYSIS UTILITIES
    // =============================================================================
    
    private fun calculateRMS(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0
        val sumSquares = audioData.sumOf { (it.toDouble() * it.toDouble()) }
        return sqrt(sumSquares / audioData.size)
    }
    
    private fun calculateZeroCrossings(audioData: ShortArray): Int {
        var crossings = 0
        for (i in 1 until audioData.size) {
            if ((audioData[i - 1] >= 0 && audioData[i] < 0) ||
                (audioData[i - 1] < 0 && audioData[i] >= 0)) {
                crossings++
            }
        }
        return crossings
    }
    
    private fun analyzeSpectralFeatures(audioData: ShortArray): SpectralFeatures {
        val windowSize = minOf(1024, audioData.size)
        val numWindows = audioData.size / windowSize
        
        var highFreqEnergy = 0.0
        var lowFreqEnergy = 0.0
        
        for (w in 0 until numWindows) {
            val start = w * windowSize
            val window = audioData.copyOfRange(start, minOf(start + windowSize, audioData.size))
            
            val windowRMS = calculateRMS(window)
            val windowZeroCrossings = calculateZeroCrossings(window)
            
            if (windowZeroCrossings > windowSize * 0.3) {
                highFreqEnergy += windowRMS
            } else {
                lowFreqEnergy += windowRMS
            }
        }
        
        return SpectralFeatures(
            hasHighFrequencyPeaks = highFreqEnergy > lowFreqEnergy * 1.5,
            hasLowFrequencyPeaks = lowFreqEnergy > highFreqEnergy * 1.5
        )
    }
    
    private fun analyzeTemporalFeatures(audioData: ShortArray): TemporalFeatures {
        val windowSize = minOf(512, audioData.size)
        val numWindows = audioData.size / windowSize
        
        val windowRMSValues = mutableListOf<Double>()
        var impulsiveCount = 0
        
        for (w in 0 until numWindows) {
            val start = w * windowSize
            val window = audioData.copyOfRange(start, minOf(start + windowSize, audioData.size))
            
            val windowRMS = calculateRMS(window)
            windowRMSValues.add(windowRMS)
            
            // Detect impulsive sounds
            val maxSample = window.maxOfOrNull { abs(it.toInt()) } ?: 0
            if (maxSample > windowRMS * 3) {
                impulsiveCount++
            }
        }
        
        // Calculate variation coefficient for periodicity
        val mean = windowRMSValues.average()
        val variance = windowRMSValues.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        val coefficientOfVariation = if (mean > 0) stdDev / mean else 1.0
        
        return TemporalFeatures(
            isImpulsive = impulsiveCount > numWindows * 0.2,
            isPeriodic = coefficientOfVariation < 0.3,
            isIrregular = coefficientOfVariation > 0.5
        )
    }
    
    /**
     * Assess audio quality
     */
    private fun assessAudioQuality(audioData: ShortArray): AudioQuality {
        if (audioData.isEmpty()) return AudioQuality.Unknown
        
        val rms = calculateRMS(audioData)
        val dbLevel = 20 * log10(rms.coerceAtLeast(1.0))
        
        return when {
            dbLevel < MIN_DB_LEVEL -> AudioQuality.TooQuiet(
                "Signal trop faible (${dbLevel.toInt()} dB). Rapprochez le micro du moteur."
            )
            dbLevel > MAX_DB_LEVEL -> AudioQuality.TooNoisy(
                "Trop de bruit ambiant (${dbLevel.toInt()} dB). Trouvez un endroit plus calme."
            )
            else -> AudioQuality.Good
        }
    }
    
    /**
     * Update waveform for visualization
     */
    private fun updateWaveform(samples: List<Short>) {
        val step = maxOf(1, samples.size / WAVEFORM_BARS)
        val waveform = mutableListOf<Float>()
        
        for (i in 0 until WAVEFORM_BARS) {
            val index = i * step
            if (index < samples.size) {
                val amplitude = abs(samples[index].toFloat()) / Short.MAX_VALUE
                waveform.add(amplitude)
            } else {
                waveform.add(0f)
            }
        }
        
        _waveformData.value = waveform
    }
    
    /**
     * Aggregate classifications from multiple chunks
     */
    private fun aggregateClassifications(
        chunkClassifications: List<List<AudioClassification>>
    ): List<AudioClassification> {
        // Group by label and average confidence
        val grouped = mutableMapOf<String, MutableList<Float>>()
        
        chunkClassifications.flatten().forEach { classification ->
            grouped.getOrPut(classification.label) { mutableListOf() }
                .add(classification.confidence)
        }
        
        return grouped.map { (label, confidences) ->
            val avgConfidence = confidences.average().toFloat()
            AudioClassification(
                label = label,
                confidence = avgConfidence,
                description = EngineSoundTypes.descriptions[label] ?: ""
            )
        }.sortedByDescending { it.confidence }
    }
    
    /**
     * Combine and deduplicate results
     */
    private fun combineAndDeduplicateResults(
        classifications: List<AudioClassification>
    ): List<AudioClassification> {
        return classifications
            .groupBy { it.label }
            .map { (label, items) ->
                val maxConfidence = items.maxOf { it.confidence }
                AudioClassification(
                    label = label,
                    confidence = maxConfidence,
                    description = items.first().description
                )
            }
            .filter { it.confidence >= 0.3f }
            .sortedByDescending { it.confidence }
    }
    
    /**
     * Save audio to file for history
     */
    private suspend fun saveAudioToFile(audioData: ShortArray): String =
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "audio_${System.currentTimeMillis()}.pcm")
                FileOutputStream(file).use { fos ->
                    val byteBuffer = ByteBuffer.allocate(audioData.size * 2)
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    audioData.forEach { byteBuffer.putShort(it) }
                    fos.write(byteBuffer.array())
                }
                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save audio: ${e.message}")
                ""
            }
        }
    
    // =============================================================================
    // CLEANUP
    // =============================================================================
    
    fun release() {
        stopRecording()
        interpreter?.close()
        interpreter = null
        modelLoaded = false
    }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class SpectralFeatures(
    val hasHighFrequencyPeaks: Boolean,
    val hasLowFrequencyPeaks: Boolean
)

data class TemporalFeatures(
    val isImpulsive: Boolean,
    val isPeriodic: Boolean,
    val isIrregular: Boolean
)

sealed class AudioQuality(val message: String) {
    object Unknown : AudioQuality("Analyse en cours...")
    object Good : AudioQuality("Qualité audio excellente")
    data class TooQuiet(val reason: String) : AudioQuality(reason)
    data class TooNoisy(val reason: String) : AudioQuality(reason)
}

sealed class ClassificationResult {
    data class Success(
        val classifications: List<AudioClassification>,
        val audioFilePath: String,
        val durationMs: Int
    ) : ClassificationResult()
    
    data class PoorQuality(val message: String) : ClassificationResult()
    data class Ambiguous(val message: String) : ClassificationResult()
    data class Error(val message: String) : ClassificationResult()
}
