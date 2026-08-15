package com.example.shopsafe.util

import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * ShopSafe Security, Anti-Fraud & Data Sanitization Engine
 * 
 * Provides:
 * - Barcode & QR Code payload sanitization against injection attacks (SQLi, XSS, Command Injection)
 * - PII Masking for customer & driver data (phone numbers, sensitive address details)
 * - Receipt & Photo hash duplicate prevention (detects reused delivery photos & duplicate receipts)
 * - GPS velocity & anti-spoofing sanity checks (detects impossible speed jumps > 150mph)
 * - Price adjustment & checkout discrepancy fraud flagging (>20% or >$15 difference)
 * - Security audit logging for critical courier actions
 */
object ShopSafeSecurityUtils {

    private val seenReceiptHashes = ConcurrentHashMap<String, Long>()
    private val seenDeliveryPhotoHashes = ConcurrentHashMap<String, Long>()
    private val auditLogs = mutableListOf<SecurityAuditEntry>()

    data class SecurityAuditEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val actionType: String,
        val details: String,
        val severity: String = "INFO", // INFO, WARNING, FRAUD_FLAG, CRITICAL
        val isFlaggedForReview: Boolean = false
    )

    /**
     * Sanitizes Barcode or QR-code raw input string.
     * Restricts characters to safe alphanumeric and common barcode delimiters.
     * Prevents XSS, command injection, and control characters.
     */
    fun sanitizeBarcodeOrQrInput(rawInput: String?): String {
        if (rawInput.isNullOrBlank()) return ""
        // Strip control characters, quotes, HTML tags, and SQL delimiters
        val cleaned = rawInput.trim()
            .replace(Regex("[<>'\"`;()|&$]"), "")
            .filter { it.isLetterOrDigit() || it == '-' || it == '_' || it == ':' || it == '.' || it == '/' }
        return cleaned.take(128)
    }

    /**
     * Masks customer or driver phone numbers for privacy protection.
     * Example: "(555) 890-1234" -> "(555) •••-1234"
     */
    fun maskPhoneNumber(phone: String?): String {
        if (phone.isNullOrBlank()) return "(•••) •••-••••"
        val digits = phone.filter { it.isDigit() }
        return if (digits.length >= 10) {
            "(${digits.take(3)}) •••-${digits.takeLast(4)}"
        } else {
            "•••-•••-${phone.takeLast(4)}"
        }
    }

    /**
     * Masks customer exact unit or sensitive address identifiers.
     */
    fun maskSensitiveAddress(address: String?): String {
        if (address.isNullOrBlank()) return "Encrypted Address"
        return address.replace(Regex("Apt\\s*\\w+", RegexOption.IGNORE_CASE), "Apt ••")
            .replace(Regex("Unit\\s*\\w+", RegexOption.IGNORE_CASE), "Unit ••")
            .replace(Regex("Suite\\s*\\w+", RegexOption.IGNORE_CASE), "Ste ••")
    }

    /**
     * Computes a SHA-256 fingerprint from image byte data or image URI strings.
     */
    fun generatePhotoFingerprint(photoContent: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(photoContent.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Fraud Check: Detects if a receipt photo has already been submitted for a previous order.
     */
    fun isDuplicateReceipt(photoFingerprint: String, orderId: String): Boolean {
        val lastSeen = seenReceiptHashes[photoFingerprint]
        if (lastSeen != null && System.currentTimeMillis() - lastSeen < 86400000L * 7) {
            recordAuditLog(
                actionType = "DUPLICATE_RECEIPT_DETECTED",
                details = "Receipt hash $photoFingerprint was previously submitted. Order: $orderId",
                severity = "FRAUD_FLAG",
                isFlaggedForReview = true
            )
            return true
        }
        seenReceiptHashes[photoFingerprint] = System.currentTimeMillis()
        return false
    }

    /**
     * Fraud Check: Detects duplicate delivery confirmation photo reuse.
     */
    fun isDuplicateDeliveryPhoto(photoFingerprint: String, orderId: String): Boolean {
        val lastSeen = seenDeliveryPhotoHashes[photoFingerprint]
        if (lastSeen != null && System.currentTimeMillis() - lastSeen < 86400000L * 7) {
            recordAuditLog(
                actionType = "DUPLICATE_DELIVERY_PHOTO_DETECTED",
                details = "Proof-of-delivery hash $photoFingerprint was reused across orders. Order: $orderId",
                severity = "FRAUD_FLAG",
                isFlaggedForReview = true
            )
            return true
        }
        seenDeliveryPhotoHashes[photoFingerprint] = System.currentTimeMillis()
        return false
    }

    /**
     * GPS Anti-Spoofing Check:
     * Calculates implied speed between two GPS coordinates and timestamps.
     * Flags impossible speeds (>150 mph) without blocking navigation.
     */
    fun validateGpsSpeedSanity(
        lat1: Double, lng1: Double, time1Ms: Long,
        lat2: Double, lng2: Double, time2Ms: Long
    ): Boolean {
        if (time2Ms <= time1Ms) return true
        val timeDiffHours = (time2Ms - time1Ms) / (1000.0 * 60.0 * 60.0)
        if (timeDiffHours <= 0.0001) return true

        // Haversine approximation
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lng2 - lng1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distanceMiles = 3958.8 * c

        val speedMph = distanceMiles / timeDiffHours
        if (speedMph > 150.0) {
            recordAuditLog(
                actionType = "SUSPICIOUS_GPS_TELEPORT",
                details = "GPS jump of ${String.format(Locale.US, "%.1f", distanceMiles)} mi in ${String.format(Locale.US, "%.1f", (time2Ms - time1Ms)/1000.0)}s (${String.format(Locale.US, "%.0f", speedMph)} mph).",
                severity = "WARNING",
                isFlaggedForReview = false
            )
            return false
        }
        return true
    }

    /**
     * Checkout Price Mismatch Guard:
     * Checks if actual store receipt total exceeds expected total by >20% or >$15.
     */
    fun evaluatePriceDiscrepancy(expectedTotal: Double, actualTotal: Double): Pair<Boolean, String?> {
        val diff = actualTotal - expectedTotal
        if (diff > 15.00 || (expectedTotal > 0 && (diff / expectedTotal) > 0.20)) {
            val reason = "Actual total ($${String.format(Locale.US, "%.2f", actualTotal)}) is $${String.format(Locale.US, "%.2f", diff)} higher than expected ($${String.format(Locale.US, "%.2f", expectedTotal)}). Auto-flagged for item review."
            recordAuditLog(
                actionType = "PRICE_DISCREPANCY_FLAG",
                details = reason,
                severity = "WARNING",
                isFlaggedForReview = true
            )
            return Pair(true, reason)
        }
        return Pair(false, null)
    }

    /**
     * Records a secure internal audit log entry.
     */
    fun recordAuditLog(
        actionType: String,
        details: String,
        severity: String = "INFO",
        isFlaggedForReview: Boolean = false
    ) {
        synchronized(auditLogs) {
            auditLogs.add(
                SecurityAuditEntry(
                    timestamp = System.currentTimeMillis(),
                    actionType = actionType,
                    details = details,
                    severity = severity,
                    isFlaggedForReview = isFlaggedForReview
                )
            )
            if (auditLogs.size > 200) {
                auditLogs.removeAt(0)
            }
        }
    }

    fun getAuditLogs(): List<SecurityAuditEntry> {
        return synchronized(auditLogs) { auditLogs.toList() }
    }
}
