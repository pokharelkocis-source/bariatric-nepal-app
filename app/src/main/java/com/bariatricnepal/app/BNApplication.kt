package com.bariatricnepal.app

import android.app.Application
import com.bariatricnepal.app.data.api.ApiClient
import com.bariatricnepal.app.data.local.SessionStore
import com.bariatricnepal.app.data.repository.BNRepository

class BNApplication : Application() {

    lateinit var sessionStore: SessionStore
        private set

    lateinit var repository: BNRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        rebuildRepository()
    }

    /**
     * Call this whenever the server URL changes (initial setup, or the
     * user picks "Change clinic website" later) so the network client
     * points at the right place. The auth token itself is read fresh on
     * every request by the interceptor, so logging in/out doesn't need
     * a rebuild — only the base URL does.
     */
    fun rebuildRepository() {
        if (sessionStore.isServerConfigured) {
            repository = BNRepository(ApiClient.create(sessionStore))
        }
    }
}
