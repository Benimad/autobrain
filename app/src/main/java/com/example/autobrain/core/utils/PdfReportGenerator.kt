package com.example.autobrain.core.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.example.autobrain.data.ai.ComprehensiveAudioDiagnostic
import com.example.autobrain.data.ai.RepairScenario
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.domain.model.User
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF Report Generator for Audio Diagnostics
 * 
 * Generates professional diagnostic reports with:
 * - Header with logo and car details
 * - Score gauge visualization
 * - Detected issues table
 * - Recommendations list
 * - Comprehensive analysis sections
 * - Footer with timestamp
 */
object PdfReportGenerator {
    private const val TAG = "PdfReportGenerator"
    
    // Page dimensions (A4)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)
    
    // Colors
    private val COLOR_PRIMARY = Color.parseColor("#00FFFF")
    private val COLOR_BACKGROUND = Color.parseColor("#001F3F")
    private val COLOR_TEXT = Color.WHITE
    private val COLOR_TEXT_SECONDARY = Color.parseColor("#B0BEC5")
    private val COLOR_SUCCESS = Color.parseColor("#00E676")
    private val COLOR_WARNING = Color.parseColor("#FFD700")
    private val COLOR_ERROR = Color.parseColor("#FF5252")
    
    /**
     * Generate PDF report for audio diagnostic
     */
    fun generateAudioDiagnosticReport(
        context: Context,
        diagnostic: AudioDiagnosticData,
        comprehensive: ComprehensiveAudioDiagnostic?,
        user: User?
    ): File? {
        return try {
            val document = PdfDocument()
            var pageNumber = 1
            
            // Page 1: Overview
            pageNumber = addOverviewPage(document, pageNumber, diagnostic, user)
            
            // Page 2: Detailed Analysis
            if (diagnostic.detectedIssues.isNotEmpty()) {
                pageNumber = addIssuesPage(document, pageNumber, diagnostic)
            }
            
            // Page 3+: Comprehensive Analysis
            if (comprehensive != null) {
                pageNumber = addComprehensivePage(document, pageNumber, comprehensive)
            }
            
            // Save to file
            val fileName = "AutoBrain_Report_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            
            Log.d(TAG, "PDF generated: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "PDF generation failed: ${e.message}", e)
            null
        }
    }
    
    /**
     * Page 1: Overview with score and summary
     */
    private fun addOverviewPage(
        document: PdfDocument,
        pageNumber: Int,
        diagnostic: AudioDiagnosticData,
        user: User?
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPos = MARGIN.toFloat()
        
        // Header
        yPos = drawHeader(canvas, yPos, "Rapport de Diagnostic Audio")
        yPos += 20
        
        // Car & User Info
        yPos = drawCarInfo(canvas, yPos, user, diagnostic)
        yPos += 30
        
        // Score Gauge
        yPos = drawScoreGauge(canvas, yPos, diagnostic.rawScore, diagnostic.healthStatus)
        yPos += 40
        
        // Summary Box
        yPos = drawSummaryBox(canvas, yPos, diagnostic)
        yPos += 30
        
        // Quick Stats
        yPos = drawQuickStats(canvas, yPos, diagnostic)
        
        // Footer
        drawFooter(canvas, pageNumber, diagnostic.createdAt)
        
        document.finishPage(page)
        return pageNumber + 1
    }
    
    /**
     * Page 2: Detailed Issues
     */
    private fun addIssuesPage(
        document: PdfDocument,
        pageNumber: Int,
        diagnostic: AudioDiagnosticData
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPos = MARGIN.toFloat()
        
        // Header
        yPos = drawHeader(canvas, yPos, "Problèmes Détectés")
        yPos += 30
        
        // Issues Table
        diagnostic.detectedIssues.forEach { issue ->
            yPos = drawIssueCard(canvas, yPos, issue)
            yPos += 20
            
            if (yPos > PAGE_HEIGHT - 100) {
                drawFooter(canvas, pageNumber, diagnostic.createdAt)
                document.finishPage(page)
                return addIssuesPage(document, pageNumber + 1, diagnostic)
            }
        }
        
        // Recommendations
        if (diagnostic.recommendations.isNotEmpty()) {
            yPos += 20
            yPos = drawRecommendations(canvas, yPos, diagnostic.recommendations)
        }
        
        drawFooter(canvas, pageNumber, diagnostic.createdAt)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    /**
     * Page 3+: Comprehensive Analysis
     */
    private fun addComprehensivePage(
        document: PdfDocument,
        pageNumber: Int,
        comprehensive: ComprehensiveAudioDiagnostic
    ): Int {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPos = MARGIN.toFloat()
        
        // Header
        yPos = drawHeader(canvas, yPos, "Analyse Complète Gemini AI")
        yPos += 30
        
        // Enhanced Score
        yPos = drawEnhancedScore(canvas, yPos, comprehensive)
        yPos += 30
        
        // Primary Diagnosis
        yPos = drawPrimaryDiagnosis(canvas, yPos, comprehensive)
        yPos += 30
        
        // Repair Scenarios
        if (comprehensive.detailedRepairPlan.repairScenarios.isNotEmpty()) {
            yPos = drawRepairScenarios(canvas, yPos, comprehensive.detailedRepairPlan.repairScenarios)
        }
        
        drawFooter(canvas, pageNumber, System.currentTimeMillis())
        document.finishPage(page)
        return pageNumber + 1
    }
    
    // =========================================================================
    // DRAWING UTILITIES
    // =========================================================================
    
    private fun drawHeader(canvas: Canvas, yPos: Float, title: String): Float {
        val paint = Paint().apply {
            color = COLOR_PRIMARY
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        // Title
        canvas.drawText(title, MARGIN.toFloat(), yPos + 24, paint)
        
        // Line
        paint.strokeWidth = 2f
        canvas.drawLine(
            MARGIN.toFloat(),
            yPos + 35,
            (PAGE_WIDTH - MARGIN).toFloat(),
            yPos + 35,
            paint
        )
        
        return yPos + 45
    }
    
    private fun drawCarInfo(canvas: Canvas, yPos: Float, user: User?, diagnostic: AudioDiagnosticData): Float {
        val paint = Paint().apply {
            color = COLOR_TEXT_SECONDARY
            textSize = 12f
            isAntiAlias = true
        }
        
        val carDetails = user?.carDetails
        val info = listOf(
            "Véhicule: ${carDetails?.make ?: "N/A"} ${carDetails?.model ?: ""}",
            "Année: ${carDetails?.year ?: "N/A"}",
            "Date: ${formatDate(diagnostic.createdAt)}"
        )
        
        var y = yPos
        info.forEach { line ->
            canvas.drawText(line, MARGIN.toFloat(), y, paint)
            y += 18
        }
        
        return y
    }
    
    private fun drawScoreGauge(canvas: Canvas, yPos: Float, score: Int, status: String): Float {
        val centerX = PAGE_WIDTH / 2f
        val centerY = yPos + 80
        val radius = 70f
        
        // Background circle
        val bgPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, radius, bgPaint)
        
        // Score arc
        val arcPaint = Paint().apply {
            color = getScoreColor(score)
            style = Paint.Style.STROKE
            strokeWidth = 12f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        
        val sweepAngle = (score / 100f) * 360f
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        canvas.drawArc(rect, -90f, sweepAngle, false, arcPaint)
        
        // Score text
        val scorePaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(score.toString(), centerX, centerY + 12, scorePaint)
        
        // Status text
        val statusPaint = Paint().apply {
            color = COLOR_TEXT_SECONDARY
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(status, centerX, centerY + 35, statusPaint)
        
        return centerY + radius + 20
    }
    
    private fun drawSummaryBox(canvas: Canvas, yPos: Float, diagnostic: AudioDiagnosticData): Float {
        val boxHeight = 80f
        val boxPaint = Paint().apply {
            color = Color.parseColor("#1A2332")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        val rect = RectF(
            MARGIN.toFloat(),
            yPos,
            (PAGE_WIDTH - MARGIN).toFloat(),
            yPos + boxHeight
        )
        canvas.drawRoundRect(rect, 8f, 8f, boxPaint)
        
        // Border
        val borderPaint = Paint().apply {
            color = COLOR_PRIMARY
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
        
        // Content
        val textPaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 12f
            isAntiAlias = true
        }
        
        canvas.drawText(
            "Son Principal: ${diagnostic.topSoundLabel}",
            MARGIN + 15f,
            yPos + 25,
            textPaint
        )
        
        canvas.drawText(
            "Confiance: ${(diagnostic.topSoundConfidence * 100).toInt()}%",
            MARGIN + 15f,
            yPos + 45,
            textPaint
        )
        
        canvas.drawText(
            "Problèmes: ${diagnostic.detectedIssues.size}",
            MARGIN + 15f,
            yPos + 65,
            textPaint
        )
        
        return yPos + boxHeight
    }
    
    private fun drawQuickStats(canvas: Canvas, yPos: Float, diagnostic: AudioDiagnosticData): Float {
        val paint = Paint().apply {
            color = COLOR_TEXT
            textSize = 11f
            isAntiAlias = true
        }
        
        val stats = listOf(
            "Durée: ${diagnostic.durationMs / 1000}s",
            "Coût Réparation: $${diagnostic.minRepairCost.toInt()}-$${diagnostic.maxRepairCost.toInt()}",
            "Urgence: ${diagnostic.urgencyLevel}"
        )
        
        var y = yPos
        stats.forEach { stat ->
            canvas.drawText(stat, MARGIN.toFloat(), y, paint)
            y += 18
        }
        
        return y
    }
    
    private fun drawIssueCard(canvas: Canvas, yPos: Float, issue: com.example.autobrain.data.local.entity.IssueData): Float {
        val cardHeight = 70f
        
        // Card background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#1A2332")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        val rect = RectF(MARGIN.toFloat(), yPos, (PAGE_WIDTH - MARGIN).toFloat(), yPos + cardHeight)
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        
        // Severity indicator
        val severityPaint = Paint().apply {
            color = when (issue.severity) {
                "CRITICAL" -> COLOR_ERROR
                "HIGH" -> Color.parseColor("#FF9100")
                "MEDIUM" -> COLOR_WARNING
                else -> COLOR_SUCCESS
            }
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN.toFloat(), yPos, MARGIN + 4f, yPos + cardHeight, severityPaint)
        
        // Text
        val titlePaint = Paint().apply {
            color = COLOR_PRIMARY
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(issue.soundType.replace("_", " "), MARGIN + 15f, yPos + 20, titlePaint)
        
        val descPaint = Paint().apply {
            color = COLOR_TEXT_SECONDARY
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText(issue.description, MARGIN + 15f, yPos + 38, descPaint)
        
        val costPaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText(
            "Coût: $${issue.minCost.toInt()}-$${issue.maxCost.toInt()}",
            MARGIN + 15f,
            yPos + 56,
            costPaint
        )
        
        return yPos + cardHeight
    }
    
    private fun drawRecommendations(canvas: Canvas, yPos: Float, recommendations: List<String>): Float {
        val titlePaint = Paint().apply {
            color = COLOR_PRIMARY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Recommandations", MARGIN.toFloat(), yPos, titlePaint)
        
        val textPaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 10f
            isAntiAlias = true
        }
        
        var y = yPos + 20
        recommendations.take(5).forEach { rec ->
            canvas.drawText("• $rec", MARGIN + 10f, y, textPaint)
            y += 16
        }
        
        return y
    }
    
    private fun drawEnhancedScore(canvas: Canvas, yPos: Float, comprehensive: ComprehensiveAudioDiagnostic): Float {
        val paint = Paint().apply {
            color = COLOR_TEXT
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        canvas.drawText(
            "Score Amélioré: ${comprehensive.enhancedHealthScore}/100",
            MARGIN.toFloat(),
            yPos,
            paint
        )
        
        return yPos + 20
    }
    
    private fun drawPrimaryDiagnosis(canvas: Canvas, yPos: Float, comprehensive: ComprehensiveAudioDiagnostic): Float {
        val titlePaint = Paint().apply {
            color = COLOR_PRIMARY
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Diagnostic Principal", MARGIN.toFloat(), yPos, titlePaint)
        
        val textPaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 11f
            isAntiAlias = true
        }
        
        var y = yPos + 20
        canvas.drawText(comprehensive.primaryDiagnosis.issue, MARGIN.toFloat(), y, textPaint)
        y += 18
        canvas.drawText("Sévérité: ${comprehensive.primaryDiagnosis.severity}", MARGIN.toFloat(), y, textPaint)
        
        return y + 10
    }
    
    private fun drawRepairScenarios(canvas: Canvas, yPos: Float, scenarios: List<RepairScenario>): Float {
        val titlePaint = Paint().apply {
            color = COLOR_PRIMARY
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Scénarios de Réparation", MARGIN.toFloat(), yPos, titlePaint)
        
        val textPaint = Paint().apply {
            color = COLOR_TEXT
            textSize = 10f
            isAntiAlias = true
        }
        
        var y = yPos + 20
        scenarios.take(3).forEach { scenario ->
            canvas.drawText("• ${scenario.scenario}", MARGIN + 10f, y, textPaint)
            y += 14
            canvas.drawText("  Coût: $${scenario.totalCostUsd.toInt()}", MARGIN + 20f, y, textPaint)
            y += 18
        }
        
        return y
    }
    
    private fun drawFooter(canvas: Canvas, pageNumber: Int, timestamp: Long) {
        val paint = Paint().apply {
            color = COLOR_TEXT_SECONDARY
            textSize = 9f
            isAntiAlias = true
        }
        
        val footerY = PAGE_HEIGHT - 20f
        canvas.drawText("AutoBrain Diagnostic Report", MARGIN.toFloat(), footerY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber", (PAGE_WIDTH - MARGIN).toFloat(), footerY, paint)
    }
    
    // =========================================================================
    // HELPERS
    // =========================================================================
    
    private fun getScoreColor(score: Int): Int {
        return when {
            score >= 80 -> COLOR_SUCCESS
            score >= 60 -> COLOR_WARNING
            else -> COLOR_ERROR
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
