package com.android.aegentcam.configs

import android.content.Context
import com.android.aegentcam.helper.SessionManager
import com.android.aegentcam.interfaces.ApiService
import com.android.aegentcam.network.AuthTokenInterceptor
import com.android.aegentcam.network.NetworkInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

@Module
class NetworkModule @Inject
constructor(private val mBaseUrl: String, val mDomain: String) {

    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        return loggingInterceptor
    }

    @Provides
    @Singleton
    fun providesGson(): Gson {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setLenient()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(context: Context, httpLoggingInterceptor: HttpLoggingInterceptor, sessionManager: SessionManager): OkHttpClient.Builder {
        val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MINUTES)
        client.addInterceptor(httpLoggingInterceptor)
        client.addInterceptor(NetworkInterceptor(context))
        client.addInterceptor(AuthTokenInterceptor(sessionManager))
        //  client.authenticator(new TokenRenewInterceptor(sessionManager));

        return client
    }

    @Provides
    @Singleton
    fun providesRetrofitService(okHttpClient: OkHttpClient.Builder, gson: Gson): Retrofit {


        okHttpClient.hostnameVerifier(HostnameVerifier { hostname, session ->
            val hv = HttpsURLConnection.getDefaultHostnameVerifier()
            hv.verify(mDomain, session)

        })



        return Retrofit.Builder()
            .baseUrl(mBaseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient.build())
            .build()
    }

    @Provides
    @Singleton
    fun providesApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    private fun getFirstCn(cert: X509Certificate): String? {
        val subjectPrincipal: String = cert.getSubjectX500Principal().toString()
        for (token in subjectPrincipal.split(",".toRegex()).toTypedArray()) {
            val x = token.indexOf("CN=")
            if (x >= 0) {
                return token.substring(x + 3)
            }
        }
        return null
    }
}