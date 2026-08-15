package com.aistudio.shopsafe.payment

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Retrofit API
interface StripeApiService {
    @POST("/create-payment-intent")
    suspend fun createPaymentIntent(@Body body: Map<String, Any>): Map<String, String>
}

class PaymentViewModel : ViewModel() {
    var publishableKey by mutableStateOf("")
    var clientSecret by mutableStateOf<String?>(null)
    var paymentResult by mutableStateOf<String?>(null)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://us-central1-YOUR_PROJECT.cloudfunctions.net/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(StripeApiService::class.java)

    fun initialize(publishableKey: String) {
        this.publishableKey = publishableKey
        PaymentConfiguration.init(/* context= */ null as android.content.Context, publishableKey)
    }

    fun createPayment(amount: Int = 500) {
        viewModelScope.launch {
            try {
                val resp = api.createPaymentIntent(mapOf("amount" to amount, "currency" to "usd"))
                clientSecret = resp["clientSecret"]
            } catch (e: Exception) {
                Log.e("PaymentVM", "Error creating payment intent", e)
                paymentResult = "Error creating payment intent: ${e.message}"
            }
        }
    }
}

@Composable
fun PaymentScreen(viewModel: PaymentViewModel) {
    Column(Modifier.padding(16.dp)) {
        Button(onClick = { viewModel.createPayment() }) {
            Text("Create Payment")
        }

        Spacer(Modifier.height(8.dp))

        viewModel.clientSecret?.let { clientSecret ->
            // Present PaymentSheet when clientSecret is available
            var presentSheet by remember { mutableStateOf(false) }
            val paymentSheet = remember { PaymentSheet(/* activity = */ null as androidx.fragment.app.FragmentActivity, ::onPaymentSheetResult) }
            Button(onClick = { presentSheet = true }) {
                Text("Pay")
            }
            if (presentSheet) {
                // Incomplete: actual PaymentSheet present needs an Activity context and configuration
            }
        }

        viewModel.paymentResult?.let { result ->
            Text(result)
        }
    }
}

fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
    when (paymentResult) {
        is PaymentSheetResult.Completed -> {
            // Handle success
            // update UI/state
        }
        is PaymentSheetResult.Canceled -> {
            // Handle cancel
        }
        is PaymentSheetResult.Failed -> {
            // Handle failure
        }
    }
}
