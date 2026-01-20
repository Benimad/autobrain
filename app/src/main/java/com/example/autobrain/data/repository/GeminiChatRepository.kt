package com.example.autobrain.data.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import com.example.autobrain.data.repository.VideoDiagnosticRepository
import com.example.autobrain.domain.repository.AIScoreRepository
import com.example.autobrain.domain.repository.CarLogRepository
import com.example.autobrain.domain.repository.UserRepository
import com.example.autobrain.presentation.screens.chat.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class AiResponse(
    val content: String,
    val riskLevel: String? = null
)

@Singleton
class GeminiChatRepository @Inject constructor(
    private val geminiModel: GenerativeModel,
    private val userRepository: UserRepository,
    private val carLogRepository: CarLogRepository,
    private val aiScoreRepository: AIScoreRepository,
    private val audioDiagnosticRepository: AudioDiagnosticRepository,
    private val videoDiagnosticRepository: VideoDiagnosticRepository,
    private val auth: FirebaseAuth
) {
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    private val systemPrompt = """
You are AutoBrain, an AI-powered diagnostic assistant for car owners, powered by Gemini. Your primary role is to help users troubleshoot and understand car issues in a safe, user-friendly manner.

**Key Capabilities:**
- Analyze engine sounds, videos, maintenance logs, and AI scores to identify potential issues.
- Ask intelligent, targeted questions to gather more details about the car (e.g., model, year, symptoms, recent usage).
- Explain diagnostic results from audio/video inputs in simple terms.
- Provide personalized recommendations based on vehicle data, without giving step-by-step repair instructions.
- Assess and communicate risk levels for identified issues: LOW (minor, no immediate action needed), MEDIUM (monitor closely, potential escalation), HIGH (urgent, seek professional help immediately).
- Adapt all explanations for non-technical users, using everyday language and avoiding jargon. If technical terms are necessary, define them simply.
- Always consider local driving conditions, such as hot climates, dusty roads, traffic congestion, and fuel quality variations, which may affect vehicle performance.

**Rules and Guidelines:**
- Never provide mechanical repair instructions or advice that could lead to unsafe actions (e.g., do not suggest DIY fixes involving tools, wiring, or engine disassembly).
- Keep questions clear, simple, and one at a time to avoid overwhelming the user.
- Base recommendations on safe practices, like consulting a certified mechanic or scheduling maintenance.
- Do not give dangerous advice; always prioritize user safety and vehicle reliability.
- End every response with a clear next step or advice, such as "Please upload a video of the issue" or "Visit a trusted mechanic soon."

**Response Structure:**
1. Acknowledge the user's input or query empathetically.
2. Summarize key findings from diagnostics if available.
3. Ask clarifying questions if more data is needed.
4. Provide explanations and risk assessments.
5. Offer recommendations.
6. Conclude with a specific next step.

IMPORTANT: Always respond in English. When assessing risk, explicitly state it as "RISK: LOW", "RISK: MEDIUM", or "RISK: HIGH" in your response.
""".trimIndent()

    suspend fun sendMessage(message: String): AiResponse {
        try {
            android.util.Log.d("GeminiChatRepository", "sendMessage called with: $message")
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            android.util.Log.d("GeminiChatRepository", "User authenticated: $userId")
            
            val carContext = buildCarContext(userId)
            
            val chat = geminiModel.startChat(
                history = conversationHistory.map { (user, ai) ->
                    listOf(
                        content(role = "user") { text(user) },
                        content(role = "model") { text(ai) }
                    )
                }.flatten()
            )

            val prompt = if (conversationHistory.isEmpty()) {
                """
$systemPrompt

=== VEHICLE AND USER DATA ===
$carContext

=== FIRST USER QUESTION ===
$message

Now, analyze the data above and respond to the user's question intelligently. If you detect problems in the data (low AI score, overdue maintenance, recent diagnostics), mention them in your response.
                """.trimIndent()
            } else {
                """
Current vehicle context:
$carContext

User question: $message
                """.trimIndent()
            }

            val response = chat.sendMessage(prompt)
            val responseText = response.text ?: "Je suis désolé, je n'ai pas pu générer une réponse."

            conversationHistory.add(message to responseText)

            val riskLevel = extractRiskLevel(responseText)
            
            val currentMessages = _chatHistory.value.toMutableList()
            currentMessages.add(ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                content = message,
                isUser = true
            ))
            currentMessages.add(ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                content = responseText,
                isUser = false,
                riskLevel = riskLevel
            ))
            _chatHistory.value = currentMessages
            
            android.util.Log.d("GeminiChatRepository", "Chat history updated with ${currentMessages.size} messages")

            return AiResponse(
                content = responseText,
                riskLevel = riskLevel
            )
        } catch (e: Exception) {
            android.util.Log.e("GeminiChatRepository", "Error in sendMessage", e)
            throw Exception("Error connecting to Gemini AI: ${e.message}")
        }
    }

    private suspend fun buildCarContext(userId: String): String {
        val contextBuilder = StringBuilder()
        
        try {
            val user = when (val result = userRepository.getUserById(userId)) {
                is Result.Success -> result.data
                is Result.Error -> null
                is Result.Loading -> null
            }
            
            user?.let {
                contextBuilder.append("**USER INFORMATION:**\n")
                contextBuilder.append("- Name: ${it.name}\n")
                it.carDetails?.let { car ->
                    contextBuilder.append("- Vehicle: ${car.make} ${car.model} (${car.year})\n")
                    contextBuilder.append("- Plate: ${car.licensePlate}\n")
                    contextBuilder.append("- Color: ${car.color}\n")
                }
                contextBuilder.append("\n")
            }
            
            val latestAiScore = aiScoreRepository.getLatestAIScore(userId).firstOrNull()
            latestAiScore?.let { score ->
                contextBuilder.append("**CURRENT AI SCORE:**\n")
                contextBuilder.append("- Overall score: ${score.score}/100\n")
                contextBuilder.append("- Condition: ${score.condition}\n")
                contextBuilder.append("- Engine score: ${score.engineScore}\n")
                contextBuilder.append("- Transmission score: ${score.transmissionScore}\n")
                contextBuilder.append("- Chassis score: ${score.chassisScore}\n")
                contextBuilder.append("- Risk level: ${score.riskLevel}\n")
                if (score.redFlags.isNotEmpty()) {
                    contextBuilder.append("- Alerts: ${score.redFlags.joinToString(", ")}\n")
                }
                contextBuilder.append("\n")
            }
            
            val maintenanceRecords = when (val result = carLogRepository.getMaintenanceRecords(userId)) {
                is Result.Success -> result.data
                else -> emptyList()
            }
            
            if (maintenanceRecords.isNotEmpty()) {
                contextBuilder.append("**MAINTENANCE HISTORY:**\n")
                maintenanceRecords.take(5).forEach { record ->
                    contextBuilder.append("- ${record.type}: ${record.description} (${record.date})\n")
                }
                contextBuilder.append("\n")
            }
            
            val upcomingReminders = when (val result = carLogRepository.getUpcomingReminders(userId)) {
                is Result.Success -> result.data
                else -> emptyList()
            }
            
            if (upcomingReminders.isNotEmpty()) {
                contextBuilder.append("**UPCOMING MAINTENANCE REMINDERS:**\n")
                upcomingReminders.forEach { reminder ->
                    contextBuilder.append("- ${reminder.title}: ${reminder.description} (due: ${reminder.dueDate})\n")
                }
                contextBuilder.append("\n")
            }
            
            val recentAudioDiagnostics = when (val result = audioDiagnosticRepository.getRecentDiagnostics(userId, limit = 3)) {
                is Result.Success -> result.data
                else -> emptyList()
            }
            if (recentAudioDiagnostics.isNotEmpty()) {
                contextBuilder.append("**RECENT AUDIO DIAGNOSTICS:**\n")
                recentAudioDiagnostics.forEach { diagnostic ->
                    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.ENGLISH)
                        .format(java.util.Date(diagnostic.createdAt))
                    contextBuilder.append("- $date: ${diagnostic.healthStatus}\n")
                    contextBuilder.append("  Score: ${diagnostic.rawScore}/100\n")
                    diagnostic.detectedIssues.take(2).forEach { issue ->
                        contextBuilder.append("  • ${issue.description} (severity: ${issue.severity})\n")
                    }
                }
                contextBuilder.append("\n")
            }
            
            val recentVideoDiagnostics = when (val result = videoDiagnosticRepository.getRecentDiagnostics(userId, limit = 3)) {
                is Result.Success -> result.data
                else -> emptyList()
            }
            if (recentVideoDiagnostics.isNotEmpty()) {
                contextBuilder.append("**RECENT VIDEO DIAGNOSTICS:**\n")
                recentVideoDiagnostics.forEach { diagnostic ->
                    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.ENGLISH)
                        .format(java.util.Date(diagnostic.createdAt))
                    contextBuilder.append("- $date: ${diagnostic.healthStatus}\n")
                    contextBuilder.append("  Score: ${diagnostic.finalScore}/100\n")
                    diagnostic.detectedIssues.take(2).forEach { issue ->
                        contextBuilder.append("  • ${issue.description} (severity: ${issue.severity})\n")
                    }
                }
                contextBuilder.append("\n")
            }
            
        } catch (_: Exception) {
            contextBuilder.append("Note: Unable to load some vehicle data.\n")
        }
        
        return if (contextBuilder.isEmpty()) {
            "No vehicle data available. Ask the user questions to better understand their situation."
        } else {
            contextBuilder.toString()
        }
    }

    private fun extractRiskLevel(text: String): String? {
        return when {
            text.contains("RISK: HIGH", ignoreCase = true) || 
            text.contains("RISQUE: HIGH", ignoreCase = true) ||
            text.contains("RISQUE: ÉLEVÉ", ignoreCase = true) -> "HIGH"
            text.contains("RISK: MEDIUM", ignoreCase = true) ||
            text.contains("RISQUE: MEDIUM", ignoreCase = true) ||
            text.contains("RISQUE: MOYEN", ignoreCase = true) -> "MEDIUM"
            text.contains("RISK: LOW", ignoreCase = true) ||
            text.contains("RISQUE: LOW", ignoreCase = true) ||
            text.contains("RISQUE: FAIBLE", ignoreCase = true) -> "LOW"
            else -> null
        }
    }

    fun getChatHistory(): Flow<List<ChatMessage>> = _chatHistory

    fun clearChat() {
        _chatHistory.value = emptyList()
        conversationHistory.clear()
    }
}
