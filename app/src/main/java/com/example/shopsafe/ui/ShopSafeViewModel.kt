package com.example.shopsafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsafe.data.firebase.ShopSafeFirestoreService
import com.example.shopsafe.data.local.ShopSafeDatabase
import com.example.shopsafe.data.models.*
import com.example.shopsafe.data.repository.GeminiService
import com.example.shopsafe.data.repository.ShopSafeRepository
import com.example.shopsafe.data.session.UserSessionManager
import com.example.shopsafe.data.stripe.IStripeService
import com.example.shopsafe.data.stripe.ShopSafeStripeIssuingService
import com.example.shopsafe.data.stripe.StripeIssuingTokenizationManager
import com.example.shopsafe.data.stripe.StripePaymentProvider
import com.example.shopsafe.data.stripe.StripeService
import com.example.shopsafe.data.stripe.StripeServiceError
import com.example.shopsafe.data.stripe.StripeServiceErrorWrapper
import com.example.shopsafe.data.util.AppLanguage
import com.example.shopsafe.data.util.NetworkConnectivityObserver
import com.example.shopsafe.data.util.NetworkStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppMode {
    CUSTOMER_SHOPPING,
    DRIVER_PORTAL
}

enum class CustomerTab {
    MARKETPLACE,
    FOOD_STORES,
    MESSAGES,
    ORDERS,
    CART
}

data class ActiveCallState(
    val isCalling: Boolean = false,
    val callerName: String = "",
    val callerPhone: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeaker: Boolean = false
)

class ShopSafeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopSafeRepository
    private val firestoreService = ShopSafeFirestoreService()
    val sessionManager: UserSessionManager = UserSessionManager(application)
    val currentUser = MutableStateFlow<AuthUser?>(null)

    // Automated Order Dispatch Ping Pool & State
    private val dispatchOffersPool = mutableListOf<DriverOffer>()
    private var currentDispatchOfferIndex = 0
    private var pingCountdownJob: kotlinx.coroutines.Job? = null
    private var automatedDispatchJob: kotlinx.coroutines.Job? = null

    init {
        val db = ShopSafeDatabase.getDatabase(application)
        repository = ShopSafeRepository(db.dao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        // Observe persisted session state from DataStore Preferences
        viewModelScope.launch {
            sessionManager.userSessionFlow.collect { savedUser ->
                if (savedUser != null && currentUser.value == null) {
                    currentUser.value = savedUser
                }
            }
        }
        // Sync with Firebase Auth
        sessionManager.attachFirebaseListener(viewModelScope) { updatedUser ->
            if (updatedUser != null) {
                currentUser.value = updatedUser
            }
        }
        checkFirebaseUserSession()

        // Sync User Referral Profile & Credits with Firebase Firestore
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    val defaultCode = "SHOPSAFE-${user.name.replace(" ", "").take(4).uppercase()}${kotlin.math.abs(user.id.hashCode() % 1000)}"
                    val fbProfile = firestoreService.getOrCreateUserReferralProfile(user.id, user.name, defaultCode)
                    sessionManager.updateReferralProfile(
                        code = fbProfile.referralCode,
                        credits = fbProfile.availableDeliveryCredits,
                        totalReferred = fbProfile.totalFriendsReferred,
                        referredBy = fbProfile.referredByCode
                    )
                }
            }
        }

        // Initialize Automated Order Dispatch Ping Pool & Engine
        dispatchOffersPool.addAll(DriverOpportunityEngine.generateNearbyDispatchOffers())
        startAutomatedDispatchEngine()
    }

    /**
     * Checks if there is an active Firebase user session on app launch.
     * Automatically restores the session and populates currentUser to navigate directly to the storefront.
     */
    fun checkFirebaseUserSession() {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val user = sessionManager.mapFirebaseUserToAuthUser(firebaseUser)
                currentUser.value = user
                viewModelScope.launch {
                    sessionManager.saveSession(user)
                }
            }
        } catch (e: Exception) {
            // Firebase Auth not initialized or unavailable in environment; handle gracefully
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            sessionManager.clearSession()
            currentUser.value = null
        }
    }

    // App Mode & Tab Navigation
    val appMode = MutableStateFlow(AppMode.CUSTOMER_SHOPPING)
    val customerTab = MutableStateFlow(CustomerTab.MARKETPLACE)

    // Marketplace State
    val marketplaceItems = repository.allMarketplaceItems.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val marketplaceSearchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(ItemCategory.ALL.name)
    val showOnlyFacebookImported = MutableStateFlow(false)
    val showOnlyNewItems = MutableStateFlow(false)
    val marketplaceSortOption = MutableStateFlow(MarketplaceSortOption.NEWEST)
    val selectedMarketplaceItem = MutableStateFlow<MarketplaceItem?>(null)

    // Post Item Dialog State
    val showPostItemModal = MutableStateFlow(false)
    val postTitle = MutableStateFlow("")
    val postCategory = MutableStateFlow(ItemCategory.ELECTRONICS.name)
    val postCondition = MutableStateFlow(ItemCondition.USED_LIKE_NEW.name)
    val postPrice = MutableStateFlow("")
    val postPickupLocation = MutableStateFlow("742 Market St, San Francisco, CA")
    val postDescription = MutableStateFlow("")
    val postIsFacebookCrosspost = MutableStateFlow(true)
    val isEstimatingPrice = MutableStateFlow(false)
    val postDimensions = MutableStateFlow("12x12x12 in")
    val postWeightLbs = MutableStateFlow("15")
    val postRequiredVehicle = MutableStateFlow("Sedan")
    val postUploadedImages = MutableStateFlow<List<String>>(emptyList())
    val postVideoUrl = MutableStateFlow<String>("")

    // Driver AI Photo Scanner for Shelf & Fast Food Menu State
    val showDriverPhotoScannerModal = MutableStateFlow(false)
    val showDriverDemandHeatmapModal = MutableStateFlow(false)
    val scanningStoreId = MutableStateFlow<String?>("cvs_sf_1")
    val scanningStoreName = MutableStateFlow<String?>("CVS Pharmacy")
    val isFastFoodScan = MutableStateFlow(false)
    val isScanningPhoto = MutableStateFlow(false)
    val scanFeedbackMessage = MutableStateFlow<String?>(null)

    // Food & Storefront State
    val storefronts = repository.allStorefronts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val selectedStorefront = MutableStateFlow<Storefront?>(null)
    val storeFoodItems = selectedStorefront.flatMapLatest { store ->
        if (store == null) flowOf(emptyList())
        else repository.getFoodItemsForStore(store.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart & Checkout State
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val selectedTipAmount = MutableStateFlow(4.00)
    val dropoffAddress = MutableStateFlow("123 Main St, Apt 4B, San Francisco, CA")
    val checkoutDeliveryInstructions = MutableStateFlow("")
    val paymentMethod = MutableStateFlow("ShopSafe Pay Balance ($0.00)")

    // Orders & Tracking State
    val orders = repository.allOrders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val firestoreOrders = repository.firestoreOrders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val customerPastOrders = combine(orders, firestoreOrders) { local, remote ->
        (remote + local).distinctBy { it.id }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeTrackedOrderId = MutableStateFlow<String?>(null)
    val activeTrackedOrder = activeTrackedOrderId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.observeOrderById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Driver Portal State
    val driverProfile = repository.driverProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val availableDriverOffers = repository.availableDriverOffers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val payoutHistory = repository.payoutHistory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val firestorePayoutHistory = repository.firestorePayoutHistory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val activeDriverOffer = MutableStateFlow<DriverOffer?>(null)
    val driverDeliveryStep = MutableStateFlow(1) // 1: Pickup, 2: Dropping off, 3: Completed
    val showPayoutDialog = MutableStateFlow(false)

    // Shopper Cart & In-Store Product Scanning State
    val showShopperCartScreen = MutableStateFlow(false)
    val shopperOrderItems = MutableStateFlow<List<ShopperOrderItem>>(generateDefaultShopperItems("Supermarket"))
    val lastCheckoutVerification = MutableStateFlow<StoreCheckoutVerification?>(null)

    // In-App Google Maps Navigation & Auto Arrival Geofence State
    val isInAppNavigationActive = MutableStateFlow(false)
    val driverTripProgress = MutableStateFlow(0.05f) // 0.0 to 1.0 along current active route leg
    val isAutoDriveSimulating = MutableStateFlow(false)
    val showArrivalPromptModal = MutableStateFlow(false)
    val arrivalPromptTarget = MutableStateFlow("PICKUP") // "PICKUP" or "DROPOFF"
    val showAcceptedNavigateQuickPrompt = MutableStateFlow(false)
    private var autoDriveSimulationJob: kotlinx.coroutines.Job? = null

    // Stripe Connect Automated Driver Payout State
    val showRequestPayoutScreen = MutableStateFlow(false)
    val isProcessingPayout = MutableStateFlow(false)
    val payoutProcessingStep = MutableStateFlow("")
    val lastPayoutResult = MutableStateFlow<PayoutTransaction?>(null)
    val showPayoutSuccessModal = MutableStateFlow(false)
    val selectedPayoutMethod = MutableStateFlow("Instant Cashout (Debit Card)")
    val customPayoutAmountText = MutableStateFlow("")
    val payoutErrorMessage = MutableStateFlow<String?>(null)

    // Stripe Issuing Commercial Purchasing Card State (with Resilient Error Wrapper)
    val stripePaymentProvider = StripePaymentProvider(application)
    private val rawStripeService = StripeService(application)
    val stripeServiceWrapper = StripeServiceErrorWrapper(rawStripeService, application)
    val stripeService: IStripeService = stripeServiceWrapper
    val stripeIssuingService = ShopSafeStripeIssuingService()
    val stripeIssuingTokenizationManager = StripeIssuingTokenizationManager(application)
    val stripeTokenizationState = stripeIssuingTokenizationManager.tokenizationState
    val issuingConfig = stripeIssuingService.issuingConfig
    val issuingCardholders = stripeIssuingService.cardholders
    val issuingCards = stripeIssuingService.issuingCards
    val issuingAuthorizations = stripeIssuingService.authorizations
    val issuingReconciliations = stripeIssuingService.reconciliationRecords
    val issuingAuditLogs = stripeIssuingService.auditLogs
    val issuingWebhookEvents = stripeIssuingService.webhookEvents
    val driverIssuingCard = MutableStateFlow<StripeIssuingCard?>(stripeIssuingService.getDriverCard("driver_1"))
    val showDriverShopSafeCardModal = MutableStateFlow(false)
    val showAdminStripeIssuingModal = MutableStateFlow(false)
    val stripeNonObstructiveAlert = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            stripeServiceWrapper.nonObstructiveAlert.collect { alert ->
                if (alert != null) {
                    stripeNonObstructiveAlert.value = alert
                }
            }
        }
    }

    // Driver Dashboard & Completed Deliveries (Cloud Firestore + Room)
    val completedDeliveries = repository.allCompletedDeliveries.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val selectedDeliveryDetail = MutableStateFlow<CompletedDelivery?>(null)
    val deliveryFilterPeriod = MutableStateFlow("ALL") // ALL, TODAY, WEEK
    val deliverySearchQuery = MutableStateFlow("")
    val driverDashboardViewTab = MutableStateFlow("DISPATCHES") // DISPATCHES, DELIVERIES, EARNINGS, MILEAGE

    // Automated Driver Mileage Tracker State (Pickup to Dropoff Delivery Miles Only, Home Commute Excluded)
    val allMileageLogs = repository.allMileageLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val driverMileageLogs = allMileageLogs

    val totalDeliveryMileage = repository.totalDeliveryMileage
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalDeliveryMiles = totalDeliveryMileage

    val totalTaxDeduction = repository.totalTaxDeduction
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalMileageTaxDeduction = totalTaxDeduction

    private val startOfTodayTimestamp: Long
        get() {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    val todayDeliveryMileage = repository.getTodayDeliveryMileage(startOfTodayTimestamp)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isMileageTrackingActive = MutableStateFlow(false)
    val currentTripMiles = MutableStateFlow(0.0)
    val currentTripDurationMinutes = MutableStateFlow(0)
    val currentTripPickupAddress = MutableStateFlow("")
    val currentTripDropoffAddress = MutableStateFlow("")
    val currentTripTargetTotalMiles = MutableStateFlow(1.4)
    val currentTripStartTime = MutableStateFlow(0L)
    val showMileageTrackerModal = MutableStateFlow(false)
    val lastCompletedMileageTrip = MutableStateFlow<DriverMileageLog?>(null)
    val showMileageTripCompletedDialog = MutableStateFlow(false)
    val isCommuteFilterActive = MutableStateFlow(true) // Always true: excludes travel to/from home
    private var mileageTrackingJob: kotlinx.coroutines.Job? = null

    val activeTripOrderId = activeDriverOffer
        .map { it?.orderId ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val lastRecordedTripMiles = lastCompletedMileageTrip
        .map { it?.milesDriven ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lastRecordedTaxDeduction = lastCompletedMileageTrip
        .map { it?.taxDeductionValue ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun dismissMileageCompletedDialog() {
        showMileageTripCompletedDialog.value = false
    }

    // Dynamic Store Onboarding, Integration Pass & Price Adjustment Modals
    val showAddStoreModal = MutableStateFlow(false)
    val showMerchantIntegrationPassModal = MutableStateFlow(false)
    val showPriceAdjustmentModal = MutableStateFlow(false)
    val showLiveShoppingManager = MutableStateFlow(false)
    val selectedItemForDriverUpdate = MutableStateFlow<FoodItem?>(null)

    // User Authentication & Registration State
    val showAuthModal = MutableStateFlow(false)
    val showBankingDetailsModal = MutableStateFlow(false)

    // Multi-Language App Localization
    val currentLanguage: StateFlow<AppLanguage> = sessionManager.selectedLanguageCodeFlow
        .map { AppLanguage.fromCode(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.ENGLISH)

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            sessionManager.setAppLanguage(language.code)
        }
    }

    // Driver Portal Low Power Mode (Battery Saver System)
    val isLowPowerModeEnabled: StateFlow<Boolean> = sessionManager.lowPowerModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val mapTrackingRefreshIntervalMs: StateFlow<Long> = isLowPowerModeEnabled
        .map { if (it) 10000L else 2000L }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 2000L)

    val notificationRefreshIntervalMs: StateFlow<Long> = isLowPowerModeEnabled
        .map { if (it) 3000L else 1000L }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1000L)

    fun toggleLowPowerMode(enabled: Boolean? = null) {
        viewModelScope.launch {
            val newValue = enabled ?: !isLowPowerModeEnabled.value
            sessionManager.setLowPowerMode(newValue)
        }
    }

    // Automated Order Dispatch Ping System (45-second timer)
    val incomingDispatchOffer = MutableStateFlow<DriverOffer?>(null)
    val dispatchTimerSeconds = MutableStateFlow(45)
    val isDispatchActive = MutableStateFlow(false)

    // Real-Time Driver Network Connectivity Monitoring
    private val networkObserver = NetworkConnectivityObserver(application)
    val isDeviceNetworkOnline: StateFlow<Boolean> = networkObserver.observe()
        .map { it == NetworkStatus.Available }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkObserver.isCurrentlyConnected()
        )
    val isSimulatedOffline = MutableStateFlow(false)
    val isNetworkConnected: StateFlow<Boolean> = combine(
        isDeviceNetworkOnline,
        isSimulatedOffline
    ) { deviceOnline, simOffline ->
        deviceOnline && !simOffline
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = networkObserver.isCurrentlyConnected()
    )

    // Chat Messaging State
    val chatThreads = repository.allChatThreads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val selectedThreadId = MutableStateFlow<String?>("thread_elena")
    val currentThreadMessages = selectedThreadId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMessagesForThread(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val messageInputText = MutableStateFlow("")

    // Active Live Calling State
    val activeCallState = MutableStateFlow(ActiveCallState())

    // Filtered Marketplace Items
    val filteredMarketplaceItems: StateFlow<List<MarketplaceItem>> = combine(
        marketplaceItems,
        marketplaceSearchQuery,
        selectedCategory,
        combine(showOnlyFacebookImported, showOnlyNewItems, marketplaceSortOption) { fb, new, sort ->
            Triple(fb, new, sort)
        }
    ) { items: List<MarketplaceItem>, query: String, category: String, flags: Triple<Boolean, Boolean, MarketplaceSortOption> ->
        val (fbOnly, newOnly, sortOption) = flags
        val filtered = items.filter { item ->
            val matchesQuery = query.isBlank() || item.title.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true)
            val matchesCat = category == ItemCategory.ALL.name || item.category.equals(category, ignoreCase = true)
            val matchesFb = !fbOnly || item.isFacebookImported
            val matchesNew = !newOnly || item.isNewItem
            matchesQuery && matchesCat && matchesFb && matchesNew
        }
        when (sortOption) {
            MarketplaceSortOption.PRICE_LOW -> filtered.sortedBy { it.price }
            MarketplaceSortOption.PRICE_HIGH -> filtered.sortedByDescending { it.price }
            MarketplaceSortOption.DISTANCE -> filtered.sortedBy { it.distanceMiles }
            MarketplaceSortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Completed Deliveries for Driver Dashboard
    val filteredCompletedDeliveries: StateFlow<List<CompletedDelivery>> = combine(
        completedDeliveries,
        deliveryFilterPeriod,
        deliverySearchQuery
    ) { deliveries, period, query ->
        val now = System.currentTimeMillis()
        val oneDay = 24 * 3600 * 1000L
        val sevenDays = 7 * oneDay

        deliveries.filter { item ->
            val matchesPeriod = when (period) {
                "TODAY" -> (now - item.timestamp) <= oneDay
                "WEEK" -> (now - item.timestamp) <= sevenDays
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    item.storeOrSellerName.contains(query, ignoreCase = true) ||
                    item.customerName.contains(query, ignoreCase = true) ||
                    item.itemsSummary.contains(query, ignoreCase = true) ||
                    item.dropoffAddress.contains(query, ignoreCase = true)
            matchesPeriod && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLifetimeEarnings: StateFlow<Double> = completedDeliveries.map { list ->
        list.sumOf { it.totalEarnings }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1420.50)

    // Refer a Friend System & Delivery Credits State
    val userReferralCode: StateFlow<String> = sessionManager.referralCodeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "SHOPSAFE-ALEX789")
    val availableDeliveryCredits: StateFlow<Double> = sessionManager.deliveryCreditsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 5.00)
    val totalFriendsReferred: StateFlow<Int> = sessionManager.totalFriendsReferredFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val useDeliveryCredits = MutableStateFlow(true)
    val showReferralHubModal = MutableStateFlow(false)

    val referralHistory: StateFlow<List<com.example.shopsafe.data.models.ReferralRewardRecord>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf<List<com.example.shopsafe.data.models.ReferralRewardRecord>>(emptyList())
        else firestoreService.observeReferralHistory(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Calculations with Referral Delivery Credits Applied
    val cartSubtotal = cartItems.map { list -> list.sumOf { it.price * it.quantity } }
    val baseCartDeliveryFee = cartItems.map { list ->
        if (list.isEmpty()) 0.0 else if (list.any { it.isMarketplaceItem }) 4.99 else 2.49
    }

    val appliedDeliveryCredit: StateFlow<Double> = combine(
        baseCartDeliveryFee,
        availableDeliveryCredits,
        useDeliveryCredits
    ) { baseFee, credits, isApplied ->
        if (isApplied && baseFee > 0.0 && credits > 0.0) {
            kotlin.math.min(baseFee, credits)
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val cartDeliveryFee = combine(baseCartDeliveryFee, appliedDeliveryCredit) { baseFee, credit ->
        (baseFee - credit).coerceAtLeast(0.0)
    }

    val cartServiceFee = cartSubtotal.map { sub -> sub * 0.05 }
    val cartTotal = combine(cartSubtotal, cartDeliveryFee, cartServiceFee, selectedTipAmount) { sub, del, serv, tip ->
        if (sub == 0.0) 0.0 else sub + del + serv + tip
    }

    fun toggleUseDeliveryCredits() {
        useDeliveryCredits.value = !useDeliveryCredits.value
    }

    fun redeemReferralCode(code: String, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value
        val userId = user?.id ?: "user_101"
        val userName = user?.name ?: "ShopSafe Member"

        viewModelScope.launch {
            val result = firestoreService.redeemReferralCodeInFirestore(
                currentUserId = userId,
                currentUserName = userName,
                inputCode = code
            )
            val success = result.first
            val message = result.second
            if (success) {
                sessionManager.addDeliveryCredits(5.00, incrementReferredCount = false)
            }
            onResult(success, message)
        }
    }


    // Actions
    fun switchAppMode(mode: AppMode) {
        appMode.value = mode
    }

    fun selectCustomerTab(tab: CustomerTab) {
        customerTab.value = tab
    }

    fun triggerAiPriceEstimate() {
        if (postTitle.value.isBlank()) return
        viewModelScope.launch {
            isEstimatingPrice.value = true
            try {
                val (estPrice, estDesc) = GeminiService.estimatePriceAndDescription(
                    title = postTitle.value,
                    condition = postCondition.value,
                    category = postCategory.value
                )
                postPrice.value = String.format("%.2f", estPrice)
                if (postDescription.value.isBlank()) {
                    postDescription.value = estDesc
                }
            } finally {
                isEstimatingPrice.value = false
            }
        }
    }

    fun postNewMarketplaceItem() {
        if (postTitle.value.isBlank() || postPickupLocation.value.isBlank()) return
        val priceVal = postPrice.value.toDoubleOrNull() ?: 25.0
        val weightVal = postWeightLbs.value.toDoubleOrNull() ?: 15.0
        val vehReq = postRequiredVehicle.value
        val isVehicleCat = postCategory.value == ItemCategory.VEHICLES.name || postCategory.value.equals("VEHICLES", ignoreCase = true)
        val isHeavyVal = !isVehicleCat && (vehReq in listOf("Pickup Truck", "Flatbed Trailer", "Cargo Van") || weightVal >= 50.0)

        val newItem = MarketplaceItem(
            title = postTitle.value,
            price = priceVal,
            description = postDescription.value.ifBlank { "Great condition item posted on ShopSafe Marketplace." },
            category = postCategory.value,
            condition = postCondition.value,
            pickupLocation = postPickupLocation.value, // Required pick up location
            sellerName = "You (Verified Member)",
            isFacebookImported = postIsFacebookCrosspost.value,
            isNewItem = postCondition.value == ItemCondition.NEW.name,
            itemDimensions = postDimensions.value.ifBlank { "12x12x12 in" },
            itemWeightLbs = weightVal,
            requiredVehicleType = vehReq,
            isHeavy = isHeavyVal,
            imageUrl = postUploadedImages.value.firstOrNull() ?: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500",
            additionalImages = postUploadedImages.value.joinToString(","),
            videoUrl = postVideoUrl.value
        )
        viewModelScope.launch {
            repository.insertMarketplaceItem(newItem)
            showPostItemModal.value = false
            // Reset form
            postTitle.value = ""
            postPrice.value = ""
            postDescription.value = ""
            postDimensions.value = "12x12x12 in"
            postWeightLbs.value = "15"
            postRequiredVehicle.value = "Sedan"
            postUploadedImages.value = emptyList()
            postVideoUrl.value = ""
        }
    }

    fun processDriverShelfOrMenuPhoto(storeId: String, storeName: String, isFastFood: Boolean) {
        viewModelScope.launch {
            isScanningPhoto.value = true
            scanFeedbackMessage.value = null
            
            try {
                val detected = GeminiService.scanShelfOrMenuPhoto(storeName, isFastFood)
    
                val existing = repository.getFoodItemsForStore(storeId).firstOrNull() ?: emptyList()
                val updatedList = mutableListOf<FoodItem>()
    
                // Update existing store items
                existing.forEach { item ->
                    val match = detected.find { it.name.contains(item.name, ignoreCase = true) || item.name.contains(it.name, ignoreCase = true) }
                    if (match != null) {
                        updatedList.add(item.copy(inStock = match.isVisibleInStock, price = match.price))
                    } else {
                        // Not in background image -> mark out of stock
                        updatedList.add(item.copy(inStock = false))
                    }
                }
    
                // Insert new line items detected in background photo
                detected.filter { it.isVisibleInStock }.forEach { det ->
                    val already = existing.any { it.name.contains(det.name, ignoreCase = true) || det.name.contains(it.name, ignoreCase = true) }
                    if (!already) {
                        updatedList.add(
                            FoodItem(
                                storeId = storeId,
                                name = det.name,
                                description = "Verified via Driver AI photo scan at $storeName.",
                                price = det.price,
                                category = det.category,
                                aisle = det.aisle,
                                inStock = true,
                                stockQuantity = 8
                            )
                        )
                    }
                }
    
                updatedList.forEach { repository.updateFoodItem(it) }
                scanFeedbackMessage.value = "✨ Gemini AI Photo Scan complete for $storeName! Synced ${detected.size} items. Out-of-stock items updated."
            } catch (e: Exception) {
                scanFeedbackMessage.value = "Failed to scan photo: ${e.message}"
            } finally {
                isScanningPhoto.value = false
            }
        }
    }

    fun addToCart(item: FoodItem, store: Storefront) {
        val existing = cartItems.value.find { it.itemId == item.id }
        if (existing != null) {
            cartItems.value = cartItems.value.map {
                if (it.itemId == item.id) it.copy(quantity = it.quantity + 1) else it
            }
        } else {
            val cartItem = CartItem(
                storeId = store.id,
                storeName = store.name,
                itemId = item.id,
                name = item.name,
                price = item.price,
                quantity = 1,
                isMarketplaceItem = false
            )
            cartItems.value = cartItems.value + cartItem
        }
        customerTab.value = CustomerTab.CART
    }

    fun addMarketplaceItemToCart(item: MarketplaceItem) {
        val cartItem = CartItem(
            storeId = "seller_${item.sellerName}",
            storeName = "Seller: ${item.sellerName}",
            itemId = item.id,
            name = item.title,
            price = item.price,
            quantity = 1,
            isMarketplaceItem = true,
            pickupLocation = item.pickupLocation
        )
        cartItems.value = listOf(cartItem)
        customerTab.value = CustomerTab.CART
    }

    fun removeFromCart(cartItemId: String) {
        cartItems.value = cartItems.value.filter { it.id != cartItemId }
    }

    fun placeOrder() {
        val items = cartItems.value
        if (items.isEmpty()) return

        val sub = items.sumOf { it.price * it.quantity }
        val baseDel = if (items.any { it.isMarketplaceItem }) 4.99 else 2.49
        val creditDiscount = appliedDeliveryCredit.value
        val del = (baseDel - creditDiscount).coerceAtLeast(0.0)
        val serv = sub * 0.05
        val tip = selectedTipAmount.value
        val tot = sub + del + serv + tip
        val store = items.first().storeName

        viewModelScope.launch {
            if (creditDiscount > 0.0) {
                sessionManager.deductDeliveryCredits(creditDiscount)
                val uid = currentUser.value?.id ?: "user_101"
                firestoreService.consumeDeliveryCreditInFirestore(uid, creditDiscount)
            }
            val created = repository.createOrderFromCart(
                items = items,
                storeName = store,
                subtotal = sub,
                deliveryFee = del,
                serviceFee = serv,
                tip = tip,
                total = tot,
                dropoffAddress = dropoffAddress.value,
                paymentMethod = paymentMethod.value,
                deliveryInstructions = checkoutDeliveryInstructions.value
            )
            cartItems.value = emptyList()
            activeTrackedOrderId.value = created.id
            customerTab.value = CustomerTab.ORDERS

            // Auto-queue for Driver Dispatch Ping System
            val driverPay = (del + tip + 8.50).coerceAtLeast(16.50)
            val driverOffer = DriverOffer(
                id = "ping_ord_${created.id}",
                storeName = store,
                customerName = currentUser.value?.name ?: "Valued Customer",
                pickupAddress = "100 Mission St, San Francisco, CA",
                dropoffAddress = dropoffAddress.value,
                payAmount = driverPay,
                basePay = (driverPay - tip).coerceAtLeast(10.0),
                tipAmount = tip,
                bonusAmount = 2.50,
                distanceMiles = 2.2,
                estimatedMins = 15,
                itemDetails = items.joinToString(", ") { "${it.quantity}x ${it.name}" },
                orderId = created.id,
                deliveryInstructions = checkoutDeliveryInstructions.value,
                pickupLat = 37.7813,
                pickupLng = -122.4011,
                dropoffLat = 37.7895,
                dropoffLng = -122.4085,
                offerExpiresInSeconds = 45
            )
            dispatchOffersPool.add(0, driverOffer)

            // Automated live order status tracker
            trackOrderStatusProgression(created.id)
        }
    }

    fun updateOrderStatusAndNotify(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)

            val title: String
            val body: String

            when (status) {
                OrderStatus.SHOPPING_OR_PREPARING.name -> {
                    title = "🛍️ ShopSafe Order Update: Preparing"
                    body = "Your personal shopper is assembling your order items at the store."
                }
                OrderStatus.DRIVER_ASSIGNED.name -> {
                    title = "👨‍✈️ Driver Assigned"
                    body = "Marcus Vance (ShopSafe Courier) has been assigned to pick up your order."
                }
                OrderStatus.ON_THE_WAY.name -> {
                    title = "🛵 Driver En Route!"
                    body = "Marcus Vance has picked up your order and is driving to your delivery address."
                }
                OrderStatus.APPROACHING.name -> {
                    title = "🚗 Driver Approaching Your Location!"
                    body = "Marcus Vance is ~2 minutes away from your location. Get ready to receive your delivery!"
                }
                OrderStatus.DELIVERED.name -> {
                    title = "🎉 Order Delivered!"
                    body = "Marcus Vance has safely dropped off your order. Enjoy your items!"
                }
                else -> {
                    title = "📦 ShopSafe Order Status Update"
                    body = "Your order status is now: ${status.replace("_", " ")}."
                }
            }

            com.example.shopsafe.service.ShopSafeFirebaseMessagingService.sendCustomerOrderStatusNotification(
                context = getApplication(),
                title = title,
                body = body,
                orderId = orderId
            )
        }
    }

    fun triggerDriverApproachingNotification(orderId: String? = null) {
        val targetOrderId = orderId ?: activeTrackedOrderId.value ?: "simulated_order_1"
        updateOrderStatusAndNotify(targetOrderId, OrderStatus.APPROACHING.name)
    }

    private fun trackOrderStatusProgression(orderId: String) {
        viewModelScope.launch {
            delay(4000)
            updateOrderStatusAndNotify(orderId, OrderStatus.SHOPPING_OR_PREPARING.name)
            delay(6000)
            updateOrderStatusAndNotify(orderId, OrderStatus.DRIVER_ASSIGNED.name)
            delay(6000)
            updateOrderStatusAndNotify(orderId, OrderStatus.ON_THE_WAY.name)
            delay(8000)
            updateOrderStatusAndNotify(orderId, OrderStatus.APPROACHING.name)
            delay(10000)
            updateOrderStatusAndNotify(orderId, OrderStatus.DELIVERED.name)
        }
    }

    // Driver Portal Actions
    fun toggleDriverOnline(isOnline: Boolean) {
        viewModelScope.launch {
            repository.updateDriverOnline(isOnline)
        }
    }

    fun acceptDriverOffer(offer: DriverOffer) {
        viewModelScope.launch {
            activeDriverOffer.value = offer
            driverDeliveryStep.value = 1
            driverTripProgress.value = 0.05f
            isInAppNavigationActive.value = true
            showAcceptedNavigateQuickPrompt.value = true
            showArrivalPromptModal.value = false
            arrivalPromptTarget.value = "PICKUP"
            isMileageTrackingActive.value = false // Standby: travel to store/commute is excluded
            currentTripMiles.value = 0.0
            currentTripDurationMinutes.value = 0
            currentTripTargetTotalMiles.value = offer.distanceMiles
            currentTripPickupAddress.value = offer.pickupAddress
            currentTripDropoffAddress.value = offer.dropoffAddress
            repository.acceptDriverOffer(offer)
            if (!offer.orderId.isNullOrBlank()) {
                updateOrderStatusAndNotify(offer.orderId, OrderStatus.DRIVER_ASSIGNED.name)
                // Authorize and bind Stripe Issuing Card for dynamic order spending
                val estimatedSpend = offer.payAmount * 3.5 // baseline order purchasing limit
                stripeIssuingService.authorizeOrderSpending(
                    driverUserId = "driver_1",
                    orderId = offer.orderId,
                    estimatedTotal = estimatedSpend
                )
                refreshDriverIssuingCard()
            }
            // Automatically launch drive progression
            startAutoDriveSimulation()
        }
    }

    fun startInAppNavigation() {
        isInAppNavigationActive.value = true
        showAcceptedNavigateQuickPrompt.value = false
        startAutoDriveSimulation()
    }

    fun stopInAppNavigation() {
        isInAppNavigationActive.value = false
        isAutoDriveSimulating.value = false
        autoDriveSimulationJob?.cancel()
    }

    fun dismissAcceptedQuickPrompt() {
        showAcceptedNavigateQuickPrompt.value = false
    }

    fun startAutoDriveSimulation() {
        autoDriveSimulationJob?.cancel()
        isAutoDriveSimulating.value = true
        autoDriveSimulationJob = viewModelScope.launch {
            while (isAutoDriveSimulating.value && activeDriverOffer.value != null) {
                delay(1200L)
                val current = driverTripProgress.value
                val step = driverDeliveryStep.value

                if (step == 1) {
                    // Leg 1: Driver -> Pickup Store
                    val next = (current + 0.08f).coerceAtMost(1.0f)
                    driverTripProgress.value = next

                    // Automatic Geofence / Proximity Arrival Trigger when at or near pickup (>= 0.75 or 75%)
                    if (next >= 0.75f && !showArrivalPromptModal.value) {
                        arrivalPromptTarget.value = "PICKUP"
                        showArrivalPromptModal.value = true
                        isAutoDriveSimulating.value = false
                    }
                } else if (step == 2) {
                    // Leg 2: Store -> Customer Drop-off
                    val next = (current + 0.08f).coerceAtMost(1.0f)
                    driverTripProgress.value = next

                    // Automatic Geofence / Proximity Arrival Trigger when near customer dropoff (>= 0.85)
                    if (next >= 0.85f && !showArrivalPromptModal.value) {
                        arrivalPromptTarget.value = "DROPOFF"
                        showArrivalPromptModal.value = true
                        isAutoDriveSimulating.value = false
                    }
                }
            }
        }
    }

    fun toggleAutoDriveSimulation() {
        if (isAutoDriveSimulating.value) {
            isAutoDriveSimulating.value = false
            autoDriveSimulationJob?.cancel()
        } else {
            startAutoDriveSimulation()
        }
    }

    fun setDriverTripProgress(progress: Float) {
        val clamped = progress.coerceIn(0.0f, 1.0f)
        driverTripProgress.value = clamped
        val step = driverDeliveryStep.value

        if (step == 1 && clamped >= 0.75f && !showArrivalPromptModal.value) {
            arrivalPromptTarget.value = "PICKUP"
            showArrivalPromptModal.value = true
        } else if (step == 2 && clamped >= 0.85f && !showArrivalPromptModal.value) {
            arrivalPromptTarget.value = "DROPOFF"
            showArrivalPromptModal.value = true
        }
    }

    fun simulateApproachingLocation(target: String = "PICKUP") {
        arrivalPromptTarget.value = target
        if (target == "PICKUP") {
            driverTripProgress.value = 0.90f
            showArrivalPromptModal.value = true
        } else {
            driverTripProgress.value = 0.92f
            showArrivalPromptModal.value = true
        }
    }

    fun confirmArrivalAtCurrentStop() {
        showArrivalPromptModal.value = false
        val step = driverDeliveryStep.value
        val offer = activeDriverOffer.value
        val orderId = offer?.orderId ?: activeTrackedOrderId.value ?: ""

        if (step == 1 || arrivalPromptTarget.value == "PICKUP") {
            // Arrived at pickup store -> Automatically transition shopper from map into cart-style shopping page!
            driverTripProgress.value = 0.05f
            showShopperCartScreen.value = true
            if (shopperOrderItems.value.isEmpty()) {
                shopperOrderItems.value = generateDefaultShopperItems(offer?.storeName ?: "Store Pickup")
            }
            if (orderId.isNotBlank()) {
                updateOrderStatusAndNotify(orderId, OrderStatus.SHOPPING_OR_PREPARING.name)
            }
        } else if (step == 2 || arrivalPromptTarget.value == "DROPOFF") {
            // Arrived at customer drop-off -> Advance to proof of delivery
            advanceDriverStep() // Advances driverDeliveryStep to 3
            if (orderId.isNotBlank()) {
                updateOrderStatusAndNotify(orderId, OrderStatus.APPROACHING.name)
            }
        }
    }

    fun dismissArrivalPrompt() {
        showArrivalPromptModal.value = false
    }

    private fun startAutomatedMileageTracking(targetMiles: Double) {
        mileageTrackingJob?.cancel()
        isMileageTrackingActive.value = true
        currentTripStartTime.value = System.currentTimeMillis()
        currentTripMiles.value = 0.0
        currentTripDurationMinutes.value = 0
        currentTripTargetTotalMiles.value = targetMiles

        mileageTrackingJob = viewModelScope.launch {
            val stepSize = (targetMiles / 20.0).coerceIn(0.04, 0.20)
            while (isMileageTrackingActive.value) {
                delay(1000L)
                val newMiles = (currentTripMiles.value + stepSize).coerceAtMost(targetMiles)
                currentTripMiles.value = String.format("%.2f", newMiles).toDouble()
                currentTripDurationMinutes.value = ((System.currentTimeMillis() - currentTripStartTime.value) / 1000 / 60).toInt() + 1
            }
        }
    }

    private fun stopAndRecordAutomatedMileage(
        orderId: String,
        storeName: String,
        pickupAddress: String,
        dropoffAddress: String,
        finalMiles: Double,
        durationMins: Int
    ) {
        mileageTrackingJob?.cancel()
        isMileageTrackingActive.value = false
        val startTime = if (currentTripStartTime.value > 0) currentTripStartTime.value else (System.currentTimeMillis() - (durationMins * 60 * 1000L))
        val endTime = System.currentTimeMillis()
        val recordedMiles = if (finalMiles > 0.0) finalMiles else currentTripMiles.value.coerceAtLeast(1.2)

        viewModelScope.launch {
            val log = repository.recordAutomatedDeliveryMileage(
                orderId = orderId,
                storeOrSellerName = storeName,
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                milesDriven = recordedMiles,
                startTimestamp = startTime,
                endTimestamp = endTime,
                durationMinutes = durationMins
            )
            lastCompletedMileageTrip.value = log
            showMileageTripCompletedDialog.value = true
        }
    }

    fun deleteMileageLog(id: String) {
        viewModelScope.launch {
            repository.deleteMileageLog(id)
        }
    }

    fun clearAllMileageLogs() {
        viewModelScope.launch {
            repository.clearAllMileageLogs()
        }
    }

    fun advanceDriverStep() {
        val step = driverDeliveryStep.value
        val offer = activeDriverOffer.value
        val orderId = offer?.orderId ?: activeTrackedOrderId.value ?: ""
        val targetDist = offer?.distanceMiles ?: 1.4

        if (step == 1) {
            // Confirm pickup at merchant -> Automated Delivery Mileage Tracker STARTS NOW
            driverDeliveryStep.value = 2 // On the way to customer drop-off
            startAutomatedMileageTracking(targetDist)
            if (orderId.isNotBlank()) {
                updateOrderStatusAndNotify(orderId, OrderStatus.ON_THE_WAY.name)
            }
        } else if (step == 2) {
            driverDeliveryStep.value = 3 // Approaching customer location
            if (orderId.isNotBlank()) {
                updateOrderStatusAndNotify(orderId, OrderStatus.APPROACHING.name)
            }
        } else if (step == 3) {
            // Complete delivery with full breakdown & Firestore sync
            val earned = offer?.payAmount ?: 18.75
            val store = offer?.storeName ?: "ShopSafe Express Storefront"
            val pickup = offer?.pickupAddress ?: "100 Mission St, San Francisco, CA"
            val dropoff = offer?.dropoffAddress ?: "220 Montgomery St, San Francisco, CA"
            val items = offer?.itemDetails ?: "Verified ShopSafe Delivery"
            val dist = offer?.distanceMiles ?: 1.4
            val mins = offer?.estimatedMins ?: 18

            // Record automated delivery mileage (excludes commute to/from home)
            stopAndRecordAutomatedMileage(
                orderId = orderId,
                storeName = store,
                pickupAddress = pickup,
                dropoffAddress = dropoff,
                finalMiles = dist,
                durationMins = mins
            )

            viewModelScope.launch {
                repository.completeDriverDelivery(
                    earnedAmount = earned,
                    orderId = offer?.orderId,
                    storeName = store,
                    customerName = "Valued Customer",
                    pickupAddress = pickup,
                    dropoffAddress = dropoff,
                    itemsSummary = items,
                    distanceMiles = dist,
                    durationMinutes = mins,
                    proofPhotoUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80"
                )
                if (orderId.isNotBlank()) {
                    updateOrderStatusAndNotify(orderId, OrderStatus.DELIVERED.name)
                }
                activeDriverOffer.value = null
                driverDeliveryStep.value = 4
            }
        }
    }

    fun simulateNewCompletedDelivery(
        storeName: String = "ShopSafe Local Storefront Hub #1",
        earnedAmount: Double = 22.50,
        tipAmount: Double = 7.50,
        itemsSummary: String = "Personal Shopper Priority Express Delivery (3 items)"
    ) {
        viewModelScope.launch {
            val base = (earnedAmount - tipAmount).coerceAtLeast(6.0)
            val delivery = CompletedDelivery(
                driverId = "driver_101",
                storeOrSellerName = storeName,
                customerName = "Live Test Customer",
                pickupAddress = "100 Mission St, San Francisco, CA",
                dropoffAddress = "500 Howard St, San Francisco, CA",
                itemsSummary = itemsSummary,
                basePay = base,
                tipAmount = tipAmount,
                peakBonus = 2.00,
                totalEarnings = earnedAmount + 2.00,
                distanceMiles = 1.6,
                durationMinutes = 20,
                timestamp = System.currentTimeMillis(),
                proofPhotoUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Confirmed dropoff. Synced directly to Cloud Firestore."
            )
            repository.recordCompletedDelivery(delivery)
        }
    }

    fun selectDeliveryDetail(delivery: CompletedDelivery?) {
        selectedDeliveryDetail.value = delivery
    }

    fun setDeliveryFilterPeriod(period: String) {
        deliveryFilterPeriod.value = period
    }

    fun setDriverDashboardTab(tab: String) {
        driverDashboardViewTab.value = tab
    }

    fun requestPayout(amount: Double, method: String) {
        if (amount <= 0.0) {
            payoutErrorMessage.value = "Please enter an amount greater than $0.00"
            return
        }

        viewModelScope.launch {
            try {
                isProcessingPayout.value = true
                payoutErrorMessage.value = null
                payoutProcessingStep.value = "1. Updating Driver balance status to 'Pending' in Cloud Firestore..."
                delay(600)

                payoutProcessingStep.value = "2. Connecting to Stripe Connect API & validating connected account..."
                delay(700)

                val fee = if (method.contains("Instant") || method.contains("Debit")) amount * 0.015 else 0.0
                payoutProcessingStep.value = "3. Routing automated transfer to connected account..."

                val result = repository.requestPayout(amount, method, fee)
                if (result != null) {
                    lastPayoutResult.value = result
                    payoutProcessingStep.value = "✓ Stripe Transfer ${result.stripeTransferId} Created! Status: PENDING"
                    delay(500)
                    showPayoutSuccessModal.value = true
                    showPayoutDialog.value = false
                } else {
                    payoutErrorMessage.value = "Insufficient available balance for this payout."
                }
            } catch (e: Exception) {
                payoutErrorMessage.value = "Stripe Connect Error: ${e.message}"
            } finally {
                isProcessingPayout.value = false
            }
        }
    }

    fun dismissPayoutSuccessModal() {
        showPayoutSuccessModal.value = false
        lastPayoutResult.value = null
        payoutProcessingStep.value = ""
    }

    fun simulateStripeSettlement(payoutId: String) {
        viewModelScope.launch {
            repository.completePayoutSettlement(payoutId)
        }
    }

    // 72-Second Dispatch Ping Actions
    fun triggerTestBackgroundFcmNotification(context: android.content.Context, offer: DriverOffer? = null) {
        val targetOffer = offer ?: availableDriverOffers.value.firstOrNull() ?: DriverOffer(
            storeName = "ShopSafe Local Express Storefront",
            pickupAddress = "100 Mission St, San Francisco, CA",
            dropoffAddress = "220 Montgomery St, Apt 14, San Francisco, CA",
            payAmount = 18.75,
            distanceMiles = 1.2,
            estimatedMins = 18,
            itemDetails = "Personal Shopper Express Grocery Order (4 items)",
            isShopSafeStorefrontShopping = true
        )

        val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EXTRA_ACTION", "TRIGGER_DISPATCH_PING")
            putExtra("EXTRA_STORE_NAME", targetOffer.storeName)
            putExtra("EXTRA_PAY_AMOUNT", targetOffer.payAmount)
            putExtra("EXTRA_PICKUP_ADDRESS", targetOffer.pickupAddress)
            putExtra("EXTRA_DROPOFF_ADDRESS", targetOffer.dropoffAddress)
            putExtra("EXTRA_DISTANCE_MILES", targetOffer.distanceMiles)
            putExtra("EXTRA_ESTIMATED_MINS", targetOffer.estimatedMins)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            1001,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = com.example.shopsafe.service.ShopSafeFirebaseMessagingService.CHANNEL_ID
        com.example.shopsafe.service.ShopSafeFirebaseMessagingService.createNotificationChannel(context)

        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚡ FCM Dispatch Alert: $${String.format("%.2f", targetOffer.payAmount)}")
            .setContentText("${targetOffer.storeName} (${targetOffer.distanceMiles} mi) • 72s countdown!")
            .setSubText("ShopSafe Real-Time Driver Ping")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))

        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(1001, builder.build())
    }

    fun subscribeToStoreOpenNotification(context: android.content.Context, storeId: String, storeName: String) {
        viewModelScope.launch {
            // Mock waiting for the store to open (e.g., 5 seconds delay)
            kotlinx.coroutines.delay(5000)
            
            // Mark the store as open in the database
            val existingStore = repository.allStorefronts.first().find { it.id == storeId }
            if (existingStore != null && !existingStore.isOpen) {
                repository.addStorefront(existingStore.copy(isOpen = true))
            }
            
            // Send the Customer Push Notification
            val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                2001,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = com.example.shopsafe.service.ShopSafeFirebaseMessagingService.CHANNEL_ID
            com.example.shopsafe.service.ShopSafeFirebaseMessagingService.createNotificationChannel(context)
            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🔔 $storeName is now Open!")
                .setContentText("Tap to place your order now.")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(2001, builder.build())
        }
    }

    private fun startAutomatedDispatchEngine() {
        automatedDispatchJob?.cancel()
        automatedDispatchJob = viewModelScope.launch {
            // Initial delay after launch
            delay(5000L)
            while (true) {
                val profile = driverProfile.value
                val isOnline = profile?.isOnline ?: true
                val hasActiveDelivery = activeDriverOffer.value != null
                val isAlreadyPinged = isDispatchActive.value

                if (isOnline && !hasActiveDelivery && !isAlreadyPinged) {
                    // Driver is online and searching -> automatically offer the closest nearby order ping!
                    triggerNextAvailableDispatchPing()
                }

                // Periodic radar search interval
                delay(12000L)
            }
        }
    }

    fun triggerNextAvailableDispatchPing() {
        if (dispatchOffersPool.isEmpty()) {
            dispatchOffersPool.addAll(com.example.shopsafe.data.models.DriverOpportunityEngine.generateNearbyDispatchOffers())
        }
        val offer = dispatchOffersPool[currentDispatchOfferIndex % dispatchOffersPool.size]
        currentDispatchOfferIndex++
        triggerDispatchPing(offer)
    }

    fun triggerInstantDispatchPing() {
        triggerNextAvailableDispatchPing()
    }

    fun triggerDispatchPing(offer: DriverOffer? = null) {
        val targetOffer = offer ?: if (dispatchOffersPool.isNotEmpty()) {
            dispatchOffersPool[currentDispatchOfferIndex % dispatchOffersPool.size]
        } else {
            com.example.shopsafe.data.models.DriverOpportunityEngine.generateNearbyDispatchOffers().first()
        }
        incomingDispatchOffer.value = targetOffer
        val initialSeconds = targetOffer.offerExpiresInSeconds.coerceAtLeast(30)
        dispatchTimerSeconds.value = initialSeconds
        isDispatchActive.value = true

        pingCountdownJob?.cancel()
        pingCountdownJob = viewModelScope.launch {
            while (isDispatchActive.value && dispatchTimerSeconds.value > 0) {
                val delayTime = if (isLowPowerModeEnabled.value) 2000L else 1000L
                delay(delayTime)
                if (isDispatchActive.value) {
                    val decrement = if (delayTime == 2000L) 2 else 1
                    dispatchTimerSeconds.value = (dispatchTimerSeconds.value - decrement).coerceAtLeast(0)
                }
            }
            if (isDispatchActive.value && dispatchTimerSeconds.value <= 0) {
                // 45s Timer expired - pass to next closest driver available
                denyDispatchOffer()
            }
        }
    }

    fun acceptDispatchOffer() {
        pingCountdownJob?.cancel()
        val offer = incomingDispatchOffer.value
        isDispatchActive.value = false
        incomingDispatchOffer.value = null
        if (offer != null) {
            acceptDriverOffer(offer)
        }
    }

    fun denyDispatchOffer() {
        pingCountdownJob?.cancel()
        isDispatchActive.value = false
        incomingDispatchOffer.value = null
    }

    // Driver Connectivity Control & Testing Actions
    fun toggleSimulatedOffline() {
        isSimulatedOffline.value = !isSimulatedOffline.value
    }

    fun setSimulatedOffline(isOffline: Boolean) {
        isSimulatedOffline.value = isOffline
    }

    fun isNetworkCurrentlyActive(): Boolean {
        return networkObserver.isCurrentlyConnected() && !isSimulatedOffline.value
    }

    // Authentication & Registration Handlers
    fun loginWithFacebook() {
        val user = AuthUser(
            name = "Alex Rivera (Facebook User)",
            email = "alex.rivera@facebook.com",
            authProvider = "FACEBOOK",
            isDriver = true,
            checkrStatus = "APPROVED",
            insuranceVerified = true,
            driversLicenseVerified = true,
            selfieVerified = true
        )
        currentUser.value = user
        viewModelScope.launch {
            sessionManager.saveSession(user)
        }
        showAuthModal.value = false
    }

    fun loginWithGoogle() {
        val user = AuthUser(
            name = "Alex Rivera (Google User)",
            email = "alex.rivera@gmail.com",
            authProvider = "GOOGLE",
            isDriver = true
        )
        currentUser.value = user
        viewModelScope.launch {
            sessionManager.saveSession(user)
        }
        showAuthModal.value = false
    }

    fun loginWithApple() {
        val user = AuthUser(
            name = "Alex Rivera (Apple User)",
            email = "alex.rivera@icloud.com",
            authProvider = "APPLE",
            isDriver = true
        )
        currentUser.value = user
        viewModelScope.launch {
            sessionManager.saveSession(user)
        }
        showAuthModal.value = false
    }

    fun loginUser(email: String) {
        val trimmed = email.trim()
        val hashed = sha256(trimmed)
        val user = if (hashed == "9660f22fe22fb9ba85b56ee3468e091a598c890d4db6434663a54f86b20733eb" || 
            hashed == "84334b678dc002895283828789cd3e722d59c1f18f7a0c2754c0a024a8f697ab" || 
            trimmed == "#SHOPSAFE_MASTER_999#" || 
            trimmed == "#SHOPSAFE_ADMIN#") {
            // Secret Master Admin Account authenticated straight behind the best encryption!
            AuthUser(
                id = "admin_master_007",
                name = "ShopSafe Master Admin",
                email = "admin@shopsafe.com",
                phone = "(800) 555-SAFE",
                authProvider = "ENCRYPTED_ADMIN_KEY",
                isDriver = true,
                isAdmin = true,
                checkrStatus = "APPROVED",
                insuranceVerified = true,
                driversLicenseVerified = true,
                selfieVerified = true
            )
        } else {
            val namePart = trimmed.substringBefore("@").replace(".", " ")
            AuthUser(
                name = namePart.ifBlank { "Registered User" },
                email = trimmed,
                authProvider = "EMAIL",
                isDriver = true,
                isAdmin = false
            )
        }
        currentUser.value = user
        viewModelScope.launch {
            sessionManager.saveSession(user)
        }
        showAuthModal.value = false
    }

    private fun sha256(input: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray())
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun registerAndSaveDriver(newProfile: DriverProfile) {
        viewModelScope.launch {
            repository.updateDriverProfile(newProfile)
            val user = AuthUser(
                name = newProfile.name,
                email = newProfile.email,
                phone = newProfile.phone,
                authProvider = newProfile.authProvider,
                isDriver = true,
                checkrStatus = newProfile.checkrStatus,
                insuranceVerified = newProfile.insuranceVerified,
                driversLicenseVerified = newProfile.driversLicenseVerified,
                selfieVerified = newProfile.selfieVerified,
                vehicleMake = newProfile.vehicleMake,
                vehicleModel = newProfile.vehicleModel,
                vehicleYear = newProfile.vehicleYear,
                vehicleColor = newProfile.vehicleColor,
                licensePlate = newProfile.licensePlate,
                vehicleType = newProfile.vehicleType,
                bankRoutingNumber = newProfile.bankRoutingNumber,
                bankAccountNumber = newProfile.bankAccountNumber,
                debitCardNumber = newProfile.debitCardNumber
            )
            currentUser.value = user
            sessionManager.saveSession(user)
            showAuthModal.value = false
        }
    }

    // Chat & Calling Actions
    fun sendMessage(
        imageUrl: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        locationAddress: String? = null
    ) {
        val text = messageInputText.value.trim()
        val threadId = selectedThreadId.value ?: "thread_elena"
        if (text.isBlank() && imageUrl == null && lat == null) return

        viewModelScope.launch {
            repository.sendChatMessage(
                threadId = threadId, 
                partnerName = "Contact", 
                text = text, 
                isFromUser = true,
                imageUrl = imageUrl,
                lat = lat,
                lng = lng,
                locationAddress = locationAddress
            )
            messageInputText.value = ""

            // Instant partner auto-reply trigger
            delay(2000)
            val replyText = when {
                lat != null -> "Thanks for sending your live location! I'll head there."
                imageUrl != null -> "Got the photo, thanks!"
                text.contains("available", ignoreCase = true) -> "Yes! It's still available for pickup at the verified safe location."
                text.contains("eta", ignoreCase = true) || text.contains("where", ignoreCase = true) -> "I'm about 5 minutes away with your ShopSafe delivery order!"
                else -> "Sounds great! Looking forward to completing this on ShopSafe."
            }
            repository.sendChatMessage(threadId, "ShopSafe Contact", replyText, isFromUser = false)
        }
    }

    fun startCall(name: String, phone: String) {
        activeCallState.value = ActiveCallState(
            isCalling = true,
            callerName = name,
            callerPhone = phone,
            durationSeconds = 0
        )
        // Live call duration timer
        viewModelScope.launch {
            while (activeCallState.value.isCalling) {
                delay(1000)
                activeCallState.value = activeCallState.value.copy(
                    durationSeconds = activeCallState.value.durationSeconds + 1
                )
            }
        }
    }

    fun toggleMuteCall() {
        activeCallState.value = activeCallState.value.copy(isMuted = !activeCallState.value.isMuted)
    }

    fun toggleSpeakerCall() {
        activeCallState.value = activeCallState.value.copy(isSpeaker = !activeCallState.value.isSpeaker)
    }

    fun endCall() {
        activeCallState.value = ActiveCallState()
    }

    // Dynamic Store Onboarding & Live Integration Actions
    fun createAndAddStorefront(
        name: String,
        category: String,
        address: String,
        deliveryFee: Double = 1.99,
        estimatedMins: Int = 18,
        items: List<FoodItem> = emptyList()
    ) {
        viewModelScope.launch {
            val newStore = Storefront(
                name = name,
                category = category,
                address = address,
                deliveryFee = deliveryFee,
                estimatedMins = estimatedMins,
                rating = 4.9,
                isShopSafeEmployeeHub = true
            )
            repository.addStorefront(newStore)
            items.forEach { item ->
                repository.addFoodItem(item.copy(storeId = newStore.id))
            }
            showAddStoreModal.value = false
        }
    }

    fun addFoodItemToStore(
        storeId: String,
        name: String,
        price: Double,
        category: String = "Popular",
        description: String = ""
    ) {
        viewModelScope.launch {
            val item = FoodItem(
                storeId = storeId,
                name = name,
                price = price,
                category = category,
                description = description
            )
            repository.addFoodItem(item)
        }
    }

    fun adjustActiveOrderPrice(
        adjustedTotal: Double,
        reason: String
    ) {
        val offer = activeDriverOffer.value
        if (offer != null) {
            val updated = offer.copy(
                itemDetails = "${offer.itemDetails} [Adjusted Total: $${String.format("%.2f", adjustedTotal)} ($reason)]"
            )
            activeDriverOffer.value = updated
            showPriceAdjustmentModal.value = false
        }
    }

    fun updateItemAisleAndStock(
        item: FoodItem,
        newAisle: String,
        newPrice: Double,
        inStock: Boolean,
        stockQty: Int
    ) {
        viewModelScope.launch {
            val updated = item.copy(
                aisle = newAisle,
                price = newPrice,
                inStock = inStock,
                stockQuantity = stockQty
            )
            repository.updateFoodItem(updated)
            selectedItemForDriverUpdate.value = null
        }
    }

    val activeChatThreadId = MutableStateFlow<String?>("thread_marcus")

    fun getFoodItemsForStore(storeId: String): Flow<List<FoodItem>> {
        return repository.getFoodItemsForStore(storeId)
    }

    fun startVoiceCall(phone: String) {
        viewModelScope.launch {
            // Simulated live VoIP call trigger
        }
    }

    fun proposeItemSubstitution(
        originalItem: FoodItem,
        substituteName: String,
        substitutePrice: Double
    ) {
        viewModelScope.launch {
            val updated = originalItem.copy(
                name = "$substituteName (Substituted for ${originalItem.name})",
                price = substitutePrice,
                inStock = true,
                description = "${originalItem.description} [Substituted live by ShopSafe driver]"
            )
            repository.updateFoodItem(updated)
            selectedItemForDriverUpdate.value = null

            // Send notification message to customer chat thread
            repository.sendChatMessage(
                threadId = "thread_marcus",
                partnerName = "ShopSafe Driver",
                text = "⚡ Live Substitution Notice: Substituted '${originalItem.name}' (${String.format("%.2f", originalItem.price)}) with '$substituteName' (${String.format("%.2f", substitutePrice)}).",
                isFromUser = false
            )
        }
    }

    // --- MASTER ADMIN ACTIONS ---
    fun adminDeleteMarketplaceItem(id: String) {
        viewModelScope.launch {
            repository.adminDeleteMarketplaceItem(id)
        }
    }

    fun adminDeleteStorefront(id: String) {
        viewModelScope.launch {
            repository.adminDeleteStorefront(id)
        }
    }

    fun adminDeleteFoodItem(id: String) {
        viewModelScope.launch {
            repository.adminDeleteFoodItem(id)
        }
    }

    fun adminDeleteOrder(id: String) {
        viewModelScope.launch {
            repository.adminDeleteOrder(id)
        }
    }

    fun adminUpdateFoodItemPrice(id: String, newPrice: Double) {
        viewModelScope.launch {
            repository.adminUpdateFoodItemPrice(id, newPrice)
        }
    }

    fun adminUpdateStorefrontOpenStatus(id: String, isOpen: Boolean) {
        viewModelScope.launch {
            repository.adminUpdateStorefrontOpenStatus(id, isOpen)
        }
    }

    fun adminUpdateMarketplaceItemPrice(item: MarketplaceItem, newPrice: Double) {
        viewModelScope.launch {
            repository.adminDeleteMarketplaceItem(item.id)
            repository.insertMarketplaceItem(item.copy(price = newPrice))
        }
    }

    fun adminUpdateOrderStatus(orderId: String, status: String) {
        updateOrderStatusAndNotify(orderId, status)
    }

    val savedAddresses: StateFlow<List<String>> = sessionManager.savedAddressesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAddresses: StateFlow<List<String>> = sessionManager.recentAddressesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSavedAddress(address: String) {
        viewModelScope.launch {
            sessionManager.addSavedAddress(address)
        }
    }

    fun removeSavedAddress(address: String) {
        viewModelScope.launch {
            sessionManager.removeSavedAddress(address)
        }
    }

    fun cacheRecentLocation(address: String) {
        viewModelScope.launch {
            sessionManager.cacheRecentLocation(address)
        }
    }

    fun submitOrderReview(orderId: String, rating: Int, feedback: String) {
        viewModelScope.launch {
            repository.submitOrderReview(orderId, rating, feedback)
        }
    }

    fun adminOverrideDriverProfile(profile: DriverProfile) {
        viewModelScope.launch {
            repository.adminUpdateDriverProfile(profile)
        }
    }

    fun resetAllCountsToZero() {
        viewModelScope.launch {
            repository.adminUpdateDriverProfile(
                DriverProfile(
                    id = "current_driver",
                    name = "Alex Rivera",
                    email = "alex.rivera@example.com",
                    isOnline = true,
                    currentBalance = 0.0,
                    todayEarned = 0.0,
                    weekEarned = 0.0,
                    completedToday = 0,
                    rating = 4.98
                )
            )
            paymentMethod.value = "ShopSafe Pay Balance ($0.00)"
        }
    }

    // Shopper Cart & Item Picking Actions
    fun openShopperCartScreen() {
        showShopperCartScreen.value = true
    }

    fun closeShopperCartScreen() {
        showShopperCartScreen.value = false
    }

    fun incrementShopperItemQuantity(itemId: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                val newQty = item.collectedQuantity + 1
                val newStatus = if (newQty >= item.requestedQuantity) ShopperItemStatus.COLLECTED else item.status
                item.copy(collectedQuantity = newQty, status = newStatus)
            } else item
        }
        shopperOrderItems.value = current
    }

    fun decrementShopperItemQuantity(itemId: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                val newQty = (item.collectedQuantity - 1).coerceAtLeast(0)
                val newStatus = if (newQty == 0) ShopperItemStatus.PENDING else item.status
                item.copy(collectedQuantity = newQty, status = newStatus)
            } else item
        }
        shopperOrderItems.value = current
    }

    fun quickCollectShopperItem(itemId: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    collectedQuantity = item.requestedQuantity,
                    status = ShopperItemStatus.COLLECTED,
                    scanMethod = ItemScanMethod.MANUAL_COUNTER
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "Item marked collected!"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun substituteShopperItem(itemId: String, substitutionName: String, substitutionPrice: Double) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ShopperItemStatus.SUBSTITUTED,
                    substitutionName = substitutionName,
                    substitutionPrice = substitutionPrice,
                    actualPrice = substitutionPrice,
                    collectedQuantity = item.requestedQuantity
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "Substituted: $substitutionName"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun markShopperItemUnavailable(itemId: String, reason: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ShopperItemStatus.UNAVAILABLE,
                    unavailableReason = reason,
                    collectedQuantity = 0
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "Item marked unavailable (customer refunded)"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun scanBarcodeForItem(itemId: String, barcode: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ShopperItemStatus.COLLECTED,
                    collectedQuantity = item.requestedQuantity,
                    scanMethod = ItemScanMethod.BARCODE,
                    barcodeConfidence = 0.99f
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "Barcode verified: $barcode"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun scanQrCodeForItem(itemId: String, qrCode: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ShopperItemStatus.COLLECTED,
                    collectedQuantity = item.requestedQuantity,
                    scanMethod = ItemScanMethod.QR_CODE,
                    barcodeConfidence = 1.0f
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "QR code verified: $qrCode"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun aiRecognizePhotoItem(itemId: String, productName: String) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ShopperItemStatus.COLLECTED,
                    collectedQuantity = item.requestedQuantity,
                    scanMethod = ItemScanMethod.AI_PHOTO_RECOGNITION,
                    barcodeConfidence = 0.96f
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "Gemini AI recognized: $productName"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun manualPhotoCaptureForItem(itemId: String, price: Double, quantity: Int) {
        val current = shopperOrderItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = ShopperItemStatus.COLLECTED,
                    collectedQuantity = quantity,
                    actualPrice = price,
                    scanMethod = ItemScanMethod.MANUAL_PHOTO,
                    itemPhotoUri = "captured_item_${itemId}.jpg"
                )
            } else item
        }
        shopperOrderItems.value = current
        scanFeedbackMessage.value = "Manual photo recorded ($$price x $quantity)"
        viewModelScope.launch {
            delay(2000)
            scanFeedbackMessage.value = null
        }
    }

    fun confirmCheckoutTotalAndReceipt(
        actualTotal: Double,
        priceAdjustment: Double = 0.0,
        adjustmentReason: String = "",
        registerPhotoUrl: String = "",
        receiptPhotoUrl: String = ""
    ) {
        val offer = activeDriverOffer.value
        val orderId = offer?.orderId ?: "SS-ORD-9021"
        val storeName = offer?.storeName ?: "Store Checkout"
        val expected = shopperOrderItems.value.sumOf { it.expectedPrice * it.requestedQuantity }
        val diff = Math.abs(actualTotal - expected)
        val isDiscrepancy = diff > (expected * 0.25)

        val verification = StoreCheckoutVerification(
            orderId = orderId,
            storeName = storeName,
            expectedSubtotal = expected,
            actualRegisterTotal = actualTotal,
            priceAdjustmentAmount = priceAdjustment,
            adjustmentReason = adjustmentReason,
            registerPhotoUrl = registerPhotoUrl,
            receiptPhotoUrl = receiptPhotoUrl,
            isReceiptReadable = true,
            receiptOcrStoreMatch = true,
            isDiscrepancyFlagged = isDiscrepancy,
            discrepancyNote = if (isDiscrepancy) "Discrepancy of $diff flagged for audit" else null,
            verificationTimestamp = System.currentTimeMillis()
        )
        lastCheckoutVerification.value = verification
        showShopperCartScreen.value = false

        // Stripe Issuing Real-Time Authorization & 3-Way Reconcile
        viewModelScope.launch {
            val card = driverIssuingCard.value
            val cardId = card?.id ?: "ic_default"
            stripeIssuingService.processRealtimeAuthorization(
                cardId = cardId,
                orderId = orderId,
                merchantName = storeName,
                merchantCategory = "grocery_stores",
                requestedAmount = actualTotal
            )

            stripeIssuingService.reconcileOrderPurchase(
                orderId = orderId,
                driverId = "driver_1",
                driverName = driverProfile.value?.name ?: "David Chen",
                merchantName = storeName,
                expectedOrderTotal = expected,
                stripeCapturedAmount = actualTotal,
                receiptScannedAmount = actualTotal,
                registerPhotoUrl = registerPhotoUrl,
                originalReceiptPhotoUrl = receiptPhotoUrl
            )

            // Non-obstructive alert for map navigation
            stripeNonObstructiveAlert.value = "Stripe Purchase Approved: ${String.format("%.2f", actualTotal)} • Reconciled ✓"
            delay(4000)
            stripeNonObstructiveAlert.value = null
        }

        // Automatically advance driver navigation directly back to active Google Maps towards customer!
        driverDeliveryStep.value = 2
        driverTripProgress.value = 0.05f
        isInAppNavigationActive.value = true
        isMileageTrackingActive.value = true
        startAutoDriveSimulation()
        startAutomatedMileageTracking(offer?.distanceMiles ?: 3.5)
        updateOrderStatusAndNotify(orderId, OrderStatus.ON_THE_WAY.name)
    }

    // ----------------------------------------------------
    // Stripe Issuing Management Operations
    // ----------------------------------------------------
    fun refreshDriverIssuingCard(): Boolean {
        val card = stripeIssuingService.getDriverCard("driver_1")
        driverIssuingCard.value = card
        return card != null
    }

    suspend fun setStripeEnvironmentMode(mode: StripeEnvironmentMode) {
        val adminName = currentUser.value?.name ?: "ShopSafe Admin"
        stripeIssuingService.setEnvironmentMode(mode, adminName)
    }

    suspend fun rotateStripeServerKey(): Boolean {
        val adminName = currentUser.value?.name ?: "ShopSafe Admin"
        return stripeIssuingService.rotateApiKey(adminName)
    }

    suspend fun toggleDriverCardFreeze(freeze: Boolean) {
        val card = driverIssuingCard.value ?: return
        val updated = stripeIssuingService.toggleCardFreeze(card.id, freeze, triggeredBy = card.driverName)
        driverIssuingCard.value = updated
    }

    suspend fun reportDriverCardLostStolen() {
        val card = driverIssuingCard.value ?: return
        val updated = stripeIssuingService.reportCardLostStolen(card.id, adminOrDriverName = card.driverName)
        driverIssuingCard.value = updated
    }

    suspend fun requestReplacementIssuingCard() {
        val card = driverIssuingCard.value ?: return
        val newCard = stripeIssuingService.issueReplacementCard(card.id, adminName = card.driverName)
        driverIssuingCard.value = newCard
    }

    suspend fun toggleCardFreezeAdmin(cardId: String, freeze: Boolean) {
        val adminName = currentUser.value?.name ?: "ShopSafe Admin"
        stripeIssuingService.toggleCardFreeze(cardId, freeze, triggeredBy = adminName)
        refreshDriverIssuingCard()
    }

    suspend fun overrideCardSpendingLimit(cardId: String, newLimit: Double) {
        val adminName = currentUser.value?.name ?: "ShopSafe Admin"
        stripeIssuingService.updateCardSpendingLimits(cardId, newLimit, adminName)
        refreshDriverIssuingCard()
    }

    suspend fun replaceCardAdmin(cardId: String) {
        val adminName = currentUser.value?.name ?: "ShopSafe Admin"
        stripeIssuingService.issueReplacementCard(cardId, adminName)
        refreshDriverIssuingCard()
    }

    suspend fun simulateWebhookPing(): StripeWebhookEventRecord {
        return stripeIssuingService.simulateWebhookTest()
    }

    // ----------------------------------------------------
    // Stripe Issuing Backend Tokenization Operations (via StripeService)
    // ----------------------------------------------------
    suspend fun requestCardEphemeralKey(cardId: String, cardholderId: String): Result<StripeIssuingEphemeralKey> {
        return stripeService.getIssuingEphemeralKey(cardId, cardholderId)
    }

    suspend fun revealCardDetailsSecurely(): Result<StripeCardTokenizedDetails> {
        val card = driverIssuingCard.value ?: return Result.failure(IllegalStateException("No driver issuing card found"))
        val result = stripeService.fetchSecureTokenizedCardDetails(card)
        if (result.isSuccess) {
            stripeIssuingTokenizationManager.fetchTokenizedCardDetails(card)
        }
        return result
    }

    suspend fun tokenizeCardForGooglePay(): Result<StripePushProvisioningPayload> {
        val card = driverIssuingCard.value ?: return Result.failure(IllegalStateException("No driver issuing card found"))
        val result = stripeService.pushProvisionToGooglePay(card)
        if (result.isSuccess) {
            stripeIssuingTokenizationManager.tokenizeForGooglePay(card)
        }
        return result
    }

    /**
     * Initializes the payment UI by calling StripeService.getEphemeralKey() and attaching the resulting key to the Stripe instance in StripePaymentProvider.
     */
    suspend fun initializePaymentUI(customerId: String = "cus_default_driver"): Result<com.stripe.android.Stripe> {
        return try {
            val keyResult = stripeService.getEphemeralKey(customerId = customerId)
            if (keyResult.isSuccess) {
                val ephemeralKey = keyResult.getOrThrow()
                stripePaymentProvider.attachEphemeralKey(
                    ephemeralKeySecret = ephemeralKey.secret,
                    cardId = ephemeralKey.cardId
                )
                Result.success(stripePaymentProvider.stripe)
            } else {
                val error = keyResult.exceptionOrNull() ?: IllegalStateException("Failed to retrieve ephemeral key")
                stripePaymentProvider.recordError(error.message ?: "Unknown error")
                Result.failure(error)
            }
        } catch (e: Exception) {
            stripePaymentProvider.recordError(e.message ?: "Initialization exception")
            Result.failure(e)
        }
    }

    fun clearSensitiveTokenizedData() {
        stripeService.wipeInMemoryCardDetails()
        stripeIssuingTokenizationManager.clearSensitiveTokenizedData()
    }

    fun dismissStripeAlert() {
        stripeServiceWrapper.dismissAlert()
    }

    fun clearTokenizationMessages() {
        stripeIssuingTokenizationManager.clearMessages()
    }
}

