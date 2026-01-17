package com.example.autobrain.presentation.screens.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autobrain.core.utils.*
import com.example.autobrain.presentation.components.AdaptiveScaffold
import com.example.autobrain.presentation.components.AdaptiveCard
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDiagnosticsScreen(
    navController: NavController
) {
    AdaptiveScaffold(
        title = "Diagnostics IA",
        showBackButton = true,
        onBackClick = { navController.popBackStack() },
        containerColor = Color(0xFF0A1628),
        scrollable = false
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(AdaptiveSpacing.medium()),
            verticalArrangement = Arrangement.spacedBy(AdaptiveSpacing.medium())
        ) {
            item {
                Text(
                    text = "Outils d'analyse AutoBrain",
                    fontSize = (20.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = AdaptiveSpacing.small())
                )
            }

            item {
                DiagnosticToolCard(
                    title = "Analyse Sonore Moteur",
                    description = "Détectez les cliquetis, bruits de courroie et problèmes mécaniques via le micro.",
                    icon = Icons.Outlined.Mic,
                    color1 = Color(0xFF4facfe),
                    color2 = Color(0xFF00f2fe),
                    onClick = { navController.navigate(Screen.EngineSoundAnalysis.route) }
                )
            }

            item {
                DiagnosticToolCard(
                    title = "Analyse Vidéo Visuelle",
                    description = "Identifiez la fumée d'échappement (couleur) et les vibrations moteur par caméra.",
                    icon = Icons.Outlined.Videocam,
                    color1 = Color(0xFF43e97b),
                    color2 = Color(0xFF38f9d7),
                    onClick = { navController.navigate(Screen.VideoAnalysis.route) }
                )
            }

            item {
                DiagnosticToolCard(
                    title = "Estimation Prix Marché",
                    description = "Obtenez une estimation précise basée sur le marché marocain actuel.",
                    icon = Icons.Outlined.AttachMoney,
                    color1 = Color(0xFFfa709a),
                    color2 = Color(0xFFfee140),
                    onClick = { navController.navigate(Screen.PriceEstimation.route) }
                )
            }

            item {
                DiagnosticToolCard(
                    title = "Rapport de Confiance",
                    description = "Synthèse complète anti-arnaque combinant toutes les analyses.",
                    icon = Icons.Outlined.VerifiedUser,
                    color1 = Color(0xFF667eea),
                    color2 = Color(0xFF764ba2),
                    onClick = { 
                        // Assuming current car context or generic entry
                        navController.navigate(Screen.AIScore.route) 
                    }
                )
            }
        }
    }
}

@Composable
fun DiagnosticToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    color1: Color,
    color2: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2639)),
        elevation = CardDefaults.cardElevation(adaptiveCardElevation())
    ) {
        Row(
            modifier = Modifier
                .padding(AdaptiveSpacing.medium())
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(adaptiveIconSize(56.dp))
                    .background(
                        brush = Brush.linearGradient(listOf(color1, color2)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(adaptiveIconSize(32.dp))
                )
            }

            Spacer(modifier = Modifier.width(AdaptiveSpacing.medium()))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(AdaptiveSpacing.extraSmall()))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(adaptiveIconSize())
            )
        }
    }
}
