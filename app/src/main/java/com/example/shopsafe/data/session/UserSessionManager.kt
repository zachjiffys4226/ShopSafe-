package com.example.shopsafe.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.shopsafe.data.models.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session_prefs")

/**
 * UserSessionManager securely manages authentication tokens and user profile state.
 * It wraps Firebase Auth state listeners and integrates with Jetpack DataStore Preferences
 * for fast, persistent, zero-latency session hydration across app launches.
 */
class UserSessionManager(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        private val KEY_AUTH_PROVIDER = stringPreferencesKey("auth_provider")
        private val KEY_IS_DRIVER = booleanPreferencesKey("is_driver")
        private val KEY_IS_ADMIN = booleanPreferencesKey("is_admin")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_SAVED_ADDRESSES = stringPreferencesKey("saved_addresses_csv")
        private val KEY_RECENT_ADDRESSES = stringPreferencesKey("recent_addresses_csv")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language_code")
        private val KEY_LOW_POWER_MODE = booleanPreferencesKey("low_power_mode_enabled")
        private val KEY_REFERRAL_CODE = stringPreferencesKey("referral_code")
        private val KEY_DELIVERY_CREDITS = doublePreferencesKey("delivery_credits_balance")
        private val KEY_TOTAL_REFERRED = intPreferencesKey("total_friends_referred")
        private val KEY_REFERRED_BY_CODE = stringPreferencesKey("referred_by_code")
    }

    /**
     * Flow emitting user's unique referral code.
     */
    val referralCodeFlow: Flow<String> = dataStore.data.map { prefs ->
        val cached = prefs[KEY_REFERRAL_CODE]
        if (!cached.isNullOrBlank()) {
            cached
        } else {
            val namePart = (prefs[KEY_USER_NAME] ?: "ALEX").replace(" ", "").take(4).uppercase()
            "SHOPSAFE-$namePart${kotlin.math.abs((prefs[KEY_USER_ID] ?: "101").hashCode() % 1000)}"
        }
    }

    /**
     * Flow emitting available ShopSafe delivery fee credits balance.
     */
    val deliveryCreditsFlow: Flow<Double> = dataStore.data.map { prefs ->
        prefs[KEY_DELIVERY_CREDITS] ?: 5.00 // Default $5.00 welcome referral credit
    }

    /**
     * Flow emitting count of friends successfully referred.
     */
    val totalFriendsReferredFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_REFERRED] ?: 1
    }

    /**
     * Flow emitting code of friend who referred current user.
     */
    val referredByCodeFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_REFERRED_BY_CODE]
    }

    suspend fun updateReferralProfile(
        code: String,
        credits: Double,
        totalReferred: Int,
        referredBy: String? = null
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_REFERRAL_CODE] = code
            prefs[KEY_DELIVERY_CREDITS] = credits
            prefs[KEY_TOTAL_REFERRED] = totalReferred
            if (referredBy != null) {
                prefs[KEY_REFERRED_BY_CODE] = referredBy
            }
        }
    }

    suspend fun addDeliveryCredits(amount: Double, incrementReferredCount: Boolean = false) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_DELIVERY_CREDITS] ?: 5.00
            prefs[KEY_DELIVERY_CREDITS] = current + amount
            if (incrementReferredCount) {
                val currentCount = prefs[KEY_TOTAL_REFERRED] ?: 0
                prefs[KEY_TOTAL_REFERRED] = currentCount + 1
            }
        }
    }

    suspend fun deductDeliveryCredits(amount: Double) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_DELIVERY_CREDITS] ?: 5.00
            prefs[KEY_DELIVERY_CREDITS] = (current - amount).coerceAtLeast(0.0)
        }
    }


    /**
     * Flow emitting currently selected app language code cached in DataStore.
     */
    val selectedLanguageCodeFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_APP_LANGUAGE] ?: "en"
    }

    suspend fun setAppLanguage(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = languageCode
        }
    }

    /**
     * Flow emitting Low Power Mode preference cached in DataStore.
     */
    val lowPowerModeFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_LOW_POWER_MODE] ?: false
    }

    suspend fun setLowPowerMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_LOW_POWER_MODE] = enabled
        }
    }

    /**
     * Flow emitting saved delivery addresses cached in DataStore.
     */
    val savedAddressesFlow: Flow<List<String>> = dataStore.data.map { prefs ->
        val csv = prefs[KEY_SAVED_ADDRESSES] ?: "100 Mission St, San Francisco, CA|450 Geary St, San Francisco, CA|742 Evergreen Terrace, Springfield, OR"
        if (csv.isBlank()) emptyList() else csv.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }

    /**
     * Flow emitting recently used delivery locations cached in DataStore.
     */
    val recentAddressesFlow: Flow<List<String>> = dataStore.data.map { prefs ->
        val csv = prefs[KEY_RECENT_ADDRESSES] ?: "100 Mission St, San Francisco, CA|220 Montgomery St, San Francisco, CA"
        if (csv.isBlank()) emptyList() else csv.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }

    suspend fun addSavedAddress(address: String) {
        if (address.isBlank()) return
        dataStore.edit { prefs ->
            val currentCsv = prefs[KEY_SAVED_ADDRESSES] ?: "100 Mission St, San Francisco, CA|450 Geary St, San Francisco, CA|742 Evergreen Terrace, Springfield, OR"
            val list = currentCsv.split("|").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
            if (!list.contains(address)) {
                list.add(0, address)
                prefs[KEY_SAVED_ADDRESSES] = list.joinToString("|")
            }
        }
    }

    suspend fun removeSavedAddress(address: String) {
        dataStore.edit { prefs ->
            val currentCsv = prefs[KEY_SAVED_ADDRESSES] ?: ""
            val list = currentCsv.split("|").map { it.trim() }.filter { it.isNotBlank() && it != address }
            prefs[KEY_SAVED_ADDRESSES] = list.joinToString("|")
        }
    }

    suspend fun cacheRecentLocation(address: String) {
        if (address.isBlank()) return
        dataStore.edit { prefs ->
            val currentCsv = prefs[KEY_RECENT_ADDRESSES] ?: ""
            val list = currentCsv.split("|").map { it.trim() }.filter { it.isNotBlank() && it != address }.toMutableList()
            list.add(0, address)
            val trimmed = list.take(5)
            prefs[KEY_RECENT_ADDRESSES] = trimmed.joinToString("|")
        }
    }

    /**
     * Flow emitting the currently persisted user profile from DataStore, or null if unauthenticated.
     */
    val userSessionFlow: Flow<AuthUser?> = dataStore.data.map { prefs ->
        val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        if (!isLoggedIn) {
            null
        } else {
            AuthUser(
                id = prefs[KEY_USER_ID] ?: "user_${System.currentTimeMillis()}",
                name = prefs[KEY_USER_NAME] ?: "Member",
                email = prefs[KEY_USER_EMAIL] ?: "",
                phone = prefs[KEY_USER_PHONE] ?: "",
                authProvider = prefs[KEY_AUTH_PROVIDER] ?: "FIREBASE",
                isDriver = prefs[KEY_IS_DRIVER] ?: true,
                isAdmin = prefs[KEY_IS_ADMIN] ?: false
            )
        }
    }

    /**
     * Flow emitting the cached authentication token.
     */
    val authTokenFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN]
    }

    /**
     * Saves user session state and optional auth token to DataStore.
     */
    suspend fun saveSession(user: AuthUser, token: String? = null) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_NAME] = user.name
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_PHONE] = user.phone
            prefs[KEY_AUTH_PROVIDER] = user.authProvider
            prefs[KEY_IS_DRIVER] = user.isDriver
            prefs[KEY_IS_ADMIN] = user.isAdmin
            if (token != null) {
                prefs[KEY_AUTH_TOKEN] = token
            }
        }
    }

    /**
     * Clears user session state from DataStore and signs out from Firebase Auth.
     */
    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Gracefully handle if Firebase Auth is not initialized or offline
        }
    }

    /**
     * Attaches Firebase AuthStateListener to synchronize Firebase Auth state with DataStore.
     */
    fun attachFirebaseListener(scope: CoroutineScope, onUserUpdated: (AuthUser?) -> Unit) {
        try {
            val auth = FirebaseAuth.getInstance()
            auth.addAuthStateListener { firebaseAuth ->
                val fbUser = firebaseAuth.currentUser
                if (fbUser != null) {
                    val user = mapFirebaseUserToAuthUser(fbUser)
                    scope.launch {
                        fbUser.getIdToken(false).addOnSuccessListener { tokenResult ->
                            val token = tokenResult.token
                            scope.launch {
                                saveSession(user, token)
                                onUserUpdated(user)
                            }
                        }.addOnFailureListener {
                            scope.launch {
                                saveSession(user)
                                onUserUpdated(user)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Firebase Auth not active or available in environment
        }
    }

    /**
     * Helper to convert FirebaseUser to AuthUser domain model.
     */
    fun mapFirebaseUserToAuthUser(fbUser: FirebaseUser): AuthUser {
        val email = fbUser.email ?: ""
        val displayName = fbUser.displayName.takeIf { !it.isNullOrBlank() }
            ?: email.substringBefore("@").replace(".", " ").ifBlank { "Member User" }

        return AuthUser(
            id = fbUser.uid,
            name = displayName,
            email = email,
            phone = fbUser.phoneNumber ?: "",
            authProvider = fbUser.providerId.ifBlank { "GOOGLE" },
            isDriver = true
        )
    }
}