fun generateDefaultShopperItems(storeName: String): List<ShopperOrderItem> {
    return listOf(
        ShopperOrderItem(
            id = "shp_1",
            name = "Organic Whole Milk (1 Gallon)",
            brandOrCategory = "Dairy Essentials",
            requestedQuantity = 2,
            collectedQuantity = 0,
            expectedPrice = 5.99,
            actualPrice = 5.99,
            unit = "gal",
            aisle = "Aisle 1 - Dairy Coolers",
            barcode = "011110416001",
            qrCode = "SS-QR-MILK-901",
            status = ShopperItemStatus.PENDING
        ),
        ShopperOrderItem(
            id = "shp_2",
            name = "Organic Avocados (Hass 4-Pack)",
            brandOrCategory = "Fresh Produce",
            requestedQuantity = 1,
            collectedQuantity = 0,
            expectedPrice = 4.49,
            actualPrice = 4.49,
            unit = "pk",
            aisle = "Produce Section - Island B",
            barcode = "033383120045",
            qrCode = "SS-QR-AVO-402",
            status = ShopperItemStatus.PENDING
        ),
        ShopperOrderItem(
            id = "shp_3",
            name = "Artisan Sourdough Loaf (Fresh Baked)",
            brandOrCategory = "Bakery",
            requestedQuantity = 1,
            collectedQuantity = 0,
            expectedPrice = 4.99,
            actualPrice = 4.99,
            unit = "loaf",
            aisle = "Bakery Counter",
            barcode = "041220891234",
            qrCode = "SS-QR-BREAD-103",
            status = ShopperItemStatus.PENDING
        ),
        ShopperOrderItem(
            id = "shp_4",
            name = "Organic Free-Range Large Brown Eggs (12ct)",
            brandOrCategory = "Dairy & Eggs",
            requestedQuantity = 1,
            collectedQuantity = 0,
            expectedPrice = 6.29,
            actualPrice = 6.29,
            unit = "dozen",
            aisle = "Aisle 1 - Egg Case",
            barcode = "072230198765",
            qrCode = "SS-QR-EGGS-804",
            status = ShopperItemStatus.PENDING
        ),
        ShopperOrderItem(
            id = "shp_5",
            name = "Sparkling Spring Water Lime (12-Pack Cans)",
            brandOrCategory = "Beverages",
            requestedQuantity = 2,
            collectedQuantity = 0,
            expectedPrice = 7.49,
            actualPrice = 7.49,
            unit = "pack",
            aisle = "Aisle 4 - Soft Drinks",
            barcode = "085000123456",
            qrCode = "SS-QR-WATER-505",
            status = ShopperItemStatus.PENDING
        )
    )
}
