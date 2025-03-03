package com.android.aegentcam.network

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null,
    val requestCode: Int? = null,
    val isLoading:Boolean = false
) {

    class Success<T>(data: T,requestCode: Int) : NetworkResult<T>(data, requestCode=requestCode)

    class Error<T>(message: String?, data: T? = null,requestCode: Int) : NetworkResult<T>(data, message,requestCode = requestCode)

    class Loading<T>(isLoading : Boolean,requestCode: Int) : NetworkResult<T>(isLoading = isLoading, requestCode = requestCode)

}