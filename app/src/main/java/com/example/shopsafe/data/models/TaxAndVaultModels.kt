package com.example.shopsafe.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Types of delivery media stored securely in the ShopSafe Delivery Media Vault.
 */
enum class DeliveryMediaType {
    PICKUP_CONFIRMATION,
    CHECKOUT_REGISTER,
    STORE_RECEIPT,
    DELIVERY_CONDITION,
    DROPOFF_PROOF,
    DELIVERY_VIDEO,
    EXCEPTION_DAMAGE
}

/**
 * An individual media file within the ShopSafe Delivery Media Vault.
 * Organized by Order -> Date -> Customer/Order -> File Type with strict RBAC.
 */
data class DeliveryMediaFile(
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val orderNumber: String,
    val fileType: DeliveryMediaType,
    val fileUrl: String,
    val thumbnailUri: String = "",
    val uploadDate: String,
    val captureTimestamp: Long = System.currentTimeMillis(),
    val driverId: String = "driver_101",
    val storeOrCustomerName: String = "",
    val verificationStatus: String = "VERIFIED", // PENDING, VERIFIED, FLAGGED, REJECTED
    val fileSizeBytes: Long = 1024 * 512,
    val notes: String = "",
    val retentionExpiryDays: Int = 365,
    val retentionExpiryTimestamp: Long = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
)

/**
 * Grouped Delivery Order Media Container in the Vault.
 */
data class DeliveryOrderVault(
    val orderId: String,
    val orderNumber: String,
    val customerName: String,
    val storeName: String,
    val deliveryDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaFiles: List<DeliveryMediaFile> = emptyList()
)

/**
 * Legitimate driver business expense categories.
 */
enum class DriverExpenseCategory(val displayName: String, val isTypicalTaxWriteOff: Boolean) {
    FUEL("Fuel & Gas", true),
    VEHICLE_MAINTENANCE("Vehicle Maintenance", true),
    REPAIRS("Repairs & Service", true),
    TIRES("Tires & Alignment", true),
    PARKING("Parking Fees", true),
    TOLLS("Tolls & Express Lanes", true),
    DELIVERY_SUPPLIES("Insulated Bags & Supplies", true),
    PHONE_SERVICE("Phone & Data Service", true),
    BUSINESS_EQUIPMENT("Dashcam & Car Mounts", true),
    PROFESSIONAL_SERVICES("Accounting & Legal", true),
    MEALS_TRAVEL("Travel & Meals (Rules Apply)", false),
    OTHER("Other Business Expense", false)
}

/**
 * Extracted OCR data from receipt scanning.
 * Note: The original untouched image is ALWAYS preserved separately.
 */
data class ReceiptOcrResult(
    val merchant: String = "",
    val dateString: String = "",
    val subtotal: Double = 0.0,
    val salesTax: Double = 0.0,
    val tip: Double = 0.0,
    val total: Double = 0.0,
    val receiptNumber: String = "",
    val detectedCategory: DriverExpenseCategory = DriverExpenseCategory.FUEL,
    val confidenceScore: Float = 0.95f
)

/**
 * Driver Expense Record with full audit trail, original receipt preservation,
 * duplicate detection, and tax categorization.
 */
@Entity(tableName = "driver_expenses")
data class DriverExpense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driverId: String = "driver_101",
    val merchant: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = "Aug 15, 2026",
    val amount: Double,
    val subtotal: Double = amount,
    val salesTax: Double = 0.0,
    val tip: Double = 0.0,
    val paymentMethod: String = "ShopSafe Driver Card (...4021)",
    val category: DriverExpenseCategory = DriverExpenseCategory.FUEL,
    val isPotentialTaxWriteOff: Boolean = true,
    val taxDisclaimerNote: String = "Potential Business Expense - Consult Tax Advisor",
    val notes: String = "",
    val originalReceiptUri: String, // Critical: untouched original photo is NEVER replaced
    val receiptNumber: String = "",
    val orderId: String? = null,
    val mileageLogId: String? = null,
    val uploadedTimestamp: Long = System.currentTimeMillis(),
    val editedTimestamp: Long = System.currentTimeMillis(),
    val verificationStatus: String = "VERIFIED", // VERIFIED, PENDING_REVIEW, FLAGGED
    val quickbooksSyncStatus: String = "SYNCED", // NOT_SYNCED, SYNCED, PENDING, ERROR
    val quickbooksRecordId: String? = "QB-EXP-${UUID.randomUUID().toString().take(8)}",
    val isFlaggedDuplicate: Boolean = false
)

/**
 * Separate Business Accounting Expense System for ShopSafe Owner / Company.
 * Completely isolated from driver records with QuickBooks synchronization.
 */
enum class BusinessExpenseCategory(val displayName: String) {
    SOFTWARE_SAAS("Software & Cloud Subscriptions"),
    ADVERTISING_MARKETING("Advertising & Marketing"),
    BUSINESS_SERVICES("Business Services & APIs"),
    OFFICE_SUPPLIES("Office & Logistics Supplies"),
    CONTRACTOR_PAYOUTS("Contractor & Fleet Payouts"),
    EQUIPMENT_HARDWARE("Hardware & Equipment"),
    LEGAL_PROFESSIONAL("Legal & Accounting Fees"),
    MERCHANT_PROCESSING("Payment Processing Fees"),
    OTHER_COMPANY("Other Company Expense")
}

@Entity(tableName = "business_expenses")
data class BusinessExpense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val companyEntity: String = "ShopSafe Technologies Inc.",
    val merchant: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = "Aug 15, 2026",
    val amount: Double,
    val category: BusinessExpenseCategory = BusinessExpenseCategory.SOFTWARE_SAAS,
    val notes: String = "",
    val originalReceiptUri: String, // Untouched company receipt photo
    val quickbooksRecordId: String = "QB-CO-${UUID.randomUUID().toString().take(8)}",
    val quickbooksSyncStatus: String = "SYNCED",
    val uploadedByUserId: String = "admin_owner",
    val uploadedTimestamp: Long = System.currentTimeMillis(),
    val taxDeductibleCategory: String = "Operating Expense (Schedule C / 1120-S)"
)

/**
 * Driver Tax Document container for 1099-NEC, Year-End summaries, and W-9.
 */
data class DriverTaxDocument(
    val id: String = UUID.randomUUID().toString(),
    val driverId: String = "driver_101",
    val title: String,
    val documentType: String, // 1099_NEC, ANNUAL_EXPENSE_PACKAGE, MILEAGE_SUMMARY, W9, QUARTERLY_REPORT
    val taxYear: Int = 2026,
    val fileUrl: String,
    val fileSizeFormatted: String = "2.4 MB",
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val isAvailableForDownload: Boolean = true
)

/**
 * Comprehensive Year-End Driver Tax & Expense Package.
 */
data class YearEndTaxPackage(
    val driverId: String = "driver_101",
    val driverName: String = "Alex Rivera",
    val taxYear: Int = 2026,
    val totalGrossEarnings: Double = 14250.00,
    val completedDeliveries: Int = 590,
    val totalDeliveryMiles: Double = 3120.4,
    val totalIrsMileageDeduction: Double = 2090.67, // 3120.4 * $0.67
    val totalRecordedExpenses: Double = 1845.50,
    val totalPotentialTaxWriteOffs: Double = 1680.00,
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val receiptCount: Int = 42,
    val generatedDate: String = "Aug 15, 2026",
    val taxComplianceDisclaimer: String = "ShopSafe is not a licensed tax professional or CPA. Information provided is an organized business record summary to assist in tax preparation."
)
