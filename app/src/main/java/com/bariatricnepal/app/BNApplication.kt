package com.bariatricnepal.app

import android.app.Application
import com.bariatricnepal.app.data.api.ApiClient
import com.bariatricnepal.app.data.local.SessionStore
import com.bariatricnepal.app.data.repository.BNRepository

class BNApplication : Application() {

    lateinit var sessionStore: SessionStore
        private set

    // Always non-null — URL is hardcoded to www.bariatricnepal.com
    lateinit var repository: BNRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        // Always set the hardcoded URL on startup
        if (!sessionStore.isServerConfigured) {
            sessionStore.baseUrl = ApiClient.normalizeBaseUrl("www.bariatricnepal.com")
        }
        rebuildRepository()
    }

    fun rebuildRepository() {
        repository = BNRepository(ApiClient.create(sessionStore))
    }
}
