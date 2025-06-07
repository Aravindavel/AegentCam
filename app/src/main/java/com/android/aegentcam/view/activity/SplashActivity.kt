package com.android.aegentcam.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.android.aegentcam.databinding.ActivitySplashBinding
import com.android.aegentcam.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableBluetooth()
    }

    fun enableBluetooth() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            startActivityForResult(enableBtIntent, 1001)
        }else{
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                if(sessionManager.getStartedShown == "0"){
                    startActivity(Intent(this@SplashActivity, OpeningScreenActivity::class.java))
                }else{
                    startActivity(Intent(this@SplashActivity, BluetoothConnectionActivity::class.java))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    if(sessionManager.getStartedShown == "0"){
                        startActivity(Intent(this@SplashActivity, OpeningScreenActivity::class.java))
                    }else{
                        startActivity(Intent(this@SplashActivity, BluetoothConnectionActivity::class.java))
                    }
                }
            } else {
                commonMethods.showToast(this, "Please enable bluetooth")
            }
        }
    }


    override fun onSuccess(networkResult: NetworkResult<Any>) {
        
    }

    override fun onFailure(networkResult: NetworkResult<Any>) {
        
    }

    override fun onLoading(networkResult: NetworkResult<Any>) {
        
    }
}