package com.example.myapplication.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

class GenerativeAIModel(private val apiKey: String) {
    private val config = generationConfig {
        temperature = 0.7f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1024
    }

    private var generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = config
    )
    
    private var chat = generativeModel.startChat()

    suspend fun generateInsights(prompt: String): String? {
        return try {
            val response = chat.sendMessage(prompt)
            response.text
        } catch (e: Exception) {
            val errorMsg = e.message ?: ""
            if (errorMsg.contains("503") || errorMsg.contains("UNAVAILABLE") || errorMsg.contains("404")) {
                // Retry/Fallback logic
                val response = chat.sendMessage(prompt)
                response.text
            } else {
                "Error: $errorMsg"
            }
        }
    }

    suspend fun identifyPlant(bitmap: Bitmap): String? {
        return try {
            val inputContent = content {
                image(bitmap)
                text("Identify this plant. Provide its common name and scientific name. If it is not a plant, please inform the user.")
            }
            val response = generativeModel.generateContent(inputContent)
            response.text
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
