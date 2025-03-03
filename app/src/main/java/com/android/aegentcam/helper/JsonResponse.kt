package com.android.aegentcam.helper


class JsonResponse {
    var url: String = ""
    var method: String = ""
    var responseCode: Int = 0
    var requestCode: Int = 0
    var errorMsg: String = ""
    var isOnline: Boolean = false
    var statusMsg: String= ""
    var statusCode: String=""
    var isSuccess: Boolean = false
    var strResponse: String=""
    var requestData: String=""

    fun clearAll() {
        this.url = ""
        this.method = ""
        this.errorMsg = ""
        this.statusMsg = ""
        this.strResponse = ""
        this.requestData = ""
        this.requestCode = 0
        this.responseCode = 0
        this.isOnline = false
        this.isSuccess = false
    }
}
