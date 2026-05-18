// Updated: 2026-05-06 13:17
package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.myapplication.ai.GeminiChatScreen
import com.example.myapplication.ai.GenerativeAIModel
import com.example.myapplication.ai.CameraScreen
import com.example.myapplication.auth.AuthScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth

enum class AppScreen { MAP, CHAT, CAMERA, GUIDE }

class MainActivity : ComponentActivity() {
    // Replace with your actual Gemini API key
    private val geminiApiKey = " "
    private val generativeAIModel by lazy { GenerativeAIModel(geminiApiKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val auth = FirebaseAuth.getInstance()
                var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
                var currentScreen by remember { mutableStateOf(AppScreen.MAP) }

                when {
                    !isLoggedIn -> AuthScreen(onLoginSuccess = { isLoggedIn = true })
                    currentScreen == AppScreen.CHAT -> GeminiChatScreen(
                        apiKey = geminiApiKey,
                        onBack = { currentScreen = AppScreen.MAP }
                    )
                    currentScreen == AppScreen.CAMERA -> CameraScreen(
                        generativeAIModel = generativeAIModel,
                        onBack = { currentScreen = AppScreen.MAP }
                    )
                    currentScreen == AppScreen.GUIDE -> SpeciesGuideScreen(
                        onBack = { currentScreen = AppScreen.MAP }
                    )
                    else -> MapScreen(
                        onSignOut = { auth.signOut(); isLoggedIn = false },
                        onOpenChat = { currentScreen = AppScreen.CHAT },
                        onOpenCamera = { currentScreen = AppScreen.CAMERA },
                        onOpenGuide = { currentScreen = AppScreen.GUIDE }
                    )
                }
            }
        }
    }
}
