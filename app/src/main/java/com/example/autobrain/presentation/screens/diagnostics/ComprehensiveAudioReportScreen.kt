package com.example.autobrain.presentation.screens.diagnostics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.autobrain.data.ai.ComprehensiveAudioDiagnostic
import com.example.autobrain.data.ai.PrimaryDiagnosis
import com.example.autobrain.data.ai.SecondaryIssue
import com.example.autobrain.data.ai.RootCauseAnalysis
import com.example.autobrain.data.ai.MaintenanceCorrelation
import com.example.autobrain.data.ai.RepairScenario
import com.example.autobrain.data.ai.getMostLikelyRepairCost
import com.example.autobrain.data.ai.isSafeToDrive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveAudioReportScreen(
    diagnostic: ComprehensiveAudioDiagnostic?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onExportPdf: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comprehensive Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (diagnostic != null) {
                        IconButton(onClick = onExportPdf) {
                            Icon(Icons.Default.PictureAsPdf, "Export PDF")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingView(Modifier.padding(padding))
            diagnostic != null -> ReportContent(diagnostic, Modifier.padding(padding))
            else -> ErrorView(Modifier.padding(padding))
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Generating comprehensive report...")
        }
    }
}

@Composable
private fun ReportContent(diagnostic: ComprehensiveAudioDiagnostic, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HealthScoreCard(diagnostic.enhancedHealthScore)
        PrimaryDiagnosisCard(diagnostic.primaryDiagnosis)
        
        if (diagnostic.secondaryIssues.isNotEmpty()) {
            SecondaryIssuesCard(diagnostic.secondaryIssues)
        }
        
        RootCauseCard(diagnostic.rootCauseAnalysis)
        MaintenanceCard(diagnostic.maintenanceCorrelation)
        
        if (diagnostic.detailedRepairPlan.immediateActions.isNotEmpty()) {
            RecommendationsCard(diagnostic.detailedRepairPlan.immediateActions)
        }
        
        diagnostic.getMostLikelyRepairCost()?.let { scenario ->
            RepairCostCard(scenario)
        }
        
        SafetyCard(diagnostic.primaryDiagnosis.severity, diagnostic.isSafeToDrive())
        
        if (diagnostic.intelligentRecommendations.forCurrentOwner.isNotEmpty()) {
            NextStepsCard(diagnostic.intelligentRecommendations.forCurrentOwner)
        }
    }
}

@Composable
private fun HealthScoreCard(score: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Health Score", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "$score/100",
                style = MaterialTheme.typography.displayLarge,
                color = when {
                    score >= 80 -> Color.Green
                    score >= 60 -> Color(0xFFFFA500)
                    else -> Color.Red
                }
            )
        }
    }
}

@Composable
private fun PrimaryDiagnosisCard(diagnosis: PrimaryDiagnosis) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = getSeverityColor(diagnosis.severity))
                Spacer(Modifier.width(8.dp))
                Text(diagnosis.issue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Technical: ${diagnosis.technicalName}", style = MaterialTheme.typography.bodySmall)
            Text("Severity: ${diagnosis.severity}")
            Text("Confidence: ${(diagnosis.confidence * 100).toInt()}%")
            if (diagnosis.affectedComponents.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Affected: ${diagnosis.affectedComponents.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SecondaryIssuesCard(issues: List<SecondaryIssue>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Secondary Issues", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            issues.forEach { issue ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = getSeverityColor(issue.severity))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(issue.issue, fontWeight = FontWeight.Medium)
                        Text("${(issue.confidence * 100).toInt()}% confidence", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun RootCauseCard(analysis: RootCauseAnalysis) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Root Cause Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(analysis.mostLikelyCause, style = MaterialTheme.typography.bodyMedium)
            Text("Probability: ${(analysis.probability * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MaintenanceCard(correlation: MaintenanceCorrelation) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, null)
                Spacer(Modifier.width(8.dp))
                Text("Maintenance Impact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(correlation.oilChangeImpact, style = MaterialTheme.typography.bodyMedium)
            Text("Quality: ${correlation.serviceHistoryQuality}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            recommendations.forEachIndexed { index, rec ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("${index + 1}. ", fontWeight = FontWeight.Bold)
                    Text(rec)
                }
            }
        }
    }
}

@Composable
private fun RepairCostCard(scenario: RepairScenario) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Estimated Repair Cost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(scenario.scenario, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("$${scenario.totalCostUsd.toInt()}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text("Duration: ${scenario.durationDays} days", style = MaterialTheme.typography.bodySmall)
            Text("Probability: ${(scenario.probability * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SafetyCard(severity: String, isSafe: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = if (isSafe) Color.Green else Color.Red)
                Spacer(Modifier.width(8.dp))
                Text("Safety Assessment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (isSafe) "Safe to drive with caution" else "DO NOT DRIVE - Critical issue",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSafe) Color.Green else Color.Red
            )
        }
    }
}

@Composable
private fun NextStepsCard(steps: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Next Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            steps.forEachIndexed { index, step ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(step)
                }
            }
        }
    }
}

@Composable
private fun ErrorView(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No report available")
    }
}

private fun getSeverityColor(severity: String) = when (severity.lowercase()) {
    "critical" -> Color.Red
    "high" -> Color(0xFFFFA500)
    "medium" -> Color(0xFFFFD700)
    else -> Color.Green
}
