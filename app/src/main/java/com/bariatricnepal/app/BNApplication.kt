package com.bariatricnepal.app

import android.app.Application
import com.bariatricnepal.app.data.api.ApiClient
import com.bariatricnepal.app.data.local.SessionStore
import com.bariatricnepal.app.data.repository.BNRepository

class BNApplication : Application() {

    lateinit var sessionStore: SessionStore
        private set

    // Made nullable so we never crash if accessed before server is configured
    var repository: BNRepository? = null
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        rebuildRepository()
    }

    fun rebuildRepository() {
        repository = try {
            if (sessionStore.isServerConfigured) {
                BNRepository(ApiClient.create(sessionStore))
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Safe accessor — throws a clear error instead of a cryptic NPE
    fun requireRepository(): BNRepository {
        return repository ?: throw IllegalStateException("Repository not initialized — server URL missing")
    }
}
