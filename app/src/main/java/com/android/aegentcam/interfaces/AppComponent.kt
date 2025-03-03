package com.android.aegentcam.interfaces


import com.android.aegentcam.configs.AppContainerModule
import com.android.aegentcam.configs.ApplicationModule
import com.android.aegentcam.configs.NetworkModule
import com.android.aegentcam.helper.CommonMethods
import com.android.aegentcam.helper.SessionManager
import com.android.aegentcam.network.ApiExceptionHandler
import com.android.aegentcam.repository.CommonRepository
import com.android.aegentcam.view.activity.BaseActivity
import com.android.aegentcam.view.adapter.BTDeviceRecyclerAdapter
import com.android.aegentcam.view.fragment.BaseFragment
import com.android.aegentcam.view.fragment.HomeFragment
import com.android.aegentcam.viewmodel.CommonViewModel
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [NetworkModule::class, ApplicationModule::class, AppContainerModule::class])
interface AppComponent {

    fun inject(baseActivity: BaseActivity)

    fun inject(commonMethods: CommonMethods)

    fun inject(sessionManager: SessionManager)

    fun inject(commonRepository: CommonRepository)

    fun inject(commonViewModel: CommonViewModel)

    fun inject(apiExceptionHandler: ApiExceptionHandler)

    fun inject(btDeviceRecyclerAdapter: BTDeviceRecyclerAdapter)

    fun inject(homeFragment: HomeFragment)

    fun inject(baseFragment: BaseFragment)


}