package com.aistudio.shopsafe.repository

import com.aistudio.shopsafe.network.StripeApiService

class PaymentRepository(private val api: StripeApiService) {
    suspend fun createPayment(amount: Int) = api.createPaymentIntent(mapOf("amount" to amount, "currency" to "usd"))
}
