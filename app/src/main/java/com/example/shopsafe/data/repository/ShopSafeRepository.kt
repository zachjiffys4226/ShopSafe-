package com.example.shopsafe.data.repository

import com.example.shopsafe.data.firebase.ShopSafeFirestoreService
import com.example.shopsafe.data.local.ShopSafeDao
import com.example.shopsafe.data.models.*
import com.example.shopsafe.data.seed.DriverSeedData
import com.example.shopsafe.data.seed.MarketplaceSeedData
import com.example.shopsafe.data.stripe.StripeConnectService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ShopSafeRepository(
    private val dao: ShopSafeDao,
    private val firestoreService: ShopSafeFirestoreService = ShopSafeFirestoreService(),
    val stripeConnectService: StripeConnectService = StripeConnectService()
) {

    val allMarketplaceItems: Flow<List<MarketplaceItem>> = dao.getAllMarketplaceItems()
    val allStorefronts: Flow<List<Storefront>> = dao.getAllStorefronts()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    val firestoreOrders: Flow<List<Order>> = firestoreService.observeCustomerOrders()
    val allChatThreads: Flow<List<ChatThread>> = dao.getAllChatThreads()
    val driverProfile: Flow<DriverProfile?> = dao.getDriverProfile()
    val availableDriverOffers: Flow<List<DriverOffer>> = dao.getAvailableDriverOffers()
    val payoutHistory: Flow<List<PayoutTransaction>> = dao.getPayoutHistory()
    val firestorePayoutHistory: Flow<List<PayoutTransaction>> = firestoreService.observePayoutHistory("driver_101")
    val allCompletedDeliveries: Flow<List<CompletedDelivery>> = dao.getAllCompletedDeliveries()
    val firestoreDeliveries: Flow<List<CompletedDelivery>> = firestoreService.observeCompletedDeliveries("driver_101")
    val allMileageLogs: Flow<List<DriverMileageLog>> = dao.getAllMileageLogs()
    val firestoreMileageLogs: Flow<List<DriverMileageLog>> = firestoreService.observeMileageLogs("driver_101")
    val totalDeliveryMileage: Flow<Double?> = dao.getTotalDeliveryMileage()
    val totalTaxDeduction: Flow<Double?> = dao.getTotalTaxDeduction()

    fun getFoodItemsForStore(storeId: String): Flow<List<FoodItem>> = dao.getFoodItemsForStore(storeId)
    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>> = dao.getMessagesForThread(threadId)
    fun observeOrderById(id: String): Flow<Order?> = dao.observeOrderById(id)

    suspend fun seedInitialDataIfEmpty() {
        // Seed Marketplace if count is under 250 items
        val currentCount = dao.getMarketplaceItemCount()
        if (currentCount < 250) {
            val all250Items = MarketplaceSeedData.get250MarketplaceItems()
            dao.insertMarketplaceItems(all250Items)
        }

        // Seed Storefronts & Food items if empty
        val existingStores = dao.getAllStorefronts().firstOrNull()
        if (existingStores.isNullOrEmpty()) {
            val store1 = Storefront(
                id = "store_shopsafe_hub_1",
                name = "ShopSafe Local Storefront Hub #1",
                category = "ShopSafe Local Storefront",
                rating = 4.9,
                deliveryFee = 1.99,
                estimatedMins = 18,
                distanceMiles = 0.6,
                address = "100 Mission St, San Francisco, CA",
                isShopSafeEmployeeHub = true
            )
            val store2 = Storefront(
                id = "store_burger_craft",
                name = "Burger Craft & Shake Co.",
                category = ItemCategory.FAST_FOOD.name,
                rating = 4.8,
                deliveryFee = 2.49,
                estimatedMins = 25,
                distanceMiles = 1.4,
                address = "450 Geary St, San Francisco, CA",
                isShopSafeEmployeeHub = false,
                isOpen = false
            )
            val store3 = Storefront(
                id = "store_fresh_market",
                name = "ShopSafe Express Grocery & Organics",
                category = ItemCategory.GROCERY.name,
                rating = 4.95,
                deliveryFee = 0.00,
                estimatedMins = 20,
                distanceMiles = 0.9,
                address = "220 Montgomery St, San Francisco, CA",
                isShopSafeEmployeeHub = true
            )
            dao.insertStorefronts(listOf(store1, store2, store3))

            // Seed Nationwide CVS Pharmacy Locations & Items
            seedCvsNationwideStores()

            // Food & Hub Items
            val foodItems = listOf(
                FoodItem(
                    storeId = store1.id,
                    name = "Personal Shopper Grocery Bundle (Milk, Eggs, Bread, Produce)",
                    description = "ShopSafe employee shops for your fresh groceries in real-time at local storefront and delivers instantly.",
                    price = 24.99,
                    category = "Hub Fast Express",
                    isPopular = true
                ),
                FoodItem(
                    storeId = store1.id,
                    name = "ShopSafe Same-Time In-Person Package Drop-off Service",
                    description = "Employee personal shopper picks up your prepaid package or marketplace item and drops off directly to your door.",
                    price = 9.99,
                    category = "Hub Services",
                    isPopular = true
                ),
                FoodItem(
                    storeId = store2.id,
                    name = "Double Bacon Cheeseburger Combo",
                    description = "100% Angus beef double patty, crispy hardwood smoked bacon, cheddar cheese, special sauce, fries & beverage.",
                    price = 15.99,
                    category = "Burgers & Meals",
                    isPopular = true
                ),
                FoodItem(
                    storeId = store2.id,
                    name = "Truffle Parmesan Fries",
                    description = "Hand-cut Idaho potatoes tossed in black truffle oil, fresh parmesan cheese, and parsley.",
                    price = 6.49,
                    category = "Sides",
                    isPopular = false
                ),
                FoodItem(
                    storeId = store3.id,
                    name = "Organic Whole Milk (Gallon)",
                    description = "Fresh local organic pasture-raised whole milk.",
                    price = 5.99,
                    category = "Dairy",
                    isPopular = true
                )
            )
            dao.insertFoodItems(foodItems)
        }

        // Seed Driver Profile & Offers
        val profile = dao.getDriverProfile().firstOrNull()
        if (profile == null) {
            dao.insertOrUpdateDriverProfile(
                DriverProfile(
                    id = "current_driver",
                    name = "Alex Rivera",
                    isOnline = true,
                    currentBalance = 0.0,
                    todayEarned = 0.0,
                    weekEarned = 0.0,
                    completedToday = 0,
                    rating = 4.98,
                    preferredPayoutMethod = "Instant Cashout (Debit Card)"
                )
            )
        }

        val existingOffers = dao.getAvailableDriverOffers().firstOrNull()
        if (existingOffers.isNullOrEmpty()) {
            val offers = listOf(
                DriverOffer(
                    storeName = "ShopSafe Local Storefront Hub #1",
                    pickupAddress = "100 Mission St, San Francisco, CA",
                    dropoffAddress = "50 California St, San Francisco, CA",
                    payAmount = 22.80, // Base + Tip + $8.30 Surge
                    distanceMiles = 1.1,
                    estimatedMins = 16,
                    itemDetails = "Personal Shopper Grocery Order (3 items)",
                    isShopSafeStorefrontShopping = true,
                    surgeMultiplier = 2.8,
                    surgeBonusAmount = 8.30,
                    surgeZoneName = "Financial District / Downtown Core"
                ),
                DriverOffer(
                    storeName = "Burger Craft & Shake Co.",
                    pickupAddress = "450 Geary St, San Francisco, CA",
                    dropoffAddress = "789 Pine St, San Francisco, CA",
                    payAmount = 18.15, // Base + Tip + $6.90 Surge
                    distanceMiles = 1.8,
                    estimatedMins = 20,
                    itemDetails = "Fast Food Combo + Shake",
                    isShopSafeStorefrontShopping = false,
                    surgeMultiplier = 2.4,
                    surgeBonusAmount = 6.90,
                    surgeZoneName = "SoMa Technology Hub"
                ),
                DriverOffer(
                    storeName = "ShopSafe Marketplace Pick Up & Deliver",
                    pickupAddress = "742 Market St (Seller: Elena Vance)",
                    dropoffAddress = "333 Bush St, San Francisco, CA",
                    payAmount = 27.85, // Base + Tip + $5.85 Surge
                    distanceMiles = 2.4,
                    estimatedMins = 25,
                    itemDetails = "iPhone 14 Pro Marketplace Courier Transfer",
                    isShopSafeStorefrontShopping = true,
                    surgeMultiplier = 2.1,
                    surgeBonusAmount = 5.85,
                    surgeZoneName = "Mission District Corridor"
                )
            )
            dao.insertDriverOffers(offers)
        }

        // Seed Chat Threads
        val chatThreads = dao.getAllChatThreads().firstOrNull()
        if (chatThreads.isNullOrEmpty()) {
            val t1 = ChatThread(
                threadId = "thread_elena",
                title = "iPhone 14 Pro Purchase",
                partnerName = "Elena Vance (Seller)",
                partnerPhone = "(555) 345-6789",
                lastMessage = "Hi! Yes, the pickup location at 742 Market St is verified and safe.",
                isDriverThread = false
            )
            val t2 = ChatThread(
                threadId = "thread_marcus",
                title = "ShopSafe Delivery #892",
                partnerName = "Marcus Vance (Driver)",
                partnerPhone = "(555) 890-1234",
                lastMessage = "I am at the store shopping for your items now!",
                isDriverThread = true
            )
            dao.insertChatThread(t1)
            dao.insertChatThread(t2)

            dao.insertChatMessage(ChatMessage(threadId = t1.threadId, senderName = "Elena Vance", senderRole = "Seller", text = "Hi! Is there anything you'd like to ask about the iPhone 14 Pro?", isFromUser = false))
            dao.insertChatMessage(ChatMessage(threadId = t1.threadId, senderName = "Elena Vance", senderRole = "Seller", text = "Hi! Yes, the pickup location at 742 Market St is verified and safe.", isFromUser = false))

            dao.insertChatMessage(ChatMessage(threadId = t2.threadId, senderName = "Marcus Vance", senderRole = "Driver", text = "I am at the store shopping for your items now!", isFromUser = false))
        }

        // Seed Historical Completed Deliveries for Driver Dashboard & Firestore
        val completedCount = dao.getCompletedDeliveryCount()
        if (completedCount == 0) {
            val initialDeliveries = DriverSeedData.getInitialCompletedDeliveries("driver_101")
            dao.insertCompletedDeliveries(initialDeliveries)
            firestoreService.seedInitialCompletedDeliveriesIfEmpty("driver_101", initialDeliveries)

            // Ensure Driver Profile has accurate historical earnings aggregated
            val current = dao.getDriverProfile().firstOrNull() ?: DriverProfile()
            val totalLifetime = initialDeliveries.sumOf { it.totalEarnings }
            val todayTotal = initialDeliveries.filter {
                val now = System.currentTimeMillis()
                (now - it.timestamp) < (24 * 3600 * 1000L)
            }.sumOf { it.totalEarnings }
            val weekTotal = initialDeliveries.sumOf { it.totalEarnings }

            val updatedProfile = current.copy(
                currentBalance = 164.80,
                todayEarned = if (todayTotal > 0) todayTotal else 87.55,
                weekEarned = if (weekTotal > 0) weekTotal else 438.00,
                lifetimeEarned = totalLifetime,
                completedToday = 4,
                totalCompletedDeliveries = initialDeliveries.size
            )
            dao.insertOrUpdateDriverProfile(updatedProfile)
            firestoreService.updateDriverProfileInFirestore(updatedProfile)
        }
    }

    suspend fun updateDriverProfile(profile: DriverProfile) {
        dao.insertOrUpdateDriverProfile(profile)
        firestoreService.updateDriverProfileInFirestore(profile)
    }

    suspend fun insertMarketplaceItem(item: MarketplaceItem) = dao.insertMarketplaceItem(item)

    suspend fun createOrderFromCart(
        items: List<CartItem>,
        storeName: String,
        subtotal: Double,
        deliveryFee: Double,
        serviceFee: Double,
        tip: Double,
        total: Double,
        dropoffAddress: String,
        paymentMethod: String,
        deliveryInstructions: String = ""
    ): Order {
        val summary = items.joinToString(", ") { "${it.quantity}x ${it.name}" }
        val pickup = items.firstOrNull()?.pickupLocation?.ifBlank { "ShopSafe Storefront Hub #1" } ?: "ShopSafe Storefront Hub #1"
        val newOrder = Order(
            storeOrSellerName = storeName,
            itemsSummary = summary,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            serviceFee = serviceFee,
            tip = tip,
            total = total,
            status = OrderStatus.PLACED.name,
            pickupAddress = pickup,
            dropoffAddress = dropoffAddress,
            paymentMethod = paymentMethod,
            deliveryInstructions = deliveryInstructions
        )
        dao.insertOrder(newOrder)
        firestoreService.saveCustomerOrderInFirestore(newOrder)

        // Also create a driver offer for this order in the driver portal!
        dao.insertDriverOffers(
            listOf(
                DriverOffer(
                    storeName = storeName,
                    pickupAddress = pickup,
                    dropoffAddress = dropoffAddress,
                    payAmount = deliveryFee + tip + 4.0, // Base pay + 100% tip
                    distanceMiles = 1.5,
                    estimatedMins = 20,
                    itemDetails = summary,
                    isShopSafeStorefrontShopping = true,
                    orderId = newOrder.id,
                    deliveryInstructions = deliveryInstructions
                )
            )
        )
        return newOrder
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        dao.updateOrderStatus(orderId, status)
        val updatedOrder = dao.observeOrderById(orderId).firstOrNull()
        if (updatedOrder != null) {
            firestoreService.saveCustomerOrderInFirestore(updatedOrder)
        }
    }

    suspend fun submitOrderReview(orderId: String, rating: Int, feedback: String) {
        dao.updateOrderReview(orderId, rating, feedback)
        val updatedOrder = dao.observeOrderById(orderId).firstOrNull()
        if (updatedOrder != null) {
            firestoreService.saveCustomerOrderInFirestore(updatedOrder)
        }
    }

    suspend fun updateDriverOnline(isOnline: Boolean) {
        val current = dao.getDriverProfile().firstOrNull() ?: DriverProfile()
        dao.insertOrUpdateDriverProfile(current.copy(isOnline = isOnline))
    }

    suspend fun acceptDriverOffer(offer: DriverOffer) {
        val current = dao.getDriverProfile().firstOrNull() ?: DriverProfile()
        dao.insertOrUpdateDriverProfile(current.copy(activeOrderId = offer.orderId.ifBlank { offer.id }))
        dao.deleteDriverOffer(offer.id)
        if (offer.orderId.isNotBlank()) {
            dao.updateOrderStatus(offer.orderId, OrderStatus.DRIVER_ASSIGNED.name)
        }
    }

    suspend fun recordCompletedDelivery(delivery: CompletedDelivery) {
        // 1. Persist in local Room database
        dao.insertCompletedDelivery(delivery)

        // 2. Update Driver Profile locally
        val current = dao.getDriverProfile().firstOrNull() ?: DriverProfile()
        val newBalance = current.currentBalance + delivery.totalEarnings
        val newToday = current.todayEarned + delivery.totalEarnings
        val newWeek = current.weekEarned + delivery.totalEarnings
        val newLifetime = current.lifetimeEarned + delivery.totalEarnings
        val newCompletedToday = current.completedToday + 1
        val newTotalDeliveries = current.totalCompletedDeliveries + 1

        val updatedProfile = current.copy(
            currentBalance = newBalance,
            todayEarned = newToday,
            weekEarned = newWeek,
            lifetimeEarned = newLifetime,
            completedToday = newCompletedToday,
            totalCompletedDeliveries = newTotalDeliveries,
            activeOrderId = null
        )
        dao.insertOrUpdateDriverProfile(updatedProfile)

        if (delivery.orderId.isNotBlank()) {
            dao.updateOrderStatus(delivery.orderId, OrderStatus.DELIVERED.name)
        }

        // 3. Persist and synchronize to Cloud Firestore
        firestoreService.saveCompletedDelivery(delivery)
        firestoreService.updateDriverProfileInFirestore(updatedProfile)
    }

    suspend fun completeDriverDelivery(
        earnedAmount: Double,
        orderId: String?,
        storeName: String = "ShopSafe Verified Merchant",
        customerName: String = "Customer",
        pickupAddress: String = "100 Mission St, San Francisco, CA",
        dropoffAddress: String = "220 Montgomery St, San Francisco, CA",
        itemsSummary: String = "Verified Delivery Order",
        distanceMiles: Double = 1.4,
        durationMinutes: Int = 18,
        proofPhotoUrl: String = ""
    ) {
        val basePay = (earnedAmount * 0.65).coerceAtLeast(6.0)
        val tip = (earnedAmount - basePay).coerceAtLeast(2.0)
        val completed = CompletedDelivery(
            driverId = "driver_101",
            orderId = orderId ?: "",
            storeOrSellerName = storeName,
            customerName = customerName,
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
            itemsSummary = itemsSummary,
            basePay = basePay,
            tipAmount = tip,
            peakBonus = 0.0,
            totalEarnings = earnedAmount,
            distanceMiles = distanceMiles,
            durationMinutes = durationMinutes,
            timestamp = System.currentTimeMillis(),
            proofPhotoUrl = proofPhotoUrl,
            status = "COMPLETED",
            customerRating = 5.0,
            deliveryNotes = "Successfully delivered and confirmed via ShopSafe verified PIN."
        )
        recordCompletedDelivery(completed)
    }

    suspend fun requestPayout(
        amount: Double,
        method: String,
        fee: Double = 0.0
    ): PayoutTransaction? {
        val current = dao.getDriverProfile().firstOrNull() ?: DriverProfile()
        if (current.currentBalance < amount || amount <= 0.0) {
            return null
        }

        val remainingBalance = (current.currentBalance - amount).coerceAtLeast(0.0)

        // 1. Immediately update Driver balance status to 'PENDING' in Cloud Firestore & local Room
        val pendingProfile = current.copy(
            currentBalance = remainingBalance,
            balanceStatus = "PENDING",
            pendingTransferAmount = amount,
            pendingTransferTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateDriverProfile(pendingProfile)

        firestoreService.updateDriverBalanceStatusInFirestore(
            driverId = current.id,
            status = "PENDING",
            pendingAmount = amount,
            newCurrentBalance = remainingBalance
        )

        // 2. Call Stripe Connect API for automated payout execution
        val stripeResult = stripeConnectService.initiateDriverPayout(
            amount = amount,
            method = method,
            connectedAccountId = current.stripeConnectAccountId,
            driverName = current.name,
            driverEmail = current.email
        )

        // 3. Record PayoutTransaction in Room and Cloud Firestore
        val transaction = PayoutTransaction(
            driverId = "driver_101",
            amount = amount,
            method = method,
            fee = stripeResult.feeDeducted,
            netPayoutAmount = stripeResult.netAmount,
            timestamp = System.currentTimeMillis(),
            status = if (stripeResult.isSuccess) "PENDING" else "FAILED",
            stripeTransferId = stripeResult.transferId,
            stripePayoutId = stripeResult.payoutId,
            stripeConnectAccountId = current.stripeConnectAccountId,
            destinationAccount = stripeResult.destinationAccount,
            currency = "usd",
            estimatedArrival = stripeResult.estimatedArrival,
            stripeStatus = stripeResult.stripeStatus,
            failureReason = stripeResult.errorMessage
        )

        dao.insertPayoutTransaction(transaction)
        firestoreService.savePayoutTransactionInFirestore(transaction)

        return transaction
    }

    suspend fun completePayoutSettlement(transactionId: String) {
        val payout = dao.getPayoutById(transactionId) ?: return
        val updated = payout.copy(status = "COMPLETED", stripeStatus = "paid")
        dao.insertPayoutTransaction(updated)
        firestoreService.savePayoutTransactionInFirestore(updated)

        val current = dao.getDriverProfile().firstOrNull() ?: DriverProfile()
        val finalizedProfile = current.copy(
            balanceStatus = "AVAILABLE",
            pendingTransferAmount = 0.0
        )
        dao.insertOrUpdateDriverProfile(finalizedProfile)
        firestoreService.updateDriverProfileInFirestore(finalizedProfile)
    }

    suspend fun sendChatMessage(
        threadId: String, 
        partnerName: String, 
        text: String, 
        isFromUser: Boolean,
        imageUrl: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        locationAddress: String? = null
    ) {
        val msg = ChatMessage(
            threadId = threadId,
            senderName = if (isFromUser) "You" else partnerName,
            senderRole = if (isFromUser) "Customer" else "Contact",
            text = text,
            isFromUser = isFromUser,
            imageUrl = imageUrl,
            lat = lat,
            lng = lng,
            locationAddress = locationAddress
        )
        dao.insertChatMessage(msg)
        
        val snippet = when {
            imageUrl != null -> "📷 Sent an image"
            lat != null && lng != null -> "📍 Sent a location"
            else -> text
        }
        dao.updateThreadLastMessage(threadId, snippet, System.currentTimeMillis())
    }

    suspend fun insertDriverOffers(offers: List<DriverOffer>) {
        dao.insertDriverOffers(offers)
    }

    suspend fun addStorefront(store: Storefront) {
        dao.insertStorefronts(listOf(store))
    }

    suspend fun addFoodItem(item: FoodItem) {
        dao.insertFoodItems(listOf(item))
    }

    suspend fun updateFoodItem(item: FoodItem) {
        dao.insertFoodItem(item)
    }

    suspend fun seedCvsNationwideStores() {
        val cvsStores = listOf(
            Storefront(
                id = "cvs_nyc_times_sq",
                name = "CVS Pharmacy #10432 - Times Square NYC",
                category = "PHARMACY",
                rating = 4.9,
                deliveryFee = 1.99,
                estimatedMins = 15,
                distanceMiles = 0.8,
                address = "1500 Broadway, New York, NY 10036",
                phoneNumber = "(212) 354-1234",
                taxRate = 0.08875,
                cityState = "New York, NY",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_la_dtwn",
                name = "CVS Pharmacy #09641 - Downtown Los Angeles",
                category = "PHARMACY",
                rating = 4.85,
                deliveryFee = 1.99,
                estimatedMins = 18,
                distanceMiles = 1.2,
                address = "620 S Spring St, Los Angeles, CA 90014",
                phoneNumber = "(213) 612-4321",
                taxRate = 0.0950,
                cityState = "Los Angeles, CA",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_chicago_loop",
                name = "CVS Pharmacy #02830 - Chicago Loop",
                category = "PHARMACY",
                rating = 4.88,
                deliveryFee = 1.99,
                estimatedMins = 16,
                distanceMiles = 1.0,
                address = "205 N Michigan Ave, Chicago, IL 60601",
                phoneNumber = "(312) 819-5678",
                taxRate = 0.1025,
                cityState = "Chicago, IL",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_houston_mid",
                name = "CVS Pharmacy #07412 - Houston Midtown",
                category = "PHARMACY",
                rating = 4.92,
                deliveryFee = 1.99,
                estimatedMins = 17,
                distanceMiles = 1.5,
                address = "2111 Main St, Houston, TX 77002",
                phoneNumber = "(713) 658-9012",
                taxRate = 0.0825,
                cityState = "Houston, TX",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_phoenix_center",
                name = "CVS Pharmacy #03115 - Phoenix City Center",
                category = "PHARMACY",
                rating = 4.80,
                deliveryFee = 1.99,
                estimatedMins = 20,
                distanceMiles = 2.1,
                address = "50 W Jefferson St, Phoenix, AZ 85003",
                phoneNumber = "(602) 252-3456",
                taxRate = 0.0860,
                cityState = "Phoenix, AZ",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_philly_center",
                name = "CVS Pharmacy #01892 - Philadelphia Center City",
                category = "PHARMACY",
                rating = 4.87,
                deliveryFee = 1.99,
                estimatedMins = 16,
                distanceMiles = 1.1,
                address = "1424 Chestnut St, Philadelphia, PA 19102",
                phoneNumber = "(215) 563-7890",
                taxRate = 0.0800,
                cityState = "Philadelphia, PA",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_san_antonio_rw",
                name = "CVS Pharmacy #05631 - San Antonio Riverwalk",
                category = "PHARMACY",
                rating = 4.91,
                deliveryFee = 1.99,
                estimatedMins = 18,
                distanceMiles = 1.3,
                address = "111 W Houston St, San Antonio, TX 78205",
                phoneNumber = "(210) 225-6789",
                taxRate = 0.0825,
                cityState = "San Antonio, TX",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_san_diego_gl",
                name = "CVS Pharmacy #09124 - San Diego Gaslamp",
                category = "PHARMACY",
                rating = 4.95,
                deliveryFee = 1.99,
                estimatedMins = 14,
                distanceMiles = 0.9,
                address = "645 Market St, San Diego, CA 92101",
                phoneNumber = "(619) 234-5678",
                taxRate = 0.0775,
                cityState = "San Diego, CA",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_dallas_dtwn",
                name = "CVS Pharmacy #04820 - Dallas Main St",
                category = "PHARMACY",
                rating = 4.89,
                deliveryFee = 1.99,
                estimatedMins = 19,
                distanceMiles = 1.4,
                address = "1500 Main St, Dallas, TX 75201",
                phoneNumber = "(214) 741-1234",
                taxRate = 0.0825,
                cityState = "Dallas, TX",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_miami_bch",
                name = "CVS Pharmacy #03210 - Miami Beach",
                category = "PHARMACY",
                rating = 4.93,
                deliveryFee = 1.99,
                estimatedMins = 15,
                distanceMiles = 1.0,
                address = "1421 Alton Rd, Miami Beach, FL 33139",
                phoneNumber = "(305) 538-4567",
                taxRate = 0.0700,
                cityState = "Miami Beach, FL",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_atlanta_mtwn",
                name = "CVS Pharmacy #02145 - Atlanta Midtown",
                category = "PHARMACY",
                rating = 4.86,
                deliveryFee = 1.99,
                estimatedMins = 18,
                distanceMiles = 1.6,
                address = "800 Peachtree St NE, Atlanta, GA 30308",
                phoneNumber = "(404) 872-3456",
                taxRate = 0.0890,
                cityState = "Atlanta, GA",
                isShopSafeEmployeeHub = true
            ),
            Storefront(
                id = "cvs_seattle_dtwn",
                name = "CVS Pharmacy #04088 - Seattle Downtown",
                category = "PHARMACY",
                rating = 4.90,
                deliveryFee = 1.99,
                estimatedMins = 17,
                distanceMiles = 1.1,
                address = "1401 2nd Ave, Seattle, WA 98101",
                phoneNumber = "(206) 622-1234",
                taxRate = 0.1035,
                cityState = "Seattle, WA",
                isShopSafeEmployeeHub = true
            )
        )

        dao.insertStorefronts(cvsStores)

        val cvsBaseCatalog = listOf(
            Triple("CVS Health Extra Strength Acetaminophen 500mg (100 Caplets)", 9.49, "Aisle 4A (Pain Relief)") to "Tylenol Extra Strength 500mg",
            Triple("CVS Health Cetirizine Allergy Relief 10mg (30 Tablets)", 14.99, "Aisle 4B (Allergy)") to "Zyrtec 24Hr 10mg",
            Triple("CVS Health Advanced Antibacterial Hand Sanitizer 8 oz", 3.99, "Aisle 2A (Personal Care)") to "Purell Hand Sanitizer 8oz",
            Triple("Gold Emblem Roasted & Salted Almonds 8 oz", 5.49, "Aisle 1B (Snacks)") to "Blue Diamond Almonds 6oz",
            Triple("Crest 3D White Toothpaste Radiant Mint 3.8 oz", 4.99, "Aisle 3A (Oral Care)") to "Colgate Optic White Toothpaste",
            Triple("CVS Health Digital Fever Thermometer", 12.99, "Aisle 5A (Health Care)") to "Braun Digital Thermometer",
            Triple("Gatorade Thirst Quencher Fruit Punch 28 oz", 2.49, "Aisle 1A (Beverages)") to "Powerade Mountain Berry 28oz",
            Triple("CVS Health Flexible Fabric Bandages 30 ct", 4.29, "Aisle 4C (First Aid)") to "Band-Aid Flexible Fabric 30ct",
            Triple("Huggies Little Movers Diapers Size 3 (24 ct)", 11.99, "Aisle 6B (Baby Care)") to "Pampers Cruisers Size 3",
            Triple("CVS Health Triple Antibiotic Ointment 1 oz", 5.99, "Aisle 4C (First Aid)") to "Neosporin First Aid Ointment"
        )

        val allCvsItems = mutableListOf<FoodItem>()
        cvsStores.forEach { store ->
            cvsBaseCatalog.forEachIndexed { idx, pair ->
                val (title, basePrice, aisle) = pair.first
                val substitute = pair.second
                val itemTax = Math.round(basePrice * store.taxRate * 100.0) / 100.0
                val adjustedTotalPrice = Math.round((basePrice + itemTax) * 100.0) / 100.0

                allCvsItems.add(
                    FoodItem(
                        id = "item_${store.id}_$idx",
                        storeId = store.id,
                        name = title,
                        description = "Available at ${store.name} (${store.cityState}). In-store price $${String.format("%.2f", basePrice)} + $${String.format("%.2f", itemTax)} tax (${String.format("%.2f", store.taxRate * 100)}%). Phone: ${store.phoneNumber}",
                        price = adjustedTotalPrice,
                        category = if (idx < 2 || idx == 5 || idx == 7 || idx == 9) "Pharmacy & Pain Relief" else if (idx == 3 || idx == 6) "Snacks & Drinks" else "Personal Care & Essentials",
                        isPopular = idx < 4,
                        aisle = aisle,
                        inStock = true,
                        stockQuantity = 12 + idx * 3,
                        taxAmount = itemTax,
                        substituteOptions = substitute
                    )
                )
            }
        }

        dao.insertFoodItems(allCvsItems)
    }

    // Admin Repository Operations
    suspend fun adminDeleteMarketplaceItem(id: String) {
        dao.deleteMarketplaceItem(id)
    }

    suspend fun adminDeleteStorefront(id: String) {
        dao.deleteStorefront(id)
    }

    suspend fun adminDeleteFoodItem(id: String) {
        dao.deleteFoodItem(id)
    }

    suspend fun adminDeleteOrder(id: String) {
        dao.deleteOrder(id)
    }

    suspend fun adminUpdateFoodItemPrice(id: String, newPrice: Double) {
        dao.updateFoodItemPrice(id, newPrice)
    }

    suspend fun adminUpdateStorefrontOpenStatus(id: String, isOpen: Boolean) {
        dao.updateStorefrontOpenStatus(id, isOpen)
    }

    suspend fun adminUpdateDriverProfile(profile: DriverProfile) {
        dao.insertOrUpdateDriverProfile(profile)
    }

    // Driver Automated Mileage Tracker Operations
    fun getTodayDeliveryMileage(startOfDayTimestamp: Long): Flow<Double?> = dao.getTodayDeliveryMileage(startOfDayTimestamp)

    suspend fun recordAutomatedDeliveryMileage(
        orderId: String,
        storeOrSellerName: String,
        pickupAddress: String,
        dropoffAddress: String,
        milesDriven: Double,
        startTimestamp: Long,
        endTimestamp: Long,
        durationMinutes: Int
    ): DriverMileageLog {
        val taxRate = 0.67 // Standard IRS mileage deduction rate ($0.67 / mi)
        val taxDeduction = String.format("%.2f", (milesDriven * taxRate)).toDouble()

        val log = DriverMileageLog(
            driverId = "driver_101",
            orderId = orderId,
            storeOrSellerName = storeOrSellerName,
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
            milesDriven = milesDriven,
            taxDeductionValue = taxDeduction,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            isHomeTravelExcluded = true,
            status = "LOGGED",
            durationMinutes = durationMinutes
        )

        // Save locally to Room
        dao.insertMileageLog(log)

        // Sync to Cloud Firestore
        firestoreService.saveMileageLog(log)

        return log
    }

    suspend fun deleteMileageLog(id: String) {
        dao.deleteMileageLog(id)
    }

    suspend fun clearAllMileageLogs() {
        dao.clearAllMileageLogs()
    }
}
