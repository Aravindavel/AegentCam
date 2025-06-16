package com.android.aegentcam.view.activity

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.android.aegentcam.R
import com.android.aegentcam.databinding.ActivityMainBinding
import com.android.aegentcam.interfaces.IOnBackPressed
import com.android.aegentcam.network.NetworkResult
import com.android.aegentcam.view.fragment.DeviceInfoFragment
import com.android.aegentcam.view.fragment.GalleryFragment
import com.android.aegentcam.view.fragment.HomeFragment
import com.android.aegentcam.view.fragment.RecordSettingFragment
import com.android.aegentcam.view.fragment.SettingsFragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : BaseActivity() {

    lateinit var binding : ActivityMainBinding
    private var navController: NavController? = null
    private var backPressed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNavigation.setItemSelected(R.id.homeFragment, true)
        binding.bottomNavigation.setOnItemSelectedListener(object : ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                when (id) {
                    R.id.homeFragment -> {
                        navigate(R.id.homeFragment)
                    }
                    R.id.filesFragment -> {
                        navigate(R.id.filesFragment)
                    }
                    else ->{
                        navigate(R.id.settingsFragment)
                    }
                }
            }
        })
    }



    fun navigate(id: Int) {
        runOnUiThread {
            val navController = getNavController()
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.scale_in)
                .setExitAnim(R.anim.scale_out)
                .build()
            navController.navigate(id, null, navOptions)
        }

    }

    fun getNavController(): NavController = navController
        ?: (supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment).navController.apply {
            navController = this
        }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
       
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
       
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
       
    }

    override fun onBackPressed() {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        val fragment: Fragment = navHostFragment!!.childFragmentManager.fragments[0]
        (fragment as? IOnBackPressed)?.onBackPressed()?.let {
            if (it)
                super.onBackPressed()
        } ?:when(fragment){
            is HomeFragment ->{
                if (backPressed >= 1) {
                    finishAffinity()
                    super.onBackPressed()
                } else {
                    backPressed += 1
                    commonMethods.showToast(this, getString(R.string.press_back_again))
                }
            }
            is SettingsFragment,is GalleryFragment ->{
                binding.bottomNavigation.setItemSelected(R.id.homeFragment, true)
                getNavController().popBackStack(R.id.homeFragment,false)
            }
            is DeviceInfoFragment , is RecordSettingFragment->{
                binding.bottomNavigation.setItemSelected(R.id.settingsFragment, true)
                getNavController().popBackStack(R.id.settingsFragment,false)
            }else ->
                super.onBackPressed()

        }

    }
}