package com.rlvault

import android.app.Application
import com.rlvault.di.ServiceLocator

class RLVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
