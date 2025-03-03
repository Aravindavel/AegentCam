package com.android.aegentcam.configs

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.android.aegentcam.R
import com.android.aegentcam.interfaces.AppComponent
import com.android.aegentcam.interfaces.DaggerAppComponent
import java.util.*


class AppController : Application() {
    private var mCurrentActivity: Activity? = null
    private var mRequestWifiChangedFrom = -1
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        appComponent = DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(this)) // This also corresponds to the name of your module: %component_name%Module
            .networkModule(NetworkModule(resources.getString(R.string.apiBaseUrl),resources.getString(R.string.domain))).build()
        instance = this
        globalApplicationContext = this
    }

    fun setRequestWifiChangedFrom(from: Int) {
        mRequestWifiChangedFrom = from
    }
    fun setCurrentActivity(activity: Activity) {
        mCurrentActivity = activity
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        var appComponent: AppComponent? = null
            private set
        private val APP_LOCALES = mutableListOf(Locale.ENGLISH, Locale.US)
        private var instance: AppController? = null
        val contexts: Context
            get() = instance!!.applicationContext

        @Volatile
        var globalApplicationContext: AppController? = null

        const val REQUEST_WIFI_CHANGED_FROM_PREVIEW: Int = 10
        const val REQUEST_WIFI_CHANGED_FROM_GALLERY: Int = 11


        var IS_SECRET_MODE: Boolean = false

        fun setLocale(activity: Activity, languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val resources = activity.resources
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}