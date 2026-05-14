package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SpeciesInfo(
    val name: String,
    val kannadaName: String,
    val scientificName: String,
    val description: String,
    val kannadaDescription: String,
    val oxygenFactor: Double,
    val emoji: String
)

val nativeSpecies = listOf(
    SpeciesInfo(
        name = "Neem",
        kannadaName = "ಬೇವು",
        scientificName = "Azadirachta indica",
        description = "A fast-growing tree known for its medicinal properties and high oxygen production.",
        kannadaDescription = "ಔಷಧೀಯ ಗುಣಗಳಿಗೆ ಮತ್ತು ಹೆಚ್ಚಿನ ಆಮ್ಲಜನಕ ಉತ್ಪಾದನೆಗೆ ಹೆಸರುವಾಸಿಯಾದ ವೇಗವಾಗಿ ಬೆಳೆಯುವ ಮರ.",
        oxygenFactor = 1.5,
        emoji = "🌿"
    ),
    SpeciesInfo(
        name = "Honge",
        kannadaName = "ಹೊಂಗೆ",
        scientificName = "Millettia pinnata",
        description = "An eco-friendly tree that provides great shade and is used for biofuel.",
        kannadaDescription = "ಉತ್ತಮ ನೆರಳು ನೀಡುವ ಮತ್ತು ಜೈವಿಕ ಇಂಧನಕ್ಕಾಗಿ ಬಳಸಲಾಗುವ ಪರಿಸರ ಸ್ನೇಹಿ ಮರ.",
        oxygenFactor = 1.2,
        emoji = "🌳"
    ),
    SpeciesInfo(
        name = "Peepal",
        kannadaName = "ಅರಳಿ ಮರ",
        scientificName = "Ficus religiosa",
        description = "Considered sacred, it releases oxygen even at night and lives for centuries.",
        kannadaDescription = "ಪವಿತ್ರವೆಂದು ಪರಿಗಣಿಸಲಾದ ಈ ಮರವು ರಾತ್ರಿಯಲ್ಲೂ ಆಮ್ಲಜನಕವನ್ನು ಬಿಡುಗಡೆ ಮಾಡುತ್ತದೆ ಮತ್ತು ಶತಮಾನಗಳವರೆಗೆ ಬದುಕುತ್ತದೆ.",
        oxygenFactor = 2.0,
        emoji = "🍃"
    ),
    SpeciesInfo(
        name = "Banyan",
        kannadaName = "ಆಲದ ಮರ",
        scientificName = "Ficus benghalensis",
        description = "The national tree of India, famous for its aerial roots and massive canopy.",
        kannadaDescription = "ಭಾರತದ ರಾಷ್ಟ್ರೀಯ ಮರ, ತನ್ನ ಬಿಳಲುಗಳು ಮತ್ತು ಬೃಹತ್ ಹಂದರಕ್ಕೆ ಹೆಸರುವಾಸಿ.",
        oxygenFactor = 1.8,
        emoji = "🏫"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesGuideScreen(onBack: () -> Unit) {
    val gradientBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1628), Color(0xFF0A2318))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Species Guide (ಮರಗಳ ಮಾಹಿತಿ)", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF4CAF50))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1628))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBg)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(nativeSpecies) { species ->
                    SpeciesCard(species)
                }
            }
        }
    }
}

@Composable
fun SpeciesCard(species: SpeciesInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2D1F).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(species.emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${species.name} (${species.kannadaName})",
                        color = Color(0xFF4CAF50),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = species.scientificName,
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = species.description, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = species.kannadaDescription, color = Color(0xFFB0BEC5), fontSize = 14.sp)
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2E4D35))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Oxygen Factor:", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = species.oxygenFactor.toString(),
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
