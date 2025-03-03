package com.android.aegentcam.interfaces

import com.android.aegentcam.network.NetworkResult


interface ApiListeners {
    fun onSuccess(networkResult: NetworkResult<Any>)
    fun onFailure(networkResult: NetworkResult<Any>)
    fun onLoading(networkResult: NetworkResult<Any>)
}

