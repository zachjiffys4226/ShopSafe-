package com.example.shopsafe.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsafe.data.models.StripeCardTokenizedDetails
import com.example.shopsafe.data.models.StripeIssuingCard
import com.example.shopsafe.data.models.StripeIssuingEphemeralKey
import com.example.shopsafe.data.models.StripePushProvisioningPayload
import com.example.shopsafe.data.stripe.IStripeService
import com.example.shopsafe.data.stripe.StripePaymentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State data class for StripePaymentViewModel
 */
data class StripePaymentUiState(
    val isSdkReady: Boolean = false,
    val isLoading: Boolean = false,
    val ephemeralKey: StripeIssuingEphemeralKey? = null,
    val tokenizedCardDetails: StripeCardTokenizedDetails? = null,
    val pushProvisioningPayload: StripePushProvisioningPayload? = null,
    val errorMessage: String? = null,
    val lastInitializedTimestamp: Long? = null
)

/**
 * StripePaymentViewModel injects IStripeService (and optional StripePaymentProvider),
 * triggers ephemeral key retrieval upon UI initialization, and updates Compose state
 * to signal when the Stripe SDK is ready for secure Issuing card management operations.
 */
class StripePaymentViewModel(
    private val stripeService: IStripeService,
    private val paymentProvider: StripePaymentProvider? = null
) : ViewModel() {

    // Flow State
    private val _uiState = MutableStateFlow(StripePaymentUiState())
    val uiState: StateFlow<StripePaymentUiState> = _uiState.asStateFlow()

    // Compose State for direct Jetpack Compose observation
    private val _isStripeSdkReady = mutableStateOf(false)
    val isStripeSdkReady: State<Boolean> = _isStripeSdkReady

    private val _isDetailsRevealed = mutableStateOf(false)
    val isDetailsRevealed: State<Boolean> = _isDetailsRevealed

    /**
     * Initializes the Stripe SDK for the UI by fetching a fresh ephemeral key from the backend.
     * Once retrieved, the key is attached to the provider and signals readiness via Compose state.
     */
    fun initializePaymentUI(
        customerId: String = "cus_default_driver",
        cardId: String? = null,
        cardholderId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val keyResult = if (!cardId.isNullOrEmpty() && !cardholderId.isNullOrEmpty()) {
                    stripeService.getIssuingEphemeralKey(cardId = cardId, cardholderId = cardholderId)
                } else {
                    stripeService.getEphemeralKey(customerId = customerId)
                }

                if (keyResult.isSuccess) {
                    val ephemeralKey = keyResult.getOrThrow()
                    paymentProvider?.attachEphemeralKey(
                        ephemeralKeySecret = ephemeralKey.secret,
                        cardId = cardId ?: ephemeralKey.cardId
                    )

                    _isStripeSdkReady.value = true
                    _uiState.update {
                        it.copy(
                            isSdkReady = true,
                            isLoading = false,
                            ephemeralKey = ephemeralKey,
                            lastInitializedTimestamp = System.currentTimeMillis(),
                            errorMessage = null
                        )
                    }
                } else {
                    val errorMsg = keyResult.exceptionOrNull()?.message ?: "Failed to retrieve ephemeral key"
                    paymentProvider?.recordError(errorMsg)
                    _isStripeSdkReady.value = false
                    _uiState.update {
                        it.copy(
                            isSdkReady = false,
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unexpected error initializing Stripe SDK"
                paymentProvider?.recordError(errorMsg)
                _isStripeSdkReady.value = false
                _uiState.update {
                    it.copy(
                        isSdkReady = false,
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Reveals tokenized card details in-memory for driver inspection.
     * Emits tokenized details while ensuring raw credentials are never stored on device.
     */
    fun revealIssuingCardDetails(card: StripeIssuingCard) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = stripeService.fetchSecureTokenizedCardDetails(card)
            if (result.isSuccess) {
                val details = result.getOrThrow()
                _isDetailsRevealed.value = true
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tokenizedCardDetails = details
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to decrypt card details"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Generates encrypted OPCS push provisioning payload for contactless Google Pay integration.
     */
    fun pushProvisionToGooglePay(card: StripeIssuingCard) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = stripeService.pushProvisionToGooglePay(card)
            if (result.isSuccess) {
                val payload = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pushProvisioningPayload = payload
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Google Pay provisioning failed"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Purges sensitive tokenized data and resets UI reveal state.
     */
    fun clearSensitiveData() {
        stripeService.wipeInMemoryCardDetails()
        paymentProvider?.clearEphemeralState()
        _isDetailsRevealed.value = false
        _uiState.update {
            it.copy(
                tokenizedCardDetails = null,
                pushProvisioningPayload = null
            )
        }
    }
}
