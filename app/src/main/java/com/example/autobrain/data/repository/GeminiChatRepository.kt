package com.example.autobrain.data.repository

import com.example.autobrain.presentation.screens.chat.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AiResponse(
    val content: String,
    val riskLevel: String? = null
)

@Singleton
class GeminiChatRepository @Inject constructor(
    private val geminiModel: GenerativeModel
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
- Always consider Moroccan driving conditions, such as hot climates, dusty roads, traffic congestion, and fuel quality variations, which may affect vehicle performance.

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

IMPORTANT: Always respond in French as this app is for Morocco market. When assessing risk, explicitly state it as "RISQUE: LOW", "RISQUE: MEDIUM", or "RISQUE: HIGH" in your response.
""".trimIndent()

    suspend fun sendMessage(message: String): AiResponse {
        try {
            val chat = geminiModel.startChat(
                history = conversationHistory.map { (user, ai) ->
                    listOf(
                        content(role = "user") { text(user) },
                        content(role = "model") { text(ai) }
                    )
                }.flatten()
            )

            val prompt = if (conversationHistory.isEmpty()) {
                "$systemPrompt\n\nUser: $message"
            } else {
                message
            }

            val response = chat.sendMessage(prompt)
            val responseText = response.text ?: "Je suis désolé, je n'ai pas pu générer une réponse."

            conversationHistory.add(message to responseText)

            val riskLevel = extractRiskLevel(responseText)

            return AiResponse(
                content = responseText,
                riskLevel = riskLevel
            )
        } catch (e: Exception) {
            throw Exception("Erreur de connexion avec Gemini AI: ${e.message}")
        }
    }

    private fun extractRiskLevel(text: String): String? {
        return when {
            text.contains("RISQUE: HIGH", ignoreCase = true) || 
            text.contains("RISQUE: ÉLEVÉ", ignoreCase = true) -> "HIGH"
            text.contains("RISQUE: MEDIUM", ignoreCase = true) || 
            text.contains("RISQUE: MOYEN", ignoreCase = true) -> "MEDIUM"
            text.contains("RISQUE: LOW", ignoreCase = true) || 
            text.contains("RISQUE: FAIBLE", ignoreCase = true) -> "LOW"
            else -> null
        }
    }

    fun getChatHistory(): Flow<List<ChatMessage>> = _chatHistory

    suspend fun clearChat() {
        _chatHistory.value = emptyList()
        conversationHistory.clear()
    }
}
