package com.aistudio.shopsafe.network

import retrofit2.http.Body
import retrofit2.http.POST

interface StripeApiService {
    @POST("/create-payment-intent")
    suspend fun createPaymentIntent(@Body body: Map<String, Any>): Map<String, String>
}
