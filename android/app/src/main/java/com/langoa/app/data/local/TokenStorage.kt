package com.langoa.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.langoa.app.domain.model.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "langoa_prefs")

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")
        private val SUBSCRIPTION_PLAN_KEY = stringPreferencesKey("subscription_plan")
        private val SUBSCRIPTION_IS_PRO_KEY = booleanPreferencesKey("subscription_is_pro")
        private val SUBSCRIPTION_EXPIRES_AT_KEY = stringPreferencesKey("subscription_expires_at")
    }

    fun getAccessToken(): String? = runBlocking {
        context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
    }

    fun getRefreshToken(): String? = runBlocking {
        context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }.first()
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL_KEY] = email
        }
    }

    fun getUserId(): String? = runBlocking {
        context.dataStore.data.map { it[USER_ID_KEY] }.first()
    }

    fun getUserEmail(): String? = runBlocking {
        context.dataStore.data.map { it[USER_EMAIL_KEY] }.first()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    suspend fun saveSelectedLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_LANGUAGE_KEY] = languageCode
        }
    }

    fun getSelectedLanguage(): String? = runBlocking {
        context.dataStore.data.map { it[SELECTED_LANGUAGE_KEY] }.first()
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun saveSubscriptionStatus(planCode: String, isPro: Boolean, expiresAt: String?) {
        context.dataStore.edit { prefs ->
            prefs[SUBSCRIPTION_PLAN_KEY] = planCode
            prefs[SUBSCRIPTION_IS_PRO_KEY] = isPro
            if (expiresAt != null) prefs[SUBSCRIPTION_EXPIRES_AT_KEY] = expiresAt
            else prefs.remove(SUBSCRIPTION_EXPIRES_AT_KEY)
        }
    }

    fun getCachedSubscriptionStatus(): SubscriptionStatus = runBlocking {
        val prefs = context.dataStore.data.first()
        SubscriptionStatus(
            planCode = prefs[SUBSCRIPTION_PLAN_KEY] ?: "FREE",
            isPro = prefs[SUBSCRIPTION_IS_PRO_KEY] ?: false,
            expiresAt = prefs[SUBSCRIPTION_EXPIRES_AT_KEY]
        )
    }
}
