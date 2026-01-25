package com.example.autobrain.presentation.screens.diagnostics

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autobrain.data.ai.*
import com.example.autobrain.presentation.components.GeminiIcon
import com.example.autobrain.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveVideoReportScreen(
    navController: NavController,
    diagnostic: ComprehensiveVideoDiagnostic
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapport Vidéo Complet", color = TextPrimary) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Score
            ScoreHeader(diagnostic.enhancedVisualScore, diagnostic.safetyAssessment.roadworthiness)
            
            Spacer(Modifier.height(16.dp))
            
            // Safety Warning
            if (!diagnostic.isSafeToDrive()) {
                SafetyWarningCard(diagnostic.safetyAssessment)
                Spacer(Modifier.height(16.dp))
            }
            
            // Sections
            SmokeAnalysisSection(diagnostic.smokeDeepAnalysis)
            VibrationAnalysisSection(diagnostic.vibrationEngineeringAnalysis)
            MultimodalCorrelationSection(diagnostic.combinedAudioVideoDiagnosis)
            RepairScenariosSection(diagnostic.repairScenariosVisual)
            VideoQualitySection(diagnostic.videoQualityAssessment)
            SafetyAssessmentSection(diagnostic.safetyAssessment)
            MarketImpactSection(diagnostic.marketImpactVisual)
            EnvironmentalSection(diagnostic.environmentalCompliance)
            
            Spacer(Modifier.height(16.dp))
            
            // AI Confidence Footer
            AIConfidenceFooter(diagnostic.autobrainVideoConfidence)
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ScoreHeader(score: Int, roadworthiness: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                score >= 80 -> Color(0xFF1B5E20)
                score >= 60 -> Color(0xFFF57F17)
                score >= 40 -> Color(0xFFE65100)
                else -> Color(0xFFB71C1C)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Score Visuel", fontSize = 16.sp, color = Color.White.copy(0.9f))
            Text("$score/100", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(roadworthiness, fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
private fun SafetyWarningCard(safety: SafetyAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("⚠️ CONDUITE DÉCONSEILLÉE", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Risque de panne: ${(safety.breakdownProbabilityNext30Days * 100).toInt()}%", fontSize = 14.sp, color = Color.White.copy(0.9f))
            }
        }
    }
}

@Composable
private fun SmokeAnalysisSection(smoke: SmokeDeepAnalysis) {
    if (smoke.typeDetected == "none" || smoke.typeDetected == "unknown") return
    
    ExpandableSection(
        title = "💨 Analyse Fumée",
        icon = Icons.Default.Cloud
    ) {
        InfoRow("Type", smoke.typeDetected.uppercase())
        InfoRow("Diagnostic", smoke.technicalDiagnosis)
        InfoRow("Composition", smoke.chemicalCompositionTheory)
        InfoRow("Pattern", smoke.emissionPattern)
        InfoRow("Odeur prédite", smoke.smellPrediction)
        InfoRow("Intensité", smoke.colorIntensity)
        
        if (smoke.rootCausesByProbability.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Causes Probables:", fontWeight = FontWeight.Bold, color = ElectricTeal)
            smoke.rootCausesByProbability.forEach { cause ->
                CauseCard(cause)
            }
        }
        
        if (smoke.immediateRisks.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Risques Immédiats:", fontWeight = FontWeight.Bold, color = ErrorRed)
            smoke.immediateRisks.forEach { risk ->
                Text("• $risk", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun VibrationAnalysisSection(vibration: VibrationEngineeringAnalysis) {
    if (vibration.vibrationSourceDiagnosis == "N/A") return
    
    ExpandableSection(
        title = "⚡ Analyse Vibrations",
        icon = Icons.Default.Vibration
    ) {
        InfoRow("Fréquence", vibration.vibrationFrequencyEstimation)
        InfoRow("Source", vibration.vibrationSourceDiagnosis)
        InfoRow("Phase", vibration.phaseAnalysis)
        
        if (vibration.probableMechanicalCauses.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Causes Mécaniques:", fontWeight = FontWeight.Bold, color = ElectricTeal)
            vibration.probableMechanicalCauses.forEach { cause ->
                MechanicalCauseCard(cause)
            }
        }
    }
}

@Composable
private fun MultimodalCorrelationSection(correlation: CombinedAudioVideoDiagnosis) {
    ExpandableSection(
        title = "🔗 Corrélation Audio-Vidéo",
        icon = Icons.Default.Link
    ) {
        InfoRow("Score", "${(correlation.correlationScore * 100).toInt()}%")
        InfoRow("Cause Racine", correlation.comprehensiveRootCause)
        InfoRow("Boost Confiance", correlation.confidenceBoost)
        
        if (correlation.multimodalInsights.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            correlation.multimodalInsights.forEach { insight ->
                Text("• $insight", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun RepairScenariosSection(scenarios: List<VisualRepairScenario>) {
    ExpandableSection(
        title = "💰 Scénarios de Réparation",
        icon = Icons.Default.Build
    ) {
        scenarios.forEach { scenario ->
            RepairScenarioCard(scenario)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun VideoQualitySection(quality: VideoQualityAssessment) {
    ExpandableSection(
        title = "📹 Qualité Vidéo",
        icon = Icons.Default.Videocam
    ) {
        InfoRow("Score", "${quality.recordingQualityScore}/100")
        if (quality.technicalIssues.isNotEmpty()) {
            InfoRow("Problèmes", quality.technicalIssues.joinToString(", "))
        }
        if (quality.recommendationForRerecording) {
            Text("⚠️ Recommandation: Refaire l'enregistrement", color = WarningAmber, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SafetyAssessmentSection(safety: SafetyAssessment) {
    ExpandableSection(
        title = "🚦 Évaluation Sécurité",
        icon = Icons.Default.Security
    ) {
        InfoRow("État", safety.roadworthiness)
        InfoRow("Panne (30j)", "${(safety.breakdownProbabilityNext30Days * 100).toInt()}%")
        if (safety.towingRecommendation) {
            Text("🚨 Remorquage recommandé", color = ErrorRed, fontWeight = FontWeight.Bold)
        }
        if (safety.drivingRestrictions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Restrictions:", fontWeight = FontWeight.Bold, color = WarningAmber)
            safety.drivingRestrictions.forEach { restriction ->
                if (restriction.isNotBlank()) {
                    Text("• $restriction", fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun MarketImpactSection(market: MarketImpactVisual) {
    ExpandableSection(
        title = "📊 Impact Marché",
        icon = Icons.Default.TrendingDown
    ) {
        InfoRow("Perception Acheteur", market.buyerPerception)
        InfoRow("Pouvoir Négociation", market.negotiationLeverageSeller)
        InfoRow("Réduction Prix", "$${market.priceReductionExpectedUsd}")
        InfoRow("Délai Vente", "${market.timeToSellEstimateDays} jours")
        if (market.disclosureRequirement.isNotBlank()) {
            Text("⚖️ ${market.disclosureRequirement}", fontSize = 13.sp, color = WarningAmber)
        }
    }
}

@Composable
private fun EnvironmentalSection(env: EnvironmentalCompliance) {
    ExpandableSection(
        title = "🌍 Conformité Environnementale",
        icon = Icons.Default.Eco
    ) {
        val emissionProb = env.emissionTestPassProbability.toFloatOrNull() ?: 0f
        InfoRow("Test Émissions", "${(emissionProb * 100).toInt()}%")
        InfoRow("Niveau Pollution", env.pollutionLevel)
        InfoRow("Contrôle Technique", env.controleTechniqueImpact)
        InfoRow("Vignette", env.vignettePollutionEligibility)
    }
}

@Composable
private fun AIConfidenceFooter(confidence: AutobrainVideoConfidence) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2838).copy(0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GeminiIcon(size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text("Confiance IA: ${(confidence.confidenceThisAnalysis * 100).toInt()}%", fontSize = 13.sp, color = TextSecondary)
            }
            Text("${confidence.geminiModel} • ML Kit ${confidence.mlKitAccuracy}", fontSize = 11.sp, color = TextSecondary.copy(0.7f))
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2838)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = ElectricTeal, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = TextSecondary
                )
            }
            
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label:", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(120.dp))
        Text(value, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CauseCard(cause: SmokeRootCause) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2838)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cause.cause, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${(cause.probability * 100).toInt()}%", fontSize = 14.sp, color = ElectricTeal)
            }
            Text("Coût: ${cause.estimatedCostUsd}", fontSize = 12.sp, color = TextSecondary)
            Text("Complexité: ${cause.repairComplexity}", fontSize = 12.sp, color = when(cause.repairComplexity) {
                "HIGH" -> ErrorRed
                "MEDIUM" -> WarningAmber
                else -> SuccessGreen
            })
        }
    }
}

@Composable
private fun MechanicalCauseCard(cause: VibrationMechanicalCause) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2838)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(cause.component, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(cause.failureType, fontSize = 12.sp, color = TextSecondary)
            Text("Test: ${cause.diagnosticTest}", fontSize = 12.sp, color = TextSecondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Coût: ${cause.replacementCostUsd}", fontSize = 12.sp, color = ElectricTeal)
                Text("Urgence: ${cause.urgency}", fontSize = 12.sp, color = when(cause.urgency) {
                    "HIGH", "CRITICAL" -> ErrorRed
                    "MEDIUM" -> WarningAmber
                    else -> SuccessGreen
                })
            }
        }
    }
}

@Composable
private fun RepairScenarioCard(scenario: VisualRepairScenario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2838)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(scenario.scenarioName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${(scenario.successProbability * 100).toInt()}%", fontSize = 16.sp, color = ElectricTeal)
            }
            Spacer(Modifier.height(8.dp))
            Text(scenario.applicableIf, fontSize = 12.sp, color = TextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(Modifier.height(12.dp))
            
            scenario.steps.forEach { step ->
                Text("• $step", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
            }
            
            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFF2C3848))
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Coût Total", fontSize = 12.sp, color = TextSecondary)
                    Text("$${scenario.totalCostUsd.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ElectricTeal)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Durée", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        if ((scenario.durationHours ?: 0) > 0) "${scenario.durationHours}h" else "${scenario.durationDays ?: 0}j",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
