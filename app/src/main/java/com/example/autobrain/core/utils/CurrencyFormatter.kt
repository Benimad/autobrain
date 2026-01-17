package com.example.autobrain.core.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * CurrencyFormatter - Multi-currency and localized formatting
 * 
 * Supports:
 * - US Dollar (USD/$) - Default
 * - Euro (EUR/€)
 * - Localized number formatting
 */
object CurrencyFormatter {
    
    /**
     * Currency codes
     */
    const val USD = "USD"  // US Dollar
    const val EUR = "EUR"  // Euro
    
    /**
     * Currency symbols
     */
    const val SYMBOL_USD = "$"
    const val SYMBOL_EUR = "€"
    
    /**
     * Format amount in USD (default for app)
     * Examples:
     * - formatUSD(75000) = "$75,000"
     * - formatUSD(85000.5) = "$85,000.50"
     */
    fun formatUSD(amount: Double, locale: Locale = Locale.US): String {
        return formatCurrency(amount, USD, locale)
    }
    
    /**
     * Format amount with compact notation (K, M)
     * Examples:
     * - formatCompact(75000) = "$75K"
     * - formatCompact(1500000) = "$1.5M"
     */
    fun formatCompactUSD(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                val millions = amount / 1_000_000
                String.format("$%.1fM", millions)
            }
            amount >= 1_000 -> {
                val thousands = amount / 1_000
                String.format("$%.0fK", thousands)
            }
            else -> {
                formatUSD(amount)
            }
        }
    }
    
    /**
     * Format price range
     * Example: formatRange(75000, 95000) = "$75,000 - $95,000"
     */
    fun formatRangeUSD(minPrice: Double, maxPrice: Double, locale: Locale = Locale.US): String {
        val min = formatNumber(minPrice, locale)
        val max = formatNumber(maxPrice, locale)
        return "$$min - $$max"
    }
    
    /**
     * Format price range with compact notation
     * Example: formatRangeCompact(75000, 95000) = "$75K - $95K"
     */
    fun formatRangeCompactUSD(minPrice: Double, maxPrice: Double): String {
        val min = formatCompactNumber(minPrice)
        val max = formatCompactNumber(maxPrice)
        return "$$min - $$max"
    }
    
    /**
     * Format currency with locale support
     */
    fun formatCurrency(
        amount: Double,
        currencyCode: String = USD,
        locale: Locale = Locale.US
    ): String {
        return try {
            val numberFormat = NumberFormat.getCurrencyInstance(locale)
            val currency = Currency.getInstance(currencyCode)
            numberFormat.currency = currency
            numberFormat.format(amount)
        } catch (e: Exception) {
            // Fallback formatting
            "${getCurrencySymbol(currencyCode)}${formatNumber(amount, locale)}"
        }
    }
    
    /**
     * Format number with locale-specific separators
     * US locale: 75,000.50
     * French locale: 75 000,50
     * Arabic locale: ٧٥٬٠٠٠٫٥٠
     */
    fun formatNumber(number: Double, locale: Locale = Locale.US): String {
        val numberFormat = NumberFormat.getNumberInstance(locale)
        numberFormat.minimumFractionDigits = 0
        numberFormat.maximumFractionDigits = 2
        return numberFormat.format(number)
    }
    
    /**
     * Format number with compact notation
     */
    fun formatCompactNumber(number: Double): String {
        return when {
            number >= 1_000_000 -> {
                String.format("%.1fM", number / 1_000_000)
            }
            number >= 1_000 -> {
                String.format("%.0fK", number / 1_000)
            }
            else -> {
                number.toInt().toString()
            }
        }
    }
    
    /**
     * Get currency symbol
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            USD -> SYMBOL_USD
            EUR -> SYMBOL_EUR
            else -> currencyCode
        }
    }
    
    /**
     * Parse formatted currency string to Double
     * Examples:
     * - "$75,000" → 75000.0
     * - "$75K" → 75000.0
     */
    fun parseCurrency(formatted: String): Double? {
        return try {
            // Remove currency symbols and letters
            val cleaned = formatted
                .replace(SYMBOL_EUR, "")
                .replace(SYMBOL_USD, "")
                .replace("[^0-9.,KkMm]".toRegex(), "")
                .trim()
            
            when {
                cleaned.endsWith("K", ignoreCase = true) -> {
                    cleaned.dropLast(1).toDoubleOrNull()?.times(1_000)
                }
                cleaned.endsWith("M", ignoreCase = true) -> {
                    cleaned.dropLast(1).toDoubleOrNull()?.times(1_000_000)
                }
                else -> {
                    // Handle both comma and dot as decimal separator
                    cleaned.replace(",", ".").toDoubleOrNull()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Extension functions for easy currency formatting
 */

/**
 * Format Double as USD
 */
fun Double.toUSD(locale: Locale = Locale.US): String {
    return CurrencyFormatter.formatUSD(this, locale)
}

/**
 * Format Double as compact USD
 */
fun Double.toCompactUSD(): String {
    return CurrencyFormatter.formatCompactUSD(this)
}

/**
 * Format Int as USD
 */
fun Int.toUSD(locale: Locale = Locale.US): String {
    return CurrencyFormatter.formatUSD(this.toDouble(), locale)
}

/**
 * Format Int as compact USD
 */
fun Int.toCompactUSD(): String {
    return CurrencyFormatter.formatCompactUSD(this.toDouble())
}

/**
 * Format number with locale
 */
fun Double.toFormattedNumber(locale: Locale = Locale.US): String {
    return CurrencyFormatter.formatNumber(this, locale)
}

/**
 * Format number with compact notation
 */
fun Double.toCompactNumber(): String {
    return CurrencyFormatter.formatCompactNumber(this)
}
