package com.android.aegentcam.model

import com.google.gson.annotations.SerializedName

data class PreferenceModel (

    @SerializedName("version"    ) var version   : String?           = null,
    @SerializedName("news_sites" ) var newsSites : ArrayList<String> = arrayListOf()

)