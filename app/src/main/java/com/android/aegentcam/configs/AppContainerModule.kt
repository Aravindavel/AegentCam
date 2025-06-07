package com.android.aegentcam.configs

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.android.aegentcam.R
import com.android.aegentcam.helper.CommonMethods
import com.android.aegentcam.helper.JsonResponse
import com.android.aegentcam.helper.SessionManager
import com.android.aegentcam.network.ApiExceptionHandler
import com.android.aegentcam.repository.CommonRepository
import com.android.aegentcam.viewmodel.CommonViewModel
import com.android.aegentcam.webrtcscreenshare.service.WebrtcServiceRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import java.util.ArrayList


@Module(includes = [ApplicationModule::class])
class AppContainerModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(application.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesSessionManager(): SessionManager {
        return SessionManager()
    }
    @Provides
    @Singleton
    fun providesCommonMethods(): CommonMethods {
        return CommonMethods()
    }

    @Provides
    @Singleton
    fun jsonResponse(): JsonResponse {
        return JsonResponse()
    }

    @Provides
    @Singleton
    fun repository(): CommonRepository {
        return CommonRepository()
    }


    @Provides
    @Singleton
    fun viewModel(): CommonViewModel {
        return CommonViewModel()
    }

    @Provides
    @Singleton
    fun apiExceptionHandler(): ApiExceptionHandler {
        return ApiExceptionHandler()
    }

    @Provides
    @Singleton
    fun provideWebrtcServiceRepository(context: Context): WebrtcServiceRepository {
        return WebrtcServiceRepository(context)
    }

    @Provides
    @Singleton
    fun providesContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun providesStringArrayList(): ArrayList<String> {
        return ArrayList()
    }

}