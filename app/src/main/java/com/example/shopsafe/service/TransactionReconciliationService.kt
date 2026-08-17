package com.example.shopsafe.service

import android.util.Log
import com.example.shopsafe.data.models.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * TransactionReconciliationService provides automated backend reconciliation between
 * Stripe payment transactions and ShopSafe orders, flagging any financial discrepancies
 * exceeding $0.01 for admin review and auditing.
 */
class TransactionReconciliationService {

    companion object {
        private const val TAG = "TransactionReconciliationService"
        private const val DISCREPANCY_THRESHOLD = 0.01
    }

    data class StripeTransactionRecord(
        val stripeChargeId: String,
        val orderId: String,
        val stripeAmount: Double,
        val currency: String,
        val status: String, // succeeded, pending, refunded, failed
        val customerEmail: String,
        val timestamp: Long
    )

    data class ReconciliationDiscrepancy(
        val discrepancyId: String,
        val orderId: String,
        val stripeChargeId: String,
        val stripeAmount: Double,
        val orderTotal: Double,
        val difference: Double,
        val reason: String,
        val flaggedAt: Long,
        val status: String = "PENDING_ADMIN_REVIEW" // PENDING_ADMIN_REVIEW, RESOLVED, WAIVED
    )

    data class ReconciliationReport(
        val reportId: String,
        val totalStripeTransactionsChecked: Int,
        val totalOrdersChecked: Int,
        val matchedCount: Int,
        val discrepanciesFlagged: List<ReconciliationDiscrepancy>,
        val executedAt: Long
    )

    /**
     * Reconciles a list of Stripe transactions against ShopSafe orders.
     * Flags any discrepancies where |stripeAmount - orderTotal| > $0.01.
     */
    suspend fun reconcileTransactions(
        stripeTransactions: List<StripeTransactionRecord>,
        shopSafeOrders: List<Order>
    ): ReconciliationReport = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting automated transaction reconciliation: ${stripeTransactions.size} Stripe txns, ${shopSafeOrders.size} ShopSafe orders")

        val orderMap = shopSafeOrders.associateBy { it.id }
        val discrepancies = mutableListOf<ReconciliationDiscrepancy>()
        var matchedCount = 0

        for (txn in stripeTransactions) {
            val order = orderMap[txn.orderId]
            if (order == null) {
                // Orphaned Stripe transaction with no matching order in database
                discrepancies.add(
                    ReconciliationDiscrepancy(
                        discrepancyId = "disc_" + UUID.randomUUID().toString().replace("-", "").take(12),
                        orderId = txn.orderId.ifBlank { "UNKNOWN_ORDER" },
                        stripeChargeId = txn.stripeChargeId,
                        stripeAmount = txn.stripeAmount,
                        orderTotal = 0.0,
                        difference = txn.stripeAmount,
                        reason = "Orphaned Stripe transaction: No matching ShopSafe order found.",
                        flaggedAt = System.currentTimeMillis()
                    )
                )
                continue
            }

            val diff = kotlin.math.abs(txn.stripeAmount - order.total)
            if (diff > DISCREPANCY_THRESHOLD) {
                val reason = if (txn.stripeAmount > order.total) {
                    "Stripe charge ($${txn.stripeAmount}) exceeds order total ($${order.total}) by $${String.format(java.util.Locale.US, "%.2f", diff)}"
                } else {
                    "Order total ($${order.total}) exceeds Stripe charge ($${txn.stripeAmount}) by $${String.format(java.util.Locale.US, "%.2f", diff)}"
                }

                discrepancies.add(
                    ReconciliationDiscrepancy(
                        discrepancyId = "disc_" + UUID.randomUUID().toString().replace("-", "").take(12),
                        orderId = order.id,
                        stripeChargeId = txn.stripeChargeId,
                        stripeAmount = txn.stripeAmount,
                        orderTotal = order.total,
                        difference = diff,
                        reason = reason,
                        flaggedAt = System.currentTimeMillis()
                    )
                )
                Log.w(TAG, "Discrepancy flagged for Order ${order.id}: $reason")
            } else {
                matchedCount++
            }
        }

        val report = ReconciliationReport(
            reportId = "rep_" + UUID.randomUUID().toString().replace("-", "").take(12),
            totalStripeTransactionsChecked = stripeTransactions.size,
            totalOrdersChecked = shopSafeOrders.size,
            matchedCount = matchedCount,
            discrepanciesFlagged = discrepancies,
            executedAt = System.currentTimeMillis()
        )

        Log.d(TAG, "Reconciliation complete. Checked=${report.totalStripeTransactionsChecked}, Matched=$matchedCount, FlaggedDiscrepancies=${discrepancies.size}")
        return@withContext report
    }
}
